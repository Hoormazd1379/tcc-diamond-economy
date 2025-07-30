package net.thecubecollective.diamondeconomy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {
    private static final String NOTIFICATIONS_DIR = "diamond_economy";
    private static final String NOTIFICATIONS_FILE = "pending_notifications.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Map<UUID, List<PendingTransfer>> pendingNotifications = new ConcurrentHashMap<>();
    private final Path notificationsFile;
    
    public NotificationManager(MinecraftServer server) {
        this.notificationsFile = server.getRunDirectory().resolve(NOTIFICATIONS_DIR).resolve(NOTIFICATIONS_FILE);
        loadNotifications();
    }
    
    public void addPendingTransfer(UUID recipientUUID, String senderName, long amount) {
        pendingNotifications.computeIfAbsent(recipientUUID, k -> new ArrayList<>())
                .add(new PendingTransfer(senderName, amount, System.currentTimeMillis()));
        saveNotifications();
    }
    
    public void checkAndSendPendingNotifications(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        List<PendingTransfer> notifications = pendingNotifications.remove(playerUUID);
        
        if (notifications != null && !notifications.isEmpty()) {
            // Send header message
            player.sendMessage(Text.literal("💰 === Pending Transfers ===")
                    .formatted(Formatting.GOLD, Formatting.BOLD), false);
            
            long totalReceived = 0;
            for (PendingTransfer transfer : notifications) {
                player.sendMessage(Text.literal("💎 You received " + transfer.amount + " diamonds from " + transfer.senderName + "!")
                        .formatted(Formatting.GREEN), false);
                totalReceived += transfer.amount;
            }
            
            if (notifications.size() > 1) {
                player.sendMessage(Text.literal("Total received: " + totalReceived + " diamonds")
                        .formatted(Formatting.GOLD, Formatting.BOLD), false);
            }
            
            long currentBalance = Tccdiamondeconomy.getBalanceManager().getBalance(playerUUID);
            player.sendMessage(Text.literal("Your current balance: " + currentBalance + " diamonds")
                    .formatted(Formatting.YELLOW), false);
            
            saveNotifications();
        }
    }
    
    private void loadNotifications() {
        try {
            if (Files.exists(notificationsFile)) {
                String json = Files.readString(notificationsFile);
                Map<String, List<PendingTransfer>> loadedData = GSON.fromJson(json, 
                    new TypeToken<Map<String, List<PendingTransfer>>>(){}.getType());
                
                if (loadedData != null) {
                    for (Map.Entry<String, List<PendingTransfer>> entry : loadedData.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(entry.getKey());
                            pendingNotifications.put(uuid, new ArrayList<>(entry.getValue()));
                        } catch (IllegalArgumentException e) {
                            Tccdiamondeconomy.LOGGER.warn("Invalid UUID in notifications file: " + entry.getKey());
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
            Map<String, List<PendingTransfer>> saveData = new HashMap<>();
            for (Map.Entry<UUID, List<PendingTransfer>> entry : pendingNotifications.entrySet()) {
                saveData.put(entry.getKey().toString(), entry.getValue());
            }
            
            String json = GSON.toJson(saveData);
            Files.writeString(notificationsFile, json);
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to save pending notifications", e);
        }
    }
    
    public static class PendingTransfer {
        public final String senderName;
        public final long amount;
        public final long timestamp;
        
        public PendingTransfer(String senderName, long amount, long timestamp) {
            this.senderName = senderName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }
}
