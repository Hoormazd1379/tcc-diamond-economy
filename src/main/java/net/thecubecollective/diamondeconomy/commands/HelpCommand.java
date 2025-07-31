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

public class HelpCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("tcchelp")
                .executes(HelpCommand::execute));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        
        // Header
        player.sendMessage(Text.literal("=== TCC Diamond Economy Commands ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Balance commands
        player.sendMessage(Text.literal("💰 Balance Commands:")
                .formatted(Formatting.YELLOW, Formatting.BOLD), false);
        
        player.sendMessage(Text.literal("/balance").formatted(Formatting.GREEN)
                .append(Text.literal(" or ").formatted(Formatting.GRAY))
                .append(Text.literal("/bal").formatted(Formatting.GREEN))
                .append(Text.literal(" - Check your diamond balance").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("/deposit <amount>").formatted(Formatting.GREEN)
                .append(Text.literal(" - Deposit diamonds from inventory to account").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("/withdraw <amount>").formatted(Formatting.GREEN)
                .append(Text.literal(" - Withdraw diamonds from account to inventory").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Transfer commands
        player.sendMessage(Text.literal("💎 Transfer Commands:")
                .formatted(Formatting.AQUA, Formatting.BOLD), false);
        
        player.sendMessage(Text.literal("/wiretransfer <player> <amount>").formatted(Formatting.GREEN)
                .append(Text.literal(" or ").formatted(Formatting.GRAY))
                .append(Text.literal("/wire <player> <amount>").formatted(Formatting.GREEN))
                .append(Text.literal(" - Send diamonds to another player").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Shop commands
        player.sendMessage(Text.literal("🏪 Shop Commands:")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        
        player.sendMessage(Text.literal("/createshop <price>").formatted(Formatting.GREEN)
                .append(Text.literal(" - Create a chest shop (look at trapped chest)").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("/removeshop").formatted(Formatting.GREEN)
                .append(Text.literal(" - Remove your chest shop (look at your shop)").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("/listshops").formatted(Formatting.GREEN)
                .append(Text.literal(" - List all your chest shops").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Information commands
        player.sendMessage(Text.literal("📊 Information Commands:")
                .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
        
        player.sendMessage(Text.literal("/baltop").formatted(Formatting.GREEN)
                .append(Text.literal(" - View top 10 richest players").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("/tcchelp").formatted(Formatting.GREEN)
                .append(Text.literal(" - Show this help message").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Admin commands (only show if player has OP)
        if (player.hasPermissionLevel(2)) {
            player.sendMessage(Text.literal("⚙️ Admin Commands:")
                    .formatted(Formatting.RED, Formatting.BOLD), false);
            
            player.sendMessage(Text.literal("/shopvalidate").formatted(Formatting.GREEN)
                    .append(Text.literal(" - Manually check all shops for validity").formatted(Formatting.WHITE)), false);
            
            player.sendMessage(Text.literal("/shopvalidate stats").formatted(Formatting.GREEN)
                    .append(Text.literal(" - Show shop validation statistics").formatted(Formatting.WHITE)), false);
            
            player.sendMessage(Text.literal(""), false); // Empty line
        }
        
        // Usage examples
        player.sendMessage(Text.literal("📝 Usage Examples:")
                .formatted(Formatting.BLUE, Formatting.BOLD), false);
        
        player.sendMessage(Text.literal("• /deposit 32").formatted(Formatting.GRAY)
                .append(Text.literal(" - Deposits 32 diamonds").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("• /withdraw 10").formatted(Formatting.GRAY)
                .append(Text.literal(" - Withdraws 10 diamonds").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("• /wire Steve 50").formatted(Formatting.GRAY)
                .append(Text.literal(" - Sends 50 diamonds to Steve").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("• /createshop 10").formatted(Formatting.GRAY)
                .append(Text.literal(" - Creates a shop with 10 diamonds per item").formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // Notes
        player.sendMessage(Text.literal("📋 Important Notes:")
                .formatted(Formatting.RED, Formatting.BOLD), false);
        
        player.sendMessage(Text.literal("• All amounts must be positive numbers").formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("• Transfers work with both online and offline players").formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("• Your balance is automatically saved").formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("• You need enough inventory space for withdrawals").formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("• Chest shops use trapped chests with golden particles").formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("• Only shop owners can access their shops normally").formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("• Hoppers cannot access chest shops").formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("• Shop database is validated every 10 seconds for integrity").formatted(Formatting.YELLOW), false);
        
        return 1;
    }
}
