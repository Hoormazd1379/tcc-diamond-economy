package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thecubecollective.diamondeconomy.BalanceManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaltopCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("baltop")
                .executes(BaltopCommand::execute));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        
        List<Map.Entry<UUID, BigDecimal>> topBalances = Tccdiamondeconomy.getBalanceManager().getTopBalances(10);
        
        if (topBalances.isEmpty()) {
            player.sendMessage(Text.literal("No players have any diamonds in their accounts yet!")
                    .formatted(Formatting.YELLOW), false);
            return 1;
        }
        
        player.sendMessage(Text.literal("=== Top 10 Richest Players ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        
        for (int i = 0; i < topBalances.size(); i++) {
            Map.Entry<UUID, BigDecimal> entry = topBalances.get(i);
            UUID playerUUID = entry.getKey();
            BigDecimal balance = entry.getValue();
            
            // Get player name from server
            String playerName = getPlayerName(playerUUID);
            
            Formatting rankColor;
            String medal = "";
            switch (i) {
                case 0:
                    rankColor = Formatting.YELLOW; // Bright gold for 1st place
                    medal = "🥇 ";
                    break;
                case 1:
                    rankColor = Formatting.GRAY; // Silver for 2nd place  
                    medal = "🥈 ";
                    break;
                case 2:
                    rankColor = Formatting.GOLD; // Bronze/darker gold for 3rd place
                    medal = "🥉 ";
                    break;
                default:
                    rankColor = Formatting.DARK_GRAY; // Gray that's readable but doesn't stand out
                    medal = "";
                    break;
            }
            
            player.sendMessage(Text.literal(medal + (i + 1) + ". " + playerName + ": " + BalanceManager.formatBalance(balance) + " diamonds")
                    .formatted(rankColor), false);
        }
        
        return 1;
    }
    
    private static String getPlayerName(UUID playerUUID) {
        ServerPlayerEntity player = Tccdiamondeconomy.getServer().getPlayerManager().getPlayer(playerUUID);
        if (player != null) {
            return player.getName().getString();
        }
        
        // If player is offline, try to get cached name
        String cachedName = Tccdiamondeconomy.getServer().getUserCache().getByUuid(playerUUID)
                .map(profile -> profile.getName())
                .orElse("Unknown Player");
        
        return cachedName;
    }
}
