package net.pcal.amazingchest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pcal.amazingchest.network.LockPacket;

import static net.pcal.amazingchest.AcIdentifiers.MOD_ID;

public class AcScreen extends GenericContainerScreen {

    public static final Identifier texture = new Identifier(MOD_ID, "textures/gui/button.png");
//    private static final Identifier texture = new Identifier("minecraft", "textures/gui/icons.png");

    public AcScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private static final int BUTTON_WIDTH = 20;
    @Override
    protected void init() {
        super.init();
        MinecraftClient.getInstance().getTextureManager().bindTexture(AcScreen.texture);
        SortButtonWidget button = new SortButtonWidget(this.x + this.backgroundWidth - BUTTON_WIDTH, this.y + 6);
        this.addDrawableChild(button);
    }

    private class LockButton extends ToggleButtonWidget {

        LockButton(int x, int y) {
            super(x, y, 20, 18, true);
        }

        @Override
        public void onClick(double x, double y) {
            super.onClick(x,y);
            super.setToggled(!super.isToggled());
            boolean newLockState = ((AcScreenHandler)AcScreen.this.getScreenHandler()).toggleLocked();
            System.out.println("CLICK!   "+isToggled());
            LockPacket.sendLockPacket(isToggled());

        }
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            // Render the button
            super.render(matrices, mouseX, mouseY, delta);

            // Find and render the icon
            client.getTextureManager().bindTexture(texture);

            int iconOffset = 0;
            int iconX = x + iconOffset;
            int iconY = y + iconOffset;
            int iconU = 0;
            int iconV = 0;
            drawTexture(matrices, iconX, iconY, iconU, iconV, 20, 18);
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int x, int y) {
            if (this.isHovered()) {
                //boolean isLocked = ((AcScreenHandler)AcScreen.this.getScreenHandler()).isLocked();
                boolean isLocked = super.isToggled();
                String text = isLocked ? "Click to unlock" : "Click to lock";
                MinecraftClient.getInstance().currentScreen.
                        renderTooltip(matrices, new LiteralText(text), x, y);
            }
        }
    }
}
