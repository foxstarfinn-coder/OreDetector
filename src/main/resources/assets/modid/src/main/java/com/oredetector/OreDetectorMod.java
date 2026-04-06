package com.oredetector;

import com.oredetector.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OreDetectorMod implements ModInitializer {
    public static final String MOD_ID = "oredetector";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModConfig config;

    @Override
    public void onInitialize() {
        LOGGER.info("OreDetector Mod Initializing (Keybind Only)...");
        config = ModConfig.loadConfig();
        LOGGER.info("Loaded config: maxRadius={}, exactCoords={}", 
            config.getMaxScanRadius(), config.isExactCoordinatesEnabled());
    }
    
    public static ModConfig getConfig() {
        return config;
    }
}
