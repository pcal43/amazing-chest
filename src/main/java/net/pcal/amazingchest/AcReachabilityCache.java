package net.pcal.amazingchest;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

class AcReachabilityCache<HOPPER, ITEM> {

    // ===================================================================================
    // Fields

    private final Map<HOPPER, ImmutableList<ReachableInventory<ITEM>>> cache;
    private final ReachabilityDelegate<HOPPER, ITEM> ctx;
    private final Logger logger;

    // ===================================================================================
    // Constructor

    AcReachabilityCache(ReachabilityDelegate<HOPPER, ITEM> ctx, Logger logger) {
        this.ctx = requireNonNull(ctx);
        this.cache = new HashMap<>();
        this.logger = requireNonNull(logger);
    }

    // ===================================================================================
    // Public methods

    void clearCache() {
        this.cache.clear();
    }

    List<ReachableInventory<ITEM>> getReachableChests(HOPPER from) {
        List<ReachableInventory<ITEM>> chests = this.cache.get(from);
        if (chests == null) {
            chests = traverseHopperChain(from);
            if (chests == null) {
                logger.warn("null chests from " + from);
                return Collections.emptyList();
            }
        }
        return chests;
    }

    // ===================================================================================
    // Private methods

    private List<ReachableInventory<ITEM>> traverseHopperChain(HOPPER firstHopper) {
        final Deque<HopperVisit<HOPPER, ITEM>> visitStack = new ArrayDeque<>();
        final HopperVisit<HOPPER, ITEM> firstVisit = HopperVisit.createFor(ctx, firstHopper);
        visitStack.add(firstVisit);

        while (!visitStack.isEmpty()) {
            final HopperVisit<HOPPER, ITEM> currentVisit = visitStack.peek();
            mainLoop:
            if (currentVisit.hasNext()) {
                final HOPPER nextHopper = currentVisit.next();
                List<ReachableInventory<ITEM>> cached = cache.get(nextHopper);
                if (cached != null) {
                    // we already have a cached result for this hopper; just tell everyone about it
                    visitStack.forEach(previousVisit -> previousVisit.reachableChests.addAll(cached));
                } else {
                    for (HopperVisit<HOPPER, ITEM> previouslyVisited : visitStack) {
                        if (previouslyVisited.hopper == nextHopper) {
                            // we have a cycle.  everything is reachable.  we're done.
                            // FIXME this is still broken because we haven't finished evaluating branches lower
                            // in the stack, and those will be potentially reachable from the head visitor that
                            // we're now going to unwind.
                            visitStack.forEach(p -> p.reachableChests.addAll(previouslyVisited.reachableChests));
                            break mainLoop;
                        }
                    }
                    // we're in uncharted territory.  start a new visit
                    final HopperVisit<HOPPER, ITEM> nextVisit = HopperVisit.createFor(ctx, nextHopper);
                    visitStack.push(nextVisit);
                }
            } else {
                //
                // The current visit has no more hoppers to look at.  Note the reachable chests in the context
                // (this will be the cached value for this hopper) and also notify everyone else down the stack
                // about what is reachable from the current hopper.
                //
                cache.put(currentVisit.hopper, ImmutableList.copyOf(currentVisit.reachableChests));
                visitStack.pop();
                visitStack.forEach(previousVisit -> previousVisit.reachableChests.addAll(currentVisit.reachableChests));
            }
        }
        return this.cache.get(firstHopper);
    }

    // ===================================================================================
    // Public classes

    enum TransferDisposition {
        REJECT,
        ACCEPT,
        DEMAND,
    }

    interface ReachableInventory<I> {
        TransferDisposition getDispositionToward(I item);
    }

    interface ReachabilityDelegate<H, I> {
        Pair<H[], ReachableInventory<I>> getOutboundConnections(H fromHopper);
    }

    // ===================================================================================
    // Private inner classes

    private static class HopperVisit<H, I> {

        private final H hopper;
        private final Iterator<H> outboundIterator;
        private final Set<ReachableInventory<I>> reachableChests;

        static <H, I> HopperVisit<H, I> createFor(final ReachabilityDelegate<H, I> ctx, H hopper) {
            Pair<H[], ReachableInventory<I>> pair = ctx.getOutboundConnections(hopper);
            return new HopperVisit<H, I>(hopper, pair.getLeft(), pair.getRight());
        }

        private HopperVisit(H hopper, H[] outboundHoppers, ReachableInventory<I> outboundChest) {
            this.hopper = requireNonNull(hopper);
            this.outboundIterator = outboundHoppers == null ? null :
                    List.of(outboundHoppers).iterator(); // stupid fixme.  ArrayIterator?
            this.reachableChests = new HashSet<>();
            if (outboundChest != null) this.reachableChests.add(outboundChest);
        }

        boolean hasNext() {
            return outboundIterator != null && outboundIterator.hasNext();
        }

        H next() {
            return outboundIterator.next();
        }
    }

}
