package net.thecubecollective.diamondeconomy.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    
    @Inject(method = "getInventoryAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/inventory/Inventory;", 
            at = @At("HEAD"), cancellable = true)
    private static void preventShopAccess(World world, BlockPos pos, CallbackInfoReturnable<Inventory> cir) {
        // Check if the target position contains a chest shop (handles both single and double chests)
        if (Tccdiamondeconomy.getChestShopManager() != null && 
            Tccdiamondeconomy.getChestShopManager().isShop(pos, world)) {
            // Return null to prevent hopper access to any part of the shop
            cir.setReturnValue(null);
            return;
        }
        
        // Additional check: if this is a trapped chest, verify it's not part of a double chest shop
        // This prevents access to the "other half" of a double chest that might not be the main position
        if (world.getBlockState(pos).isOf(net.minecraft.block.Blocks.TRAPPED_CHEST)) {
            BlockPos mainPos = net.thecubecollective.diamondeconomy.TrappedChestUtils.getMainChestPosition(pos, world);
            if (!mainPos.equals(pos) && Tccdiamondeconomy.getChestShopManager() != null && 
                Tccdiamondeconomy.getChestShopManager().isShop(mainPos, world)) {
                // This is the "other half" of a double chest shop, block access
                cir.setReturnValue(null);
            }
        }
    }
}
