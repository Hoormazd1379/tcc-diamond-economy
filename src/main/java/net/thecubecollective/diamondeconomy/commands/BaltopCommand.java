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
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

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
        
        List<Map.Entry<UUID, Long>> topBalances = Tccdiamondeconomy.getBalanceManager().getTopBalances(10);
        
        if (topBalances.isEmpty()) {
            player.sendMessage(Text.literal("No players have any diamonds in their accounts yet!")
                    .formatted(Formatting.YELLOW), false);
            return 1;
        }
        
        player.sendMessage(Text.literal("=== Top 10 Richest Players ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        
        for (int i = 0; i < topBalances.size(); i++) {
            Map.Entry<UUID, Long> entry = topBalances.get(i);
            UUID playerUUID = entry.getKey();
            Long balance = entry.getValue();
            
            // Get player name from server
            String playerName = getPlayerName(playerUUID);
            
            Formatting rankColor;
            String medal = "";
            switch (i) {
                case 0:
                    rankColor = Formatting.GOLD;
                    medal = "🥇 ";
                    break;
                case 1:
                    rankColor = Formatting.GRAY;
                    medal = "🥈 ";
                    break;
                case 2:
                    rankColor = Formatting.YELLOW;
                    medal = "🥉 ";
                    break;
                default:
                    rankColor = Formatting.WHITE;
                    medal = "";
                    break;
            }
            
            player.sendMessage(Text.literal(medal + (i + 1) + ". " + playerName + ": " + balance + " diamonds")
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
