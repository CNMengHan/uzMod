package com.uuz.fabrictestproj.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.uuz.fabrictestproj.UuzFabricTestProj;

/**
 * 火球术附魔
 * 允许玩家右键发射火球
 */
public class FireballEnchantment extends Enchantment {
    
    public FireballEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[] {
            EquipmentSlot.MAINHAND
        });
    }
    
    @Override
    public int getMinPower(int level) {
        return 15 + (level - 1) * 5;
    }
    
    @Override
    public int getMaxPower(int level) {
        return getMinPower(level) + 50;
    }
    
    @Override
    public int getMaxLevel() {
        return 10; // 最高10级
    }
    
    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }
    
    @Override
    public boolean isAvailableForRandomSelection() {
        return true;
    }
    
    /**
     * 检查物品是否可以应用此附魔
     */
    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || 
               stack.getItem() instanceof AxeItem || 
               stack.getItem() instanceof TridentItem ||
               stack.getItem() instanceof ToolItem;
    }
    
    /**
     * 获取附魔的显示名称
     */
    @Override
    public String getTranslationKey() {
        return "enchantment.uuzfabrictestproj.fireball";
    }
    
    /**
     * 获取基于等级的爆炸威力
     */
    public static float getExplosionPower(int level) {
        return level * 0.5f;
    }

    private static FireballEnchantment INSTANCE;

    public static FireballEnchantment getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FireballEnchantment();
        }
        return INSTANCE;
    }

    public static void register() {
        INSTANCE = new FireballEnchantment();
        Registry.register(Registries.ENCHANTMENT, 
            new Identifier(UuzFabricTestProj.MOD_ID, "fireball"), 
            INSTANCE);
    }
} 