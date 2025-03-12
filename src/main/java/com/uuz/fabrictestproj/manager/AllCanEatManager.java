package com.uuz.fabrictestproj.manager;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import java.util.Random;

public class AllCanEatManager {
    private static boolean enabled = false;
    private static final Random random = new Random();
    private static final int USE_TIME = 32; // 标准食用时间

    public static void initialize() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!enabled || world.isClient) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            ItemStack stack = player.getStackInHand(hand);
            
            // 如果物品本身就是食物，使用原有行为
            if (stack.getItem().isFood()) {
                return TypedActionResult.pass(stack);
            }

            // 设置使用动作和时间
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        });
    }

    public static UseAction getUseAction(ItemStack stack) {
        if (!enabled || stack.getItem().isFood()) {
            return UseAction.NONE;
        }
        return UseAction.EAT;
    }

    public static int getMaxUseTime(ItemStack stack) {
        if (!enabled || stack.getItem().isFood()) {
            return 0;
        }
        return USE_TIME;
    }

    public static void onItemUseComplete(PlayerEntity player, ItemStack stack) {
        if (!enabled || player.getWorld().isClient || stack.getItem().isFood()) {
            return;
        }

        // 生成随机的饱食度和生命值恢复
        int hunger = random.nextInt(6) + 1; // 1-6点饱食度
        float saturationModifier = random.nextFloat() * 0.8f + 0.2f; // 0.2-1.0饱和度修饰符
        
        // 30%概率恢复生命值
        if (random.nextFloat() < 0.3f) {
            int healing = random.nextInt(3) + 1; // 1-3点生命值
            player.heal(healing);
        }

        // 应用饱食度效果
        player.getHungerManager().add(hunger, saturationModifier);

        // 播放吃东西的音效
        World world = player.getWorld();
        world.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.ENTITY_PLAYER_BURP,
            SoundCategory.PLAYERS,
            0.5F,
            world.random.nextFloat() * 0.1F + 0.9F
        );

        // 减少物品数量
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }
} 