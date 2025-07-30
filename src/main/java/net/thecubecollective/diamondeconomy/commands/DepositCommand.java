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
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

public class DepositCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("deposit")
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(DepositCommand::execute)));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        
        // Count diamonds in player's inventory
        int diamondCount = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.DIAMOND) {
                diamondCount += stack.getCount();
            }
        }
        
        if (diamondCount < amount) {
            player.sendMessage(Text.literal("You don't have enough diamonds! You have " + diamondCount + " diamonds.")
                    .formatted(Formatting.RED), false);
            player.sendMessage(Text.literal("Use /tcchelp for command usage information.")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Remove diamonds from inventory
        int remainingToRemove = amount;
        for (int i = 0; i < player.getInventory().size() && remainingToRemove > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.DIAMOND) {
                int toRemove = Math.min(remainingToRemove, stack.getCount());
                stack.decrement(toRemove);
                remainingToRemove -= toRemove;
            }
        }
        
        // Add to balance
        Tccdiamondeconomy.getBalanceManager().addBalance(player.getUuid(), amount);
        
        player.sendMessage(Text.literal("Deposited " + amount + " diamonds! New balance: " + 
                Tccdiamondeconomy.getBalanceManager().getBalance(player.getUuid()) + " diamonds.")
                .formatted(Formatting.GREEN), false);
        
        return 1;
    }
}
