package net.thecubecollective.diamondeconomy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class ShopParticleManager {
    private static int tickCounter = 0;
    private static final int PARTICLE_INTERVAL = 10; // Spawn particles every 10 ticks (0.5 seconds) - more frequent
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= PARTICLE_INTERVAL) {
                tickCounter = 0;
                spawnAllShopParticles(server);
            }
        });
    }
    
    private static void spawnAllShopParticles(net.minecraft.server.MinecraftServer server) {
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        if (shopManager == null) return;
        
        Random random = new Random();
        
        // Spawn particles for all shops
        for (ChestShopManager.ChestShop shop : shopManager.getAllShops()) {
            // Get the world for this shop
            Identifier worldId = Identifier.tryParse(shop.worldName);
            if (worldId == null) continue;
            
            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, worldId);
            ServerWorld world = server.getWorld(worldKey);
            
            if (world != null) {
                BlockPos pos = shop.getBlockPos();
                
                // Check if the chest still exists and is a trapped chest
                if (world.getBlockState(pos).isOf(net.minecraft.block.Blocks.TRAPPED_CHEST)) {
                    // For double chest shops, spawn particles at ALL chest positions
                    java.util.List<BlockPos> chestPositions = TrappedChestUtils.getChestPositions(pos, world);
                    for (BlockPos chestPos : chestPositions) {
                        if (world.getBlockState(chestPos).isOf(net.minecraft.block.Blocks.TRAPPED_CHEST)) {
                            spawnShopParticles(world, chestPos, random);
                        }
                    }
                }
            }
        }
    }
    
    private static void spawnShopParticles(ServerWorld world, BlockPos pos, Random random) {
        // Enhanced particle effects - more particles and varied heights
        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + 0.5 + (random.nextGaussian() * 0.4);
            double y = pos.getY() + 0.8 + (random.nextDouble() * 0.6); // Varied height
            double z = pos.getZ() + 0.5 + (random.nextGaussian() * 0.4);
            
            // More varied particle types with higher frequency
            int particleType = random.nextInt(3); // Reduced from 4 to 3 (removed EFFECT)
            switch (particleType) {
                case 0:
                    world.spawnParticles(ParticleTypes.COMPOSTER, x, y, z, 1, 0, 0.05, 0, 0.03);
                    break;
                case 1:
                    world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0, 0.05, 0, 0.03);
                    break;
                case 2:
                    world.spawnParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0, 0.05, 0, 0.02);
                    break;
            }
        }
        
        // Add floating sparkle effect around the chest
        if (random.nextInt(2) == 0) { // Increased from 33% to 50% chance each tick
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 1.2;
            double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double y = pos.getY() + 1.0 + (random.nextDouble() * 0.3);
            double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;
            
            world.spawnParticles(ParticleTypes.WAX_ON, x, y, z, 1, 0, 0, 0, 0.01);
        }
        
        // Occasionally spawn a more noticeable golden effect
        if (random.nextInt(25) == 0) { // Increased from 2% to 4% chance
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;
            
            // Enhanced enchant particle effect (removed totem effect)
            world.spawnParticles(ParticleTypes.ENCHANT, x, y, z, 12, 0.4, 0.4, 0.4, 0.15);
        }
    }
}
