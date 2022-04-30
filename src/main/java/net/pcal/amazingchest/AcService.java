package net.pcal.amazingchest;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pcal.amazingchest.AcReachabilityCache.ReachableInventory;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static net.minecraft.block.HopperBlock.FACING;
import static net.minecraft.util.math.Direction.DOWN;
import static net.pcal.amazingchest.AcIdentifiers.LOG_PREFIX;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.ACCEPT;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.DEMAND;
import static net.pcal.amazingchest.AcReachabilityCache.TransferDisposition.REJECT;
import static net.pcal.amazingchest.AcService.CacheInvalidationPolicy.AGGRESSIVE_INVALIDATION;
import static net.pcal.amazingchest.AcUtils.as;
import static net.pcal.amazingchest.AcUtils.containsAtLeast;

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
        this.cachePerDimension = new HashMap<>();
        this.logger =  requireNonNull(logger);
    }

    // ===================================================================================
    // Fields

    private final Logger logger;
    private final CacheInvalidationPolicy cachePolicy;
    private final Map<Identifier, AcReachabilityCache<HopperBlockEntity, Item>> cachePerDimension;

    // ===================================================================================
    // Public methods

    Logger getLogger() {
        return this.logger;
    }

    // ===================================================================================
    // PlayerBlockBreakEvents listener

    /**
     * When they break a hopper, chest or other inventory it may change the reachability graph.
     * So, invalidate our cached view of it.  This could probably be made more efficient.
     */
    @Override
    public void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (this.cachePolicy == AGGRESSIVE_INVALIDATION || blockEntity instanceof Inventory) {
            this.logger.debug(()-> AcIdentifiers.LOG_PREFIX + "clearing cache for breaking " + blockEntity);
            this.getCacheForDimension(world).clearCache();
        }
    }

    // ===================================================================================
    // BlockItem postPlacement handler

    /**
     * When they place a hopper, chest or other inventory it may change the reachability graph.
     * So, invalidate our cached view of it.  This could probably be made more efficient.
     */
    public void afterBlockPlace(World world, BlockPos blockPos) {
        if (world.isClient()) return;
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (this.cachePolicy == AGGRESSIVE_INVALIDATION || blockEntity instanceof Inventory) {
            this.logger.debug(()-> AcIdentifiers.LOG_PREFIX + "clearing cache for placing " + blockEntity);
            this.getCacheForDimension(world).clearCache();
        }
    }

    // ===================================================================================
    // Hopper behavior

    /**
     * Called from the mixin to see if we should veto a push from a given hopper.
     */
    public boolean shouldVetoPushFrom(Inventory pushingInventory, Item item, World world, BlockPos pos) {
        final HopperBlockEntity hopper = as(pushingInventory, HopperBlockEntity.class);
        if (hopper == null) {
            logger.warn(LOG_PREFIX + "shouldVetoPushFrom() unexpectedly called on a " + pushingInventory);
            return false;
        }
        final AcReachabilityCache<HopperBlockEntity, Item> cache = this.getCacheForDimension(world);
        if (this.cachePolicy == CacheInvalidationPolicy.NO_CACHE) cache.clearCache();
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

    /**
     * Called from the mixin to see if we should veto a pull into a given hopper.
     */
    public boolean shouldVetoPullFrom(Inventory pullFrom, ItemStack stack, Direction ignored) {
        final AmazingChestBlockEntity pullFromAc = as(pullFrom, AmazingChestBlockEntity.class);
        if (pullFromAc != null) {
            return shouldVetoPullFromAC(pullFromAc, stack);
        }
        final HopperBlockEntity pullFromHopper = as(pullFrom, HopperBlockEntity.class);
        if (pullFromHopper != null) {
            return shouldVetoPullFromHopper(pullFromHopper, stack);
        }
        final HopperMinecartEntity hopperMinecart = as(pullFrom, HopperMinecartEntity.class);
        if (hopperMinecart != null) {
            return shouldVetoPullFromMinecart(hopperMinecart, stack);
        }
        final DoubleInventory doubleInventory = as(pullFrom, DoubleInventory.class);
        if (doubleInventory != null) {
            // FIXME in this case, we can't figure out what the pulling hopper is because we can't
            // tell what chest block is associated with the inventory.  To fix this, we need to
            // rethink the mixin strategy so that we can directly capture a reference to the pulling
            // hopper.
            return false;
        }
        final BlockEntity pullFromBlock = as(pullFrom, BlockEntity.class);
        if (pullFromBlock != null) {
            return shouldVetoPullFromBlock(pullFromBlock, stack);
        }
        this.logger.warn("Ignoring attempt to pull "+stack.getItem().getName()+" from unknown Inventory type "+pullFrom);
        return false;
    }

    // ===================================================================================
    // Private methods

    /**
     * If pulling from an amazing chest, never take the last item of a given type.  That way, the
     * filtering behavior will be preserved.
     */
    private boolean shouldVetoPullFromAC(AmazingChestBlockEntity pullFromAC, ItemStack stack) {
        return !containsAtLeast(pullFromAC, stack.getItem(), 2);
    }

    /**
     * Reject pulls from hopper minecart if the pulling hopper only connects to ACs that won't accept the item.
     */
    private boolean shouldVetoPullFromMinecart(HopperMinecartEntity pullFromMinecart, ItemStack stack) {
        // If pulling from an amazing chest, never take the last item of a given type.  That way, the
        // filtering behavior will be preserved.
        final World world = requireNonNull(pullFromMinecart.getWorld());
        // FIXME? calculation doesn't seem to be right.  But we're probably going to need to rework the hopper
        // identification anyway.
        final BlockEntity pullingBlock = world.getBlockEntity(pullFromMinecart.getBlockPos().offset(DOWN));
        final HopperBlockEntity pullingHopper = as(pullingBlock, HopperBlockEntity.class);
        if (pullingHopper == null) {
            logger.debug(LOG_PREFIX + "shouldVetoPullFromMinecart() unexpectedly called from minecart at " +
                    pullFromMinecart.getBlockPos()+" to a non hopper: " + pullingBlock);
            return false;
        }
        return getTransferDisposition(pullingHopper, stack.getItem()) == REJECT;
    }

    private boolean shouldVetoPullFromBlock(BlockEntity pullFromBlock, ItemStack stack) {
        final World world = requireNonNull(pullFromBlock.getWorld());
        final BlockEntity pullingBlock = world.getBlockEntity(pullFromBlock.getPos().offset(DOWN));
        final HopperBlockEntity pullingHopper = as(pullingBlock, HopperBlockEntity.class);
        if (pullingHopper == null) {
            logger.warn(LOG_PREFIX + "shouldVetoPullFromBlock() unexpectedly called for a non hopper: " + pullingBlock);
            return false;
        }
        return getTransferDisposition(pullingHopper, stack.getItem()) == REJECT;
    }

    private boolean shouldVetoPullFromHopper(HopperBlockEntity pullFromHopper, ItemStack stack) {
        final Item item = stack.getItem();
        final Direction facing = pullFromHopper.getCachedState().get(FACING);
        if (facing != DOWN) {
            final World world = requireNonNull(pullFromHopper.getWorld());
            final BlockEntity pushToBlock = world.getBlockEntity(pullFromHopper.getPos().offset(facing));
            final TransferDisposition pushDisposition = getTransferDisposition(pushToBlock, item);
            if (pushDisposition == DEMAND) return true; // somebody in the push chain wants it, so veto the pull
            final BlockEntity pullingBlock = world.getBlockEntity(pullFromHopper.getPos().offset(DOWN));
            final HopperBlockEntity pullingHopper = as(pullingBlock, HopperBlockEntity.class);
            if (pullingHopper == null) {
                logger.warn(LOG_PREFIX + "shouldVetoPullFromHopper() unexpectedly called for a non hopper: " + pullingBlock);
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
        return false;
    }

    /**
     * Get a sense of how the given target would feel about receiving the given item, accounting for
     * reachable inventories.
     */
    private TransferDisposition getTransferDisposition(BlockEntity targetBlock, Item item) {
        final HopperBlockEntity targetHopper = as(targetBlock, HopperBlockEntity.class);
        if (targetHopper != null) { // if we're pushing into another hopper
            final AcReachabilityCache<HopperBlockEntity, Item> cache = this.getCacheForDimension(targetBlock.getWorld());
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

    /**
     * Get a sense of how the given inventories would feel about receiving the given item, accounting for all
     * the inventories that are reachable
     */
    private static TransferDisposition getTransferDisposition(Collection<ReachableInventory<Item>> reachableInventories, Item item) {
        TransferDisposition currentPolicy = null;
        for (ReachableInventory<Item> reachableInventory : reachableInventories) {
            switch (reachableInventory.getDispositionToward(item)) {
                case DEMAND:
                    // If anyone downstream demands it, we demand it, end of story
                    return DEMAND;
                case REJECT:
                    // If someone downstream rejects it, rejection becomes the default
                    if (currentPolicy != ACCEPT) currentPolicy = REJECT;
                    break;
                case ACCEPT:
                    // If someone downstream will accept it, acceptance becomes the default.  It can only
                    // be upgraded to a demand.
                    currentPolicy = ACCEPT;
                    break;
            }
        }
        return currentPolicy;
    }

    /**
     * Get the reachability cache for the given world (dimension).
     */
    private AcReachabilityCache<HopperBlockEntity, Item> getCacheForDimension(World world) {
        final Identifier dimId = world.getRegistryKey().getValue(); // this corresponds to dimension
        if (!this.cachePerDimension.containsKey(dimId)) {
            this.cachePerDimension.put(dimId, new AcReachabilityCache<>(AcReachabilityDelegate.INSTANCE, logger));
        }
        return requireNonNull(this.cachePerDimension.get(dimId));
    }
}
