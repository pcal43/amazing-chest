package net.pcal.amazingchest;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pcal.amazingchest.AcReachabilityCache.Chest;
import net.pcal.amazingchest.AcReachabilityCache.ReachabilityDelegate;
import net.pcal.amazingchest.AcReachabilityCache.TransferDisposition;

import static java.util.Objects.requireNonNull;
import static net.minecraft.block.HopperBlock.FACING;
import static net.minecraft.util.math.Direction.DOWN;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.ACCEPT;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.DEMAND;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.REJECT;
import static net.pcal.amazingchest.AcUtils.as;

@SuppressWarnings("SpellCheckingInspection")
public enum AcReachabilityDelegate implements ReachabilityDelegate<HopperBlockEntity, Item> {

    INSTANCE;

    @Override
    public Pair<HopperBlockEntity[], Chest<Item>> getOutboundConnections(HopperBlockEntity fromHopper) {
        final World world = requireNonNull(fromHopper.getWorld());
        final Direction facing = fromHopper.getCachedState().get(FACING);
        final BlockPos pushPos = fromHopper.getPos().offset(facing);
        final BlockEntity pushBlock = world.getBlockEntity(pushPos);

        final Chest<Item> pushChest;
        final AmazingChestBlockEntity pushAmazingChest = as(pushBlock, AmazingChestBlockEntity.class);
        final HopperBlockEntity pushHopper = pushAmazingChest == null ? as(pushBlock, HopperBlockEntity.class) : null;
        if (pushAmazingChest != null) {
            pushChest = new AmazingReachableChest(pushAmazingChest);
        } else if (pushHopper == null && as(pushBlock, Inventory.class) != null) {
            pushChest = RegularChest.INSTANCE;
        } else {
            pushChest = null;
        }
        final HopperBlockEntity pullHopper;
        if (facing != DOWN) {
            final BlockPos pullPos = fromHopper.getPos().offset(DOWN);
            final BlockEntity pullBlock = world.getBlockEntity(pullPos);
            pullHopper = as(pullBlock, HopperBlockEntity.class);
        } else {
            pullHopper = null;
        }
        final HopperBlockEntity[] hoppers;
        if (pushHopper != null) {
            hoppers = pullHopper != null ? asArray(pushHopper, pullHopper) : asArray(pushHopper);
        } else if (pullHopper !=null) {
            hoppers = asArray(pullHopper);
        } else {
            hoppers = null;
        }
        return new Pair<>(hoppers, pushChest);
    }

    @SafeVarargs
    private static <T> T[] asArray(T... elems) {
        return elems;
    }

    private enum RegularChest implements Chest<Item> {
        INSTANCE;
        @Override
        public TransferDisposition getDispositionToward(Item item) {
            return ACCEPT;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class AmazingReachableChest implements Chest<Item> {

        private final AmazingChestBlockEntity acbe;

        private AmazingReachableChest(AmazingChestBlockEntity acbe) {
            this.acbe = requireNonNull(acbe);
        }

        @Override
        public TransferDisposition getDispositionToward(Item item) {
            return AcUtils.containsAtLeast(this.acbe, requireNonNull(item), 1) ? DEMAND : REJECT;
        }
    }

}
