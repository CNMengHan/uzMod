package com.uuz.fabrictestproj.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

/**
 * 煎蛋卷物品
 * 食用后会回满饱食度，并且有三秒的生命回复效果（三秒内一共回复3颗心的生命值）
 */
public class CookieEggJuanItem extends Item {
    
    // 创建食物属性
    private static final FoodComponent FOOD_COMPONENT = new FoodComponent.Builder()
            .hunger(20) // 回满饱食度（20点）
            .saturationModifier(1.0f) // 最大饱和度
            .statusEffect(new StatusEffectInstance(
                    StatusEffects.REGENERATION, // 生命恢复效果
                    60, // 持续时间（3秒 = 60刻）
                    1), // 效果等级（2级，每秒回复1颗心）
                    1.0f) // 100%几率获得效果
            .alwaysEdible() // 即使不饿也能吃
            .build();
    
    public CookieEggJuanItem(Settings settings) {
        super(settings.food(FOOD_COMPONENT));
    }
} 