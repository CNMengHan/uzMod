package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkStatus;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static net.minecraft.server.command.CommandManager.literal;

public class TakeNearbyPackCommand {
    private static final int MAX_SCAN_RADIUS = 100; // 最大扫描半径（区块）

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("takenearbypack")
                .executes(TakeNearbyPackCommand::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("此命令只能由玩家执行"));
            return 0;
        }

        // 获取玩家所在区块坐标
        int playerChunkX = player.getBlockX() >> 4;
        int playerChunkZ = player.getBlockZ() >> 4;
        
        // 用于存储找到的所有容器及其距离
        Map<Object, Double> containerDistances = new HashMap<>();
        final AtomicInteger scannedChunks = new AtomicInteger(0);

        // 使用正方形搜索模式，确保完全覆盖
        for (int dx = -MAX_SCAN_RADIUS; dx <= MAX_SCAN_RADIUS; dx++) {
            for (int dz = -MAX_SCAN_RADIUS; dz <= MAX_SCAN_RADIUS; dz++) {
                int targetChunkX = playerChunkX + dx;
                int targetChunkZ = playerChunkZ + dz;

                // 检查区块是否加载
                WorldChunk chunk = (WorldChunk) player.getWorld().getChunk(targetChunkX, targetChunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }

                scannedChunks.incrementAndGet();

                // 搜索这个区块中的方块实体
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof Inventory) {
                        BlockPos pos = blockEntity.getPos();
                        double distance = player.squaredDistanceTo(
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5
                        );
                        containerDistances.put(blockEntity, distance);
                    }
                }

                // 搜索这个区块中的实体
                Box chunkBox = new Box(
                    targetChunkX << 4, player.getWorld().getBottomY(), targetChunkZ << 4,
                    (targetChunkX << 4) + 16, player.getWorld().getTopY(), (targetChunkZ << 4) + 16
                );
                List<Entity> entities = player.getWorld().getEntitiesByClass(Entity.class, chunkBox, 
                    entity -> entity instanceof Inventory);
                
                for (Entity entity : entities) {
                    if (entity instanceof Inventory) {
                        double distance = player.squaredDistanceTo(entity);
                        containerDistances.put(entity, distance);
                    }
                }
            }
        }

        final int finalScannedChunks = scannedChunks.get();
        source.sendFeedback(() -> Text.literal("已扫描 " + finalScannedChunks + " 个区块"), false);

        // 按距离排序容器
        List<Map.Entry<Object, Double>> sortedContainers = new ArrayList<>(containerDistances.entrySet());
        sortedContainers.sort(Map.Entry.comparingByValue());

        // 记录拾取的物品
        final Map<String, AtomicInteger> itemsCounted = new HashMap<>();
        final AtomicInteger totalItems = new AtomicInteger(0);
        final AtomicInteger containerCount = new AtomicInteger(0);

        // 从最近的容器开始拾取物品
        for (Map.Entry<Object, Double> entry : sortedContainers) {
            Object container = entry.getKey();
            Inventory inventory = (Inventory) container;
            boolean containerHadItems = false;

            // 遍历容器中的每个槽位
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty()) {
                    // 尝试将物品添加到玩家背包
                    ItemStack stackToAdd = stack.copy();
                    if (player.getInventory().insertStack(stackToAdd)) {
                        // 如果物品被完全添加到背包
                        final int transferred = stack.getCount();
                        inventory.setStack(i, ItemStack.EMPTY);
                        containerHadItems = true;
                        
                        // 记录拾取的物品
                        final String itemName = stack.getName().getString();
                        itemsCounted.computeIfAbsent(itemName, k -> new AtomicInteger(0))
                                  .addAndGet(transferred);
                        totalItems.addAndGet(transferred);
                    } else {
                        // 如果只有部分物品被添加到背包
                        final int transferred = stack.getCount() - stackToAdd.getCount();
                        if (transferred > 0) {
                            stack.setCount(stackToAdd.getCount());
                            inventory.setStack(i, stack);
                            containerHadItems = true;
                            
                            // 记录拾取的物品
                            final String itemName = stack.getName().getString();
                            itemsCounted.computeIfAbsent(itemName, k -> new AtomicInteger(0))
                                      .addAndGet(transferred);
                            totalItems.addAndGet(transferred);
                        }
                    }
                }
            }
            
            if (containerHadItems) {
                containerCount.incrementAndGet();
            }
        }

        // 发送拾取报告
        final int finalTotalItems = totalItems.get();
        final int finalContainerCount = containerCount.get();
        if (finalTotalItems > 0) {
            source.sendFeedback(() -> Text.literal("从 " + finalContainerCount + " 个容器中拾取以下物品："), false);
            itemsCounted.forEach((itemName, count) -> {
                source.sendFeedback(() -> Text.literal("- " + itemName + " x" + count.get()), false);
            });
            source.sendFeedback(() -> Text.literal("共计: " + finalTotalItems + "个物品"), false);
        } else {
            source.sendFeedback(() -> Text.literal("附近的容器都是空的!"), false);
        }

        return finalTotalItems;
    }
} 