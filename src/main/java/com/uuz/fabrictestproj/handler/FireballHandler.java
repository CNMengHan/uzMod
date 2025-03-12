package com.uuz.fabrictestproj.handler;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.enchantment.FireballEnchantment;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 火球术附魔处理器
 */
public class FireballHandler {
    
    // 冷却时间（ticks）
    private static final int COOLDOWN_TICKS = 20; // 1秒
    
    // 玩家冷却时间映射
    private static final Map<UUID, Long> playerCooldowns = new HashMap<>();
    
    /**
     * 注册火球术处理器
     */
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient) {
                ItemStack stack = player.getStackInHand(hand);
                int level = EnchantmentHelper.getLevel(FireballEnchantment.getInstance(), stack);
                
                if (level > 0) {
                    if (isOnCooldown(player)) {
                        return TypedActionResult.pass(stack);
                    }
                    
                    fireFireball(player, level);
                    setCooldown(player);
                    
                    // 播放音效
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                        1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    
                    return TypedActionResult.success(stack);
                }
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
        
        UuzFabricTestProj.LOGGER.info("火球术处理器已注册");
    }
    
    /**
     * 发射火球
     */
    private static void fireFireball(PlayerEntity player, int level) {
        World world = player.getWorld();
        Vec3d rotation = player.getRotationVec(1.0F);
        Vec3d pos = player.getEyePos().add(rotation.multiply(0.5));
        
        // 创建小火球实体
        FireballEntity fireball = new FireballEntity(
            EntityType.FIREBALL,
            world
        );
        
        // 设置火球的位置和速度
        fireball.setPosition(pos.x, pos.y, pos.z);
        fireball.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 1.5F, 1.0F);
        
        // 设置火球的属性
        fireball.setOwner(player);
        
        // 使用反射设置爆炸威力
        try {
            java.lang.reflect.Field explosionPowerField = FireballEntity.class.getDeclaredField("explosionPower");
            explosionPowerField.setAccessible(true);
            explosionPowerField.set(fireball, (int)(level * 0.5f));
        } catch (Exception e) {
            UuzFabricTestProj.LOGGER.error("设置火球爆炸威力失败", e);
        }
        
        world.spawnEntity(fireball);
    }
    
    /**
     * 检查玩家是否在冷却时间内
     */
    private static boolean isOnCooldown(PlayerEntity player) {
        UUID playerId = player.getUuid();
        if (!playerCooldowns.containsKey(playerId)) {
            return false;
        }
        
        long lastUsed = playerCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        // 检查是否已经过了冷却时间
        return (currentTime - lastUsed) < (COOLDOWN_TICKS * 50); // 50ms per tick
    }
    
    /**
     * 设置玩家的冷却时间
     */
    private static void setCooldown(PlayerEntity player) {
        playerCooldowns.put(player.getUuid(), System.currentTimeMillis());
    }
} 