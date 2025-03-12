package com.uuz.fabrictestproj.manager;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * 村民食物管理器
 * 每30分钟在已加载区块的所有村民位置生成32个面包和16个胡萝卜
 */
public class VillagerFoodManager {
    // 30分钟 = 36000 ticks (20 ticks/s * 60s * 30min)
    private static final int FOOD_SPAWN_INTERVAL = 36000;
    
    // 面包数量
    private static final int BREAD_COUNT = 32;
    
    // 胡萝卜数量
    private static final int CARROT_COUNT = 16;
    
    // 检测范围（方块）
    private static final int DETECTION_RANGE = 1000;
    
    // 当前计时器
    private static int tickCounter = 0;
    
    /**
     * 初始化村民食物管理器
     */
    public static void initialize() {
        // 注册服务器tick事件
        ServerTickEvents.END_SERVER_TICK.register(VillagerFoodManager::onServerTick);
        
        UuzFabricTestProj.LOGGER.info("村民食物系统已初始化");
    }
    
    /**
     * 服务器tick事件处理
     */
    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        
        // 每30分钟执行一次
        if (tickCounter >= FOOD_SPAWN_INTERVAL) {
            tickCounter = 0;
            // 自动生成食物时不广播消息
            spawnFoodForVillagers(server, false);
        }
    }
    
    /**
     * 为所有已加载区块的村民生成食物
     * @param server 服务器实例
     * @param shouldBroadcast 是否广播消息
     */
    private static void spawnFoodForVillagers(MinecraftServer server, boolean shouldBroadcast) {
        int villagerCount = 0;
        
        // 获取所有在线玩家
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        
        // 如果没有玩家在线，使用世界中心点
        if (players.isEmpty()) {
            // 遍历所有维度
            for (ServerWorld world : server.getWorlds()) {
                // 使用世界中心点
                Vec3d worldCenter = new Vec3d(0, 64, 0);
                villagerCount += spawnFoodForVillagersInRange(world, worldCenter, shouldBroadcast);
            }
        } else {
            // 对于每个在线玩家，检查其周围的村民
            for (ServerPlayerEntity player : players) {
                ServerWorld world = player.getServerWorld();
                Vec3d playerPos = player.getPos();
                villagerCount += spawnFoodForVillagersInRange(world, playerPos, shouldBroadcast);
            }
        }
        
        // 记录日志，但不广播消息
        if (villagerCount > 0) {
            // 只有在需要广播时才发送消息
            if (shouldBroadcast) {
                server.getPlayerManager().broadcast(
                    Text.literal("§a已为 " + villagerCount + " 个村民生成食物补给！"),
                    false
                );
            }
            UuzFabricTestProj.LOGGER.info("已为 " + villagerCount + " 个村民生成食物补给");
        }
    }
    
    /**
     * 在指定范围内为村民生成食物
     * @param world 世界
     * @param center 中心点
     * @param shouldBroadcast 是否广播消息
     * @return 生成食物的村民数量
     */
    private static int spawnFoodForVillagersInRange(ServerWorld world, Vec3d center, boolean shouldBroadcast) {
        int count = 0;
        
        // 获取指定范围内的所有村民
        List<VillagerEntity> villagers = world.getEntitiesByType(
            EntityType.VILLAGER,
            new Box(center.add(-DETECTION_RANGE, -DETECTION_RANGE, -DETECTION_RANGE), 
                   center.add(DETECTION_RANGE, DETECTION_RANGE, DETECTION_RANGE)),
            entity -> true
        );
        
        // 为每个村民生成食物
        for (VillagerEntity villager : villagers) {
            // 只处理已加载区块中的村民
            if (world.isChunkLoaded(villager.getChunkPos().x, villager.getChunkPos().z)) {
                spawnFoodAtVillager(world, villager);
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 手动触发为所有已加载区块的村民生成食物
     * @param server 服务器实例
     * @param shouldBroadcast 是否广播消息
     * @return 是否成功生成食物（至少有一个村民）
     */
    public static boolean spawnFoodForVillagersManually(MinecraftServer server, boolean shouldBroadcast) {
        int villagerCount = 0;
        
        // 获取所有在线玩家
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        
        // 如果没有玩家在线，使用世界中心点
        if (players.isEmpty()) {
            // 遍历所有维度
            for (ServerWorld world : server.getWorlds()) {
                // 使用世界中心点
                Vec3d worldCenter = new Vec3d(0, 64, 0);
                villagerCount += spawnFoodForVillagersInRange(world, worldCenter, shouldBroadcast);
            }
        } else {
            // 对于每个在线玩家，检查其周围的村民
            for (ServerPlayerEntity player : players) {
                ServerWorld world = player.getServerWorld();
                Vec3d playerPos = player.getPos();
                villagerCount += spawnFoodForVillagersInRange(world, playerPos, shouldBroadcast);
            }
        }
        
        // 记录日志
        if (villagerCount > 0) {
            // 只有在需要广播时才发送消息
            if (shouldBroadcast) {
                server.getPlayerManager().broadcast(
                    Text.literal("§a已为 " + villagerCount + " 个村民生成食物补给！"),
                    false
                );
            }
            UuzFabricTestProj.LOGGER.info("已手动为 " + villagerCount + " 个村民生成食物补给");
            return true;
        }
        
        return false;
    }
    
    /**
     * 在指定村民位置生成食物
     */
    private static void spawnFoodAtVillager(ServerWorld world, VillagerEntity villager) {
        Vec3d pos = villager.getPos();
        
        // 生成面包
        ItemStack breadStack = new ItemStack(Items.BREAD, BREAD_COUNT);
        ItemEntity breadEntity = new ItemEntity(world, pos.x, pos.y, pos.z, breadStack);
        world.spawnEntity(breadEntity);
        
        // 生成胡萝卜
        ItemStack carrotStack = new ItemStack(Items.CARROT, CARROT_COUNT);
        ItemEntity carrotEntity = new ItemEntity(world, pos.x, pos.y, pos.z, carrotStack);
        world.spawnEntity(carrotEntity);
    }
} 