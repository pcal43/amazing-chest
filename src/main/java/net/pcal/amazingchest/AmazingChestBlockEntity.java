package net.pcal.amazingchest;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class AmazingChestBlockEntity extends ChestBlockEntity {

    private final boolean isCustomScreenEnabled;

    public AmazingChestBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, true);
    }

    public AmazingChestBlockEntity(BlockPos pos, BlockState state, boolean isCustomScreenEnabled) {
        super(AcIdentifiers.getAcBlockEntityType(), pos, state);
        this.isCustomScreenEnabled = isCustomScreenEnabled;
    }

    @Override
    public BlockEntityType<?> getType() {
        return AcIdentifiers.getAcBlockEntityType();
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        if (this.isCustomScreenEnabled) {
            return AcScreenHandler.createSingle(syncId, playerInventory, this);
        } else {
            return super.createScreenHandler(syncId, playerInventory);
        }
    }

}
