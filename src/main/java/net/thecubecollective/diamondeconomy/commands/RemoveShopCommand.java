package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
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
import net.thecubecollective.diamondeconomy.ChestShopManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

public class RemoveShopCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("removeshop")
                .executes(RemoveShopCommand::removeShop));
    }
    
    private static int removeShop(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by players!"));
            return 0;
        }
        
        // Get the block the player is looking at
        BlockHitResult hitResult = getTargetedBlock(player);
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendError(Text.literal("You must be looking at a chest shop to remove it!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Aim your crosshair at your shop and try again")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        BlockPos targetPos = hitResult.getBlockPos();
        World world = player.getWorld();
        
        // Verify it's a trapped chest (custom blocks temporarily disabled)
        BlockState targetState = world.getBlockState(targetPos);
        if (!targetState.isOf(Blocks.TRAPPED_CHEST)) {
            source.sendError(Text.literal("You can only remove shops from trapped chests!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Look at your trapped chest shop and try again")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        
        // Check if a shop exists at this location
        if (!shopManager.isShop(targetPos, world)) {
            source.sendError(Text.literal("No shop exists at this location!")
                    .formatted(Formatting.RED));
            source.sendMessage(Text.literal("💡 Use /createshop to create a shop here")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        // Check if player owns this shop
        if (!shopManager.isShopOwner(targetPos, world, player.getUuid())) {
            source.sendError(Text.literal("You can only remove your own shops!")
                    .formatted(Formatting.RED));
            ChestShopManager.ChestShop shop = shopManager.getShop(targetPos, world);
            source.sendMessage(Text.literal("💡 This shop belongs to: " + shop.ownerName)
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        
        // Remove the shop
        boolean success = shopManager.removeShop(targetPos, world, player.getUuid());
        
        if (success) {
            // Grant temporary permission to break the physical block
            net.thecubecollective.diamondeconomy.ShopRemovalTracker.grantPermission(targetPos, player.getUuid());
            
            source.sendMessage(Text.literal("✅ Shop removed successfully!")
                    .formatted(Formatting.GREEN));
            source.sendMessage(Text.literal("🔨 You now have 30 seconds to break the chest block")
                    .formatted(Formatting.YELLOW));
            source.sendMessage(Text.literal("💡 The chest is no longer protected and can be broken normally")
                    .formatted(Formatting.GRAY));
            
            Tccdiamondeconomy.LOGGER.info("Player {} removed their shop at {}", 
                    player.getName().getString(), targetPos);
        } else {
            source.sendError(Text.literal("Failed to remove shop! Please try again.")
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
