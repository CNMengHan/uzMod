package com.uuz.fabrictestproj.manager;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerGiftManager {
    // 玩家需要盯着村民的时间（5秒 = 100 ticks）
    private static final int STARE_TIME_REQUIRED = 100;
    
    // 检查范围（玩家周围5格范围内的村民）
    private static final double CHECK_RANGE = 5.0;
    
    // 村民礼物冷却时间（5分钟 = 6000 ticks）
    private static final int GIFT_COOLDOWN = 6000;
    
    // 存储玩家盯着特定村民的时间
    private static final Map<UUID, VillagerStareInfo> playerStareInfo = new HashMap<>();
    
    // 存储村民上次给予礼物的时间
    private static final Map<UUID, Long> villagerGiftCooldowns = new HashMap<>();
    
    // 保存当前服务器实例的引用
    private static MinecraftServer currentServer;
    
    /**
     * 初始化村民礼物管理器
     */
    public static void initialize() {
        // 注册服务器tick事件
        ServerTickEvents.END_SERVER_TICK.register(VillagerGiftManager::onServerTick);
        UuzFabricTestProj.LOGGER.info("村民礼物系统已初始化");
    }
    
    /**
     * 服务器tick事件处理
     * @param server Minecraft服务器实例
     */
    private static void onServerTick(MinecraftServer server) {
        // 保存服务器实例的引用
        currentServer = server;
        
        // 检查每个在线玩家
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkPlayerStaringAtVillager(player);
        }
    }
    
    /**
     * 检查玩家是否在盯着村民看
     * @param player 要检查的玩家
     */
    private static void checkPlayerStaringAtVillager(ServerPlayerEntity player) {
        // 只有玩家在蹲着的时候才检查
        if (!player.isSneaking()) {
            // 如果玩家不再蹲着，清除之前的盯着信息
            playerStareInfo.remove(player.getUuid());
            return;
        }
        
        // 获取玩家视线方向
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d targetVec = eyePos.add(lookVec.multiply(CHECK_RANGE));
        
        // 创建一个射线检测的边界盒
        Box box = player.getBoundingBox().expand(CHECK_RANGE);
        
        // 获取玩家所在世界
        ServerWorld world = player.getServerWorld();
        
        // 查找玩家视线方向上的村民
        VillagerEntity targetVillager = null;
        double closestDistance = Double.MAX_VALUE;
        
        // 使用getEntitiesByType替代getEntitiesByClass
        for (VillagerEntity villager : world.getEntitiesByType(
                EntityType.VILLAGER, 
                box, 
                e -> true)) {
            
            // 计算村民到玩家视线的距离
            Vec3d villagerPos = villager.getPos().add(0, villager.getHeight() / 2, 0);
            double distance = calculateDistanceToLine(eyePos, targetVec, villagerPos);
            
            // 如果村民在玩家视线上，且是最近的村民
            if (distance < 0.5 && distance < closestDistance) {
                // 检查是否有直接视线（没有方块阻挡）
                // 使用canSee方法检查玩家是否能看到村民
                if (player.canSee(villager)) {
                    targetVillager = villager;
                    closestDistance = distance;
                }
            }
        }
        
        UUID playerId = player.getUuid();
        
        // 如果找到了目标村民
        if (targetVillager != null) {
            UUID villagerUuid = targetVillager.getUuid();
            
            // 获取或创建玩家盯着信息
            VillagerStareInfo stareInfo = playerStareInfo.computeIfAbsent(playerId,
                id -> new VillagerStareInfo(villagerUuid, 0));
            
            // 如果玩家盯着的是同一个村民，增加计时
            if (stareInfo.villagerUuid.equals(villagerUuid)) {
                stareInfo.stareTime++;
                
                // 如果达到所需时间，且村民不在冷却中，给予礼物
                if (stareInfo.stareTime >= STARE_TIME_REQUIRED && canVillagerGiveGift(villagerUuid)) {
                    giveEmeraldToPlayer(player, targetVillager);
                    stareInfo.stareTime = 0; // 重置计时
                    
                    // 设置村民礼物冷却时间
                    villagerGiftCooldowns.put(villagerUuid, System.currentTimeMillis() + (GIFT_COOLDOWN * 50L));
                }
            } else {
                // 如果玩家盯着的是不同的村民，更新信息
                stareInfo.villagerUuid = villagerUuid;
                stareInfo.stareTime = 0;
            }
        } else {
            // 如果没有找到目标村民，清除盯着信息
            playerStareInfo.remove(playerId);
        }
    }
    
    /**
     * 计算点到线段的距离
     * @param lineStart 线段起点
     * @param lineEnd 线段终点
     * @param point 点
     * @return 点到线段的距离
     */
    private static double calculateDistanceToLine(Vec3d lineStart, Vec3d lineEnd, Vec3d point) {
        Vec3d line = lineEnd.subtract(lineStart);
        double lineLength = line.length();
        
        if (lineLength == 0) {
            return point.distanceTo(lineStart);
        }
        
        // 计算点在线上的投影
        double t = Math.max(0, Math.min(1, point.subtract(lineStart).dotProduct(line) / (lineLength * lineLength)));
        Vec3d projection = lineStart.add(line.multiply(t));
        
        return point.distanceTo(projection);
    }
    
    /**
     * 检查村民是否可以给予礼物（不在冷却中）
     * @param villagerUuid 村民UUID
     * @return 如果村民可以给予礼物返回true，否则返回false
     */
    private static boolean canVillagerGiveGift(UUID villagerUuid) {
        if (!villagerGiftCooldowns.containsKey(villagerUuid)) {
            return true;
        }
        
        long lastGiftTime = villagerGiftCooldowns.get(villagerUuid);
        
        // 使用当前服务器实例
        if (currentServer == null) {
            // 如果无法获取服务器，使用时间差来判断
            return (System.currentTimeMillis() - lastGiftTime) >= (GIFT_COOLDOWN * 50); // 转换为毫秒
        }
        
        long currentTime = currentServer.getTicks();
        return (currentTime - lastGiftTime) >= GIFT_COOLDOWN;
    }
    
    /**
     * 给玩家一个绿宝石
     * @param player 接收绿宝石的玩家
     * @param villager 给予绿宝石的村民
     */
    private static void giveEmeraldToPlayer(ServerPlayerEntity player, VillagerEntity villager) {
        // 创建绿宝石物品堆
        ItemStack emerald = new ItemStack(Items.EMERALD);
        
        // 给玩家物品
        if (!player.giveItemStack(emerald)) {
            // 如果玩家背包满了，在玩家位置生成掉落物
            player.dropItem(emerald, false);
        }
        
        // 播放声音
        player.getWorld().playSound(
            null,
            villager.getX(),
            villager.getY(),
            villager.getZ(),
            SoundEvents.ENTITY_VILLAGER_YES,
            SoundCategory.NEUTRAL,
            1.0F,
            1.0F
        );
        
        // 生成粒子效果
        ((ServerWorld) villager.getWorld()).spawnParticles(
            ParticleTypes.HAPPY_VILLAGER,
            villager.getX(),
            villager.getY() + villager.getHeight(),
            villager.getZ(),
            10, // 粒子数量
            0.5, // X扩散
            0.5, // Y扩散
            0.5, // Z扩散
            0.1 // 速度
        );
        
        // 发送消息给玩家
        player.sendMessage(
            Text.literal("这个村民似乎很喜欢你，给了你一个绿宝石！").formatted(Formatting.GREEN),
            true
        );
        
        // 记录日志
        UuzFabricTestProj.LOGGER.info("村民给了玩家 {} 一个绿宝石", player.getName().getString());
    }
    
    /**
     * 玩家盯着村民的信息类
     */
    private static class VillagerStareInfo {
        UUID villagerUuid;
        int stareTime;
        
        VillagerStareInfo(UUID villagerUuid, int stareTime) {
            this.villagerUuid = villagerUuid;
            this.stareTime = stareTime;
        }
    }
} 