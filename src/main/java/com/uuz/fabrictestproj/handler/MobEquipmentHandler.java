package com.uuz.fabrictestproj.handler;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobEquipmentHandler {
    
    // 定义每种生物可以使用的武器列表
    private static final Map<Class<?>, List<ItemStack>> MOB_WEAPONS = new HashMap<>();
    
    static {
        // 僵尸系列可用武器
        List<ItemStack> zombieWeapons = new ArrayList<>();
        zombieWeapons.add(new ItemStack(Items.WOODEN_SWORD));
        zombieWeapons.add(new ItemStack(Items.STONE_SWORD));
        zombieWeapons.add(new ItemStack(Items.IRON_SWORD));
        zombieWeapons.add(new ItemStack(Items.DIAMOND_SWORD));
        zombieWeapons.add(new ItemStack(Items.NETHERITE_SWORD));
        MOB_WEAPONS.put(ZombieEntity.class, zombieWeapons);
        MOB_WEAPONS.put(DrownedEntity.class, zombieWeapons);
        
        // 骷髅系列可用武器
        List<ItemStack> skeletonWeapons = new ArrayList<>();
        skeletonWeapons.add(new ItemStack(Items.BOW));
        skeletonWeapons.add(new ItemStack(Items.BOW));  // 添加两次增加出现概率
        skeletonWeapons.add(new ItemStack(Items.CROSSBOW));
        MOB_WEAPONS.put(SkeletonEntity.class, skeletonWeapons);
        MOB_WEAPONS.put(StrayEntity.class, skeletonWeapons);
        
        // 凋零骷髅可用武器
        List<ItemStack> witherSkeletonWeapons = new ArrayList<>();
        witherSkeletonWeapons.add(new ItemStack(Items.STONE_SWORD));
        witherSkeletonWeapons.add(new ItemStack(Items.IRON_SWORD));
        witherSkeletonWeapons.add(new ItemStack(Items.DIAMOND_SWORD));
        MOB_WEAPONS.put(WitherSkeletonEntity.class, witherSkeletonWeapons);
        
        // 猪灵可用武器
        List<ItemStack> piglinWeapons = new ArrayList<>();
        piglinWeapons.add(new ItemStack(Items.GOLDEN_SWORD));
        piglinWeapons.add(new ItemStack(Items.CROSSBOW));
        MOB_WEAPONS.put(PiglinEntity.class, piglinWeapons);
    }
    
    /**
     * 注册事件处理器
     */
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof HostileEntity hostileEntity) {
                equipMobWithWeapon(hostileEntity);
            }
        });
    }
    
    /**
     * 为生物装备武器
     */
    private static void equipMobWithWeapon(HostileEntity entity) {
        // 检查生物是否已经有主手装备
        if (!entity.getMainHandStack().isEmpty()) {
            return;
        }
        
        // 获取该生物类型可用的武器列表
        List<ItemStack> availableWeapons = MOB_WEAPONS.get(entity.getClass());
        if (availableWeapons == null || availableWeapons.isEmpty()) {
            return;
        }
        
        // 随机选择一个武器
        Random random = entity.getRandom();
        ItemStack weapon = availableWeapons.get(random.nextInt(availableWeapons.size())).copy();
        
        // 添加随机附魔
        addRandomEnchantments(weapon, random);
        
        // 装备武器
        entity.equipStack(EquipmentSlot.MAINHAND, weapon);
        
        // 设置装备掉落概率
        entity.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.01f);
    }
    
    /**
     * 添加随机附魔
     */
    private static void addRandomEnchantments(ItemStack stack, Random random) {
        // 获取可用的附魔列表
        List<Enchantment> availableEnchantments = new ArrayList<>();
        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            if (enchantment.isAcceptableItem(stack)) {
                availableEnchantments.add(enchantment);
            }
        }
        
        if (availableEnchantments.isEmpty()) {
            return;
        }
        
        // 随机选择1-3个附魔
        int enchantCount = random.nextInt(3) + 1;
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        
        for (int i = 0; i < enchantCount; i++) {
            Enchantment enchantment = availableEnchantments.get(random.nextInt(availableEnchantments.size()));
            int level = random.nextInt(enchantment.getMaxLevel()) + 1;
            enchantments.put(enchantment, level);
        }
        
        // 应用附魔
        EnchantmentHelper.set(enchantments, stack);
    }
} 