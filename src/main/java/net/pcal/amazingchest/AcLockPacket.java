package net.pcal.amazingchest;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static net.pcal.amazingchest.AcIdentifiers.MOD_ID;

/**
 * Packet we send when the lock button is clicked.
 */
public class AcLockPacket {

    private static final Identifier SORT_INV_PACKET = new Identifier(MOD_ID, "ac_lock_screen_packet");

    static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(SORT_INV_PACKET, ((server, player, handler, buf, responseSender) -> {
            boolean locked = buf.readBoolean();
            server.execute(() -> ((AcScreenHandler)player.currentScreenHandler).setLocked(locked));
        }));
    }

    @Environment(EnvType.CLIENT)
    public static void sendLockPacket(boolean lock) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(lock);
        ClientPlayNetworking.send(SORT_INV_PACKET, new PacketByteBuf(buf));
    }
}
