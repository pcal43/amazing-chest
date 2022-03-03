package net.pcal.amazingchest;


import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import static net.pcal.amazingchest.AcIdentifiers.MOD_ID;


import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class SortButtonWidget extends ToggleButtonWidget {
    protected static final int BUTTON_SIZE = 20;
    protected static final int ICON_SIZE = 20;
    protected final MinecraftClient client;
    private static final Identifier texture = new Identifier(MOD_ID, "textures/gui/button.png");

    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 18;

    protected SortButtonWidget(int x, int y) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, true);
        setTextureUV(49, 30, 26, 0, texture);
        this.client = MinecraftClient.getInstance();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Render the button
        super.renderButton(matrices, mouseX, mouseY, delta);

        // Find and render the icon
        client.getTextureManager().bindTexture(texture);

        int iconOffset = (BUTTON_SIZE / 2) - (ICON_SIZE / 2);
        int iconX = x + iconOffset;
        int iconY = y + iconOffset;
        int iconU = 0;
        int iconV = 0;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableDepthTest();
        matrices.push();
        matrices.scale(.5f, .5f, 1);
        matrices.translate(x, y, 0);
        drawTexture(matrices, this.x, this.y, 0, this.isHovered() ? 19 : 0, 20, 18, 20, 37);

//        drawTexture(matrices, iconX, iconY, iconU, iconV, ICON_SIZE, ICON_SIZE);

//        // Draw power tooltip
//        if (client.currentScreen != null && isHovered()) {
//            String tooltipText = "power." + power.getName();
//
//            if (power.isToggleable()) {
//                tooltipText += power.isToggled() ? ".enabled" : ".disabled";
//            }
//
//            tooltipText += ".name";
//            client.currentScreen.renderTooltip(matrices, new TranslatableText(tooltipText), mouseX, mouseY);
//        }
    }

//    protected void onPress(PlayerEntity player) {
//        setToggled(!isToggled() && power.isToggleable());
//
//        PacketByteBuf buf = PacketByteBufs.create();
//        buf.writeEnumConstant(power.getPowerEnum());
//        power.use(buf, player, isToggled());
//        ClientPlayNetworking.send(ServerPacketRegistry.EXECUTE_POWER, buf);
//    }
}
