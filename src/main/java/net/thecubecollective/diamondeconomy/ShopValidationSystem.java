package net.thecubecollective.diamondeconomy;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Shop Validation System - Checks every 10 seconds if shops still exist in the world
 * Removes any shops that have been destroyed by explosions, TNT, creepers, etc.
 */
public class ShopValidationSystem {
    private static final int VALIDATION_INTERVAL_SECONDS = 10;
    private static ScheduledExecutorService scheduler;
    private static MinecraftServer server;
    private static ChestShopManager shopManager;
    
    /**
     * Start the shop validation system
     */
    public static void start(MinecraftServer minecraftServer, ChestShopManager chestShopManager) {
        if (scheduler != null) {
            stop(); // Stop any existing scheduler
        }
        
        server = minecraftServer;
        shopManager = chestShopManager;
        
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "ShopValidator");
            thread.setDaemon(true);
            return thread;
        });
        
        // Start validation after initial 10 second delay, then every 10 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                validateAllShops();
            } catch (Exception e) {
                Tccdiamondeconomy.LOGGER.error("Error during shop validation", e);
            }
        }, VALIDATION_INTERVAL_SECONDS, VALIDATION_INTERVAL_SECONDS, TimeUnit.SECONDS);
        
        Tccdiamondeconomy.LOGGER.info("🔍 Shop Validation System started - checking every {} seconds", VALIDATION_INTERVAL_SECONDS);
    }
    
    /**
     * Stop the shop validation system
     */
    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
        
        Tccdiamondeconomy.LOGGER.info("🔍 Shop Validation System stopped");
    }
    
    /**
     * Validate all shops - remove any that no longer exist in the world
     */
    private static void validateAllShops() {
        if (server == null || shopManager == null) {
            return;
        }
        
        Collection<ChestShopManager.ChestShop> allShops = shopManager.getAllShops();
        if (allShops.isEmpty()) {
            return; // No shops to validate
        }
        
        List<ChestShopManager.ChestShop> shopsToRemove = new ArrayList<>();
        int totalShops = allShops.size();
        int validShops = 0;
        
        for (ChestShopManager.ChestShop shop : allShops) {
            if (!isShopStillValid(shop)) {
                shopsToRemove.add(shop);
                Tccdiamondeconomy.LOGGER.warn("🧹 Shop at {}:{}:{}:{} owned by {} was destroyed - removing from database", 
                        shop.worldName, shop.x, shop.y, shop.z, shop.ownerName);
            } else {
                validShops++;
            }
        }
        
        // Remove invalid shops
        for (ChestShopManager.ChestShop shop : shopsToRemove) {
            shopManager.removeShopDirect(shop.getBlockPos(), shop.worldName, shop.ownerUUID);
        }
        
        if (!shopsToRemove.isEmpty()) {
            Tccdiamondeconomy.LOGGER.info("🧹 Cleaned up {} destroyed shops. Valid shops remaining: {}/{}", 
                    shopsToRemove.size(), validShops, totalShops);
        }
        
        // Log periodic status (every 6th check = every minute)
        if (System.currentTimeMillis() % 60000 < VALIDATION_INTERVAL_SECONDS * 1000) {
            Tccdiamondeconomy.LOGGER.debug("🔍 Shop validation: {}/{} shops are valid", validShops, totalShops);
        }
    }
    
    /**
     * Check if a shop still exists in the world at its recorded position
     */
    private static boolean isShopStillValid(ChestShopManager.ChestShop shop) {
        try {
            // Get the world
            ServerWorld world = getWorldByName(shop.worldName);
            if (world == null) {
                Tccdiamondeconomy.LOGGER.warn("🔍 World '{}' not found for shop validation", shop.worldName);
                return false; // World doesn't exist
            }
            
            // Check if the chunk is loaded
            BlockPos pos = shop.getBlockPos();
            if (!world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                return true; // Assume valid if chunk isn't loaded (don't remove)
            }
            
            // Check if there's a trapped chest at the position
            return world.getBlockState(pos).isOf(Blocks.TRAPPED_CHEST);
            
        } catch (Exception e) {
            Tccdiamondeconomy.LOGGER.error("Error validating shop at {}:{}:{}:{}", 
                    shop.worldName, shop.x, shop.y, shop.z, e);
            return true; // Assume valid on error (don't remove)
        }
    }
    
    /**
     * Get a world by its name/identifier
     */
    private static ServerWorld getWorldByName(String worldName) {
        try {
            // Parse the world identifier
            Identifier worldId = Identifier.of(worldName);
            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, worldId);
            
            return server.getWorld(worldKey);
        } catch (Exception e) {
            Tccdiamondeconomy.LOGGER.debug("Failed to parse world name: {}", worldName, e);
            
            // Fallback: try common world names
            for (ServerWorld world : server.getWorlds()) {
                String registryName = world.getRegistryKey().getValue().toString();
                if (registryName.equals(worldName) || registryName.endsWith(worldName)) {
                    return world;
                }
            }
            
            return null;
        }
    }
    
    /**
     * Manually trigger a validation check (for testing)
     */
    public static void triggerValidation() {
        if (server != null && shopManager != null) {
            validateAllShops();
        }
    }
    
    /**
     * Get validation statistics
     */
    public static String getValidationStats() {
        if (shopManager == null) {
            return "Validation system not initialized";
        }
        
        Collection<ChestShopManager.ChestShop> allShops = shopManager.getAllShops();
        return String.format("Shop Validation: %d shops being monitored, checking every %d seconds", 
                allShops.size(), VALIDATION_INTERVAL_SECONDS);
    }
}
