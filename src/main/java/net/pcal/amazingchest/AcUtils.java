package net.pcal.amazingchest;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface AcUtils {

    /**
     * Returns true if the given inventory contains at least the given number of the given item (across all slots).
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean containsAtLeast(AmazingChestBlockEntity ace, Item item, int atLeast) {
        final Inventory inventory = getInventoryFor(ace);
        return containsAtLeast(inventory, item, atLeast);
    }

    static boolean containsAtLeast(Inventory inventory, Item item, int atLeast) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.getItem().equals(item)) {
                count += itemStack.getCount();
                if (count >= atLeast) return true; // don't bother counting the rest
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    static <B, T extends B> T as(B block, Class<T> clazz) {
        return clazz.isInstance(block) ? (T)block : null;
    }

    /**
     * @return the inventory for the given chest.  This is necessary to account for the case of a double chest.
     */
    private static Inventory getInventoryFor(AmazingChestBlockEntity ace) {
        final BlockState blockState = ace.getWorld().getBlockState(ace.getPos());
        return ChestBlock.getInventory((ChestBlock) blockState.getBlock(), blockState, ace.getWorld(), ace.getPos(), false);
    }
}
