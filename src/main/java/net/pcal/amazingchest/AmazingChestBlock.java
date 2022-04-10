package net.pcal.amazingchest;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

public class AmazingChestBlock extends ChestBlock {

    private final boolean isCustomScreenEnabled;

    public AmazingChestBlock(boolean isCustomScreenEnabled) {
        super(FabricBlockSettings.copyOf(Blocks.CHEST), () -> AcIdentifiers.getAcBlockEntityType());
        this.isCustomScreenEnabled = isCustomScreenEnabled;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AmazingChestBlockEntity(pos, state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // TODO add BlockState so we can show particles only when chest contains items?
        if (Math.abs(random.nextInt()) % 4 > 0) return;
        final DoubleBlockProperties.Type dt = getDoubleBlockType(state);
        if (dt == DoubleBlockProperties.Type.SECOND) return;
        Direction direction = state.get(FACING);
        for (int i = 0, j=Math.abs(random.nextInt()) % 4; i < j; i++) {
            direction = direction.rotateYClockwise();
        }
        double d = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        double e = (double)pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.2;
        double f = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;

        /**
        if (dt == DoubleBlockProperties.Type.FIRST) {
            Direction rot = direction.rotateYCounterclockwise();
            d += .5 * rot.getOffsetX();
            f += .5 * rot.getOffsetZ();
        }
**/
        float g = (7.5f / 16.0f);
        double h = g * (float)direction.getOffsetX();
        double i = g * (float)direction.getOffsetZ();
        world.addParticle(DustParticleEffect.DEFAULT, d + h, e, f + i, 0.0, -0.2, 0.0);
    }

    @Override
    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        if (this.isCustomScreenEnabled) {
            return this.getBlockEntitySource(state, world, pos, false).apply(NAME_RETRIEVER).orElse(null);
        } else {
            return super.createScreenHandlerFactory(state, world, pos);
        }
    }

    private static final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>> NAME_RETRIEVER = new DoubleBlockProperties.PropertyRetriever<>() {

        @Override
        public Optional<NamedScreenHandlerFactory> getFromBoth(final ChestBlockEntity chestBlockEntity, final ChestBlockEntity chestBlockEntity2) {
            final DoubleInventory inventory = new DoubleInventory(chestBlockEntity, chestBlockEntity2);
            return Optional.of(new NamedScreenHandlerFactory(){

                @Override
                @Nullable
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    if (chestBlockEntity.checkUnlocked(playerEntity) && chestBlockEntity2.checkUnlocked(playerEntity)) {
                        chestBlockEntity.checkLootInteraction(playerInventory.player);
                        chestBlockEntity2.checkLootInteraction(playerInventory.player);
                        return AcScreenHandler.createDouble(syncId, playerInventory, inventory);
                    }
                    return null;
                }

                @Override
                public Text getDisplayName() {
                    if (chestBlockEntity.hasCustomName()) {
                        return chestBlockEntity.getDisplayName();
                    }
                    if (chestBlockEntity2.hasCustomName()) {
                        return chestBlockEntity2.getDisplayName();
                    }
                    return new TranslatableText("block.amazingchest.amazing_chest_double");
                }
            });
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFrom(ChestBlockEntity chestBlockEntity) {
            return Optional.of(chestBlockEntity);
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFallback() {
            return Optional.empty();
        }
    };
}
