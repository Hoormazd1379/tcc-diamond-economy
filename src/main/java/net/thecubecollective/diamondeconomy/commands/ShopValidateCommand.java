package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thecubecollective.diamondeconomy.ShopValidationSystem;

public class ShopValidateCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, 
                               CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
            CommandManager.literal("shopvalidate")
                .requires(source -> source.hasPermissionLevel(2)) // OP only
                .executes(ShopValidateCommand::executeValidate)
                .then(CommandManager.literal("stats")
                    .executes(ShopValidateCommand::executeStats))
        );
    }

    private static int executeValidate(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            source.sendFeedback(() -> Text.literal("🔍 Triggering manual shop validation...")
                    .formatted(Formatting.YELLOW), false);
            
            ShopValidationSystem.triggerValidation();
            
            source.sendFeedback(() -> Text.literal("✅ Shop validation completed!")
                    .formatted(Formatting.GREEN), false);
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("❌ Error during shop validation: " + e.getMessage())
                    .formatted(Formatting.RED), false);
        }
        
        return 1;
    }

    private static int executeStats(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            String stats = ShopValidationSystem.getValidationStats();
            source.sendFeedback(() -> Text.literal("📊 " + stats)
                    .formatted(Formatting.AQUA), false);
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("❌ Error getting validation stats: " + e.getMessage())
                    .formatted(Formatting.RED), false);
        }
        
        return 1;
    }
}
