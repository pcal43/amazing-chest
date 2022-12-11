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
import net.pcal.amazingchest.AcService.PullPolicy;

public class AcScreenHandler extends GenericContainerScreenHandler {

    private static final int COLUMNS = 9;
    private static final int SINGLE_ROWS = 3;
    private static final int DOUBLE_ROWS = 6;

    private boolean locked = true;

    private AcScreenHandler(ScreenHandlerType<GenericContainerScreenHandler> type, int syncId, PlayerInventory playerInventory, Inventory amazingChest, int rows) {
        super(type, syncId, playerInventory, amazingChest, rows);
        for (int i = 0; i < rows * COLUMNS; i++) {
            super.slots.set(i, new AmazingSlot(super.slots.get(i)));
        }
    }

    static ScreenHandler createSingle(int syncId, PlayerInventory playerInventory, Inventory amazingChest) {
        return new AcScreenHandler(AcIdentifiers.getSingleScreenHandlerType(), syncId, playerInventory, amazingChest, SINGLE_ROWS);
    }

    static ScreenHandler createDouble(int syncId, PlayerInventory playerInventory, Inventory amazingChest) {
        return new AcScreenHandler(AcIdentifiers.getDoubleScreenHandlerType(), syncId, playerInventory, amazingChest, DOUBLE_ROWS);
    }

    static ScreenHandler registerSingle(int syncId, PlayerInventory playerInventory) {
        return new AcScreenHandler(AcIdentifiers.getSingleScreenHandlerType(), syncId, playerInventory, new SimpleInventory(COLUMNS * SINGLE_ROWS), SINGLE_ROWS);
    }

    static ScreenHandler registerDouble(int syncId, PlayerInventory playerInventory) {
        return new AcScreenHandler(AcIdentifiers.getDoubleScreenHandlerType(), syncId, playerInventory, new SimpleInventory(COLUMNS * DOUBLE_ROWS), DOUBLE_ROWS);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }


    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        if (!this.locked || index > this.getRows() * COLUMNS) return super.transferSlot(player, index);
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

        // if this stack is the last stack of the given item OR if pull-policy is set to per-stack, ensure that 1 item is left in the chest.
        if (!containsAtLeast(this.getInventory(), stack.getItem(), stack.getCount() + 1) || AcService.getInstance().getPullPolicy() == PullPolicy.PER_STACK) {
            leftover = stack.copy();
            leftover.setCount(1);
            stack.setCount(stack.getCount() - 1);
        }
        Inventory i = this.getInventory();
        if (index < this.getRows() * COLUMNS ? !this.insertItem(stack, this.getRows() * COLUMNS, this.slots.size(), true) : !this.insertItem(stack, 0, this.getRows() * COLUMNS, false)) {
            return ItemStack.EMPTY;
        }
        slot.setStack(leftover);
        slot.markDirty();
        return stack;
    }

    class AmazingSlot extends Slot {

        AmazingSlot(Slot s) {
            super(s.inventory, s.getIndex(), s.x, s.y);
            this.id = s.id;
        }

        @Override
        public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
            ItemStack itemStack = super.getStack();

            // if unlocked, do nothing custom
            if(!AcScreenHandler.this.locked) {
                return super.tryTakeStackRange(min, max, player);
            }

            // if PER_STACK is set, always take 1 less than the current stack max.
            if(AcService.getInstance().getPullPolicy() == PullPolicy.PER_STACK) {
                return super.tryTakeStackRange(min - 1, max, player);
            }

            // i don't think it's actually 'min' - seems to be the actual number they're trying to grab
            if (containsAtLeast(AcScreenHandler.this.getInventory(), itemStack.getItem(), min + 1)) {
                return super.tryTakeStackRange(min, max, player);
            } else {
                return super.tryTakeStackRange(min - 1, max, player);
            }
        }
    }
}

