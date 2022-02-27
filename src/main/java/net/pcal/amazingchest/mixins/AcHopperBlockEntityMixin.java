package net.pcal.amazingchest.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pcal.amazingchest.AcService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(HopperBlockEntity.class)
public abstract class AcHopperBlockEntityMixin {

    /**
     * Somewhat invasive change to check if the hopper should push an item out.  Not sure there's a good
     * way to do this without the redirect.
     */
    @Redirect(method = "insert", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0))
    private static boolean filterOutput(ItemStack stack, World world, BlockPos pos, BlockState state, Inventory inventory) {
        if (stack.isEmpty()) return true;
        return AcService.getInstance().shouldVetoPushFrom(inventory, stack.getItem(), world, pos);
    }


    /**
     * Check to see if a hopper should extract (pull) an item.
     */
    @Inject(method = "canExtract", at = @At("HEAD"), cancellable = true)
    private static void __canExtract (Inventory pullFrom, ItemStack stack, int slot, Direction facing, CallbackInfoReturnable<Boolean> returnable) {
        if (AcService.getInstance().shouldVetoPullFrom(pullFrom, stack, facing)) {
            returnable.setReturnValue(false);
        }
    }

}
