package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.client.ExplosionArrowManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {
    
    @Inject(method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", at = @At("HEAD"))
    private void onEntityHitHead(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object)this;
        Entity owner = arrow.getOwner();
        Entity target = entityHitResult.getEntity();
        
        // 检查是否是骷髅射出的箭
        if (owner instanceof AbstractSkeletonEntity && target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity)target;
            
            // 添加剧毒效果（5分钟，等级2）
            livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 6000, 1));
            
            // 移除调试信息
            // System.out.println("Applied poison effect to " + target.getName().getString());
        }
    }
    
    @Inject(method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", at = @At("TAIL"))
    private void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (ExplosionArrowManager.isEnabled()) {
            Entity entity = ((PersistentProjectileEntity)(Object)this);
            World world = entity.getWorld();
            if (!world.isClient) {
                world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), 
                    ExplosionArrowManager.EXPLOSION_POWER, true, World.ExplosionSourceType.MOB);
                entity.discard();
            }
        }
    }

    @Inject(method = "onBlockHit(Lnet/minecraft/util/hit/BlockHitResult;)V", at = @At("TAIL"))
    private void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (ExplosionArrowManager.isEnabled()) {
            Entity entity = ((PersistentProjectileEntity)(Object)this);
            World world = entity.getWorld();
            if (!world.isClient) {
                world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), 
                    ExplosionArrowManager.EXPLOSION_POWER, true, World.ExplosionSourceType.MOB);
                entity.discard();
            }
        }
    }
} 