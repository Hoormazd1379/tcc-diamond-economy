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

public class BalanceCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("balance")
                .executes(BalanceCommand::execute));
        
        // Also register /bal as a shorthand
        dispatcher.register(CommandManager.literal("bal")
                .executes(BalanceCommand::execute));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        
        long balance = Tccdiamondeconomy.getBalanceManager().getBalance(player.getUuid());
        
        player.sendMessage(Text.literal("Your balance: " + balance + " diamonds")
                .formatted(Formatting.GOLD), false);
        
        return 1;
    }
}
