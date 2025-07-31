package net.thecubecollective.diamondeconomy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {
    private static final String NOTIFICATIONS_DIR = "diamond_economy";
    private static final String NOTIFICATIONS_FILE = "pending_notifications.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Map<UUID, List<PendingTransfer>> pendingTransfers = new ConcurrentHashMap<>();
    private final Map<UUID, List<PendingShopSale>> pendingShopSales = new ConcurrentHashMap<>();
    private final Path notificationsFile;
    
    public NotificationManager(MinecraftServer server) {
        this.notificationsFile = server.getRunDirectory().resolve(NOTIFICATIONS_DIR).resolve(NOTIFICATIONS_FILE);
        loadNotifications();
    }
    
    public void addPendingTransfer(UUID recipientUUID, String senderName, BigDecimal amount) {
        pendingTransfers.computeIfAbsent(recipientUUID, k -> new ArrayList<>())
                .add(new PendingTransfer(senderName, amount, System.currentTimeMillis()));
        saveNotifications();
    }
    
    // Legacy method for backward compatibility
    public void addPendingTransfer(UUID recipientUUID, String senderName, long amount) {
        addPendingTransfer(recipientUUID, senderName, BigDecimal.valueOf(amount));
    }
    
    public void addPendingShopSale(UUID shopOwnerUUID, String buyerName, String itemName, int quantity, BigDecimal earnings) {
        pendingShopSales.computeIfAbsent(shopOwnerUUID, k -> new ArrayList<>())
                .add(new PendingShopSale(buyerName, itemName, quantity, earnings, System.currentTimeMillis()));
        saveNotifications();
    }
    
    // Legacy method for backward compatibility
    public void addPendingShopSale(UUID shopOwnerUUID, String buyerName, String itemName, int quantity, long earnings) {
        addPendingShopSale(shopOwnerUUID, buyerName, itemName, quantity, BigDecimal.valueOf(earnings));
    }
    
    public void checkAndSendPendingNotifications(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        List<PendingTransfer> transferNotifications = pendingTransfers.remove(playerUUID);
        List<PendingShopSale> shopSaleNotifications = pendingShopSales.remove(playerUUID);
        
        // Send transfer notifications
        if (transferNotifications != null && !transferNotifications.isEmpty()) {
            player.sendMessage(Text.literal("💰 === Pending Transfers ===")
                    .formatted(Formatting.GOLD, Formatting.BOLD), false);
            
            BigDecimal totalReceived = BigDecimal.ZERO;
            for (PendingTransfer transfer : transferNotifications) {
                BigDecimal amount = transfer.getAmount();
                player.sendMessage(Text.literal("💎 You received " + BalanceManager.formatBalance(amount) + " diamonds from " + transfer.senderName + "!")
                        .formatted(Formatting.GREEN), false);
                totalReceived = totalReceived.add(amount);
            }
            
            if (transferNotifications.size() > 1) {
                player.sendMessage(Text.literal("Total received: " + BalanceManager.formatBalance(totalReceived) + " diamonds")
                        .formatted(Formatting.GOLD, Formatting.BOLD), false);
            }
            
            player.sendMessage(Text.literal(""), false); // Empty line
        }
        
        // Send shop sale notifications
        if (shopSaleNotifications != null && !shopSaleNotifications.isEmpty()) {
            player.sendMessage(Text.literal("🏪 === Shop Sales Summary ===")
                    .formatted(Formatting.GREEN, Formatting.BOLD), false);
            
            // Consolidate sales by buyer and item
            Map<String, Map<String, ConsolidatedSale>> consolidatedSales = new HashMap<>();
            BigDecimal totalEarned = BigDecimal.ZERO;
            
            for (PendingShopSale sale : shopSaleNotifications) {
                BigDecimal saleEarnings = sale.getEarnings();
                consolidatedSales.computeIfAbsent(sale.buyerName, k -> new HashMap<>())
                    .merge(sale.itemName, 
                           new ConsolidatedSale(sale.buyerName, sale.itemName, sale.quantity, saleEarnings),
                           (existing, newSale) -> new ConsolidatedSale(
                               existing.buyerName,
                               existing.itemName,
                               existing.totalQuantity + newSale.totalQuantity,
                               existing.totalEarnings.add(newSale.totalEarnings)
                           ));
                totalEarned = totalEarned.add(saleEarnings);
            }
            
            // Display consolidated sales
            for (Map<String, ConsolidatedSale> buyerSales : consolidatedSales.values()) {
                for (ConsolidatedSale sale : buyerSales.values()) {
                    player.sendMessage(Text.literal("💰 SALE! " + sale.buyerName + " bought " + 
                            sale.totalQuantity + "x " + sale.itemName + 
                            " from your shop for " + BalanceManager.formatBalance(sale.totalEarnings) + " diamonds!")
                            .formatted(Formatting.GREEN), false);
                }
            }
            
            if (shopSaleNotifications.size() > 1) {
                player.sendMessage(Text.literal("Total shop earnings: " + BalanceManager.formatBalance(totalEarned) + " diamonds")
                        .formatted(Formatting.GOLD, Formatting.BOLD), false);
            }
            
            player.sendMessage(Text.literal(""), false); // Empty line
        }
        
        // Show current balance if any notifications were sent
        if ((transferNotifications != null && !transferNotifications.isEmpty()) || 
            (shopSaleNotifications != null && !shopSaleNotifications.isEmpty())) {
            
            BigDecimal currentBalance = Tccdiamondeconomy.getBalanceManager().getBalance(playerUUID);
            player.sendMessage(Text.literal("Your current balance: " + BalanceManager.formatBalance(currentBalance) + " diamonds")
                    .formatted(Formatting.YELLOW), false);
            
            saveNotifications();
        }
    }
    
    private void loadNotifications() {
        try {
            if (Files.exists(notificationsFile)) {
                String json = Files.readString(notificationsFile);
                NotificationData loadedData = GSON.fromJson(json, NotificationData.class);
                
                if (loadedData != null) {
                    // Load transfer notifications
                    if (loadedData.transfers != null) {
                        for (Map.Entry<String, List<PendingTransfer>> entry : loadedData.transfers.entrySet()) {
                            try {
                                UUID uuid = UUID.fromString(entry.getKey());
                                pendingTransfers.put(uuid, new ArrayList<>(entry.getValue()));
                            } catch (IllegalArgumentException e) {
                                Tccdiamondeconomy.LOGGER.warn("Invalid UUID in transfer notifications: " + entry.getKey());
                            }
                        }
                    }
                    
                    // Load shop sale notifications
                    if (loadedData.shopSales != null) {
                        for (Map.Entry<String, List<PendingShopSale>> entry : loadedData.shopSales.entrySet()) {
                            try {
                                UUID uuid = UUID.fromString(entry.getKey());
                                pendingShopSales.put(uuid, new ArrayList<>(entry.getValue()));
                            } catch (IllegalArgumentException e) {
                                Tccdiamondeconomy.LOGGER.warn("Invalid UUID in shop sale notifications: " + entry.getKey());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to load pending notifications", e);
        }
    }
    
    private void saveNotifications() {
        try {
            Files.createDirectories(notificationsFile.getParent());
            
            // Convert UUID keys to strings for JSON serialization
            Map<String, List<PendingTransfer>> transferData = new HashMap<>();
            for (Map.Entry<UUID, List<PendingTransfer>> entry : pendingTransfers.entrySet()) {
                transferData.put(entry.getKey().toString(), entry.getValue());
            }
            
            Map<String, List<PendingShopSale>> shopSaleData = new HashMap<>();
            for (Map.Entry<UUID, List<PendingShopSale>> entry : pendingShopSales.entrySet()) {
                shopSaleData.put(entry.getKey().toString(), entry.getValue());
            }
            
            NotificationData saveData = new NotificationData();
            saveData.transfers = transferData;
            saveData.shopSales = shopSaleData;
            
            String json = GSON.toJson(saveData);
            Files.writeString(notificationsFile, json);
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to save pending notifications", e);
        }
    }
    
    public static class PendingTransfer {
        public final String senderName;
        public final BigDecimal amount;
        public final Long legacyAmount; // For backward compatibility
        public final long timestamp;
        
        public PendingTransfer(String senderName, BigDecimal amount, long timestamp) {
            this.senderName = senderName;
            this.amount = amount;
            this.legacyAmount = null;
            this.timestamp = timestamp;
        }
        
        // Legacy constructor for backward compatibility
        public PendingTransfer(String senderName, long amount, long timestamp) {
            this.senderName = senderName;
            this.amount = BigDecimal.valueOf(amount);
            this.legacyAmount = amount;
            this.timestamp = timestamp;
        }
        
        public BigDecimal getAmount() {
            return amount != null ? amount : (legacyAmount != null ? BigDecimal.valueOf(legacyAmount) : BigDecimal.ZERO);
        }
    }
    
    public static class PendingShopSale {
        public final String buyerName;
        public final String itemName;
        public final int quantity;
        public final BigDecimal earnings;
        public final Long legacyEarnings; // For backward compatibility
        public final long timestamp;
        
        public PendingShopSale(String buyerName, String itemName, int quantity, BigDecimal earnings, long timestamp) {
            this.buyerName = buyerName;
            this.itemName = itemName;
            this.quantity = quantity;
            this.earnings = earnings;
            this.legacyEarnings = null;
            this.timestamp = timestamp;
        }
        
        // Legacy constructor for backward compatibility
        public PendingShopSale(String buyerName, String itemName, int quantity, long earnings, long timestamp) {
            this.buyerName = buyerName;
            this.itemName = itemName;
            this.quantity = quantity;
            this.earnings = BigDecimal.valueOf(earnings);
            this.legacyEarnings = earnings;
            this.timestamp = timestamp;
        }
        
        public BigDecimal getEarnings() {
            return earnings != null ? earnings : (legacyEarnings != null ? BigDecimal.valueOf(legacyEarnings) : BigDecimal.ZERO);
        }
    }
    
    public static class NotificationData {
        public Map<String, List<PendingTransfer>> transfers;
        public Map<String, List<PendingShopSale>> shopSales;
    }
    
    private static class ConsolidatedSale {
        public final String buyerName;
        public final String itemName;
        public final int totalQuantity;
        public final BigDecimal totalEarnings;
        
        public ConsolidatedSale(String buyerName, String itemName, int totalQuantity, BigDecimal totalEarnings) {
            this.buyerName = buyerName;
            this.itemName = itemName;
            this.totalQuantity = totalQuantity;
            this.totalEarnings = totalEarnings;
        }
    }
}
