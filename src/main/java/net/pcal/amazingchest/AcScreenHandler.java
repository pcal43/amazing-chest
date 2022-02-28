package net.pcal.amazingchest;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.ItemTags;

public class AcScreenHandler extends GenericContainerScreenHandler {

    private AcScreenHandler(ScreenHandlerType<GenericContainerScreenHandler> type, int syncId, PlayerInventory playerInventory,  int rows) {
        super(type, syncId, playerInventory, new SimpleInventory(9 * rows), rows);
    }

    static ScreenHandler create(int syncId, PlayerInventory playerInventory) {
        return new AcScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, 3);
    }

    public void setCursorStack(ItemStack stack) {
        stack.setCount(stack.getCount() - 1);
        super.setCursorStack(stack);
    }

    class AmazingSlot extends Slot {
        public AmazingSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isIn(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }
}

