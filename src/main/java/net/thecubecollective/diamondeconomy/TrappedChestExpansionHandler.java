package net.thecubecollective.diamondeconomy;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Handles trapped chest expansion for shops
 */
public class TrappedChestExpansionHandler {
    
    public static void register() {
        UseBlockCallback.EVENT.register(TrappedChestExpansionHandler::onBlockUse);
        Tccdiamondeconomy.LOGGER.info("🔧 Trapped Chest Expansion Handler registered!");
    }
    
    private static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient()) {
            return ActionResult.PASS;
        }
        
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }
        
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }
        
        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.getItem() != Items.TRAPPED_CHEST) {
            return ActionResult.PASS;
        }
        
        // Get the position where the trapped chest will be placed
        BlockPos targetPos = hitResult.getBlockPos().offset(hitResult.getSide());
        
        // Check if player is sneaking (shift-clicking) to prevent expansion
        if (player.isSneaking()) {
            return ActionResult.PASS; // Allow normal placement without expansion
        }
        
        // Check if this placement would expand an existing shop
        BlockPos existingShopPos = findAdjacentShop(targetPos, world);
        if (existingShopPos != null) {
            ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
            ChestShopManager.ChestShop shop = shopManager.getShop(existingShopPos, world);
            
            if (shop != null) {
                // Check if the player is the shop owner
                if (!shop.ownerUUID.equals(player.getUuid())) {
                    // Not the owner, prevent expansion
                    serverPlayer.sendMessage(Text.literal("❌ You cannot expand someone else's shop!")
                            .formatted(Formatting.RED), false);
                    serverPlayer.sendMessage(Text.literal("💡 Hold shift while placing to avoid expanding adjacent shops")
                            .formatted(Formatting.YELLOW), false);
                    return ActionResult.FAIL;
                }
                
                // Check if this shop can be expanded
                if (TrappedChestUtils.isDoubleChest(existingShopPos, world)) {
                    serverPlayer.sendMessage(Text.literal("❌ This shop is already a double chest!")
                            .formatted(Formatting.RED), false);
                    return ActionResult.FAIL;
                }
                
                // Check if the placement is valid for expansion
                if (!TrappedChestUtils.canExpandChest(existingShopPos, targetPos, world)) {
                    return ActionResult.PASS; // Allow normal placement if not a valid expansion
                }
                
                // This is a valid expansion by the owner
                // The chest will be placed normally by Minecraft, but we need to update our shop system
                // We'll use a delayed task to update the shop after the block is placed
                world.getServer().execute(() -> {
                    updateShopAfterExpansion(existingShopPos, targetPos, world, serverPlayer);
                });
                
                return ActionResult.PASS; // Allow the placement to continue
            }
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * Finds an adjacent shop that could be expanded by placing a chest at the target position
     */
    private static BlockPos findAdjacentShop(BlockPos targetPos, World world) {
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        if (shopManager == null) {
            return null;
        }
        
        // Check all 4 horizontal directions
        BlockPos[] adjacentPositions = {
            targetPos.north(),
            targetPos.south(),
            targetPos.east(),
            targetPos.west()
        };
        
        for (BlockPos pos : adjacentPositions) {
            if (world.getBlockState(pos).isOf(Blocks.TRAPPED_CHEST) && shopManager.isShop(pos, world)) {
                return pos;
            }
        }
        
        return null;
    }
    
    /**
     * Updates the shop system after a chest has been expanded
     */
    private static void updateShopAfterExpansion(BlockPos originalPos, BlockPos newPos, World world, ServerPlayerEntity player) {
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        
        // Verify both positions now have trapped chests
        if (!world.getBlockState(originalPos).isOf(Blocks.TRAPPED_CHEST) || 
            !world.getBlockState(newPos).isOf(Blocks.TRAPPED_CHEST)) {
            return;
        }
        
        // Verify this is now a double chest
        if (!TrappedChestUtils.isDoubleChest(originalPos, world)) {
            return;
        }
        
        // Get the main position for the double chest
        BlockPos mainPos = TrappedChestUtils.getMainChestPosition(originalPos, world);
        
        // Get the existing shop
        ChestShopManager.ChestShop originalShop = shopManager.getShop(originalPos, world);
        if (originalShop == null) {
            return;
        }
        
        // If the main position is different from the original, we need to update the shop location
        if (!mainPos.equals(originalPos)) {
            // Remove the old shop entry
            shopManager.removeShopDirect(originalPos, originalShop.worldName, originalShop.ownerUUID);
            
            // Create a new shop entry at the main position
            shopManager.createShop(
                originalShop.ownerUUID,
                originalShop.ownerName,
                originalShop.shopName,
                mainPos,
                world,
                originalShop.getPrice()
            );
            
            // Copy over the statistics
            ChestShopManager.ChestShop newShop = shopManager.getShop(mainPos, world);
            if (newShop != null) {
                newShop.totalSales = originalShop.totalSales;
                newShop.totalItemsSold = originalShop.totalItemsSold;
                newShop.totalTransactions = originalShop.totalTransactions;
                newShop.lastSaleTime = originalShop.lastSaleTime;
                newShop.createdTime = originalShop.createdTime;
                shopManager.saveShops();
            }
        }
        
        // Notify the player
        player.sendMessage(Text.literal("✅ Shop successfully expanded to double chest!")
                .formatted(Formatting.GREEN, Formatting.BOLD), false);
        player.sendMessage(Text.literal("📦 Your shop now has double the storage capacity!")
                .formatted(Formatting.AQUA), false);
        
        String shopName = (originalShop.shopName != null && !originalShop.shopName.trim().isEmpty()) 
                ? originalShop.shopName : "Unnamed Shop";
        
        Tccdiamondeconomy.LOGGER.info("Shop '{}' owned by {} expanded to double chest at {}", 
                shopName, player.getName().getString(), mainPos);
    }
}
