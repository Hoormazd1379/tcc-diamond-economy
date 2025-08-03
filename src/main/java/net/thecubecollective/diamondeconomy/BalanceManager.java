package net.thecubecollective.diamondeconomy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BalanceManager {
    private static final String BALANCE_DIR = "diamond_economy";
    private static final String BALANCE_FILE_EXTENSION = ".json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Map<UUID, BigDecimal> balanceCache = new ConcurrentHashMap<>();
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
    
    public BigDecimal getBalance(UUID playerUUID) {
        return balanceCache.getOrDefault(playerUUID, BigDecimal.ZERO);
    }
    
    public void setBalance(UUID playerUUID, BigDecimal amount) {
        balanceCache.put(playerUUID, amount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        saveBalance(playerUUID);
    }
    
    public void addBalance(UUID playerUUID, BigDecimal amount) {
        BigDecimal currentBalance = getBalance(playerUUID);
        setBalance(playerUUID, currentBalance.add(amount));
    }
    
    // Legacy method for backward compatibility (accepts long)
    public void addBalance(UUID playerUUID, long amount) {
        addBalance(playerUUID, BigDecimal.valueOf(amount));
    }
    
    public boolean removeBalance(UUID playerUUID, BigDecimal amount) {
        BigDecimal currentBalance = getBalance(playerUUID);
        if (currentBalance.compareTo(amount) >= 0) {
            setBalance(playerUUID, currentBalance.subtract(amount));
            return true;
        }
        return false;
    }
    
    // Legacy method for backward compatibility (accepts long)  
    public boolean removeBalance(UUID playerUUID, long amount) {
        return removeBalance(playerUUID, BigDecimal.valueOf(amount));
    }
    
    public List<Map.Entry<UUID, BigDecimal>> getTopBalances(int limit) {
        return balanceCache.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }
    
    /**
     * Format a BigDecimal balance for display, removing unnecessary decimal places
     */
    public static String formatBalance(BigDecimal amount) {
        if (amount.scale() <= 0 || amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            // Whole number, display without decimals
            return amount.setScale(0, RoundingMode.DOWN).toString();
        } else {
            // Has fractional part, display with up to 2 decimal places, removing trailing zeros
            return amount.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        }
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
                // Handle legacy long balances and new BigDecimal balances
                if (balance.balance != null) {
                    balanceCache.put(balance.uuid, balance.balance);
                } else if (balance.legacyBalance != null) {
                    // Migrate legacy long balance to BigDecimal
                    balanceCache.put(balance.uuid, BigDecimal.valueOf(balance.legacyBalance));
                    // Save in new format
                    saveBalance(balance.uuid);
                }
            }
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to load balance file: " + balanceFile, e);
        }
    }
    
    // Get total money in circulation across all players
    public BigDecimal getTotalMoney() {
        return balanceCache.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Get average player balance
    public BigDecimal getAverageBalance() {
        if (balanceCache.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = getTotalMoney();
        return total.divide(BigDecimal.valueOf(balanceCache.size()), 2, RoundingMode.HALF_UP);
    }
    
    // Calculate Gini coefficient for economic inequality (0 = perfect equality, 1 = maximum inequality)
    public BigDecimal getEconomicInequalityIndex() {
        if (balanceCache.size() <= 1) {
            return BigDecimal.ZERO; // Perfect equality with 0 or 1 player
        }
        
        List<BigDecimal> sortedBalances = balanceCache.values().stream()
                .sorted()
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        BigDecimal sumOfAbsoluteDifferences = BigDecimal.ZERO;
        BigDecimal mean = getAverageBalance();
        int n = sortedBalances.size();
        
        // Calculate Gini coefficient using the formula:
        // G = (1/n) * (1/mean) * sum of |xi - xj| for all i,j / 2
        // Simplified calculation for better performance
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                BigDecimal diff = sortedBalances.get(j).subtract(sortedBalances.get(i));
                sumOfAbsoluteDifferences = sumOfAbsoluteDifferences.add(diff);
            }
        }
        
        if (mean.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // G = (2 * sumOfAbsoluteDifferences) / (n * (n-1) * mean)
        BigDecimal denominator = BigDecimal.valueOf(n)
                .multiply(BigDecimal.valueOf(n - 1))
                .multiply(mean);
        
        BigDecimal gini = sumOfAbsoluteDifferences.multiply(BigDecimal.valueOf(2))
                .divide(denominator, 4, RoundingMode.HALF_UP);
        
        return gini.min(BigDecimal.ONE); // Cap at 1.0
    }
    
    // Get number of players with balances
    public int getPlayerCount() {
        return balanceCache.size();
    }
    
    // Find a player's UUID by their name (for offline transfers)
    // This method checks all balance files to find a player who has played before
    public UUID findPlayerUUIDByName(String playerName) {
        try {
            if (!Files.exists(balanceDirectory)) {
                return null;
            }
            
            // Go through all balance files to find a matching player
            return Files.list(balanceDirectory)
                    .filter(path -> path.toString().endsWith(BALANCE_FILE_EXTENSION))
                    .map(this::extractUUIDFromFile)
                    .filter(Objects::nonNull)
                    .filter(uuid -> {
                        // Check multiple sources for the player name
                        // 1. Try user cache first (fast)
                        Optional<String> cachedName = Tccdiamondeconomy.getServer().getUserCache()
                                .getByUuid(uuid)
                                .map(profile -> profile.getName());
                        
                        if (cachedName.isPresent()) {
                            return cachedName.get().equalsIgnoreCase(playerName);
                        }
                        
                        // 2. If not in cache, we found a UUID but can't verify the name
                        // This is a limitation, but we'll skip this UUID for safety
                        return false;
                    })
                    .findFirst()
                    .orElse(null);
                    
        } catch (IOException e) {
            Tccdiamondeconomy.LOGGER.error("Failed to search for player UUID by name: " + playerName, e);
            return null;
        }
    }
    
    private UUID extractUUIDFromFile(Path balanceFile) {
        try {
            String fileName = balanceFile.getFileName().toString();
            // Remove the .json extension
            String uuidString = fileName.substring(0, fileName.length() - BALANCE_FILE_EXTENSION.length());
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            Tccdiamondeconomy.LOGGER.warn("Invalid UUID in balance file name: " + balanceFile);
            return null;
        }
    }
    
    private static class PlayerBalance {
        public final UUID uuid;
        public final BigDecimal balance;
        public final Long legacyBalance; // For backward compatibility during migration
        
        public PlayerBalance(UUID uuid, BigDecimal balance) {
            this.uuid = uuid;
            this.balance = balance;
            this.legacyBalance = null;
        }
        
        // For legacy compatibility during JSON deserialization
        public PlayerBalance(UUID uuid, Long legacyBalance) {
            this.uuid = uuid;
            this.balance = null;
            this.legacyBalance = legacyBalance;
        }
    }
}
