package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkStatus;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static net.minecraft.server.command.CommandManager.literal;

public class TakeLostItemsCommand {
    private static final int MAX_SCAN_RADIUS = 100; // 最大扫描半径（区块）

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("takelostitems")
                .executes(TakeLostItemsCommand::execute)));
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
        
        // 记录拾取的物品
        final Map<String, AtomicInteger> itemsCounted = new HashMap<>();
        final AtomicInteger totalItems = new AtomicInteger(0);
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

                // 搜索这个区块中的掉落物品
                Box chunkBox = new Box(
                    targetChunkX << 4, player.getWorld().getBottomY(), targetChunkZ << 4,
                    (targetChunkX << 4) + 16, player.getWorld().getTopY(), (targetChunkZ << 4) + 16
                );
                List<ItemEntity> items = player.getWorld().getEntitiesByClass(
                    ItemEntity.class,
                    chunkBox,
                    itemEntity -> true // 接受所有掉落物
                );

                // 处理找到的掉落物
                for (ItemEntity itemEntity : items) {
                    ItemStack stack = itemEntity.getStack();
                    if (!stack.isEmpty()) {
                        // 尝试将物品添加到玩家背包
                        ItemStack stackToAdd = stack.copy();
                        if (player.getInventory().insertStack(stackToAdd)) {
                            // 如果物品被完全添加到背包
                            final int transferred = stack.getCount();
                            itemEntity.discard(); // 移除掉落物实体
                            
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
                                itemEntity.setStack(stack);
                                
                                // 记录拾取的物品
                                final String itemName = stack.getName().getString();
                                itemsCounted.computeIfAbsent(itemName, k -> new AtomicInteger(0))
                                          .addAndGet(transferred);
                                totalItems.addAndGet(transferred);
                            }
                        }
                    }
                }
            }
        }

        final int finalScannedChunks = scannedChunks.get();
        source.sendFeedback(() -> Text.literal("已扫描 " + finalScannedChunks + " 个区块"), false);

        // 发送拾取报告
        final int finalTotalItems = totalItems.get();
        if (finalTotalItems > 0) {
            source.sendFeedback(() -> Text.literal("已拾取以下掉落物："), false);
            itemsCounted.forEach((itemName, count) -> {
                source.sendFeedback(() -> Text.literal("- " + itemName + " x" + count.get()), false);
            });
            source.sendFeedback(() -> Text.literal("共计: " + finalTotalItems + "个物品"), false);
        } else {
            source.sendFeedback(() -> Text.literal("附近没有找到掉落物!"), false);
        }

        return finalTotalItems;
    }
} 