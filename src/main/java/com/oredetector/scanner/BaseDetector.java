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

public class BaseDetector {
    private static class PotentialBase {
        BlockPos center; int score; List<BlockPos> blocks;
        PotentialBase(BlockPos center, int score, List<BlockPos> blocks) {
            this.center = center; this.score = score; this.blocks = blocks;
        }
    }
    
    public static CompletableFuture<Void> detectBases(ServerPlayerEntity player, int radius) {
        ServerWorld world = player.getServerWorld();
        BlockPos center = player.getBlockPos();
        Set<String> baseBlocks = OreDetectorMod.getConfig().getIncludedBaseBlocks();
        
        player.sendMessage(ChatFormatter.info("§7Scanning for bases within §e" + radius + " §7blocks..."), false);
        
        return AsyncScanner.scanBlocksAsync(world, center, radius, baseBlocks, null)
            .thenAccept(blocks -> {
                if (blocks.isEmpty()) {
                    player.sendMessage(ChatFormatter.warning("No potential bases detected."), false);
                    return;
                }
                List<List<BlockPos>> clusters = clusterBlocks(blocks, 50);
                List<PotentialBase> bases = new ArrayList<>();
                for (List<BlockPos> cluster : clusters) {
                    if (cluster.size() >= 3) {
                        int score = calculateScore(cluster, world);
                        BlockPos clusterCenter = calculateCenter(cluster);
                        bases.add(new PotentialBase(clusterCenter, score, cluster));
                    }
                }
                bases.sort((a,b) -> Integer.compare(b.score, a.score));
                player.sendMessage(ChatFormatter.highlight("=== Potential Bases (" + bases.size() + ") ==="), false);
                int limit = Math.min(bases.size(), 5);
                for (int i = 0; i < limit; i++) {
                    PotentialBase base = bases.get(i);
                    int dist = ChatFormatter.getDistance(center, base.center);
                    String dir = ChatFormatter.getDirection(center, base.center);
                    String conf = base.score >= 80 ? "§cHIGH" : (base.score >= 60 ? "§6MEDIUM" : (base.score >= 40 ? "§eLOW" : "§7VERY LOW"));
                    player.sendMessage(Text.literal(""), false);
                    player.sendMessage(Text.literal("§6[" + conf + "] §7Base " + (i+1) + ": §e" + dir + " §7(" + dist + " blocks)"), false);
                    if (OreDetectorMod.getConfig().isExactCoordinatesEnabled()) {
                        player.sendMessage(Text.literal("§8  └─ §7Coords: §f" + base.center.getX() + ", " + base.center.getY() + ", " + base.center.getZ()), false);
                    }
                    player.sendMessage(Text.literal("§8  └─ §7Blocks: §e" + base.blocks.size() + " §7(Score: " + base.score + "/100)"), false);
                }
                player.sendMessage(ChatFormatter.success("Found " + blocks.size() + " relevant blocks."), false);
            }).exceptionally(ex -> {
                player.sendMessage(ChatFormatter.error("Base detection failed."), false);
                return null;
            });
    }
    
    private static List<List<BlockPos>> clusterBlocks(List<BlockPos> blocks, int maxDist) {
        List<List<BlockPos>> clusters = new ArrayList<>();
        Set<BlockPos> unassigned = new HashSet<>(blocks);
        while (!unassigned.isEmpty()) {
            BlockPos seed = unassigned.iterator().next();
            List<BlockPos> cluster = new ArrayList<>();
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(seed); cluster.add(seed); unassigned.remove(seed);
            while (!queue.isEmpty()) {
                BlockPos cur = queue.poll();
                List<BlockPos> toRemove = new ArrayList<>();
                for (BlockPos cand : unassigned) {
                    if (Math.abs(cur.getX() - cand.getX()) <= maxDist && Math.abs(cur.getZ() - cand.getZ()) <= maxDist) {
                        cluster.add(cand); queue.add(cand); toRemove.add(cand);
                    }
                }
                unassigned.removeAll(toRemove);
            }
            clusters.add(cluster);
        }
        return clusters;
    }
    
    private static int calculateScore(List<BlockPos> blocks, ServerWorld world) {
        int score = 0;
        for (BlockPos pos : blocks) {
            String id = world.getBlockState(pos).getBlock().toString();
            if (id.contains("chest")) score += 15;
            else if (id.contains("shulker_box")) score += 20;
            else if (id.contains("netherite_block")) score += 30;
            else if (id.contains("diamond_block")) score += 25;
            else if (id.contains("armor_stand")) score += 10;
            else if (id.contains("hopper")) score += 8;
            else if (id.contains("sticky_piston")) score += 12;
            else score += 5;
        }
        if (blocks.size() >= 10) score += 20;
        else if (blocks.size() >= 5) score += 10;
        return Math.min(score, 100);
    }
    
    private static BlockPos calculateCenter(List<BlockPos> blocks) {
        int avgX = blocks.stream().mapToInt(BlockPos::getX).sum() / blocks.size();
        int avgY = blocks.stream().mapToInt(BlockPos::getY).sum() / blocks.size();
        int avgZ = blocks.stream().mapToInt(BlockPos::getZ).sum() / blocks.size();
        return new BlockPos(avgX, avgY, avgZ);
    }
}
