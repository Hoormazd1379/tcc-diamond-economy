package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

public class CreateShopCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("createshop")
                .then(CommandManager.argument("price", DoubleArgumentType.doubleArg(0.01))
                        .then(CommandManager.argument("shopName", StringArgumentType.greedyString())
                                .executes(CreateShopCommand::createShopWithName))
                        .executes(CreateShopCommand::createShop))
                .executes(ctx -> {
                    ctx.getSource().sendError(Text.literal("Usage: /createshop <price_per_item> [shop_name]")
                            .formatted(Formatting.RED));
                    ctx.getSource().sendMessage(Text.literal("Look at a trapped chest and specify the price in diamonds per item")
                            .formatted(Formatting.YELLOW));
                    ctx.getSource().sendMessage(Text.literal("Example: /createshop 5.5 (5.5 diamonds per item)")
                            .formatted(Formatting.GRAY));
                    ctx.getSource().sendMessage(Text.literal("Example: /createshop 0.1 \"My Food Shop\" (named shop)")
                            .formatted(Formatting.GRAY));
                    return 0;
                }));
    }
    
    private static int createShop(CommandContext<ServerCommandSource> context) {
        return createShopInternal(context, "My Shop");
    }
    
    private static int createShopWithName(CommandContext<ServerCommandSource> context) {
        String shopName = StringArgumentType.getString(context, "shopName");
        return createShopInternal(context, shopName);
    }
    
    private static int createShopInternal(CommandContext<ServerCommandSource> context, String shopName) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by players!"));
            return 0;
        }
        
        double priceInput = DoubleArgumentType.getDouble(context, "price");
        BigDecimal pricePerItem = BigDecimal.valueOf(priceInput).setScale(2, RoundingMode.HALF_UP);
        
        if (pricePerItem.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            source.sendError(Text.literal("Price must be at least 0.01 diamonds!")
                    .formatted(Formatting.RED));
            return 0;
        }
        
        // Get the block the player is looking at
        BlockHitResult hitResult = getTargetedBlock(player);
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendError(Text.literal("You must be looking at a trapped chest to create a shop!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Aim your crosshair at a trapped chest and try again")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        BlockPos targetPos = hitResult.getBlockPos();
        World world = player.getWorld();
        
        // Verify it's a trapped chest
        if (!world.getBlockState(targetPos).isOf(Blocks.TRAPPED_CHEST)) {
            source.sendError(Text.literal("You can only create shops on trapped chests!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Place a trapped chest first, then look at it and use /createshop")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        
        // Check if a shop already exists at this location
        if (shopManager.isShop(targetPos, world)) {
            source.sendError(Text.literal("A shop already exists at this location!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Use /removeshop to remove an existing shop first")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        // Create the shop with name
        boolean success = shopManager.createShop(
                player.getUuid(), 
                player.getName().getString(),
                shopName,
                targetPos, 
                world, 
                pricePerItem
        );
        
        if (success) {
            source.sendMessage(Text.literal("✅ Shop \"" + shopName + "\" created successfully!")
                    .formatted(Formatting.GREEN, Formatting.BOLD));
            source.sendMessage(Text.literal("🛡️ Your shop is protected from block breaking!")
                    .formatted(Formatting.AQUA));
            source.sendMessage(Text.literal("📍 Location: " + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ())
                    .formatted(Formatting.GRAY));
            source.sendMessage(Text.literal("💎 Price: " + BalanceManager.formatBalance(pricePerItem) + " diamonds per item")
                    .formatted(Formatting.GOLD));
            source.sendMessage(Text.literal("💡 Fill your chest with items to sell! Players will pay " + BalanceManager.formatBalance(pricePerItem) + " diamonds for each item they take.")
                    .formatted(Formatting.YELLOW));
            source.sendMessage(Text.literal("🛠️ Use /removeshop while looking at this shop to safely remove it")
                    .formatted(Formatting.GRAY));
            
            Tccdiamondeconomy.LOGGER.info("Player {} created shop '{}' at {} with price {} diamonds per item", 
                    player.getName().getString(), shopName, targetPos, pricePerItem);
        } else {
            source.sendError(Text.literal("Failed to create shop! Please try again.")
                    .formatted(Formatting.RED));
        }
        
        return success ? 1 : 0;
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
