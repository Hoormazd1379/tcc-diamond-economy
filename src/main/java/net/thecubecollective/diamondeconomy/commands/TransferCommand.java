package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import java.math.RoundingMode;
import java.util.UUID;

public class TransferCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        Tccdiamondeconomy.LOGGER.info("Registering wire transfer commands...");
        
        // Register /wiretransfer command
        dispatcher.register(CommandManager.literal("wiretransfer")
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(TransferCommand::execute))));
        
        // Register /wire as a shorthand
        dispatcher.register(CommandManager.literal("wire")
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(TransferCommand::execute))));
        
        Tccdiamondeconomy.LOGGER.info("Wire transfer commands registered successfully!");
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sender = context.getSource().getPlayerOrThrow();
        String targetPlayerName = StringArgumentType.getString(context, "player");
        double amountInput = DoubleArgumentType.getDouble(context, "amount");
        BigDecimal amount = BigDecimal.valueOf(amountInput).setScale(2, RoundingMode.HALF_UP);
        
        // Check if sender has enough balance
        BigDecimal senderBalance = Tccdiamondeconomy.getBalanceManager().getBalance(sender.getUuid());
        if (senderBalance.compareTo(amount) < 0) {
            sender.sendMessage(Text.literal("Insufficient balance! You have " + BalanceManager.formatBalance(senderBalance) + " diamonds in your account.")
                    .formatted(Formatting.RED), false);
            sender.sendMessage(Text.literal("Use /tcchelp for command usage information.")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Try to find the target player (online first)
        ServerPlayerEntity targetPlayer = Tccdiamondeconomy.getServer().getPlayerManager().getPlayer(targetPlayerName);
        UUID targetUUID = null;
        
        if (targetPlayer != null) {
            // Player is online
            targetUUID = targetPlayer.getUuid();
        } else {
            // Player is offline, try to find their UUID from cache
            targetUUID = Tccdiamondeconomy.getServer().getUserCache().findByName(targetPlayerName)
                    .map(profile -> profile.getId())
                    .orElse(null);
        }
        
        if (targetUUID == null) {
            sender.sendMessage(Text.literal("Player '" + targetPlayerName + "' not found! Make sure the name is correct.")
                    .formatted(Formatting.RED), false);
            sender.sendMessage(Text.literal("Use /tcchelp for command usage information.")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Prevent self-transfer
        if (targetUUID.equals(sender.getUuid())) {
            sender.sendMessage(Text.literal("You cannot transfer diamonds to yourself!")
                    .formatted(Formatting.RED), false);
            sender.sendMessage(Text.literal("Use /tcchelp for command usage information.")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Perform the transfer
        if (!Tccdiamondeconomy.getBalanceManager().removeBalance(sender.getUuid(), amount)) {
            sender.sendMessage(Text.literal("Transfer failed! Please try again.")
                    .formatted(Formatting.RED), false);
            return 0;
        }
        
        Tccdiamondeconomy.getBalanceManager().addBalance(targetUUID, amount);
        
        // Send success message to sender
        BigDecimal newSenderBalance = Tccdiamondeconomy.getBalanceManager().getBalance(sender.getUuid());
        sender.sendMessage(Text.literal("Successfully transferred " + BalanceManager.formatBalance(amount) + " diamonds to " + targetPlayerName + "!")
                .formatted(Formatting.GREEN), false);
        sender.sendMessage(Text.literal("Your new balance: " + BalanceManager.formatBalance(newSenderBalance) + " diamonds")
                .formatted(Formatting.GOLD), false);
        
        // Handle recipient notification
        if (targetPlayer != null) {
            // Player is online - send immediate notification
            targetPlayer.sendMessage(Text.literal("💎 You received " + BalanceManager.formatBalance(amount) + " diamonds from " + sender.getName().getString() + "!")
                    .formatted(Formatting.GREEN, Formatting.BOLD), false);
            BigDecimal targetBalance = Tccdiamondeconomy.getBalanceManager().getBalance(targetUUID);
            targetPlayer.sendMessage(Text.literal("Your new balance: " + BalanceManager.formatBalance(targetBalance) + " diamonds")
                    .formatted(Formatting.GOLD), false);
        } else {
            // Player is offline - queue notification for next login
            if (Tccdiamondeconomy.getNotificationManager() != null) {
                Tccdiamondeconomy.getNotificationManager().addPendingTransfer(targetUUID, sender.getName().getString(), amount);
            } else {
                Tccdiamondeconomy.LOGGER.warn("NotificationManager is null - offline notification not sent");
            }
        }
        
        return 1;
    }
}
