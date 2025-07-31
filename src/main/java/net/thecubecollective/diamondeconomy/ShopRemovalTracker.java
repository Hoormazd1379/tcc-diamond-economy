package net.thecubecollective.diamondeconomy;

import net.minecraft.util.math.BlockPos;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Tracks temporary permissions for shop removal
 * Players get permission when they use /removeshop command
 */
public class ShopRemovalTracker {
    private static final Map<String, Long> removalPermissions = new ConcurrentHashMap<>();
    private static final long PERMISSION_TIMEOUT = 30000; // 30 seconds
    
    /**
     * Grant temporary permission to remove a specific shop
     */
    public static void grantPermission(BlockPos pos, UUID playerUUID) {
        String key = getKey(pos, playerUUID);
        removalPermissions.put(key, System.currentTimeMillis() + PERMISSION_TIMEOUT);
        
        // Clean up expired permissions
        cleanupExpiredPermissions();
    }
    
    /**
     * Check if player has permission to remove a specific shop
     */
    public static boolean hasPermission(BlockPos pos, UUID playerUUID) {
        String key = getKey(pos, playerUUID);
        Long expireTime = removalPermissions.get(key);
        
        if (expireTime == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > expireTime) {
            // Permission expired
            removalPermissions.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Revoke permission after successful removal
     */
    public static void revokePermission(BlockPos pos, UUID playerUUID) {
        String key = getKey(pos, playerUUID);
        removalPermissions.remove(key);
    }
    
    /**
     * Clean up expired permissions
     */
    private static void cleanupExpiredPermissions() {
        long currentTime = System.currentTimeMillis();
        removalPermissions.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }
    
    /**
     * Generate unique key for position + player combination
     */
    private static String getKey(BlockPos pos, UUID playerUUID) {
        return pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":" + playerUUID.toString();
    }
}
