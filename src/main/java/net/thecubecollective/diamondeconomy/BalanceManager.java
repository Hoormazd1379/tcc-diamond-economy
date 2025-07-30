package net.thecubecollective.diamondeconomy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BalanceManager {
    private static final String BALANCE_DIR = "diamond_economy";
    private static final String BALANCE_FILE_EXTENSION = ".json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Map<UUID, Long> balanceCache = new ConcurrentHashMap<>();
    private final Path balanceDirectory;
    
    public BalanceManager(MinecraftServer server) {
        this.balanceDirectory = server.getRunDirectory().resolve(BALANCE_DIR);
        try {
            Files.createDirectories(balanceDirectory);
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to create balance directory", e);
        }
        loadAllBalances();
    }
    
    public long getBalance(UUID playerUUID) {
        return balanceCache.getOrDefault(playerUUID, 0L);
    }
    
    public void setBalance(UUID playerUUID, long amount) {
        balanceCache.put(playerUUID, Math.max(0, amount));
        saveBalance(playerUUID);
    }
    
    public void addBalance(UUID playerUUID, long amount) {
        long currentBalance = getBalance(playerUUID);
        setBalance(playerUUID, currentBalance + amount);
    }
    
    public boolean removeBalance(UUID playerUUID, long amount) {
        long currentBalance = getBalance(playerUUID);
        if (currentBalance >= amount) {
            setBalance(playerUUID, currentBalance - amount);
            return true;
        }
        return false;
    }
    
    public List<Map.Entry<UUID, Long>> getTopBalances(int limit) {
        return balanceCache.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }
    
    private void saveBalance(UUID playerUUID) {
        Path playerFile = balanceDirectory.resolve(playerUUID.toString() + BALANCE_FILE_EXTENSION);
        try {
            PlayerBalance balance = new PlayerBalance(playerUUID, getBalance(playerUUID));
            String json = GSON.toJson(balance);
            Files.writeString(playerFile, json);
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to save balance for player " + playerUUID, e);
        }
    }
    
    private void loadAllBalances() {
        try {
            if (!Files.exists(balanceDirectory)) {
                return;
            }
            
            Files.list(balanceDirectory)
                    .filter(path -> path.toString().endsWith(BALANCE_FILE_EXTENSION))
                    .forEach(this::loadBalance);
                    
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to load balances", e);
        }
    }
    
    private void loadBalance(Path balanceFile) {
        try {
            String json = Files.readString(balanceFile);
            PlayerBalance balance = GSON.fromJson(json, PlayerBalance.class);
            if (balance != null && balance.uuid != null) {
                balanceCache.put(balance.uuid, balance.balance);
            }
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to load balance file: " + balanceFile, e);
        }
    }
    
    private static class PlayerBalance {
        public final UUID uuid;
        public final long balance;
        
        public PlayerBalance(UUID uuid, long balance) {
            this.uuid = uuid;
            this.balance = balance;
        }
    }
}
