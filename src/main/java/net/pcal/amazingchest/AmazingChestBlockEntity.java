package net.pcal.amazingchest;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class AmazingChestBlockEntity extends ChestBlockEntity {

    public AmazingChestBlockEntity(BlockPos pos, BlockState state) {
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
}
