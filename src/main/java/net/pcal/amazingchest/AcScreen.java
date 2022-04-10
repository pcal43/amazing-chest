package net.pcal.amazingchest;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static java.util.Objects.requireNonNull;
import static net.pcal.amazingchest.AcIdentifiers.MOD_ID;

/**
 * Like the regular chest screen but adds the 'last item locking' behavior.
 */
public class AcScreen extends GenericContainerScreen {

    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 18;

    public AcScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        LockButtonWidget button = new LockButtonWidget(this.x + this.backgroundWidth - (BUTTON_WIDTH * 2), this.y + 6);
        this.addDrawableChild(button);
    }

    @Environment(EnvType.CLIENT)
    public static class LockButtonWidget extends ToggleButtonWidget {

        private static final int TEXTURE_WIDTH = 20;
        private static final int TEXTURE_HEIGHT = 37;

        private static final Identifier texture = new Identifier(MOD_ID, "textures/gui/lock.png");
        private final MinecraftClient client;

        public LockButtonWidget(int x, int y) {
            super(x,y , BUTTON_WIDTH, BUTTON_HEIGHT, false);
            this.client = requireNonNull(MinecraftClient.getInstance());
            this.client.getTextureManager().bindTexture(texture);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            super.setToggled(!super.isToggled());
            AcLockPacket.sendLockPacket(isLocked());
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int int_1, int int_2, float float_1) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.enableDepthTest();
            matrixStack.push();
            matrixStack.scale(.5f, .5f, 1);
            matrixStack.translate(x, y, 0);
            drawTexture(matrixStack, this.x, this.y, 0, this.isLocked() ? 0 : 19, BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            matrixStack.pop();
            if (client.currentScreen != null && isHovered()) {
                final String tooltipText = "amazingchest.tooltip." + (this.isLocked() ? "locked" : "unlocked");
                MinecraftClient.getInstance().currentScreen.renderTooltip(matrixStack,  new TranslatableText(tooltipText), x, y);
            }
        }

        private boolean isLocked() {
            return !super.isToggled();
        }
    }

}
