package com.oredetector.utils;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class ChatFormatter {
    private static final String PREFIX = "§8[§6OreDetector§8]§r ";
    
    public static Text info(String message) {
        return Text.literal(PREFIX + message).formatted(Formatting.GRAY);
    }
    
    public static Text success(String message) {
        return Text.literal(PREFIX + message).formatted(Formatting.GREEN);
    }
    
    public static Text error(String message) {
        return Text.literal(PREFIX + message).formatted(Formatting.RED);
    }
    
    public static Text warning(String message) {
        return Text.literal(PREFIX + message).formatted(Formatting.YELLOW);
    }
    
    public static Text highlight(String message) {
        return Text.literal(PREFIX + message).formatted(Formatting.GOLD, Formatting.BOLD);
    }
    
    public static String getDirection(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz)) return dx > 0 ? "East" : "West";
        else return dz > 0 ? "South" : "North";
    }
    
    public static int getDistance(BlockPos from, BlockPos to) {
        return (int) Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + 
                               Math.pow(from.getY() - to.getY(), 2) + 
                               Math.pow(from.getZ() - to.getZ(), 2));
    }
}
