package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thecubecollective.diamondeconomy.ChestShopManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

import java.util.List;

public class ListShopsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("listshops")
                .executes(ListShopsCommand::listShops));
    }
    
    private static int listShops(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by players!"));
            return 0;
        }
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        List<ChestShopManager.ChestShop> playerShops = shopManager.getShopsByOwner(player.getUuid());
        
        if (playerShops.isEmpty()) {
            player.sendMessage(Text.literal("🏪 You don't own any chest shops yet!")
                    .formatted(Formatting.YELLOW), false);
            player.sendMessage(Text.literal("💡 Use /createshop <price> while looking at a trapped chest to create one")
                    .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Header
        player.sendMessage(Text.literal("🏪 Your Chest Shops (" + playerShops.size() + ")")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal(""), false); // Empty line
        
        // List each shop
        for (int i = 0; i < playerShops.size(); i++) {
            ChestShopManager.ChestShop shop = playerShops.get(i);
            
            player.sendMessage(Text.literal((i + 1) + ". ")
                    .formatted(Formatting.WHITE)
                    .append(Text.literal("📍 " + shop.x + ", " + shop.y + ", " + shop.z)
                            .formatted(Formatting.AQUA))
                    .append(Text.literal(" in " + getWorldDisplayName(shop.worldName))
                            .formatted(Formatting.GRAY)), false);
            
            player.sendMessage(Text.literal("   💎 Price: " + shop.pricePerItem + " diamonds per item")
                    .formatted(Formatting.YELLOW), false);
            
            // Calculate days since creation
            long daysSinceCreation = (System.currentTimeMillis() - shop.createdTime) / (1000 * 60 * 60 * 24);
            player.sendMessage(Text.literal("   📅 Created: " + daysSinceCreation + " days ago")
                    .formatted(Formatting.GRAY), false);
            
            if (i < playerShops.size() - 1) {
                player.sendMessage(Text.literal(""), false); // Empty line between shops
            }
        }
        
        player.sendMessage(Text.literal(""), false); // Empty line
        player.sendMessage(Text.literal("💡 Use /removeshop while looking at a shop to remove it")
                .formatted(Formatting.GRAY), false);
        
        return 1;
    }
    
    private static String getWorldDisplayName(String worldName) {
        if (worldName.contains("overworld")) {
            return "Overworld";
        } else if (worldName.contains("nether")) {
            return "Nether";
        } else if (worldName.contains("end")) {
            return "End";
        } else {
            // Extract just the last part of the world name
            String[] parts = worldName.split(":");
            if (parts.length > 1) {
                return parts[parts.length - 1];
            }
            return worldName;
        }
    }
}
