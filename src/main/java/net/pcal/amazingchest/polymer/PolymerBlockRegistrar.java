package net.pcal.amazingchest.polymer;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import net.pcal.amazingchest.AcIdentifiers;
import net.pcal.amazingchest.AmazingChestItem;

import static net.pcal.amazingchest.AcIdentifiers.AC_BLOCK_ENTITY_TYPE_ID;
import static net.pcal.amazingchest.AcIdentifiers.AC_BLOCK_ID;
import static net.pcal.amazingchest.AcIdentifiers.AC_ITEM_ID;

@SuppressWarnings("unused")
public class PolymerBlockRegistrar implements Runnable {

    @Override
    public void run() {
        PolymerRPUtils.addAssetSource("amazingchest");
        final PolymerAmazingChestBlock acBlock = new PolymerAmazingChestBlock();
        final AmazingChestItem acItem = new PolymerAmazingChestItem(acBlock, new Item.Settings().group(ItemGroup.REDSTONE));
        acItem.appendBlocks(Item.BLOCK_ITEMS, acItem); // wat
        Registry.register(Registry.ITEM, AC_ITEM_ID, acItem);
        Registry.register(Registry.BLOCK, AC_BLOCK_ID, acBlock);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, AC_BLOCK_ENTITY_TYPE_ID,
                FabricBlockEntityTypeBuilder.create(PolymerAmazingChestBlockEntity::new, acBlock).build(null));
        PolymerBlockUtils.registerBlockEntity(AcIdentifiers.getAcBlockEntityType());
    }
}
