package net.thecubecollective.diamondeconomy.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.thecubecollective.diamondeconomy.BalanceManager;
import net.thecubecollective.diamondeconomy.ChestShopManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;

public class ShopBrowserScreenHandler extends GenericContainerScreenHandler {
    private final ChestShopManager.ChestShop shop;
    private final BlockPos pos;
    private final World world;
    private final Inventory actualShopInventory;
    private final SimpleInventory displayInventory;
    
    public ShopBrowserScreenHandler(int syncId, PlayerInventory playerInventory, 
                                  Inventory shopInventory, ChestShopManager.ChestShop shop, 
                                  BlockPos pos, World world) {
        super(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, new SimpleInventory(27), 3);
        this.shop = shop;
        this.pos = pos;
        this.world = world;
        this.actualShopInventory = shopInventory;
        this.displayInventory = (SimpleInventory) this.getInventory();
        
        // Populate the display inventory after initialization
        updateDisplayInventory();
    }
    
    private void updateDisplayInventory() {
        // Clear display inventory
        displayInventory.clear();
        
        // Copy items from actual shop inventory to display inventory
        for (int i = 0; i < Math.min(actualShopInventory.size(), 27); i++) {
            ItemStack original = actualShopInventory.getStack(i);
            if (!original.isEmpty()) {
                // Create a perfect copy of the original item
                ItemStack display = original.copy();
                
                // Get the original name (either custom name or default item name)
                Text originalName = display.get(net.minecraft.component.DataComponentTypes.CUSTOM_NAME);
                if (originalName == null) {
                    originalName = display.getName();
                }
                
                // Create new display name with price info
                Text newDisplayName = Text.literal("")
                    .append(originalName.copy().formatted(Formatting.GOLD, Formatting.BOLD))
                    .append(Text.literal(" - " + shop.pricePerItem + "💎 each")
                        .formatted(Formatting.YELLOW));
                
                // Set the custom name for display purposes
                display.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, newDisplayName);
                
                displayInventory.setStack(i, display);
            }
        }
        
        // Mark inventory as changed to force client sync
        this.sendContentUpdates();
    }
    
    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        // Only handle clicks on the display inventory (shop items)
        if (slotIndex >= 0 && slotIndex < 27) {
            handleShopItemClick(slotIndex, button, player);
            return;
        }
        
        // For player inventory slots, allow normal interaction
        super.onSlotClick(slotIndex, button, actionType, player);
    }
    
    private void handleShopItemClick(int slot, int button, PlayerEntity player) {
        ItemStack displayItem = displayInventory.getStack(slot);
        ItemStack actualItem = actualShopInventory.getStack(slot);
        
        if (displayItem.isEmpty() || actualItem.isEmpty()) {
            player.sendMessage(Text.literal("❌ No item available in this slot!")
                    .formatted(Formatting.RED), true);
            return;
        }
        
        // Determine quantity to buy based on click type
        int quantityToBuy = 1;
        if (button == 1) { // Right click - buy full stack or available amount
            quantityToBuy = actualItem.getCount();
        }
        
        purchaseItems(player, slot, quantityToBuy);
    }
    
    private void purchaseItems(PlayerEntity player, int slot, int requestedQuantity) {
        ItemStack actualItem = actualShopInventory.getStack(slot);
        if (actualItem.isEmpty()) {
            player.sendMessage(Text.literal("❌ Item no longer available!")
                    .formatted(Formatting.RED), true);
            updateDisplayInventory();
            return;
        }
        
        // Calculate actual quantity to buy (limited by available stock)
        int availableQuantity = actualItem.getCount();
        int quantityToBuy = Math.min(requestedQuantity, availableQuantity);
        
        if (quantityToBuy <= 0) {
            player.sendMessage(Text.literal("❌ No items available!")
                    .formatted(Formatting.RED), true);
            return;
        }
        
        // Get the original item name BEFORE any modifications
        String originalItemName = actualItem.getName().getString();
        
        BalanceManager balanceManager = Tccdiamondeconomy.getBalanceManager();
        long playerBalance = balanceManager.getBalance(player.getUuid());
        long totalCost = (long) shop.pricePerItem * quantityToBuy;
        
        if (playerBalance < totalCost) {
            player.sendMessage(Text.literal("❌ Insufficient funds!")
                    .formatted(Formatting.RED), true);
            player.sendMessage(Text.literal("💎 Cost: " + totalCost + " diamonds | Your balance: " + playerBalance + " diamonds")
                    .formatted(Formatting.YELLOW), false);
            return;
        }
        
        // Create the item stack to give to player
        ItemStack purchaseStack = actualItem.copy();
        purchaseStack.setCount(quantityToBuy);
        
        // Check if player has inventory space
        if (!player.getInventory().insertStack(purchaseStack)) {
            player.sendMessage(Text.literal("❌ Not enough inventory space!")
                    .formatted(Formatting.RED), true);
            return;
        }
        
        // Process the purchase
        balanceManager.removeBalance(player.getUuid(), totalCost);
        balanceManager.addBalance(shop.ownerUUID, totalCost);
        
        // Remove items from the actual shop inventory
        if (quantityToBuy >= actualItem.getCount()) {
            actualShopInventory.setStack(slot, ItemStack.EMPTY);
        } else {
            actualItem.decrement(quantityToBuy);
        }
        
        // Update display inventory
        updateDisplayInventory();
        
        // Force sync to all clients
        this.sendContentUpdates();
        
        // Send confirmation messages
        player.sendMessage(Text.literal("✅ Purchase successful!")
                .formatted(Formatting.GREEN), true);
        player.sendMessage(Text.literal("🛒 Bought " + quantityToBuy + "x " + originalItemName + 
                " for " + totalCost + " diamonds")
                .formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("💰 New balance: " + 
                balanceManager.getBalance(player.getUuid()) + " diamonds")
                .formatted(Formatting.AQUA), false);
        
        // Notify shop owner if online
        ServerPlayerEntity shopOwner = Tccdiamondeconomy.getServer().getPlayerManager().getPlayer(shop.ownerUUID);
        if (shopOwner != null) {
            shopOwner.sendMessage(Text.literal("💰 SALE! " + player.getName().getString() + " bought " + 
                    quantityToBuy + "x " + originalItemName + 
                    " from your shop for " + totalCost + " diamonds!")
                    .formatted(Formatting.GREEN, Formatting.BOLD));
            shopOwner.sendMessage(Text.literal("💎 You earned " + totalCost + " diamonds!")
                    .formatted(Formatting.GOLD), false);
        }
        
        Tccdiamondeconomy.LOGGER.info("Player {} purchased {}x {} from {}'s shop for {} diamonds", 
                player.getName().getString(), quantityToBuy, originalItemName,
                shop.ownerName, totalCost);
    }
    
    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        // Prevent shift-clicking from shop display
        if (slot < 27) {
            handleShopItemClick(slot, 0, player); // Treat as left-click purchase
            return ItemStack.EMPTY;
        }
        return super.quickMove(player, slot);
    }
    
    @Override
    public boolean canUse(PlayerEntity player) {
        // Verify the shop still exists
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        return shopManager.isShop(pos, world);
    }
    
    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        // Prevent inserting items into shop display
        if (startIndex < 27) {
            return false;
        }
        return super.insertItem(stack, startIndex, endIndex, fromLast);
    }
    
    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        // Prevent inserting into shop display slots
        if (slot.inventory == this.displayInventory) {
            return false;
        }
        return super.canInsertIntoSlot(stack, slot);
    }
}
