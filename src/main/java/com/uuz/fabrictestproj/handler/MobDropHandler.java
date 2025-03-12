package com.uuz.fabrictestproj.handler;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.enchantment.FireballEnchantment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import java.util.Random;

/**
 * 生物掉落处理器
 * 处理骷髅和烈焰人掉落火球术附魔书的逻辑
 */
public class MobDropHandler {
    
    // 掉落几率（1/X）
    private static final int DROP_CHANCE_SKELETON = 50; // 骷髅掉落几率 1/50
    private static final int DROP_CHANCE_BLAZE = 20;    // 烈焰人掉落几率 1/20
    
    private static final Random random = new Random();
    
    /**
     * 注册生物掉落处理器
     */
    public static void register() {
        // 监听生物死亡事件
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // 只在服务器端处理
            if (entity.getWorld().isClient) {
                return;
            }
            
            // 处理骷髅掉落
            if (entity instanceof SkeletonEntity) {
                if (random.nextInt(DROP_CHANCE_SKELETON) == 0) {
                    dropFireballBook(entity);
                }
            }
            
            // 处理烈焰人掉落
            if (entity instanceof BlazeEntity) {
                if (random.nextInt(DROP_CHANCE_BLAZE) == 0) {
                    dropFireballBook(entity);
                }
            }
        });
    }
    
    /**
     * 掉落火球术附魔书
     */
    private static void dropFireballBook(LivingEntity entity) {
        FireballEnchantment fireball = FireballEnchantment.getInstance();
        
        // 随机选择附魔等级（1-10）
        int level = random.nextInt(fireball.getMaxLevel()) + 1;
        
        // 创建附魔书物品
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(enchantedBook, 
            new EnchantmentLevelEntry(fireball, level));
        
        // 生成掉落物
        entity.dropStack(enchantedBook);
    }
} 