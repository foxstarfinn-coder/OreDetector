package com.oredetector.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.oredetector.OreDetectorMod;
import com.oredetector.scanner.BaseDetector;
import com.oredetector.scanner.BlockFinder;
import com.oredetector.scanner.OreScanner;
import com.oredetector.utils.ChatFormatter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.*;

public class ScanCommands {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("scandebug")
            .requires(source -> source.hasPermissionLevel(2))
            .then(literal("ore")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        OreScanner.scanForOres(player, OreDetectorMod.getConfig().getOreScanRadius());
                    }
                    return 1;
                })
                .then(argument("radius", IntegerArgumentType.integer(1, OreDetectorMod.getConfig().getMaxScanRadius()))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            int radius = IntegerArgumentType.getInteger(context, "radius");
                            OreScanner.scanForOres(player, radius);
                        }
                        return 1;
                    })))
            .then(literal("find")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        BlockFinder.findConfiguredBlocks(player, 100);
                    }
                    return 1;
                })
                .then(argument("radius", IntegerArgumentType.integer(1, OreDetectorMod.getConfig().getMaxScanRadius()))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            int radius = IntegerArgumentType.getInteger(context, "radius");
                            BlockFinder.findConfiguredBlocks(player, radius);
                        }
                        return 1;
                    })))
            .then(literal("base")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        BaseDetector.detectBases(player, 200);
                    }
                    return 1;
                })
                .then(argument("radius", IntegerArgumentType.integer(1, OreDetectorMod.getConfig().getMaxScanRadius()))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            int radius = IntegerArgumentType.getInteger(context, "radius");
                            BaseDetector.detectBases(player, radius);
                        }
                        return 1;
                    })))
            .then(literal("help")
                .executes(context -> {
                    context.getSource().sendMessage(net.minecraft.text.Text.literal("§6=== OreDetector Commands ==="));
                    context.getSource().sendMessage(net.minecraft.text.Text.literal("§7/scandebug ore [radius] - Scan for ores"));
                    context.getSource().sendMessage(net.minecraft.text.Text.literal("§7/scandebug find [radius] - Find configured blocks"));
                    context.getSource().sendMessage(net.minecraft.text.Text.literal("§7/scandebug base [radius] - Detect bases"));
                    return 1;
                }))
        );
    }
}
