package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.network.BoatFlyInputPacket;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    
    private boolean lastJumping = false;
    private boolean lastSneaking = false;
    private boolean lastForward = false;
    private int tickCounter = 0;
    
    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        
        // 只有当玩家在船上时才发送输入
        if (player.hasVehicle() && player.getVehicle() instanceof BoatEntity) {
            boolean jumping = player.input.jumping;
            boolean sneaking = player.input.sneaking;
            boolean forward = player.input.movementForward > 0;
            
            // 每5个tick强制发送一次状态，确保服务器能收到
            tickCounter++;
            boolean shouldSendUpdate = jumping != lastJumping || sneaking != lastSneaking || forward != lastForward || tickCounter >= 5;
            
            if (shouldSendUpdate) {
                BoatFlyInputPacket.send(jumping, sneaking, forward);
                
                lastJumping = jumping;
                lastSneaking = sneaking;
                lastForward = forward;
                tickCounter = 0;
            }
        } else {
            // 如果玩家不在船上，重置状态
            if (lastJumping || lastSneaking || lastForward) {
                lastJumping = false;
                lastSneaking = false;
                lastForward = false;
                BoatFlyInputPacket.send(false, false, false);
            }
        }
    }
} 