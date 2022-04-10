package net.pcal.amazingchest.polymer;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.pcal.amazingchest.AmazingChestBlockEntity;

public class PolymerAmazingChestBlockEntity extends AmazingChestBlockEntity {

    // FIXME i18n?  Also, I don't see a way  to distinguish a single/double chests here.
    private static final Text NAME = new LiteralText("Sorting Chest");

    public PolymerAmazingChestBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, false);
        super.setCustomName(NAME);
    }

    @Override
    public Text getDisplayName() {
        return NAME;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }
}
