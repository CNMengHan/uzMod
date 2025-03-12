package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.handler.VillagerTradeHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {

    /**
     * 注入到村民繁殖方法中，使村民每次生两个小村民
     */
    @Inject(method = "createChild", at = @At("RETURN"))
    private void onCreateChild(ServerWorld world, PassiveEntity other, CallbackInfoReturnable<VillagerEntity> cir) {
        // 获取原始的小村民
        VillagerEntity originalChild = cir.getReturnValue();
        
        if (originalChild != null) {
            // 创建第二个小村民
            VillagerEntity secondChild = EntityType.VILLAGER.create(world);
            
            if (secondChild != null) {
                // 复制原始小村民的数据
                VillagerData villagerData = originalChild.getVillagerData();
                secondChild.setVillagerData(villagerData);
                
                // 设置位置和其他属性
                secondChild.refreshPositionAndAngles(
                    originalChild.getX(),
                    originalChild.getY(),
                    originalChild.getZ(),
                    0.0F,
                    0.0F
                );
                
                secondChild.setBaby(true);
                
                // 将第二个小村民添加到世界中
                world.spawnEntity(secondChild);
                
                // 记录日志
                UuzFabricTestProj.LOGGER.info("村民繁殖生成了双胞胎！");
            }
        }
    }
    
    /**
     * 注入到村民刷新交易方法中，添加随机交易
     */
    @Inject(method = "fillRecipes", at = @At("RETURN"))
    private void onFillRecipes(CallbackInfo ci) {
        // 获取当前村民实体
        VillagerEntity villager = (VillagerEntity)(Object)this;
        
        // 添加随机交易
        VillagerTradeHandler.addRandomTrades(villager);
    }
} 