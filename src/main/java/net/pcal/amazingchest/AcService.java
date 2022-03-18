package net.pcal.amazingchest;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import net.pcal.amazingchest.AcReachabilityCache.Chest;

import java.util.Collection;

import static java.util.Objects.requireNonNull;
import static net.minecraft.block.HopperBlock.FACING;
import static net.minecraft.util.math.Direction.DOWN;
import static net.pcal.amazingchest.AcIdentifiers.LOG_PREFIX;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.ACCEPT;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.DEMAND;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.REJECT;
import static net.pcal.amazingchest.AcUtils.as;
import static net.pcal.amazingchest.AcUtils.containsAtLeast;

import static net.pcal.amazingchest.AcService.CacheInvalidationPolicy.AGGRESSIVE_INVALIDATION;

/**
 * Central singleton service.
 */
@SuppressWarnings("ClassCanBeRecord")
public class AcService implements PlayerBlockBreakEvents.After {

    // ===================================================================================
    // Constants

    enum CacheInvalidationPolicy {
        DEFAULT,
        AGGRESSIVE_INVALIDATION,
        NO_CACHE,
    }

    // ===================================================================================
    // Singleton

    private static AcService INSTANCE;

    public static AcService getInstance() {
        return INSTANCE;
    }

    public static synchronized void initialize(CacheInvalidationPolicy cachePolicy, Logger logger) {
        if (INSTANCE != null) throw new IllegalStateException();
        INSTANCE = new AcService(cachePolicy, new AcReachabilityCache<>(AcReachabilityDelegate.INSTANCE, logger), logger);
    }

    private AcService(CacheInvalidationPolicy cachePolicy, AcReachabilityCache<HopperBlockEntity, Item> cache, Logger logger) {
        this.cachePolicy = requireNonNull(cachePolicy);
        this.cache = requireNonNull(cache);
        this.logger =  requireNonNull(logger);
    }

    // ===================================================================================
    // Fields

    private final Logger logger;
    private final CacheInvalidationPolicy cachePolicy;
    private final AcReachabilityCache<HopperBlockEntity, Item> cache;

    // ===================================================================================
    // Public methods

    Logger getLogger() {
        return this.logger;
    }

    // ===================================================================================
    // PlayerBlockBreakEvents listener

