package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.thecubecollective.diamondeconomy.BalanceManager;
import net.thecubecollective.diamondeconomy.ChestShopManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;
import net.thecubecollective.diamondeconomy.TrappedChestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ShopStatsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("shopstats")
                .executes(ShopStatsCommand::showShopStats));
    }
    
    private static int showShopStats(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by players!"));
            return 0;
        }
        
        // Get the block the player is looking at
        BlockHitResult hitResult = getTargetedBlock(player);
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendError(Text.literal("You must be looking at a shop to view its statistics!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Aim your crosshair at a chest shop and try again")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        BlockPos targetPos = hitResult.getBlockPos();
        World world = player.getWorld();
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        ChestShopManager.ChestShop shop = shopManager.getShop(targetPos, world);
        
        if (shop == null) {
            source.sendError(Text.literal("No shop found at this location!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Look at a chest shop to view its statistics")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        // Display shop statistics
        String displayName = (shop.shopName != null && !shop.shopName.trim().isEmpty()) ? shop.shopName : "Unnamed Shop";
        
        player.sendMessage(Text.literal("=== Shop Statistics ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal(""), false); // Empty line
        
        player.sendMessage(Text.literal("🏪 Shop Name: ")
                .formatted(Formatting.AQUA)
                .append(Text.literal(displayName)
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal("👤 Owner: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(shop.ownerName)
                        .formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal("📍 Location: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(shop.x + ", " + shop.y + ", " + shop.z)
                        .formatted(Formatting.WHITE)), false);
        
        String chestType = TrappedChestUtils.getChestType(targetPos, world);
        player.sendMessage(Text.literal("📦 Shop Type: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(chestType + " Chest")
                        .formatted(Formatting.WHITE)), false);
        
        player.sendMessage(Text.literal(""), false); // Empty line
        
        player.sendMessage(Text.literal("💰 Total Sales: ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(BalanceManager.formatBalance(shop.totalSales) + " diamonds")
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal("📦 Items Sold: ")
                .formatted(Formatting.BLUE)
                .append(Text.literal(String.valueOf(shop.totalItemsSold))
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal("🔄 Transactions: ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(shop.totalTransactions))
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        player.sendMessage(Text.literal("💎 Price per Item: ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(BalanceManager.formatBalance(shop.pricePerItem) + " diamonds")
                        .formatted(Formatting.WHITE, Formatting.BOLD)), false);
        
        // Show creation date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String createdDate = dateFormat.format(new Date(shop.createdTime));
        player.sendMessage(Text.literal("📅 Created: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(createdDate)
                        .formatted(Formatting.WHITE)), false);
        
        // Show last sale date if any sales have occurred
        if (shop.lastSaleTime > 0) {
            String lastSaleDate = dateFormat.format(new Date(shop.lastSaleTime));
            player.sendMessage(Text.literal("🕒 Last Sale: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(lastSaleDate)
                            .formatted(Formatting.WHITE)), false);
        } else {
            player.sendMessage(Text.literal("🕒 Last Sale: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal("No sales yet")
                            .formatted(Formatting.DARK_GRAY)), false);
        }
        
        return 1;
    }
    
    private static BlockHitResult getTargetedBlock(ServerPlayerEntity player) {
        // Raycast to find the block the player is looking at
        double reach = 5.0; // 5 block reach
        HitResult result = player.raycast(reach, 0, false);
        
        if (result instanceof BlockHitResult blockHit) {
            return blockHit;
        }
        
        return null;
    }
}
