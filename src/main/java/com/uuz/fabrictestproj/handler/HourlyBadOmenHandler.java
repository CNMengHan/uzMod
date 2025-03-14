package com.uuz.fabrictestproj.handler;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 处理每小时给玩家施加不祥之兆效果的功能
 */
public class HourlyBadOmenHandler {
    
    // 一小时的游戏刻数 (20 ticks/s * 60 s/min * 60 min/h)
    private static final int ONE_HOUR_TICKS = 20 * 60 * 60;
    
    // 不祥之兆效果持续时间（游戏刻）
    private static final int BAD_OMEN_DURATION_TICKS = 10 * 20; // 10秒
    
    // 不祥之兆效果等级（最高为5级）
    private static final int BAD_OMEN_LEVEL = 5;
    
    // 计时器
    private static int tickCounter = 0;
    
    /**
     * 注册每小时不祥之兆处理器
     */
    public static void register() {
        // 注册服务器tick事件
        ServerTickEvents.END_SERVER_TICK.register(HourlyBadOmenHandler::onServerTick);
    }
    
    /**
     * 服务器tick事件处理
     */
    private static void onServerTick(MinecraftServer server) {
        // 增加计时器
        tickCounter++;
        
        // 每小时触发一次
        if (tickCounter >= ONE_HOUR_TICKS) {
            // 重置计时器
            tickCounter = 0;
            
            // 给所有在线玩家施加不祥之兆效果
            applyBadOmenToAllPlayers(server);
        }
    }
    
    /**
     * 给所有在线玩家施加不祥之兆效果
     */
    public static void applyBadOmenToAllPlayers(MinecraftServer server) {
        // 遍历所有在线玩家
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // 创建不祥之兆效果实例
            StatusEffectInstance badOmen = new StatusEffectInstance(
                StatusEffects.BAD_OMEN,          // 效果类型：不祥之兆
                BAD_OMEN_DURATION_TICKS,         // 持续时间：10秒
                BAD_OMEN_LEVEL - 1,              // 效果等级：5级（索引从0开始，所以是4）
                false,                           // 是否环境效果：否
                false,                           // 是否显示粒子：否
                false                            // 是否显示图标：否
            );
            
            // 给玩家施加效果
            player.addStatusEffect(badOmen);
        }
    }
} 