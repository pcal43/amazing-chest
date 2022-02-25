package net.pcal.amazingchest.mixins;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.pcal.amazingchest.AcIdentifiers;
import net.pcal.amazingchest.AmazingChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.render.TexturedRenderLayers.CHEST_ATLAS_TEXTURE;

@Mixin(TexturedRenderLayers.class)
public class AcTextureRenderLayersMixin {

    private static final SpriteIdentifier AC_SPRITE = new SpriteIdentifier(CHEST_ATLAS_TEXTURE, AcIdentifiers.AC_TEXTURE);
    private static final SpriteIdentifier AC_SPRITE_LEFT = new SpriteIdentifier(CHEST_ATLAS_TEXTURE, AcIdentifiers.AC_TEXTURE_LEFT);
    private static final SpriteIdentifier AC_SPRITE_RIGHT = new SpriteIdentifier(CHEST_ATLAS_TEXTURE, AcIdentifiers.AC_TEXTURE_RIGHT);

    @Inject(method = "getChestTexture(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/block/enums/ChestType;Z)Lnet/minecraft/client/util/SpriteIdentifier;",
            at = @At("HEAD"),
            cancellable = true)
    private static void __getChestTexture  (BlockEntity blockEntity, ChestType type, boolean christmas, CallbackInfoReturnable<SpriteIdentifier> returnable) {
        if (blockEntity instanceof AmazingChestBlockEntity) {
            switch (type) {
                case SINGLE -> returnable.setReturnValue(AC_SPRITE);
                case LEFT -> returnable.setReturnValue(AC_SPRITE_LEFT);
                case RIGHT -> returnable.setReturnValue(AC_SPRITE_RIGHT);
                default -> throw new IllegalStateException();
            }
        }
    }

}
