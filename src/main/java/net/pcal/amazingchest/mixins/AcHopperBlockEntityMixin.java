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
     * Somewhat invasive change to prevent the hopper from pushing out it's last item.  Basically make the stacks
     * read as empty if they shouldn't be pushed out.
     */
    @Redirect(method = "insert",
            at = @At(value = "INVOKE",
                    ordinal = 0,
                    target = "Lnet/minecraft/inventory/Inventory;getStack(I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack __getStack(
         // target params
         Inventory pushingInventory, int slot,
         // method params
        World world, BlockPos pos, BlockState state, Inventory ignored) {
        final ItemStack original = pushingInventory.getStack(slot);
        if (AcService.getInstance().shouldVetoPushFrom(pushingInventory, original.getItem(), world, pos)) {
            return ItemStack.EMPTY;
        }
        return original;
    }



    @Inject(method = "canExtract", at = @At("HEAD"), cancellable = true)
    private static void __canExtract (Inventory pullFrom, ItemStack stack, int slot, Direction facing, CallbackInfoReturnable<Boolean> returnable) {
        if (AcService.getInstance().shouldVetoPullFrom(pullFrom, stack, facing)) {
            returnable.setReturnValue(false);
        }
    }

    /**
     * Somewhat invasive change to prevent the hopper from pushing out it's last item.  Basically make the stacks
     * read as empty if they shouldn't be pushed out.

    @Redirect(method = "canExtract",
            at = @At(value = "INVOKE",
                    ordinal = 0,
                    target = "Lnet/minecraft/inventory/Inventory;getStack(I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack _extract(
            // target params
            Inventory pullingInventory, int slot1,
            // method params
            Hopper hopper, Inventory inventory, int slot, Direction side) {
//        final ItemStack original = fromInventory.getStack(slot);
//        if (AcService.getInstance().shouldVetoPullInto(toHopper, original.getItem())) {
//            return ItemStack.EMPTY;
//        }
//        return original;
        return null;
    }
    **/
}
