package com.uuz.fabrictestproj.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/**
 * 处理末影人传送能力
 */
public class EnderTeleportHandler {
    private static final Identifier ENDER_TELEPORT_PACKET_ID = new Identifier("uuzfabrictestproj", "ender_teleport");
    private static boolean wasRightClickDown = false;
    private static final int COOLDOWN_TICKS = 200; // 10秒冷却时间
    private static int remainingCooldown = 0;
    
    /**
     * 注册客户端处理器
     */
    public static void registerClient() {
        // 注册客户端tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            // 更新冷却时间
            if (remainingCooldown > 0) {
                remainingCooldown--;
            }
            
            // 检查玩家是否蹲下并按下右键
            boolean isSneaking = client.player.isSneaking();
            boolean isRightClickDown = MinecraftClient.getInstance().options.useKey.isPressed();
            
            if (isSneaking && isRightClickDown && !wasRightClickDown) {
                // 发送传送请求到服务器
                ClientPlayNetworking.send(ENDER_TELEPORT_PACKET_ID, PacketByteBufs.create());
            }
            
            wasRightClickDown = isRightClickDown;
        });
    }
    
    /**
     * 注册服务器处理器
     */
    public static void registerServer() {
        // 注册服务器接收处理器
        ServerPlayNetworking.registerGlobalReceiver(ENDER_TELEPORT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            // 在服务器线程上执行
            server.execute(() -> {
                // 所有玩家都可以使用末影传送能力
                doEnderTeleport((ServerPlayerEntity) player);
            });
        });
    }
    
    /**
     * 执行末影传送
     */
    private static void doEnderTeleport(ServerPlayerEntity player) {
        // 检查冷却时间
        if (remainingCooldown > 0) {
            player.sendMessage(Text.literal("§c末影传送能力还在冷却中！剩余时间：§e" + (remainingCooldown / 20) + "秒"), false);
            return;
        }
        
        // 随机传送距离
        Random random = player.getRandom();
        double distance = 8.0 + random.nextDouble() * 24.0; // 8-32格随机距离
        double angle = random.nextDouble() * Math.PI * 2.0; // 随机角度
        
        // 计算目标位置
        double targetX = player.getX() + Math.sin(angle) * distance;
        double targetZ = player.getZ() + Math.cos(angle) * distance;
        
        // 查找安全的着陆点
        BlockPos targetPos = new BlockPos((int)targetX, (int)player.getY(), (int)targetZ);
        while (!player.getWorld().getBlockState(targetPos).isAir() && targetPos.getY() < 256) {
            targetPos = targetPos.up();
        }
        while (player.getWorld().getBlockState(targetPos.down()).isAir() && targetPos.getY() > 0) {
            targetPos = targetPos.down();
        }
        
        // 传送玩家
        player.teleport(targetX, targetPos.getY() + 1, targetZ);
        
        // 播放音效
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        
        // 添加短暂的抗性效果
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 2));
        
        // 设置冷却时间
        remainingCooldown = COOLDOWN_TICKS;
        
        // 发送成功消息
        player.sendMessage(Text.literal("§a成功传送！"), false);
    }
} 