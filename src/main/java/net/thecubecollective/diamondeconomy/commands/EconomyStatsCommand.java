package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thecubecollective.diamondeconomy.BalanceManager;
import net.thecubecollective.diamondeconomy.ChestShopManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EconomyStatsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("economystats")
                .executes(EconomyStatsCommand::showEconomyStats));
    }
    
    private static int showEconomyStats(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by players!"));
            return 0;
        }
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        BalanceManager balanceManager = Tccdiamondeconomy.getBalanceManager();
        
        // Get shop statistics
        ChestShopManager.EconomyStats shopStats = shopManager.getEconomyStats();
        
        // Get total money in circulation (from all player balances)
        BigDecimal totalMoneyInCirculation = balanceManager.getTotalMoney();
        BigDecimal averagePlayerBalance = balanceManager.getAverageBalance();
        BigDecimal economicInequalityIndex = balanceManager.getEconomicInequalityIndex();
        int playerCount = balanceManager.getPlayerCount();
        
        // Get shop activity index
        BigDecimal shopActivityIndex = shopManager.getShopActivityIndex();
        
        // Display economy statistics
        player.sendMessage(Text.literal("=== Server Economy Statistics ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Money statistics
        player.sendMessage(Text.literal("💰 Money in Circulation: ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(BalanceManager.formatBalance(totalMoneyInCirculation) + " diamonds")
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal("🛒 Total Shop Sales: ")
                .formatted(Formatting.BLUE)
                .append(Text.literal(BalanceManager.formatBalance(shopStats.totalMoney) + " diamonds")
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Player statistics
        player.sendMessage(Text.literal("👥 Active Players: ")
                .formatted(Formatting.AQUA)
                .append(Text.literal(String.valueOf(playerCount))
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal("⚖️ Average Player Balance: ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(BalanceManager.formatBalance(averagePlayerBalance) + " diamonds")
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        // Economic inequality index with interpretation
        String inequalityColor = getInequalityColor(economicInequalityIndex);
        String inequalityDescription = getInequalityDescription(economicInequalityIndex);
        
        player.sendMessage(Text.literal("📊 Economic Inequality Index: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(BalanceManager.formatBalance(economicInequalityIndex.multiply(BigDecimal.valueOf(100))) + "%")
                        .formatted(Formatting.valueOf(inequalityColor), Formatting.BOLD))
                .append(Text.literal(" (" + inequalityDescription + ")")
                        .formatted(Formatting.GRAY)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Shop statistics
        player.sendMessage(Text.literal("🏪 Total Shops: ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(shopStats.totalShops))
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        // Shop activity index with color coding
        String activityColor = getActivityColor(shopActivityIndex);
        
        player.sendMessage(Text.literal("📈 Shop Activity Index: ")
                .formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(BalanceManager.formatBalance(shopActivityIndex) + "%")
                        .formatted(Formatting.valueOf(activityColor), Formatting.BOLD))
                .append(Text.literal(" (shops with sales in last 7 days)")
                        .formatted(Formatting.GRAY)), false);
        
        player.sendMessage(Text.literal("🔄 Total Transactions: ")
                .formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(String.valueOf(shopStats.totalTransactions))
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal("📦 Total Items Sold: ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(shopStats.totalItemsSold))
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        // Most successful shop
        if (shopStats.mostSuccessfulShop != null) {
            player.sendMessage(Text.literal(""), false); // Empty line
            
            String topShopName = (shopStats.mostSuccessfulShop.shopName != null && !shopStats.mostSuccessfulShop.shopName.trim().isEmpty()) 
                    ? shopStats.mostSuccessfulShop.shopName : "Unnamed Shop";
            
            player.sendMessage(Text.literal("🏆 Most Successful Shop:")
                    .formatted(Formatting.GOLD, Formatting.BOLD), false);
            
            player.sendMessage(Text.literal("   🏪 Name: ")
                    .formatted(Formatting.AQUA)
                    .append(Text.literal(topShopName)
                            .formatted(Formatting.WHITE, Formatting.BOLD)), false);
            
            player.sendMessage(Text.literal("   👤 Owner: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(shopStats.mostSuccessfulShop.ownerName)
                            .formatted(Formatting.WHITE)), false);
            
            player.sendMessage(Text.literal("   💰 Sales: ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(BalanceManager.formatBalance(shopStats.mostSuccessfulShop.totalSales) + " diamonds")
                            .formatted(Formatting.WHITE, Formatting.BOLD)), false);
            
            player.sendMessage(Text.literal("   📍 Location: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(shopStats.mostSuccessfulShop.x + ", " + shopStats.mostSuccessfulShop.y + ", " + shopStats.mostSuccessfulShop.z)
                            .formatted(Formatting.WHITE)), false);
        } else {
            player.sendMessage(Text.literal(""), false); // Empty line
            player.sendMessage(Text.literal("🏆 No shops have made any sales yet!")
                    .formatted(Formatting.GRAY), false);
        }
        
        // Fun statistics
        if (shopStats.totalTransactions > 0) {
            BigDecimal avgTransactionValue = shopStats.totalMoney.divide(BigDecimal.valueOf(shopStats.totalTransactions), 2, RoundingMode.HALF_UP);
            
            player.sendMessage(Text.literal(""), false); // Empty line
            player.sendMessage(Text.literal("📈 Fun Stats:")
                    .formatted(Formatting.YELLOW, Formatting.BOLD), false);
            
            player.sendMessage(Text.literal("   💸 Average Transaction: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(BalanceManager.formatBalance(avgTransactionValue) + " diamonds")
                            .formatted(Formatting.WHITE)), false);
            
            if (shopStats.totalItemsSold > 0) {
                BigDecimal avgItemPrice = shopStats.totalMoney.divide(BigDecimal.valueOf(shopStats.totalItemsSold), 2, RoundingMode.HALF_UP);
                player.sendMessage(Text.literal("   💎 Average Item Price: ")
                        .formatted(Formatting.GRAY)
                        .append(Text.literal(BalanceManager.formatBalance(avgItemPrice) + " diamonds")
                                .formatted(Formatting.WHITE)), false);
            }
        }
        
        return 1;
    }
    
    /**
     * Get color for economic inequality index based on its value
     */
    private static String getInequalityColor(BigDecimal index) {
        double value = index.doubleValue();
        if (value < 0.3) return "GREEN";      // Low inequality
        if (value < 0.5) return "YELLOW";     // Moderate inequality
        if (value < 0.7) return "GOLD";       // High inequality
        return "RED";                         // Very high inequality
    }
    
    /**
     * Get description for economic inequality index
     */
    private static String getInequalityDescription(BigDecimal index) {
        double value = index.doubleValue();
        if (value < 0.3) return "Low inequality";
        if (value < 0.5) return "Moderate inequality";
        if (value < 0.7) return "High inequality";
        return "Very high inequality";
    }
    
    /**
     * Get color for shop activity index based on its value
     */
    private static String getActivityColor(BigDecimal index) {
        double value = index.doubleValue();
        if (value >= 75) return "GREEN";      // High activity
        if (value >= 50) return "YELLOW";     // Moderate activity
        if (value >= 25) return "GOLD";       // Low activity
        return "RED";                         // Very low activity
    }
}
