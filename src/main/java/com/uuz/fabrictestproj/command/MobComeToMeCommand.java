package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobComeToMeCommand {
    // 使用ConcurrentHashMap避免并发修改异常
    private static final Map<UUID, MobTargetInfo> movingMobs = new ConcurrentHashMap<>();
    // 存储当前活跃的生物类型
    private static final Map<String, EntityType<?>> activeEntityTypes = new ConcurrentHashMap<>();
    // 检测范围（方块）
    private static final int DETECTION_RANGE = 1000;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("uuz")
            .then(CommandManager.literal("mobcometome")
                .executes(MobComeToMeCommand::executeRelease) // 无参数时执行解除命令
                .then(CommandManager.argument("entityType", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE))
                    .executes(MobComeToMeCommand::execute))));
    }
    
    // 解除命令的执行方法
    private static int executeRelease(CommandContext<ServerCommandSource> context) {
        try {
            ServerCommandSource source = context.getSource();
            
            // 清除所有正在移动的生物
            movingMobs.clear();
            
            // 获取所有活跃的生物类型
            if (activeEntityTypes.isEmpty()) {
                source.sendFeedback(() -> Text.translatable("command.uuzfabrictestproj.mobcometome.no_active"), false);
                return 0;
            }
            
            // 创建一个副本以避免并发修改异常
            Set<String> entityTypeNames = new HashSet<>(activeEntityTypes.keySet());
            int count = entityTypeNames.size();
            
            for (String entityTypeName : entityTypeNames) {
                // 向玩家发送反馈
                source.sendFeedback(() -> Text.translatable("command.uuzfabrictestproj.mobcometome.released", 
                    entityTypeName), false);
            }
            
            // 清空活跃生物类型
            activeEntityTypes.clear();
            
            return count;
        } catch (Exception e) {
            UuzFabricTestProj.LOGGER.error("执行mobcometome解除命令时发生错误", e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private static int execute(CommandContext<ServerCommandSource> context) {
        try {
            ServerCommandSource source = context.getSource();
            ServerWorld world = source.getWorld();
            Vec3d playerPos = source.getPosition();
            
            // 获取实体类型
            RegistryEntry.Reference<EntityType<?>> entityTypeRef = (RegistryEntry.Reference<EntityType<?>>) context.getArgument(
                "entityType", 
                RegistryEntry.Reference.class
            );
            EntityType<?> entityType = entityTypeRef.value();
            String entityTypeName = entityTypeRef.registryKey().getValue().getPath();
            
            // 记录当前活跃的生物类型
            activeEntityTypes.put(entityTypeName, entityType);
            
            // 获取所有已加载区块中的指定类型实体
            // 使用更大的检测范围
            List<? extends Entity> entities = world.getEntitiesByType(
                entityType,
                new Box(playerPos.add(-DETECTION_RANGE, -DETECTION_RANGE, -DETECTION_RANGE), 
                       playerPos.add(DETECTION_RANGE, DETECTION_RANGE, DETECTION_RANGE)),
                entity -> true
            );
            
            final int[] count = {0};
            BlockPos targetPos = new BlockPos((int)playerPos.x, (int)playerPos.y, (int)playerPos.z);

            // 创建一个临时列表来存储要添加的生物，避免在循环中直接修改集合
            List<MobTargetInfo> newTargetInfos = new ArrayList<>();
            
            for (Entity entity : entities) {
                if (entity instanceof PathAwareEntity pathAwareEntity) {
                    // 设置实体的移动目标
                    pathAwareEntity.getNavigation().startMovingTo(playerPos.x, playerPos.y, playerPos.z, 1.0);
                    
                    // 记录这个生物，以便持续更新其路径
                    MobTargetInfo targetInfo = new MobTargetInfo(
                        pathAwareEntity.getUuid(),
                        targetPos,
                        world.getRegistryKey(),
                        entityType
                    );
                    newTargetInfos.add(targetInfo);
                    count[0]++;
                }
            }
            
            // 批量添加所有新的目标信息
            for (MobTargetInfo info : newTargetInfos) {
                movingMobs.put(info.getMobId(), info);
            }
            
            // 注册服务器tick事件来更新生物路径
            if (!MobPathUpdater.isRegistered) {
                MobPathUpdater.register();
            }

            // 发送反馈消息
            source.sendFeedback(() -> Text.translatable("command.uuzfabrictestproj.mobcometome.success", 
                count[0], entityTypeName), true);

            return count[0];
        } catch (Exception e) {
            UuzFabricTestProj.LOGGER.error("执行mobcometome命令时发生错误", e);
            return 0;
        }
    }
    
    // 生物目标信息类
    public static class MobTargetInfo {
        private final UUID mobId;
        private final BlockPos targetPos;
        private final net.minecraft.registry.RegistryKey<net.minecraft.world.World> worldKey;
        private final EntityType<?> entityType;
        
        public MobTargetInfo(UUID mobId, BlockPos targetPos, net.minecraft.registry.RegistryKey<net.minecraft.world.World> worldKey, EntityType<?> entityType) {
            this.mobId = mobId;
            this.targetPos = targetPos;
            this.worldKey = worldKey;
            this.entityType = entityType;
        }
        
        public UUID getMobId() {
            return mobId;
        }
        
        public BlockPos getTargetPos() {
            return targetPos;
        }
        
        public net.minecraft.registry.RegistryKey<net.minecraft.world.World> getWorldKey() {
            return worldKey;
        }
        
        public EntityType<?> getEntityType() {
            return entityType;
        }
    }
    
    // 生物路径更新器
    public static class MobPathUpdater {
        private static boolean isRegistered = false;
        
        public static void register() {
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
                // 每20个tick更新一次路径（约1秒）
                if (server.getTicks() % 20 != 0) return;
                
                // 创建一个要移除的UUID集合
                Set<UUID> toRemove = new HashSet<>();
                
                // 遍历所有需要移动的生物
                for (Map.Entry<UUID, MobTargetInfo> entry : movingMobs.entrySet()) {
                    try {
                        UUID mobId = entry.getKey();
                        MobTargetInfo info = entry.getValue();
                        
                        // 获取生物所在的世界
                        ServerWorld world = server.getWorld(info.getWorldKey());
                        if (world == null) {
                            toRemove.add(mobId);
                            continue;
                        }
                        
                        // 获取生物实体
                        Entity entity = world.getEntity(mobId);
                        if (entity == null || !(entity instanceof PathAwareEntity)) {
                            toRemove.add(mobId);
                            continue;
                        }
                        
                        PathAwareEntity pathAwareEntity = (PathAwareEntity) entity;
                        
                        // 检查生物是否已经到达目标位置
                        BlockPos entityPos = entity.getBlockPos();
                        BlockPos targetPos = info.getTargetPos();
                        
                        // 无论距离如何，都持续更新路径，确保生物一直朝向目标移动
                        // 重新设置路径
                        pathAwareEntity.getNavigation().startMovingTo(
                            targetPos.getX() + 0.5, 
                            targetPos.getY(), 
                            targetPos.getZ() + 0.5, 
                            1.0
                        );
                        
                        // 不再自动移除到达目标的生物，只有在执行解除命令时才会移除
                    } catch (Exception e) {
                        // 出现异常，移除这个实体
                        toRemove.add(entry.getKey());
                        UuzFabricTestProj.LOGGER.error("更新生物路径时发生错误", e);
                    }
                }
                
                // 批量移除不需要继续控制的生物（只有出错的情况）
                for (UUID id : toRemove) {
                    movingMobs.remove(id);
                }
            });
            
            isRegistered = true;
        }
    }
} 