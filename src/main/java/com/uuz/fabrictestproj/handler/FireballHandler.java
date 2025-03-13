package com.uuz.fabrictestproj.handler;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.enchantment.FireballEnchantment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

/**
 * 火球术附魔处理器
 */
public class FireballHandler {
    
    // 冷却时间（ticks）
    private static final int COOLDOWN_TICKS = 20; // 1秒
    private static final Map<UUID, Long> playerCooldowns = new HashMap<>();
    
    // 使用ConcurrentHashMap替代WeakHashMap，避免并发修改异常
    private static final Map<FireballEntity, Vec3d> fireballVelocities = new ConcurrentHashMap<>();
    
    /**
     * 注册火球术处理器
     */
    public static void register() {
        // 注册使用物品回调
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
        
        // 注册服务器tick事件，用于维持火球速度
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            // 使用迭代器安全地移除元素
            Iterator<Map.Entry<FireballEntity, Vec3d>> iterator = fireballVelocities.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<FireballEntity, Vec3d> entry = iterator.next();
                FireballEntity fireball = entry.getKey();
                Vec3d velocity = entry.getValue();
                
                // 如果火球已经不存在或已经被移除，则从映射中移除
                if (fireball == null || fireball.isRemoved()) {
                    iterator.remove();
                    continue;
                }
                
                // 重新设置火球的速度，确保匀速运动
                fireball.setVelocity(velocity.x, velocity.y, velocity.z);
                fireball.setNoGravity(true);
            }
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
        
        // 设置火球的位置
        fireball.setPosition(pos.x, pos.y, pos.z);
        
        // 计算火球的速度向量 - 使用玩家的视线方向
        double speed = 1.5; // 火球速度
        Vec3d velocity = rotation.multiply(speed);
        
        // 直接设置火球的速度向量
        fireball.setVelocity(velocity.x, velocity.y, velocity.z);
        
        // 设置火球不受重力影响
        fireball.setNoGravity(true);
        
        // 设置火球的属性
        fireball.setOwner(player);
        
        // 设置爆炸威力
        try {
            java.lang.reflect.Field explosionPowerField = FireballEntity.class.getDeclaredField("explosionPower");
            explosionPowerField.setAccessible(true);
            explosionPowerField.set(fireball, (int)(level * 0.5f));
        } catch (Exception e) {
            UuzFabricTestProj.LOGGER.error("设置火球爆炸威力失败", e);
        }
        
        // 将火球添加到世界中
        world.spawnEntity(fireball);
        
        // 将火球和其速度添加到跟踪映射中
        fireballVelocities.put(fireball, velocity);
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