package net.pcal.amazingchest;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static java.util.Objects.requireNonNull;

public abstract class AcIdentifiers {

    public static final String LOG_PREFIX = "[AmazingChest] ";
    public static final String MOD_ID = "amazingchest";
    public static final Identifier AC_TEXTURE = new Identifier(MOD_ID, "entity/chest/amazing_chest");
    public static final Identifier AC_TEXTURE_LEFT = new Identifier(MOD_ID, "entity/chest/amazing_chest_left");
    public static final Identifier AC_TEXTURE_RIGHT = new Identifier(MOD_ID, "entity/chest/amazing_chest_right");
    public static final Identifier AC_BLOCK_ENTITY_TYPE_ID = new Identifier(MOD_ID + ":amazing_chest_entity");
    public static final Identifier AC_ITEM_ID = new Identifier(MOD_ID + ":amazing_chest");
    public static final Identifier AC_BLOCK_ID = new Identifier(MOD_ID + ":amazing_chest");
    public static final Identifier AC_SINGLE_SCREEN_ID = new Identifier(MOD_ID + ":amazing_chest_single");
    public static final Identifier AC_DOUBLE_SCREEN_ID = new Identifier(MOD_ID + ":amazing_chest_double");

    public static AmazingChestBlock getAcBlock() {
        return (AmazingChestBlock) requireNonNull(Registry.BLOCK.get(AC_BLOCK_ID));
    }

    public static BlockEntityType<AmazingChestBlockEntity> getAcBlockEntityType() {
        //noinspection unchecked
        return (BlockEntityType<AmazingChestBlockEntity>)
                requireNonNull(Registry.BLOCK_ENTITY_TYPE.get(AC_BLOCK_ENTITY_TYPE_ID));
    }

    public static ScreenHandlerType<GenericContainerScreenHandler> getDoubleScreenHandlerType() {
        //noinspection unchecked
        return /*requireNonNull*/((ScreenHandlerType<GenericContainerScreenHandler>)
                Registry.SCREEN_HANDLER.get(AC_DOUBLE_SCREEN_ID));

    }
    public static ScreenHandlerType<GenericContainerScreenHandler> getSingleScreenHandlerType() {
        //noinspection unchecked
        return /*requireNonNull*/((ScreenHandlerType<GenericContainerScreenHandler>)
                Registry.SCREEN_HANDLER.get(AC_SINGLE_SCREEN_ID));
    }

}
