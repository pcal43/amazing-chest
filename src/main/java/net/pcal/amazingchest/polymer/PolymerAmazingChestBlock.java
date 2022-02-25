package net.pcal.amazingchest.polymer;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.client.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.pcal.amazingchest.AmazingChestBlock;

public class PolymerAmazingChestBlock extends AmazingChestBlock implements PolymerBlock, PolymerKeepModel {//, PolymerClientDecoded {

    public PolymerAmazingChestBlock() {}

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.CHEST;
    }

    /**
    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.getDefaultState()
                .with(HopperBlock.FACING, state.get(HopperBlock.FACING))
                .with(HopperBlock.ENABLED, state.get(HopperBlock.ENABLED));
    }
**/
}
