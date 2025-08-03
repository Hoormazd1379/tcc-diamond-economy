package net.thecubecollective.diamondeconomy;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class ChestShopEventHandler {
    
    private static int tickCounter = 0;
    private static final int CLEANUP_INTERVAL = 6000; // Every 5 minutes (6000 ticks)
    
    public static void register() {
        // Register block use event (when players right-click chests)
        UseBlockCallback.EVENT.register(ChestShopEventHandler::onBlockUse);
        
        // Register periodic cleanup to remove destroyed shops
        ServerTickEvents.END_SERVER_TICK.register(ChestShopEventHandler::onServerTick);
        
        // Note: Block break protection is now handled by UltimateShopProtection
    }
    
    private static void onServerTick(net.minecraft.server.MinecraftServer server) {
        tickCounter++;
        if (tickCounter >= CLEANUP_INTERVAL) {
            tickCounter = 0;
            performShopCleanup(server);
        }
    }
    
    private static void performShopCleanup(net.minecraft.server.MinecraftServer server) {
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        Set<String> removedShops = new HashSet<>();
        
        // Check all registered shops
        for (ChestShopManager.ChestShop shop : shopManager.getAllShops()) {
            try {
                // Find the world by name
                ServerWorld world = null;
                for (ServerWorld serverWorld : server.getWorlds()) {
                    if (serverWorld.getRegistryKey().getValue().toString().equals(shop.worldName)) {
                        world = serverWorld;
                        break;
                    }
                }
                
                if (world == null) {
                    // World doesn't exist, remove shop
                    shopManager.removeShopDirect(shop.getBlockPos(), shop.worldName, shop.ownerUUID);
                    removedShops.add(shop.ownerName + " at " + shop.getBlockPos());
                    continue;
                }
                
                BlockPos pos = shop.getBlockPos();
                
                // For double chest shops, validate that ALL parts still exist
                java.util.List<BlockPos> allPositions = net.thecubecollective.diamondeconomy.TrappedChestUtils.getChestPositions(pos, world);
                boolean allPartsExist = true;
                
                for (BlockPos chestPos : allPositions) {
                    BlockState chestState = world.getBlockState(chestPos);
                    if (!chestState.isOf(Blocks.TRAPPED_CHEST)) {
                        allPartsExist = false;
                        break;
                    }
                }
                
                // Check if any part of the shop was destroyed
                if (!allPartsExist) {
                    // Shop block(s) were destroyed somehow, remove from registry
                    shopManager.removeShopDirect(pos, shop.worldName, shop.ownerUUID);
                    removedShops.add(shop.ownerName + " at " + pos);
                    
                    // Try to notify owner if they're online
                    ServerPlayerEntity owner = server.getPlayerManager().getPlayer(shop.ownerUUID);
                    if (owner != null) {
                        if (allPositions.size() > 1) {
                            owner.sendMessage(Text.literal("⚠️ Your double chest shop at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " was destroyed!")
                                    .formatted(Formatting.RED), false);
                        } else {
                            owner.sendMessage(Text.literal("⚠️ Your shop at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " was destroyed!")
                                    .formatted(Formatting.RED), false);
                        }
                        owner.sendMessage(Text.literal("🛡️ The shop has been removed from the registry.")
                                .formatted(Formatting.YELLOW), false);
                    }
                }
            } catch (Exception e) {
                Tccdiamondeconomy.LOGGER.error("Error during shop cleanup for shop at {}: {}", 
                        shop.getBlockPos(), e.getMessage());
            }
        }
        
        if (!removedShops.isEmpty()) {
            Tccdiamondeconomy.LOGGER.info("Cleaned up {} destroyed shop(s): {}", 
                    removedShops.size(), String.join(", ", removedShops));
        }
    }
    
    private static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!(player instanceof ServerPlayerEntity) || !(world instanceof ServerWorld)) {
            return ActionResult.PASS;
        }
        
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        
        // Only handle trapped chests (custom blocks temporarily disabled)
        if (!state.isOf(Blocks.TRAPPED_CHEST)) {
            return ActionResult.PASS;
        }
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        
        // Check if this is a shop
        if (!shopManager.isShop(pos, world)) {
            return ActionResult.PASS; // Not a shop, let normal chest behavior happen
        }
        
        ChestShopManager.ChestShop shop = shopManager.getShop(pos, world);
        if (shop == null) {
            return ActionResult.PASS;
        }
        
        // If the player is the owner, allow normal access
        if (shop.ownerUUID.equals(player.getUuid())) {
            player.sendMessage(Text.literal("🏪 Shop - Managing")
                    .formatted(Formatting.GREEN), true);
            player.sendMessage(Text.literal("💎 Price: " + shop.pricePerItem + " diamonds per item")
                    .formatted(Formatting.GOLD), false);
            return ActionResult.PASS; // Let them access normally
        }
        
        // For non-owners, show shop info and open custom shop browser
        player.sendMessage(Text.literal("🏪 Welcome to " + shop.ownerName + "'s Shop!")
                .formatted(Formatting.GOLD, Formatting.BOLD), true);
        player.sendMessage(Text.literal("💎 Price: " + shop.pricePerItem + " diamonds per item")
                .formatted(Formatting.YELLOW), false);
        player.sendMessage(Text.literal("💰 Your balance: " + BalanceManager.formatBalance(Tccdiamondeconomy.getBalanceManager().getBalance(player.getUuid())) + " diamonds")
                .formatted(Formatting.AQUA), false);
        player.sendMessage(Text.literal("🛒 Left-click to buy 1 item, Right-click to buy full stack")
                .formatted(Formatting.GRAY), false);
        
        // Open custom shop browser interface for customers
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof Inventory && player instanceof ServerPlayerEntity serverPlayer) {
            // For double chests, we need to get the combined inventory
            Inventory shopInventory = getShopInventory(world, pos);
            
            serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) -> new net.thecubecollective.diamondeconomy.gui.ShopBrowserScreenHandler(
                    syncId, 
                    playerInventory, 
                    shopInventory, 
                    shop, 
                    pos, 
                    world
                ),
                Text.literal("🏪 " + getShopDisplayName(shop) + " - " + shop.ownerName)
            ));
        }
        
        return ActionResult.SUCCESS; // Prevent normal chest opening for customers
    }
    
    private static String getShopDisplayName(ChestShopManager.ChestShop shop) {
        return (shop.shopName != null && !shop.shopName.trim().isEmpty()) ? shop.shopName : "Unnamed Shop";
    }
    
    /**
     * Gets the complete inventory for a shop, handling both single and double trapped chests
     */
    private static Inventory getShopInventory(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        
        if (!state.isOf(Blocks.TRAPPED_CHEST)) {
            // Fallback to block entity if not a trapped chest
            BlockEntity blockEntity = world.getBlockEntity(pos);
            return blockEntity instanceof Inventory ? (Inventory) blockEntity : null;
        }
        
        // Check if this is a double chest by looking at the chest type
        if (state.getBlock() instanceof ChestBlock) {
            ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
            
            if (chestType != ChestType.SINGLE) {
                // This is a double chest, get the DoubleChestInventory
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ChestBlockEntity) {
                    return ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true);
                }
            }
        }
        
        // Single chest or fallback
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof Inventory ? (Inventory) blockEntity : null;
    }
}