    @Override
    public void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (this.cachePolicy == AGGRESSIVE_INVALIDATION || blockEntity instanceof Inventory) {
            this.logger.debug(()-> AcIdentifiers.LOG_PREFIX + "clearing cache for breaking " + blockEntity);
            this.cache.clearCache();
        }
    }

    // ===================================================================================
    // BlockItem postPlacement handler

    public void afterBlockPlace(World world, BlockPos blockPos) {
        if (world.isClient()) return;
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (this.cachePolicy == AGGRESSIVE_INVALIDATION || blockEntity instanceof Inventory) {
            this.logger.debug(()-> AcIdentifiers.LOG_PREFIX + "clearing cache for placing " + blockEntity);
            this.cache.clearCache();
        }
    }

    // ===================================================================================
    // Hopper behavior

    public boolean shouldVetoPushFrom(Inventory pushingInventory, Item item, World world, BlockPos pos) {
        final HopperBlockEntity hopper = as(pushingInventory, HopperBlockEntity.class);
        if (hopper == null) {
            logger.warn(LOG_PREFIX + "shouldVetoPushFrom() unexpectedly called on a " + pushingInventory);
            return false;
        }
        if (this.cachePolicy == CacheInvalidationPolicy.NO_CACHE) this.cache.clearCache();
        final Direction facing = hopper.getCachedState().get(FACING);
        final BlockPos pushToPos = hopper.getPos().offset(facing);
        final BlockEntity pushToBlock = world.getBlockEntity(pushToPos);
        final TransferDisposition pushPolicy = getTransferDisposition(pushToBlock, item);
        if (pushPolicy == DEMAND) {
            return false;
        }
        if (pushPolicy == REJECT) {
            return true;
        }
        // ok, check the block below; if it's a hopper, we might also want to veto in order to let it pull the item
        final BlockEntity underBlock = world.getBlockEntity(hopper.getPos().offset(DOWN));
        final HopperBlockEntity underHopper = as(underBlock, HopperBlockEntity.class);
        if (underHopper != null) {
            final TransferDisposition underPolicy = getTransferDisposition(underHopper, item);
            return underPolicy == DEMAND; // veto the push if somebody in the pull chain wants it
        }
        return false;
    }

    public boolean shouldVetoPullFrom(Inventory pullFrom, ItemStack stack, Direction ignored) {
        final BlockEntity pullFromBlock = as(pullFrom, BlockEntity.class);
        if (pullFromBlock == null) {
            logger.warn(LOG_PREFIX + "shouldVetoPullFrom() unexpectedly called on a non-BlockEntity: " + pullFrom);
            return false;
        }
        if (this.cachePolicy == CacheInvalidationPolicy.NO_CACHE) {
            this.cache.clearCache();
        }
        final Item item = stack.getItem();
        final HopperBlockEntity pullFromHopper = as(pullFrom, HopperBlockEntity.class);
        if (pullFromHopper != null) {
            final Direction facing = pullFromHopper.getCachedState().get(FACING);
            if (facing != DOWN) {
                final World world = requireNonNull(pullFromBlock.getWorld());
                final BlockEntity pushToBlock = world.getBlockEntity(pullFromHopper.getPos().offset(facing));
                final TransferDisposition pushDisposition = getTransferDisposition(pushToBlock, item);
                if (pushDisposition == DEMAND) return true; // somebody in the push chain wants it, so veto the pull
                final BlockEntity pullingBlock = world.getBlockEntity(pullFromBlock.getPos().offset(DOWN));
                final HopperBlockEntity pullingHopper = as(pullingBlock, HopperBlockEntity.class);
                if (pullingHopper == null) {
                    logger.warn(LOG_PREFIX + "shouldVetoPullFrom() unexpectedly called for a non hopper: " + pullingBlock);
                    return false;
                }
                final TransferDisposition pullDisposition = getTransferDisposition(pullingBlock, item);
                if (pullDisposition == DEMAND || pullDisposition == ACCEPT) {
                    return false;
                }
                if (pullDisposition == REJECT && pushDisposition != REJECT) {
                    return true;
                }
            }
        }
        final AmazingChestBlockEntity pullFromAC = as(pullFrom, AmazingChestBlockEntity.class);
        if (pullFromAC != null) {
            // If pulling from an amazing chest, never take the last item of a given type.  That way, the
            // filtering behavior will be preserved.
            return !containsAtLeast(pullFromAC, item, 2);
        }
        return false;
    }

    // ===================================================================================
    // Private methods

    private TransferDisposition getTransferDisposition(BlockEntity targetBlock, Item item) {
        final HopperBlockEntity targetHopper = as(targetBlock, HopperBlockEntity.class);
        if (targetHopper != null) { // if we're pushing into another hopper
            return getTransferDisposition(cache.getReachableChests(targetHopper), item);
        }
        final AmazingChestBlockEntity targetAmazingChest = as(targetBlock, AmazingChestBlockEntity.class);
        if (targetAmazingChest != null) {
            // if they're trying to put it into an AC, veto it if the chest doesn't have one
            return containsAtLeast(targetAmazingChest, item, 1) ? DEMAND : REJECT;
        }
        if (as(targetBlock, Inventory.class) != null) {
            return ACCEPT;
        } else {
            return null;
        }
    }

    private TransferDisposition getTransferDisposition(Collection<Chest<Item>> chests, Item item) {
        TransferDisposition currentPolicy = null;
        for (Chest<Item> chest : chests) {
            switch (chest.getDispositionToward(item)) {
                case DEMAND:
                    return DEMAND;
                case REJECT:
                    if (currentPolicy != ACCEPT) currentPolicy = REJECT;
                    break;
                case ACCEPT:
                    currentPolicy = ACCEPT;
            }
        }
        return currentPolicy;
    }
}
