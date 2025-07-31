package net.thecubecollective.diamondeconomy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChestShopManager {
    private static final String SHOPS_DIR = "diamond_economy";
    private static final String SHOPS_FILE = "chest_shops.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Map<String, ChestShop> shops = new ConcurrentHashMap<>();
    private final Path shopsFile;
    
    public ChestShopManager(MinecraftServer server) {
        Path shopsDirectory = server.getRunDirectory().resolve(SHOPS_DIR);
        try {
            Files.createDirectories(shopsDirectory);
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to create shops directory", e);
        }
        
        this.shopsFile = shopsDirectory.resolve(SHOPS_FILE);
        loadShops();
    }
    
    public static class ChestShop {
        public UUID ownerUUID;
        public String ownerName;
        public String worldName;
        public int x, y, z;
        public BigDecimal pricePerItem;
        public Long legacyPricePerItem; // For backward compatibility during migration
        public long createdTime;
        
        public ChestShop() {} // For Gson
        
        public ChestShop(UUID ownerUUID, String ownerName, BlockPos pos, String worldName, BigDecimal pricePerItem) {
            this.ownerUUID = ownerUUID;
            this.ownerName = ownerName;
            this.worldName = worldName;
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.pricePerItem = pricePerItem;
            this.legacyPricePerItem = null;
            this.createdTime = System.currentTimeMillis();
        }
        
        // Legacy constructor for backward compatibility
        public ChestShop(UUID ownerUUID, String ownerName, BlockPos pos, String worldName, long pricePerItem) {
            this(ownerUUID, ownerName, pos, worldName, BigDecimal.valueOf(pricePerItem));
        }
        
        public BigDecimal getPrice() {
            if (pricePerItem != null) {
                return pricePerItem;
            } else if (legacyPricePerItem != null) {
                // Migrate legacy price
                return BigDecimal.valueOf(legacyPricePerItem);
            }
            return BigDecimal.ZERO;
        }
        
        public BlockPos getBlockPos() {
            return new BlockPos(x, y, z);
        }
        
        public String getLocationKey() {
            return worldName + ":" + x + ":" + y + ":" + z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ChestShop shop = (ChestShop) obj;
            return x == shop.x && y == shop.y && z == shop.z && 
                   Objects.equals(worldName, shop.worldName);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(worldName, x, y, z);
        }
    }
    
    public boolean createShop(UUID ownerUUID, String ownerName, BlockPos pos, World world, BigDecimal pricePerItem) {
        String locationKey = getLocationKey(world, pos);
        
        if (shops.containsKey(locationKey)) {
            return false; // Shop already exists at this location
        }
        
        ChestShop shop = new ChestShop(ownerUUID, ownerName, pos, world.getRegistryKey().getValue().toString(), pricePerItem);
        shops.put(locationKey, shop);
        saveShops();
        
        Tccdiamondeconomy.LOGGER.info("Created chest shop at {} for player {} with price {} diamonds per item", 
                locationKey, ownerName, BalanceManager.formatBalance(pricePerItem));
        
        return true;
    }
    
    // Legacy method for backward compatibility
    public boolean createShop(UUID ownerUUID, String ownerName, BlockPos pos, World world, long pricePerItem) {
        return createShop(ownerUUID, ownerName, pos, world, BigDecimal.valueOf(pricePerItem));
    }
    
    public boolean removeShop(BlockPos pos, World world, UUID playerUUID) {
        String locationKey = getLocationKey(world, pos);
        ChestShop shop = shops.get(locationKey);
        
        if (shop == null) {
            return false; // No shop at this location
        }
        
        if (!shop.ownerUUID.equals(playerUUID)) {
            return false; // Not the owner
        }
        
        shops.remove(locationKey);
        saveShops();
        
        Tccdiamondeconomy.LOGGER.info("Removed chest shop at {} owned by {}", locationKey, shop.ownerName);
        
        return true;
    }
    
    // Direct removal for cleanup purposes (bypasses owner check)
    public boolean removeShopDirect(BlockPos pos, String worldName, UUID ownerUUID) {
        String locationKey = worldName + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ();
        ChestShop shop = shops.get(locationKey);
        
        if (shop == null) {
            return false; // No shop at this location
        }
        
        shops.remove(locationKey);
        saveShops();
        
        Tccdiamondeconomy.LOGGER.info("Cleaned up destroyed chest shop at {} owned by {}", locationKey, shop.ownerName);
        
        return true;
    }
    
    public ChestShop getShop(BlockPos pos, World world) {
        String locationKey = getLocationKey(world, pos);
        return shops.get(locationKey);
    }
    
    public boolean isShop(BlockPos pos, World world) {
        String locationKey = getLocationKey(world, pos);
        return shops.containsKey(locationKey);
    }
    
    public boolean isShopOwner(BlockPos pos, World world, UUID playerUUID) {
        ChestShop shop = getShop(pos, world);
        return shop != null && shop.ownerUUID.equals(playerUUID);
    }
    
    public Collection<ChestShop> getAllShops() {
        return new ArrayList<>(shops.values());
    }
    
    public List<ChestShop> getShopsByOwner(UUID ownerUUID) {
        return shops.values().stream()
                .filter(shop -> shop.ownerUUID.equals(ownerUUID))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    private String getLocationKey(World world, BlockPos pos) {
        return world.getRegistryKey().getValue().toString() + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ();
    }
    
    private void loadShops() {
        if (!Files.exists(shopsFile)) {
            Tccdiamondeconomy.LOGGER.info("No existing shops file found, starting with empty shops");
            return;
        }
        
        try (FileReader reader = new FileReader(shopsFile.toFile())) {
            Type shopsMapType = new TypeToken<Map<String, ChestShop>>(){}.getType();
            Map<String, ChestShop> loadedShops = GSON.fromJson(reader, shopsMapType);
            
            if (loadedShops != null) {
                shops.clear();
                shops.putAll(loadedShops);
                Tccdiamondeconomy.LOGGER.info("Loaded {} chest shops from file", shops.size());
            }
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to load chest shops", e);
        }
    }
    
    public void saveShops() {
        try (FileWriter writer = new FileWriter(shopsFile.toFile())) {
            GSON.toJson(shops, writer);
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to save chest shops", e);
        }
    }
}
