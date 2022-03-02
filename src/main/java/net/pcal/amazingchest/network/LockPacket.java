package net.pcal.amazingchest.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.pcal.amazingchest.AcScreenHandler;

import static net.pcal.amazingchest.AcIdentifiers.MOD_ID;

public class LockPacket {

    private static final Identifier SORT_INV_PACKET = new Identifier(MOD_ID, "lock_screen_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(SORT_INV_PACKET, ((server, player, handler, buf, responseSender) -> {
            boolean lock = buf.readBoolean();
            server.execute(() -> ((AcScreenHandler)player.currentScreenHandler).toggleLock());
        }));
    }

    @Environment(EnvType.CLIENT)
    public static void sendLockPacket(boolean lock) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(lock);
        ClientPlayNetworking.send(SORT_INV_PACKET, new PacketByteBuf(buf));
    }
}
