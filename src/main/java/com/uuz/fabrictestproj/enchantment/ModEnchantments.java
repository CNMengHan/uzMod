package com.uuz.fabrictestproj.enchantment;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 模组附魔注册类
 */
public class ModEnchantments {
    
    // 火球术附魔
    public static final FireballEnchantment FIREBALL = new FireballEnchantment();
    
    /**
     * 注册所有附魔
     */
    public static void registerEnchantments() {
        Registry.register(
            Registries.ENCHANTMENT,
            new Identifier(UuzFabricTestProj.MOD_ID, "fireball"),
            FIREBALL
        );
        
        UuzFabricTestProj.LOGGER.info("火球术附魔已注册");
    }
} 