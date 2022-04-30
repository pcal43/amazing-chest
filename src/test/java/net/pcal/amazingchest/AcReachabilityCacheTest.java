package net.pcal.amazingchest;

import net.minecraft.util.Pair;
import net.pcal.amazingchest.AcReachabilityCache.ReachableInventory;
import net.pcal.amazingchest.AcReachabilityCache.TransferDisposition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.pcal.amazingchest.AcReachabilityCacheTest.MockHopper.hoppers;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"SpellCheckingInspection", "ClassCanBeRecord"})
public class AcReachabilityCacheTest {

    final Logger LOGGER = LogManager.getLogger(AcReachabilityCacheTest.class);

    /**
     * Test a trivial chain of 2 hoppers leading into an amazing chest.
     */
    @Test
    public void testSimpleChain() {
        final AcReachabilityCache<MockHopper, MockItem> traverser =
                new AcReachabilityCache<>(MockReachabilityDelegate.INSTANCE, LOGGER);
        MockChest c1 = new MockChest("c1");
        MockHopper h2 = new MockHopper("h2", null, c1);
        MockHopper h1 = new MockHopper("h1", hoppers(h2), null);
        List<ReachableInventory<MockItem>> chests = traverser.getReachableChests(h1);
        assertEquals(List.of(c1), chests);
    }

    /**
     * Test a chain that branches.
     */
    @Test
    public void testBranchingChain() {
        final AcReachabilityCache<MockHopper, MockItem> traverser =
                new AcReachabilityCache<>(MockReachabilityDelegate.INSTANCE, LOGGER);
        MockChest c1a = new MockChest("c1a");
        MockChest c1b = new MockChest("c1b");
        MockHopper h3a = new MockHopper("h3a", null, c1a);
        MockHopper h3b = new MockHopper("h3b", null, c1b);
        MockHopper h2 = new MockHopper("h2", hoppers(h3a, h3b), null);
        MockHopper h1 = new MockHopper("h1", hoppers(h2), null);
        assertEquals(Set.copyOf(traverser.getReachableChests(h1)), Set.of(c1a, c1b));
        assertEquals(Set.copyOf(traverser.getReachableChests(h2)), Set.of(c1a, c1b));
        assertEquals(Set.copyOf(traverser.getReachableChests(h3a)), Set.of(c1a));
        assertEquals(Set.copyOf(traverser.getReachableChests(h3b)), Set.of(c1b));
    }


    /**
     * Test two hoppers pointing at each other.
     */
    @Test
    public void testHopperClock() {
        final AcReachabilityCache<MockHopper, MockItem> traverser =
                new AcReachabilityCache<>(MockReachabilityDelegate.INSTANCE, LOGGER);
        MockHopper h2 = new MockHopper("h2", null, null);
        MockHopper h1 = new MockHopper("h1", hoppers(h2), null);
        h2.outboundHoppers = new MockHopper[] {h1};
        assertEquals(Collections.emptyList(), traverser.getReachableChests(h1));
        assertEquals(Collections.emptyList(), traverser.getReachableChests(h2));
    }


    /**
     * Test two hoppers pointing at each other.
     */
    @Test
    public void testLoop() {
        final AcReachabilityCache<MockHopper, MockItem> traverser =
                new AcReachabilityCache<>(MockReachabilityDelegate.INSTANCE, LOGGER);
        MockChest c1 = new MockChest("c1");
        MockChest c2 = new MockChest("c2");
        MockHopper h4 = new MockHopper("h4", null, c1);
        MockHopper h3 = new MockHopper("h3", hoppers(h4), null);
        MockHopper h2 = new MockHopper("h2", hoppers(h3), c2);
        MockHopper h1 = new MockHopper("h1", hoppers(h2), null);
        h4.outboundHoppers = new MockHopper[] {h1};

        assertEquals(Set.of(c1, c2), Set.copyOf(traverser.getReachableChests(h1)));
        assertEquals(Set.of(c1, c2), Set.copyOf(traverser.getReachableChests(h2)));
        // the traversal is actually broken in the case of a cycle:
        assertEquals(Set.of(c1), Set.copyOf(traverser.getReachableChests(h3)));
        assertEquals(Set.of(c1), Set.copyOf(traverser.getReachableChests(h4)));
    }

    public enum MockReachabilityDelegate implements AcReachabilityCache.ReachabilityDelegate<MockHopper, MockItem> {
        INSTANCE;

        @Override
        public Pair<MockHopper[], ReachableInventory<MockItem>> getOutboundConnections(MockHopper fromHopper) {
            return new Pair<>(fromHopper.outboundHoppers, fromHopper.outboundChest);
        }
    }

    static class MockHopper {

        static MockHopper[] hoppers(MockHopper... hoppers) {
            return hoppers;
        }

        final String name;
        MockHopper[] outboundHoppers;
        MockChest outboundChest;

        MockHopper(String name, MockHopper[] outboundHoppers, MockChest outboundChest) {
            this.name = name;
            this.outboundHoppers = outboundHoppers;
            this.outboundChest = outboundChest;
        }
        @Override
        public String toString() {
            return this.name;
        }
    }

    static class MockChest implements ReachableInventory<MockItem> {
        static MockChest[] chests(MockChest... chests) {
            return chests;
        }

        private final String name;
        MockChest(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public TransferDisposition getDispositionToward(MockItem mockItem) {
            return TransferDisposition.ACCEPT; //FIXME
        }
    }

    static class MockItem {

    }
}
