package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity {
    
    @Shadow private int currentFuseTime;
    @Shadow private int fuseTime;
    
    protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    /**
     * 修改苦力怕的属性，增加移动速度
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityType<? extends CreeperEntity> entityType, World world, CallbackInfo ci) {
        // 增加移动速度50%
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
            .setBaseValue(this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 1.5);
        
        // 设置爆炸延时为1tick（几乎瞬间）
        this.fuseTime = 1;
        
        UuzFabricTestProj.LOGGER.debug("苦力怕移动速度已提升50%，爆炸延时已设为瞬间");
    }
    
    /**
     * 当苦力怕靠近玩家时，立即设置为最大引爆时间，实现瞬间爆炸
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            // 获取最近的玩家
            PlayerEntity player = this.getWorld().getClosestPlayer(this, 3.0); // 3格范围内
            
            if (player != null) {
                // 如果玩家在范围内，立即设置为最大引爆时间
                this.currentFuseTime = this.fuseTime;
            }
        }
    }
} 