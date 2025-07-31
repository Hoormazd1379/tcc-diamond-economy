package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Blocks;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EditShopCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("editshop")
                .then(CommandManager.literal("price")
                        .then(CommandManager.argument("newPrice", DoubleArgumentType.doubleArg(0.01))
                                .executes(EditShopCommand::editShopPrice)))
                .then(CommandManager.literal("name")
                        .then(CommandManager.argument("newName", StringArgumentType.greedyString())
                                .executes(EditShopCommand::editShopName)))
                .executes(ctx -> {
                    ctx.getSource().sendError(Text.literal("Usage: /editshop <price|name> <value>")
                            .formatted(Formatting.RED));
                    ctx.getSource().sendMessage(Text.literal("Look at your shop and choose what to edit:")
                            .formatted(Formatting.YELLOW));
                    ctx.getSource().sendMessage(Text.literal("• /editshop price 2.5 - Change price to 2.5 diamonds per item")
                            .formatted(Formatting.GRAY));
                    ctx.getSource().sendMessage(Text.literal("• /editshop name \"New Shop Name\" - Change shop name")
                            .formatted(Formatting.GRAY));
                    ctx.getSource().sendMessage(Text.literal("💡 Only shop owners can edit their shops")
                            .formatted(Formatting.DARK_GRAY));
                    return 0;
                }));
    }
    
    private static int editShopPrice(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by players!"));
            return 0;
        }
        
        double newPriceInput = DoubleArgumentType.getDouble(context, "newPrice");
        BigDecimal newPrice = BigDecimal.valueOf(newPriceInput).setScale(2, RoundingMode.HALF_UP);
        
        if (newPrice.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            source.sendError(Text.literal("Price must be at least 0.01 diamonds!")
                    .formatted(Formatting.RED));
            return 0;
        }
        
        // Get the shop the player is looking at
        BlockPos targetPos = getTargetedBlockPos(player, source);
        if (targetPos == null) {
            return 0;
        }
        
        World world = player.getWorld();
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        ChestShopManager.ChestShop shop = shopManager.getShop(targetPos, world);
        
        if (shop == null) {
            source.sendError(Text.literal("No shop found at this location!")
                    .formatted(Formatting.RED));
            return 0;
        }
        
        // Check if player is the owner
        if (!shop.ownerUUID.equals(player.getUuid())) {
            source.sendError(Text.literal("You can only edit your own shops!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 This shop belongs to " + shop.ownerName)
                    .formatted(Formatting.GRAY));
            return 0;
        }
        
        // Update the shop price
        BigDecimal oldPrice = shop.pricePerItem;
        boolean success = shopManager.updateShopPrice(targetPos, world, newPrice);
        
        if (success) {
            String shopDisplayName = (shop.shopName != null && !shop.shopName.trim().isEmpty()) ? shop.shopName : "Unnamed Shop";
            
            source.sendMessage(Text.literal("✅ Shop price updated successfully!")
                    .formatted(Formatting.GREEN, Formatting.BOLD));
            source.sendMessage(Text.literal("🏪 Shop: " + shopDisplayName)
                    .formatted(Formatting.AQUA));
            source.sendMessage(Text.literal("💎 Old Price: " + BalanceManager.formatBalance(oldPrice) + " diamonds per item")
                    .formatted(Formatting.GRAY));
            source.sendMessage(Text.literal("💎 New Price: " + BalanceManager.formatBalance(newPrice) + " diamonds per item")
                    .formatted(Formatting.GOLD, Formatting.BOLD));
            source.sendMessage(Text.literal("📊 All shop statistics have been preserved")
                    .formatted(Formatting.GREEN));
            
            Tccdiamondeconomy.LOGGER.info("Player {} updated shop '{}' price from {} to {} diamonds per item", 
                    player.getName().getString(), shopDisplayName, oldPrice, newPrice);
        } else {
            source.sendError(Text.literal("Failed to update shop price! Please try again.")
                    .formatted(Formatting.RED));
        }
        
        return success ? 1 : 0;
    }
    
    private static int editShopName(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by players!"));
            return 0;
        }
        
        String newName = StringArgumentType.getString(context, "newName");
        
        // Get the shop the player is looking at
        BlockPos targetPos = getTargetedBlockPos(player, source);
        if (targetPos == null) {
            return 0;
        }
        
        World world = player.getWorld();
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        ChestShopManager.ChestShop shop = shopManager.getShop(targetPos, world);
        
        if (shop == null) {
            source.sendError(Text.literal("No shop found at this location!")
                    .formatted(Formatting.RED));
            return 0;
        }
        
        // Check if player is the owner
        if (!shop.ownerUUID.equals(player.getUuid())) {
            source.sendError(Text.literal("You can only edit your own shops!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 This shop belongs to " + shop.ownerName)
                    .formatted(Formatting.GRAY));
            return 0;
        }
        
        // Update the shop name
        String oldName = (shop.shopName != null && !shop.shopName.trim().isEmpty()) ? shop.shopName : "Unnamed Shop";
        boolean success = shopManager.updateShopName(targetPos, world, newName);
        
        if (success) {
            source.sendMessage(Text.literal("✅ Shop name updated successfully!")
                    .formatted(Formatting.GREEN, Formatting.BOLD));
            source.sendMessage(Text.literal("🏪 Old Name: " + oldName)
                    .formatted(Formatting.GRAY));
            source.sendMessage(Text.literal("🏪 New Name: " + newName)
                    .formatted(Formatting.AQUA, Formatting.BOLD));
            source.sendMessage(Text.literal("💎 Price: " + BalanceManager.formatBalance(shop.pricePerItem) + " diamonds per item")
                    .formatted(Formatting.GOLD));
            source.sendMessage(Text.literal("📊 All shop statistics have been preserved")
                    .formatted(Formatting.GREEN));
            
            Tccdiamondeconomy.LOGGER.info("Player {} renamed shop from '{}' to '{}'", 
                    player.getName().getString(), oldName, newName);
        } else {
            source.sendError(Text.literal("Failed to update shop name! Please try again.")
                    .formatted(Formatting.RED));
        }
        
        return success ? 1 : 0;
    }
    
    private static BlockPos getTargetedBlockPos(ServerPlayerEntity player, ServerCommandSource source) {
        // Get the block the player is looking at
        BlockHitResult hitResult = getTargetedBlock(player);
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendError(Text.literal("You must be looking at a shop to edit it!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Aim your crosshair at your shop chest and try again")
                    .formatted(Formatting.YELLOW));
            return null;
        }
        
        BlockPos targetPos = hitResult.getBlockPos();
        World world = player.getWorld();
        
        // Verify it's a trapped chest
        if (!world.getBlockState(targetPos).isOf(Blocks.TRAPPED_CHEST)) {
            source.sendError(Text.literal("You can only edit chest shops!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Look at a trapped chest shop to edit it")
                    .formatted(Formatting.YELLOW));
            return null;
        }
        
        return targetPos;
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
