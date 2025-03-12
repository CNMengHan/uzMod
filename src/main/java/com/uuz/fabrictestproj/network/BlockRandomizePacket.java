package com.uuz.fabrictestproj.network;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockRandomizePacket {
    private static final Identifier PACKET_ID = new Identifier(UuzFabricTestProj.MOD_ID, "block_randomize");
    private static final Random RANDOM = new Random();
    private static final List<Block> VALID_BLOCKS = new ArrayList<>();
    
    static {
        // 初始化有效方块列表
        for (Block block : Registries.BLOCK) {
            // 排除一些不应该随机到的方块（比如空气、基岩等）
            if (block != Blocks.AIR && 
                block != Blocks.VOID_AIR && 
                block != Blocks.CAVE_AIR && 
                block != Blocks.BEDROCK && 
                block != Blocks.END_PORTAL && 
                block != Blocks.END_PORTAL_FRAME && 
                block != Blocks.NETHER_PORTAL &&
                block != Blocks.COMMAND_BLOCK &&
                block != Blocks.CHAIN_COMMAND_BLOCK &&
                block != Blocks.REPEATING_COMMAND_BLOCK &&
                block != Blocks.BARRIER &&
                block != Blocks.STRUCTURE_BLOCK &&
                block != Blocks.STRUCTURE_VOID &&
                block != Blocks.JIGSAW) {
                VALID_BLOCKS.add(block);
            }
        }
    }
    
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, (server, player, handler, buf, responseSender) -> {
            // 读取方块位置
            BlockPos pos = buf.readBlockPos();
            
            // 在服务器线程中执行
            server.execute(() -> {
                // 检查玩家是否有权限修改这个方块
                if (!canPlayerModifyBlock(player, pos)) {
                    player.sendMessage(Text.literal("§c你没有权限修改这个方块！"), false);
                    return;
                }
                
                World world = player.getWorld();
                // 获取当前方块
                Block currentBlock = world.getBlockState(pos).getBlock();
                
                // 如果当前方块是我们不想随机化的方块，就返回
                if (!isBlockRandomizable(currentBlock)) {
                    player.sendMessage(Text.literal("§c这个方块不能被随机化！"), false);
                    return;
                }
                
                // 随机选择一个新方块
                Block newBlock = getRandomBlock();
                // 设置新方块
                world.setBlockState(pos, newBlock.getDefaultState());
                
                // 发送反馈消息
                player.sendMessage(
                    Text.literal("§a方块已随机化为：§e" + newBlock.getName().getString())
                    .append(Text.literal("\n§7该能力将进入5分钟冷却时间")),
                    false
                );
            });
        });
    }
    
    public static void sendToServer(BlockPos pos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
    
    private static boolean canPlayerModifyBlock(ServerPlayerEntity player, BlockPos pos) {
        // 这里可以添加更多的权限检查
        // 比如检查WorldGuard区域、领地插件等
        return player.getWorld().canPlayerModifyAt(player, pos);
    }
    
    private static boolean isBlockRandomizable(Block block) {
        return VALID_BLOCKS.contains(block);
    }
    
    private static Block getRandomBlock() {
        return VALID_BLOCKS.get(RANDOM.nextInt(VALID_BLOCKS.size()));
    }
} 