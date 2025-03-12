package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.manager.BoatFlyManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity {
    
    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // 如果有乘客且乘客是玩家
        if (!this.getPassengerList().isEmpty() && this.getFirstPassenger() instanceof ServerPlayerEntity player) {
            // 如果玩家启用了BoatFly
            if (BoatFlyManager.isEnabled(player.getUuid())) {
                // 取消重力影响
                this.setNoGravity(true);
                
                // 防止船在水中下沉
                if (this.isInLava() || this.isSubmergedInWater()) {
                    this.setVelocity(this.getVelocity().x, Math.max(0, this.getVelocity().y), this.getVelocity().z);
                }
            } else {
                // 如果未启用BoatFly，恢复正常重力
                this.setNoGravity(false);
            }
        } else {
            // 如果没有乘客或乘客不是玩家，恢复正常重力
            this.setNoGravity(false);
        }
    }
    
    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void onUpdateVelocity(CallbackInfo ci) {
        // 如果有乘客且乘客是玩家
        if (!this.getPassengerList().isEmpty() && this.getFirstPassenger() instanceof ServerPlayerEntity player) {
            // 如果玩家启用了BoatFly，取消原版的速度更新
            if (BoatFlyManager.isEnabled(player.getUuid())) {
                ci.cancel();
            }
        }
    }
    
    @Inject(method = "checkBoatInWater", at = @At("HEAD"), cancellable = true)
    private void onCheckBoatInWater(CallbackInfoReturnable<Boolean> cir) {
        // 如果有乘客且乘客是玩家
        if (!this.getPassengerList().isEmpty() && this.getFirstPassenger() instanceof ServerPlayerEntity player) {
            // 如果玩家启用了BoatFly，取消水中检查
            if (BoatFlyManager.isEnabled(player.getUuid())) {
                cir.setReturnValue(false); // 设置返回值为false，表示船不在水中
                cir.cancel();
            }
        }
    }
} 