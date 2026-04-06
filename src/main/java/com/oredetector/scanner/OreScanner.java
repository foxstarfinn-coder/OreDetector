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

public class OreScanner {
    public static CompletableFuture<Void> scanForOres(ServerPlayerEntity player, int radius) {
        ServerWorld world = player.getServerWorld();
        BlockPos center = player.getBlockPos();
        Set<String> ores = OreDetectorMod.getConfig().getIncludedOres();
        
        player.sendMessage(ChatFormatter.info("§7Scanning for ores within §e" + radius + " §7blocks..."), false);
        
        return AsyncScanner.scanBlocksAsync(world, center, radius, ores, null)
            .thenAccept(results -> {
                if (results.isEmpty()) {
                    player.sendMessage(ChatFormatter.warning("No ores found."), false);
                    return;
                }
                
                Map<String, List<BlockPos>> groups = new HashMap<>();
                for (BlockPos pos : results) {
                    String id = world.getBlockState(pos).getBlock().toString();
                    groups.computeIfAbsent(id, k -> new ArrayList<>()).add(pos);
                }
                
                player.sendMessage(ChatFormatter.highlight("=== Ore Scan Results ==="), false);
                for (Map.Entry<String, List<BlockPos>> entry : groups.entrySet()) {
                    String name = entry.getKey().replace("minecraft:", "");
                    List<BlockPos> positions = entry.getValue();
                    player.sendMessage(Text.literal("§7- §6" + name + "§7: §e" + positions.size() + " §7found"), false);
                    
                    BlockPos closest = positions.stream()
                        .min(Comparator.comparingInt(p -> ChatFormatter.getDistance(center, p))).orElse(null);
                    if (closest != null && OreDetectorMod.getConfig().isShowDirectionalHints()) {
                        String dir = ChatFormatter.getDirection(center, closest);
                        int dist = ChatFormatter.getDistance(center, closest);
                        if (OreDetectorMod.getConfig().isExactCoordinatesEnabled()) {
                            player.sendMessage(Text.literal("§8  └─ §7Nearest: " + dir + " §7(" + dist + " blocks) at §f" + 
                                closest.getX() + ", " + closest.getY() + ", " + closest.getZ()), false);
                        } else {
                            player.sendMessage(Text.literal("§8  └─ §7Nearest: " + dir + " §7(" + dist + " blocks away)"), false);
                        }
                    }
                }
                player.sendMessage(ChatFormatter.success("Found " + results.size() + " ore blocks."), false);
            }).exceptionally(ex -> {
                player.sendMessage(ChatFormatter.error("Scan failed."), false);
                return null;
            });
    }
}
