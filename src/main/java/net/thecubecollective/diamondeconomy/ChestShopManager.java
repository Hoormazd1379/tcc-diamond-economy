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
import java.math.RoundingMode;
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
        public String shopName; // New field for shop name
        public String worldName;
        public int x, y, z;
        public BigDecimal pricePerItem;
        public Long legacyPricePerItem; // For backward compatibility during migration
        public long createdTime;
        
        // Shop statistics
        public BigDecimal totalSales = BigDecimal.ZERO; // Total money earned
        public int totalItemsSold = 0; // Total number of items sold
        public int totalTransactions = 0; // Total number of purchases
        public long lastSaleTime = 0; // Timestamp of last sale
        public boolean lowStockNotified = false; // To prevent spam notifications
        
        public ChestShop() {} // For Gson
        
        public ChestShop(UUID ownerUUID, String ownerName, String shopName, BlockPos pos, String worldName, BigDecimal pricePerItem) {
            this.ownerUUID = ownerUUID;
            this.ownerName = ownerName;
            this.shopName = shopName;
            this.worldName = worldName;
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.pricePerItem = pricePerItem;
            this.legacyPricePerItem = null;
            this.createdTime = System.currentTimeMillis();
            this.totalSales = BigDecimal.ZERO;
            this.totalItemsSold = 0;
            this.totalTransactions = 0;
            this.lastSaleTime = 0;
            this.lowStockNotified = false;
        }
        
        // Legacy constructor for backward compatibility
        public ChestShop(UUID ownerUUID, String ownerName, BlockPos pos, String worldName, long pricePerItem) {
            this(ownerUUID, ownerName, "Unnamed Shop", pos, worldName, BigDecimal.valueOf(pricePerItem));
        }
        
        // Method to record a sale
        public void recordSale(int quantity, BigDecimal totalPrice) {
            this.totalSales = this.totalSales.add(totalPrice);
            this.totalItemsSold += quantity;
            this.totalTransactions++;
            this.lastSaleTime = System.currentTimeMillis();
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
    
    public boolean createShop(UUID ownerUUID, String ownerName, String shopName, BlockPos pos, World world, BigDecimal pricePerItem) {
        // Get the main position for this chest structure (handles both single and double chests)
        BlockPos mainPos = TrappedChestUtils.getMainChestPosition(pos, world);
        String locationKey = getLocationKey(world, mainPos);
        
        if (shops.containsKey(locationKey)) {
            return false; // Shop already exists at this location
        }
        
        ChestShop shop = new ChestShop(ownerUUID, ownerName, shopName, mainPos, world.getRegistryKey().getValue().toString(), pricePerItem);
        shops.put(locationKey, shop);
        saveShops();
        
        String chestType = TrappedChestUtils.getChestType(pos, world);
        
        Tccdiamondeconomy.LOGGER.info("Created {} chest shop '{}' at {} for player {} with price {} diamonds per item", 
                chestType.toLowerCase(), shopName, locationKey, ownerName, BalanceManager.formatBalance(pricePerItem));
        
        return true;
    }
    
    // Legacy method for backward compatibility
    public boolean createShop(UUID ownerUUID, String ownerName, BlockPos pos, World world, BigDecimal pricePerItem) {
        return createShop(ownerUUID, ownerName, "Unnamed Shop", pos, world, pricePerItem);
    }
    
    // Legacy method for backward compatibility
    public boolean createShop(UUID ownerUUID, String ownerName, BlockPos pos, World world, long pricePerItem) {
        return createShop(ownerUUID, ownerName, "Unnamed Shop", pos, world, BigDecimal.valueOf(pricePerItem));
    }
    
    public boolean removeShop(BlockPos pos, World world, UUID playerUUID) {
        // Find the shop at this position (handles both single and double chests)
        ChestShop shop = getShop(pos, world);
        
        if (shop == null) {
            return false; // No shop at this location
        }
        
        if (!shop.ownerUUID.equals(playerUUID)) {
            return false; // Not the owner
        }
        
        // Remove using the shop's actual location key
        String locationKey = getLocationKey(world, shop.getBlockPos());
        shops.remove(locationKey);
        saveShops();
        
        String chestType = TrappedChestUtils.getChestType(pos, world);
        
        Tccdiamondeconomy.LOGGER.info("Removed {} chest shop '{}' at {} owned by {}", 
                chestType.toLowerCase(), shop.shopName, locationKey, shop.ownerName);
        
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
        // First check if there's a shop at this exact position
        String locationKey = getLocationKey(world, pos);
        ChestShop shop = shops.get(locationKey);
        if (shop != null) {
            return shop;
        }
        
        // If not found, check if this position is part of a double chest
        // and look for the shop at the main position
        BlockPos mainPos = TrappedChestUtils.getMainChestPosition(pos, world);
        if (!mainPos.equals(pos)) {
            String mainLocationKey = getLocationKey(world, mainPos);
            return shops.get(mainLocationKey);
        }
        
        return null;
    }
    
    public boolean isShop(BlockPos pos, World world) {
        return getShop(pos, world) != null;
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
    
    public List<ChestShop> getTopShops(int limit) {
        return shops.values().stream()
                .sorted((shop1, shop2) -> shop2.totalSales.compareTo(shop1.totalSales)) // Sort by total sales, descending
                .limit(limit)
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
    
    // Record a sale for statistics
    public void recordSale(BlockPos pos, World world, int quantity, BigDecimal totalPrice) {
        ChestShop shop = getShop(pos, world);
        if (shop != null) {
            shop.recordSale(quantity, totalPrice);
            saveShops();
        }
    }
    
    // Check if shop has low stock
    public boolean hasLowStock(BlockPos pos, World world) {
        // Check if inventory has less than 10 items total
        // This will be implemented in the shop interaction logic
        return false; // Placeholder for now
    }
    
    // Update shop price
    public boolean updateShopPrice(BlockPos pos, World world, BigDecimal newPrice) {
        ChestShop shop = getShop(pos, world);
        if (shop != null) {
            shop.pricePerItem = newPrice;
            saveShops();
            return true;
        }
        return false;
    }
    
    // Update shop name
    public boolean updateShopName(BlockPos pos, World world, String newName) {
        ChestShop shop = getShop(pos, world);
        if (shop != null) {
            shop.shopName = newName;
            saveShops();
            return true;
        }
        return false;
    }
    
    // Get economy statistics
    public EconomyStats getEconomyStats() {
        BigDecimal totalMoney = BigDecimal.ZERO;
        int totalShops = shops.size();
        int totalTransactions = 0;
        int totalItemsSold = 0;
        ChestShop mostSuccessfulShop = null;
        BigDecimal highestSales = BigDecimal.ZERO;
        
        for (ChestShop shop : shops.values()) {
            totalMoney = totalMoney.add(shop.totalSales);
            totalTransactions += shop.totalTransactions;
            totalItemsSold += shop.totalItemsSold;
            
            if (shop.totalSales.compareTo(highestSales) > 0) {
                highestSales = shop.totalSales;
                mostSuccessfulShop = shop;
            }
        }
        
        return new EconomyStats(totalMoney, totalShops, totalTransactions, totalItemsSold, mostSuccessfulShop);
    }
    
    // Calculate shop activity index (percentage of shops that had sales in the last week)
    public BigDecimal getShopActivityIndex() {
        if (shops.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L); // 7 days in milliseconds
        
        long activeShops = shops.values().stream()
                .mapToLong(shop -> shop.lastSaleTime > oneWeekAgo ? 1 : 0)
                .sum();
        
        return BigDecimal.valueOf(activeShops)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(shops.size()), 1, RoundingMode.HALF_UP);
    }
    
    // Economy statistics data class
    public static class EconomyStats {
        public final BigDecimal totalMoney;
        public final int totalShops;
        public final int totalTransactions;
        public final int totalItemsSold;
        public final ChestShop mostSuccessfulShop;
        
        public EconomyStats(BigDecimal totalMoney, int totalShops, int totalTransactions, int totalItemsSold, ChestShop mostSuccessfulShop) {
            this.totalMoney = totalMoney;
            this.totalShops = totalShops;
            this.totalTransactions = totalTransactions;
            this.totalItemsSold = totalItemsSold;
            this.mostSuccessfulShop = mostSuccessfulShop;
        }
    }
}
