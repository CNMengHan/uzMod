package com.uuz.fabrictestproj.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    
    protected LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    
    /**
     * 注入到实体死亡方法，处理任务进度
     */
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        // 检查是否是玩家击杀
        if (source.getAttacker() instanceof ServerPlayerEntity) {
            // 这里可以添加玩家击杀生物的奖励逻辑
        }
    }

    /**
     * 当生物受到伤害时，如果是蜘蛛则生成一只毒蜘蛛
     */
    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // 只在服务器端执行，且只有当伤害成功应用时
        if (this.getWorld().isClient || !cir.getReturnValue()) return;
        
        // 检查是否为蜘蛛
        if ((Object)this instanceof SpiderEntity && this.getType() == EntityType.SPIDER) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            CaveSpiderEntity caveSpider = EntityType.CAVE_SPIDER.create(serverWorld);
            
            if (caveSpider != null) {
                // 在蜘蛛附近随机位置生成毒蜘蛛
                double offsetX = this.random.nextDouble() * 2.0 - 1.0;
                double offsetZ = this.random.nextDouble() * 2.0 - 1.0;
                
                caveSpider.refreshPositionAndAngles(
                    this.getX() + offsetX,
                    this.getY(),
                    this.getZ() + offsetZ,
                    this.random.nextFloat() * 360.0F,
                    0.0F
                );
                
                caveSpider.initialize(serverWorld, serverWorld.getLocalDifficulty(caveSpider.getBlockPos()), SpawnReason.REINFORCEMENT, null, null);
                
                // 将毒蜘蛛添加到世界中
                serverWorld.spawnEntity(caveSpider);
            }
        }
    }
} 