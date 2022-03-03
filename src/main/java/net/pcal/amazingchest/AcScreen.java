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
import net.minecraft.util.Identifier;
import net.pcal.amazingchest.network.LockPacket;

import static net.pcal.amazingchest.AcIdentifiers.MOD_ID;

public class AcScreen extends GenericContainerScreen {

    public static final Identifier texture = new Identifier(MOD_ID, "textures/gui/button.png");

    public AcScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private static final int BUTTON_WIDTH = 20;
    @Override
    protected void init() {
        super.init();
        MinecraftClient.getInstance().getTextureManager().bindTexture(AcScreen.texture);
        LockButtonWidget button = new LockButtonWidget(this.x + this.backgroundWidth - (BUTTON_WIDTH * 2 + 8), this.y + 6);
        this.addDrawableChild(button);
    }

    @Environment(EnvType.CLIENT)
    public static class LockButtonWidget extends ToggleButtonWidget {
        private static final Identifier texture = new Identifier(MOD_ID, "textures/gui/button.png");

        public LockButtonWidget(int x, int y) {
            super(x,y , 20, 18, false);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            System.out.println("push!");
            super.setToggled(!super.isToggled());
            LockPacket.sendLockPacket(isToggled());
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int int_1, int int_2, float float_1) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.enableDepthTest();
            matrixStack.push();
            matrixStack.scale(.5f, .5f, 1);
            matrixStack.translate(x, y, 0);
            drawTexture(matrixStack, this.x, this.y, 0, this.isToggled() ? 19 : 0, 20, 18, 20, 37);
            //this.renderTooltip(matrixStack, int_1, int_2);
            matrixStack.pop();
        }
/**
 @Override
 public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
 int current = InventorySorterModClient.getConfig().sortType.ordinal();
 if (amount > 0) {
 current++;
 if (current >= SortCases.SortType.values().length)
 current = 0;
 } else {
 current--;
 if (current < 0)
 current = SortCases.SortType.values().length - 1;
 }
 InventorySorterModClient.getConfig().sortType = SortCases.SortType.values()[current];
 InventorySorterMod.configManager.save();
 InventorySorterModClient.syncConfig();
 return true;
 }

 @Override
 public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
 if (InventorySorterModClient.getConfig().displayTooltip && this.isHovered())
 MinecraftClient.getInstance().currentScreen.renderTooltip(matrices, new LiteralText("Sort by: " + StringUtils.capitalize(InventorySorterModClient.getConfig().sortType.toString().toLowerCase())), x, y);
 }
 */
    }

}
