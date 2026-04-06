package com.oredetector.utils;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class AsyncScanner {
    private static final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );
    
    public static CompletableFuture<List<BlockPos>> scanBlocksAsync(
        ServerWorld world, 
        BlockPos center, 
        int radius, 
        Set<String> targetBlocks,
        BiConsumer<BlockPos, String> progressCallback
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<BlockPos> results = new CopyOnWriteArrayList<>();
            int minX = center.getX() - radius;
            int minZ = center.getZ() - radius;
            int maxX = center.getX() + radius;
            int maxZ = center.getZ() + radius;
            
            int minChunkX = minX >> 4;
            int maxChunkX = maxX >> 4;
            int minChunkZ = minZ >> 4;
            int maxChunkZ = maxZ >> 4;
            
            List<CompletableFuture<Void>> chunkFutures = new ArrayList<>();
            
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    final int cx = chunkX;
                    final int cz = chunkZ;
                    chunkFutures.add(CompletableFuture.runAsync(() -> {
                        WorldChunk chunk = world.getChunk(cx, cz);
                        ChunkPos chunkPos = chunk.getPos();
                        
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = world.getBottomY(); y <= world.getTopY(); y++) {
                                    BlockPos pos = new BlockPos(
                                        (chunkPos.x << 4) + x, y, (chunkPos.z << 4) + z
                                    );
                                    if (Math.abs(pos.getX() - center.getX()) <= radius &&
                                        Math.abs(pos.getZ() - center.getZ()) <= radius) {
                                        String blockId = world.getBlockState(pos).getBlock().toString();
                                        if (targetBlocks.contains(blockId)) {
                                            results.add(pos);
                                            if (progressCallback != null) progressCallback.accept(pos, blockId);
                                        }
                                    }
                                }
                            }
                        }
                    }, executor));
                }
            }
            CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).join();
            return results;
        }, executor);
    }
    
    public static void shutdown() {
        executor.shutdown();
    }
}
