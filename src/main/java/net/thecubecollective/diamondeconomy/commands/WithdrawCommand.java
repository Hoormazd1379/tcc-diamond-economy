package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thecubecollective.diamondeconomy.BalanceManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

import java.math.BigDecimal;

public class WithdrawCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("withdraw")
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(WithdrawCommand::execute)));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        
        BigDecimal currentBalance = Tccdiamondeconomy.getBalanceManager().getBalance(player.getUuid());
        BigDecimal withdrawAmount = BigDecimal.valueOf(amount);
        
        if (currentBalance.compareTo(withdrawAmount) < 0) {
            player.sendMessage(Text.literal("Insufficient balance! You have " + BalanceManager.formatBalance(currentBalance) + " diamonds in your account.")
                    .formatted(Formatting.RED), false);
            player.sendMessage(Text.literal("Note: You can only withdraw whole diamonds, but you can transfer fractional amounts to other players.")
                    .formatted(Formatting.YELLOW), false);
            player.sendMessage(Text.literal("Use /tcchelp for command usage information.")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Check if player has enough inventory space
        int availableSlots = 0;
        int partialStacks = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                availableSlots++;
            } else if (stack.getItem() == Items.DIAMOND && stack.getCount() < stack.getMaxCount()) {
                partialStacks += stack.getMaxCount() - stack.getCount();
            }
        }
        
        int maxCapacity = (availableSlots * 64) + partialStacks;
        
        if (maxCapacity < amount) {
            player.sendMessage(Text.literal("Not enough inventory space! You can only withdraw " + maxCapacity + " diamonds.")
                    .formatted(Formatting.RED), false);
            player.sendMessage(Text.literal("Use /tcchelp for command usage information.")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Remove from balance
        if (!Tccdiamondeconomy.getBalanceManager().removeBalance(player.getUuid(), withdrawAmount)) {
            player.sendMessage(Text.literal("Transaction failed! Please try again.")
                    .formatted(Formatting.RED), false);
            return 0;
        }
        
        // Give diamonds to player
        int remainingToGive = amount;
        
        // First, fill partial stacks
        for (int i = 0; i < player.getInventory().size() && remainingToGive > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.DIAMOND && stack.getCount() < stack.getMaxCount()) {
                int toAdd = Math.min(remainingToGive, stack.getMaxCount() - stack.getCount());
                stack.increment(toAdd);
                remainingToGive -= toAdd;
            }
        }
        
        // Then, create new stacks
        while (remainingToGive > 0) {
            int stackSize = Math.min(remainingToGive, 64);
            ItemStack diamondStack = new ItemStack(Items.DIAMOND, stackSize);
            
            if (!player.getInventory().insertStack(diamondStack)) {
                // If we can't insert, give it directly to the player (this shouldn't happen due to our capacity check)
                player.dropItem(diamondStack, false);
            }
            
            remainingToGive -= stackSize;
        }
        
        player.sendMessage(Text.literal("Withdrew " + amount + " diamonds! New balance: " + 
                BalanceManager.formatBalance(Tccdiamondeconomy.getBalanceManager().getBalance(player.getUuid())) + " diamonds.")
                .formatted(Formatting.GREEN), false);
        
        return 1;
    }
}
