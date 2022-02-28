package net.pcal.amazingchest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

import static net.pcal.amazingchest.AcUtils.containsAtLeast;

public class AcScreenHandler extends GenericContainerScreenHandler {

    private AcScreenHandler(ScreenHandlerType<GenericContainerScreenHandler> type, int syncId, PlayerInventory playerInventory, Inventory amazingChest, int rows) {
        super(type, syncId, playerInventory, amazingChest, rows);
        for (int i = 0; i < 27; i++) {
            super.slots.set(i, new AmazingSlot(super.slots.get(i)));
        }
    }

    static ScreenHandler create(int syncId, PlayerInventory playerInventory, Inventory amazingChest) {
        return new AcScreenHandler(AcIdentifiers.getScreenHandlerType(), syncId, playerInventory, amazingChest, 3);
    }


    static ScreenHandler createForRegistration(int syncId, PlayerInventory playerInventory) {
        return new AcScreenHandler(AcIdentifiers.getScreenHandlerType(), syncId, playerInventory, new SimpleInventory(9 * 3), 3);
    }

    /**
    public void setCursorStack(ItemStack stack) {
        stack.setCount(stack.getCount() - 1);
        super.setCursorStack(stack);
    }
**/

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        if (index > 27) return super.transferSlot(player, index);
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack().copy();
        if (stack.isEmpty()) return stack;
        if (!containsAtLeast(this.getInventory(), stack.getItem(), 1)) {
            return ItemStack.EMPTY;
        }
        ItemStack leftover = ItemStack.EMPTY;
        if (!containsAtLeast(this.getInventory(), stack.getItem(), stack.getCount() + 1)) {
            leftover = stack.copy();
            leftover.setCount(1);
            stack.setCount(stack.getCount() - 1);
        }
        Inventory i = this.getInventory();
        if (index < this.getRows() * 9 ? !this.insertItem(stack, this.getRows() * 9, this.slots.size(), true) : !this.insertItem(stack, 0, this.getRows() * 9, false)) {
            return ItemStack.EMPTY;
        }
        slot.setStack(leftover);
        slot.markDirty();
        return stack;
    }

/**    @Override
    public ScreenHandlerType<?> getType() {
        return AcIdentifiers.getScreenHandlerType();
    }
**/
    static class AmazingSimpleInventory extends SimpleInventory {
        public AmazingSimpleInventory(int size) {
            super(size);
        }

        /**
        @Override
        public ItemStack removeStack(int slot) {
            ItemStack itemStack = super.getStack(slot);
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            Inventory i = this;
            if (containsAtLeast(i, itemStack.getItem(), itemStack.getCount()+1)) {
                return itemStack;
            }

            ItemStack leftover = itemStack.copy();
            leftover.setCount(1);
            this.setStack(slot, leftover);
            itemStack.setCount(itemStack.getCount() - 1);
            return itemStack;
        }
         **/
    }


    class AmazingSlot extends Slot {

        AmazingSlot(Slot s) {
            super(s.inventory, s.getIndex(), s.x, s.y);
            this.id = s.id;
        }

        @Override
        public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
            ItemStack itemStack = super.getStack();
            // i don't think it's actually 'min' - seems to be the actual number they're trying to grab
            if (containsAtLeast(AcScreenHandler.this.getInventory(), itemStack.getItem(), min+1)) {
                return super.tryTakeStackRange(min, max, player);
            } else {
                return super.tryTakeStackRange(min - 1, max, player);
            }
        }
/**
        @Override
        public ItemStack takeStack(int amount) {
            return this.inventory.removeStack(this.getIndex(), amount);
        }

        @Override
        public void onQuickTransfer(ItemStack newItem, ItemStack original) {
            int i = original.getCount() - newItem.getCount();
            if (i > 0) {
                this.onCrafted(original, i);
            }
        }
 **/
    }

}

