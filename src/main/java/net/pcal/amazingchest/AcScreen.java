package net.pcal.amazingchest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pcal.amazingchest.network.LockPacket;

import static net.pcal.amazingchest.AcIdentifiers.MOD_ID;

public class AcScreen extends GenericContainerScreen {

    private static final Identifier texture = new Identifier(MOD_ID, "textures/button.png");

    public AcScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        super.addDrawableChild(new LockButton(this.x + 20, this.y +20));
    }

    private class LockButton extends TexturedButtonWidget {

        LockButton(int x, int y) {
            super(x, y, 16, 16, 0, 0, 19, texture, 20, 37, null, new LiteralText(""));
        }

        @Override
        public void onPress() {
            boolean newLockState = ((AcScreenHandler)AcScreen.this.getScreenHandler()).toggleLock();
            System.out.println("CLICK!   "+newLockState);
            LockPacket.sendLockPacket(newLockState);
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int x, int y) {
            if (this.isHovered()) {
                boolean isLocked = ((AcScreenHandler)AcScreen.this.getScreenHandler()).isLocked();
                String text = isLocked ? "Click to unlock" : "Click to lock";
                MinecraftClient.getInstance().currentScreen.
                        renderTooltip(matrices, new LiteralText("LOCK UNLOCK"), x, y);
            }
        }
    }
}
