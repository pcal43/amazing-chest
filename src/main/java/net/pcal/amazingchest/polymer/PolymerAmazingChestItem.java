package net.pcal.amazingchest.polymer;

import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerKeepModel;
import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pcal.amazingchest.AmazingChestItem;
import org.jetbrains.annotations.Nullable;

public class PolymerAmazingChestItem extends AmazingChestItem implements PolymerItem, PolymerKeepModel, PolymerClientDecoded {

    public PolymerAmazingChestItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.CHEST;
    }

}
