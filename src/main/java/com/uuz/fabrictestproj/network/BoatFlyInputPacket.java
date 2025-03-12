package com.uuz.fabrictestproj.network;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.manager.BoatFlyManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class BoatFlyInputPacket {
    private static final Identifier PACKET_ID = new Identifier(UuzFabricTestProj.MOD_ID, "boat_fly_input");
    
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, (server, player, handler, buf, responseSender) -> {
            boolean jumping = buf.readBoolean();
            boolean sneaking = buf.readBoolean();
            boolean forward = buf.readBoolean();
            
            server.execute(() -> {
                handlePacket(player, jumping, sneaking, forward);
            });
        });
    }
    
    private static void handlePacket(ServerPlayerEntity player, boolean jumping, boolean sneaking, boolean forward) {
        // 检查玩家是否启用了BoatFly
        if (BoatFlyManager.isEnabled(player.getUuid())) {
            BoatFlyManager.setPlayerJumping(player.getUuid(), jumping);
            BoatFlyManager.setPlayerSneaking(player.getUuid(), sneaking);
            BoatFlyManager.setPlayerForward(player.getUuid(), forward);
        }
    }
    
    public static void send(boolean jumping, boolean sneaking, boolean forward) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(jumping);
        buf.writeBoolean(sneaking);
        buf.writeBoolean(forward);
        
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
} 