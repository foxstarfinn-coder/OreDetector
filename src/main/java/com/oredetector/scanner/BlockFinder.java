package com.oredetector.scanner;

import com.oredetector.OreDetectorMod;
import com.oredetector.utils.AsyncScanner;
import com.oredetector.utils.ChatFormatter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlockFinder {
    public static CompletableFuture<Void> findConfiguredBlocks(ServerPlayerEntity player, int radius) {
        ServerWorld world = player.getServerWorld();
        BlockPos center = player.getBlockPos();
        Set<String> targets = OreDetectorMod.getConfig().getFindBlocks();
        
        player.sendMessage(ChatFormatter.info("§7Searching for configured blocks within §e" + radius + " §7blocks..."), false);
        
        return AsyncScanner.scanBlocksAsync(world, center, radius, targets, null)
            .thenAccept(results -> {
                if (results.isEmpty()) {
                    player.sendMessage(ChatFormatter.warning("No target blocks found."), false);
                    return;
                }
                player.sendMessage(ChatFormatter.highlight("=== Found " + results.size() + " blocks ==="), false);
                results.sort(Comparator.comparingInt(p -> ChatFormatter.getDistance(center, p)));
                int limit = Math.min(results.size(), 15);
                for (int i = 0; i < limit; i++) {
                    BlockPos pos = results.get(i);
                    int dist = ChatFormatter.getDistance(center, pos);
                    String dir = ChatFormatter.getDirection(center, pos);
                    if (OreDetectorMod.getConfig().isExactCoordinatesEnabled()) {
                        player.sendMessage(Text.literal("§7- " + dir + " §7(" + dist + " blocks) → §f" + 
                            pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                    } else {
                        player.sendMessage(Text.literal("§7- " + dir + " §7(" + dist + " blocks away)"), false);
                    }
                }
                if (results.size() > limit) player.sendMessage(Text.literal("§8... and " + (results.size() - limit) + " more"), false);
            }).exceptionally(ex -> {
                player.sendMessage(ChatFormatter.error("Search failed."), false);
                return null;
            });
    }
}
