package com.oredetector.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oredetector.OreDetectorMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private int maxScanRadius = 10000;
    private boolean exactCoordinatesEnabled = true;
    private Set<String> includedOres = new HashSet<>();
    private Set<String> findBlocks = new HashSet<>();
    private Set<String> includedBaseBlocks = new HashSet<>();
    private int asyncThreadPoolSize = 4;
    private boolean showDirectionalHints = true;
    private int oreScanRadius = 50;
    
    public ModConfig() {
        includedOres.add("minecraft:diamond_ore");
        includedOres.add("minecraft:deepslate_diamond_ore");
        includedOres.add("minecraft:ancient_debris");
        
        findBlocks.add("minecraft:chest");
        findBlocks.add("minecraft:shulker_box");
        
        includedBaseBlocks.add("minecraft:chest");
        includedBaseBlocks.add("minecraft:shulker_box");
        includedBaseBlocks.add("minecraft:netherite_block");
        includedBaseBlocks.add("minecraft:diamond_block");
        includedBaseBlocks.add("minecraft:armor_stand");
        includedBaseBlocks.add("minecraft:hopper");
        includedBaseBlocks.add("minecraft:sticky_piston");
    }
    
    public static ModConfig loadConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("oredetector.json");
        File configFile = configPath.toFile();
        
        if (configFile.exists()) {
            try (Reader reader = new FileReader(configFile)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                OreDetectorMod.LOGGER.error("Failed to load config, using defaults", e);
            }
        }
        
        ModConfig defaultConfig = new ModConfig();
        defaultConfig.saveConfig();
        return defaultConfig;
    }
    
    public void saveConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("oredetector.json");
        try (Writer writer = new FileWriter(configPath.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            OreDetectorMod.LOGGER.error("Failed to save config", e);
        }
    }
    
    // Getters
    public int getMaxScanRadius() { return maxScanRadius; }
    public boolean isExactCoordinatesEnabled() { return exactCoordinatesEnabled; }
    public Set<String> getIncludedOres() { return includedOres; }
    public Set<String> getFindBlocks() { return findBlocks; }
    public Set<String> getIncludedBaseBlocks() { return includedBaseBlocks; }
    public int getAsyncThreadPoolSize() { return asyncThreadPoolSize; }
    public boolean isShowDirectionalHints() { return showDirectionalHints; }
    public int getOreScanRadius() { return oreScanRadius; }
}
