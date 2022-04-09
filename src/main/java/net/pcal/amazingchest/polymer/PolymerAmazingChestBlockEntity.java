package net.pcal.amazingchest.polymer;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.pcal.amazingchest.AcIdentifiers;

public class PolymerAmazingChestBlockEntity extends ChestBlockEntity {

    public PolymerAmazingChestBlockEntity(BlockPos pos, BlockState state) {
        super(AcIdentifiers.getAcBlockEntityType(), pos, state);
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
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }
    /**
    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return AcScreenHandler.createSingle(syncId, playerInventory, this);
    }**/

}
