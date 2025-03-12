package com.uuz.fabrictestproj.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatEntity.class)
public abstract class BatEntityMixin extends AmbientEntity {
    
    // 添加一个标志，表示是否已经尝试生成骷髅
    private boolean hasTriedToSpawnSkeleton = false;
    
    protected BatEntityMixin(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // 只在服务器端执行，并且只尝试一次生成骷髅
        if (this.getWorld().isClient || hasTriedToSpawnSkeleton) return;
        
        // 标记为已尝试生成骷髅
        hasTriedToSpawnSkeleton = true;
        
        // 如果蝙蝠没有乘客，则生成一个骷髅骑在上面
        if (this.getPassengerList().isEmpty() && !this.isBaby()) {
            try {
                ServerWorld serverWorld = (ServerWorld) this.getWorld();
                SkeletonEntity skeleton = EntityType.SKELETON.create(serverWorld);
                
                if (skeleton != null) {
                    // 先将骷髅添加到世界中，确保它完全初始化
                    skeleton.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
                    skeleton.initialize(serverWorld, serverWorld.getLocalDifficulty(skeleton.getBlockPos()), SpawnReason.JOCKEY, null, null);
                    serverWorld.spawnEntity(skeleton);
                    
                    // 设置骷髅不会消失
                    skeleton.setPersistent();
                    
                    // 让骷髅骑在蝙蝠上
                    skeleton.startRiding(this, true);
                }
            } catch (Exception e) {
                // 如果生成过程中出现异常，记录日志但不中断游戏
                System.out.println("Failed to spawn skeleton on bat: " + e.getMessage());
            }
        }
    }
} 