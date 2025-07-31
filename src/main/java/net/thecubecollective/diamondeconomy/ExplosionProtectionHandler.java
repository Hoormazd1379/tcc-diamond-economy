package net.thecubecollective.diamondeconomy;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExplosionProtectionHandler {
    
    public static void register() {
        // Explosion protection is primarily handled by block break events
        // This handler provides utilities for explosion detection near shops
        Tccdiamondeconomy.LOGGER.info("🛡️ Explosion protection enabled via block break events");
    }
    
    /**
     * Check if any shops exist in explosion radius for logging
     */
    public static boolean hasShopsInRadius(World world, BlockPos center, float radius) {
        if (world == null) return false;
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        int radiusInt = (int) Math.ceil(radius);
        
        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    BlockPos pos = center.add(x, y, z);
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    
                    if (distance <= radius) {
                        BlockState state = world.getBlockState(pos);
                        // Check for trapped chests (custom blocks temporarily disabled)
                        if (state.isOf(Blocks.TRAPPED_CHEST) && shopManager.isShop(pos, world)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
}
