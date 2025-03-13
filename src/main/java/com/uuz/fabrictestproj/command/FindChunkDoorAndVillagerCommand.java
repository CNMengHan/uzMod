package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FindChunkDoorAndVillagerCommand {
    
    private static final int SEARCH_RADIUS_CHUNKS = 24; // 搜索半径（区块）
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("findchunkdoorandvillager")
                    .executes(FindChunkDoorAndVillagerCommand::execute)
                )
        );
        
        UuzFabricTestProj.LOGGER.info("查找区块门和村民命令已注册");
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendError(Text.literal("该命令只能由玩家执行"));
            return 0;
        }
        
        // 发送开始搜索的消息
        player.sendMessage(Text.literal("开始搜索附近区块中的门和村民...").formatted(Formatting.YELLOW), false);
        
        // 获取玩家所在的世界和位置
        ServerWorld world = player.getServerWorld();
        BlockPos playerPos = player.getBlockPos();
        
        // 使用服务器的主线程调度器来执行搜索任务
        CompletableFuture.runAsync(() -> {
            try {
                // 用于存储找到的门和村民
                List<DoorInfo> doors = new ArrayList<>();
                List<VillagerInfo> villagers = new ArrayList<>();
                
                // 获取玩家所在的区块位置
                ChunkPos playerChunkPos = new ChunkPos(playerPos);
                
                // 搜索附近的区块
                for (int chunkX = playerChunkPos.x - SEARCH_RADIUS_CHUNKS; chunkX <= playerChunkPos.x + SEARCH_RADIUS_CHUNKS; chunkX++) {
                    for (int chunkZ = playerChunkPos.z - SEARCH_RADIUS_CHUNKS; chunkZ <= playerChunkPos.z + SEARCH_RADIUS_CHUNKS; chunkZ++) {
                        final int finalChunkX = chunkX;
                        final int finalChunkZ = chunkZ;
                        
                        // 在主线程中执行区块操作
                        world.getServer().executeSync(() -> {
                            // 检查区块是否已加载
                            if (!world.isChunkLoaded(finalChunkX, finalChunkZ)) {
                                return;
                            }
                            
                            // 获取区块
                            Chunk chunk = world.getChunk(finalChunkX, finalChunkZ, ChunkStatus.FULL, false);
                            if (chunk == null) {
                                return;
                            }
                            
                            ChunkPos chunkPos = chunk.getPos();
                            
                            // 搜索区块中的门
                            searchDoorsInChunk(world, chunk, playerPos, doors);
                            
                            // 搜索区块中的村民
                            searchVillagersInChunk(world, chunkPos, playerPos, villagers);
                        });
                    }
                }
                
                // 在主线程中发送结果
                world.getServer().executeSync(() -> {
                    sendSearchResults(player, doors, villagers);
                });
                
            } catch (Exception e) {
                UuzFabricTestProj.LOGGER.error("搜索区块门和村民时发生错误", e);
                player.sendMessage(Text.literal("搜索过程中发生错误: " + e.getMessage()).formatted(Formatting.RED), false);
            }
        }, world.getServer());
        
        return 1;
    }
    
    private static void searchDoorsInChunk(ServerWorld world, Chunk chunk, BlockPos playerPos, List<DoorInfo> doors) {
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        int endX = chunkPos.getEndX();
        int endZ = chunkPos.getEndZ();
        
        // 遍历区块中的所有方块
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                // 只检查可能有门的高度范围
                for (int y = world.getBottomY(); y <= world.getTopY(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    
                    // 检查是否是门
                    if (block instanceof DoorBlock) {
                        String doorName = Registries.BLOCK.getId(block).toString();
                        double distance = Math.sqrt(pos.getSquaredDistance(playerPos));
                        synchronized (doors) {
                            doors.add(new DoorInfo(pos, doorName, distance));
                        }
                    }
                }
            }
        }
    }
    
    private static void searchVillagersInChunk(ServerWorld world, ChunkPos chunkPos, BlockPos playerPos, List<VillagerInfo> villagers) {
        // 计算区块的边界
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        int endX = chunkPos.getEndX();
        int endZ = chunkPos.getEndZ();
        
        // 创建一个包含整个区块的边界盒
        Box chunkBox = new Box(
            startX, world.getBottomY(), startZ,
            endX + 1, world.getTopY(), endZ + 1
        );
        
        // 获取边界盒内的所有村民实体
        List<VillagerEntity> villagerEntities = world.getEntitiesByType(
            EntityType.VILLAGER,
            chunkBox,
            entity -> true
        );
        
        // 处理找到的村民
        for (VillagerEntity villager : villagerEntities) {
            BlockPos villagerPos = villager.getBlockPos();
            String profession = villager.getVillagerData().getProfession().toString();
            int level = villager.getVillagerData().getLevel();
            double distance = Math.sqrt(villagerPos.getSquaredDistance(playerPos));
            
            synchronized (villagers) {
                villagers.add(new VillagerInfo(villagerPos, profession, level, distance));
            }
        }
    }
    
    private static void sendSearchResults(ServerPlayerEntity player, List<DoorInfo> doors, List<VillagerInfo> villagers) {
        // 按距离排序
        doors.sort((a, b) -> Double.compare(a.distance, b.distance));
        villagers.sort((a, b) -> Double.compare(a.distance, b.distance));
        
        // 发送搜索结果
        player.sendMessage(Text.literal("===== 搜索结果 =====").formatted(Formatting.GREEN), false);
        
        // 发送门的信息
        player.sendMessage(Text.literal("找到 " + doors.size() + " 个门:").formatted(Formatting.GOLD), false);
        for (int i = 0; i < doors.size(); i++) {
            DoorInfo door = doors.get(i);
            player.sendMessage(
                Text.literal((i + 1) + ". " + door.type + " 在 [" + 
                    door.pos.getX() + ", " + door.pos.getY() + ", " + door.pos.getZ() + 
                    "] 距离: " + String.format("%.2f", door.distance) + "米")
                    .formatted(Formatting.YELLOW),
                false
            );
        }
        
        // 发送村民的信息
        player.sendMessage(Text.literal("找到 " + villagers.size() + " 个村民:").formatted(Formatting.GOLD), false);
        for (int i = 0; i < villagers.size(); i++) {
            VillagerInfo villager = villagers.get(i);
            player.sendMessage(
                Text.literal((i + 1) + ". " + villager.profession + " (等级: " + villager.level + ") 在 [" + 
                    villager.pos.getX() + ", " + villager.pos.getY() + ", " + villager.pos.getZ() + 
                    "] 距离: " + String.format("%.2f", villager.distance) + "米")
                    .formatted(Formatting.AQUA),
                false
            );
        }
        
        // 如果没有找到任何东西
        if (doors.isEmpty() && villagers.isEmpty()) {
            player.sendMessage(Text.literal("在附近区块中没有找到任何门或村民").formatted(Formatting.RED), false);
        }
        
        // 发送搜索完成的消息
        player.sendMessage(Text.literal("搜索" + SEARCH_RADIUS_CHUNKS + "个区块" + "完成！").formatted(Formatting.GREEN), false);
    }
    
    // 门信息类
    private static class DoorInfo {
        public final BlockPos pos;
        public final String type;
        public final double distance;
        
        public DoorInfo(BlockPos pos, String type, double distance) {
            this.pos = pos;
            this.type = type;
            this.distance = distance;
        }
    }
    
    // 村民信息类
    private static class VillagerInfo {
        public final BlockPos pos;
        public final String profession;
        public final int level;
        public final double distance;
        
        public VillagerInfo(BlockPos pos, String profession, int level, double distance) {
            this.pos = pos;
            this.profession = profession;
            this.level = level;
            this.distance = distance;
        }
    }
} 