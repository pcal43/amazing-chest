package net.pcal.amazingchest.polymer;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.client.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.pcal.amazingchest.AmazingChestBlock;

public class PolymerAmazingChestBlock extends AmazingChestBlock implements PolymerBlock, PolymerKeepModel {//, PolymerClientDecoded {

    public PolymerAmazingChestBlock() {
        super(false);
    }

    // ===================================================================================
    // Block implementation

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PolymerAmazingChestBlockEntity(pos, state);
    }

    // ===================================================================================
    // PolymerBlock implementation

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.TRAPPED_CHEST;
    }



    /**
     * Generic method used for replacing BlockStates in case of no player context
     * It also controls some server side things like collisions
     *
     * @param state Server side/real BlockState
     * @return BlockState visible on client
     */
    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.getPolymerBlock(state).getDefaultState().with(CHEST_TYPE, state.get(CHEST_TYPE));
    }
}
