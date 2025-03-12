package com.uuz.fabrictestproj.manager;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LowHealthManager {
    private static final float LOW_HEALTH_THRESHOLD = 6.0f; // 血量低于6时触发警报
    private static final int ALERT_COOLDOWN = 200; // 10秒冷却时间（20 ticks/秒）
    
    // 玩家ID -> 上次发送警报的时间
    private static final Map<UUID, Long> lastAlertTimes = new HashMap<>();
    
    public static void initialize() {
        // 注册服务器tick事件
        ServerTickEvents.END_SERVER_TICK.register(LowHealthManager::onServerTick);
        UuzFabricTestProj.LOGGER.info("濒死通知系统已初始化");
    }
    
    /**
     * 服务器tick事件处理
     * @param server Minecraft服务器实例
     */
    private static void onServerTick(MinecraftServer server) {
        // 检查所有玩家的血量
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkPlayerHealth(player, server);
        }
    }
    
    /**
     * 检查玩家血量并在必要时发送警报
     * @param player 要检查的玩家
     * @param server Minecraft服务器实例
     */
    private static void checkPlayerHealth(ServerPlayerEntity player, MinecraftServer server) {
        float health = player.getHealth();
        UUID playerId = player.getUuid();
        
        // 如果玩家血量低于阈值且不在冷却时间内
        if (health <= LOW_HEALTH_THRESHOLD && canSendAlert(playerId)) {
            // 发送警报
            broadcastLowHealthAlert(player, server);
            
            // 更新上次发送警报的时间
            lastAlertTimes.put(playerId, System.currentTimeMillis());
        }
    }
    
    /**
     * 检查是否可以为指定玩家发送警报
     * @param playerId 玩家ID
     * @return 如果可以发送警报返回true，否则返回false
     */
    private static boolean canSendAlert(UUID playerId) {
        if (!lastAlertTimes.containsKey(playerId)) {
            return true;
        }
        
        long lastAlertTime = lastAlertTimes.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        // 检查是否已经过了冷却时间
        return (currentTime - lastAlertTime) >= (ALERT_COOLDOWN * 50); // 转换为毫秒
    }
    
    /**
     * 广播玩家血量低的警报
     * @param player 血量低的玩家
     * @param server Minecraft服务器实例
     */
    private static void broadcastLowHealthAlert(ServerPlayerEntity player, MinecraftServer server) {
        String playerName = player.getName().getString();
        float health = player.getHealth();
        BlockPos pos = player.getBlockPos();
        String dimension = getDimensionName(player);
        
        // 创建可点击的传送命令
        String teleportCommand = "/uuz tpto " + playerName;
        
        // 创建可点击的坐标文本
        MutableText posText = Text.literal(String.format("§e[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ()))
            .styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§a点击传送到该玩家位置")))
            );
        
        // 创建玩家名文本
        MutableText nameText = Text.literal(playerName)
            .formatted(Formatting.RED, Formatting.BOLD)
            .styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§a点击传送到该玩家位置")))
            );
        
        // 创建警报消息
        MutableText alertMessage = Text.literal("§c§l[HELP] §r")
            .append(nameText)
            .append(Text.literal(" §c血量危险！当前血量: "))
            .append(Text.literal(String.format("§c%.1f", health)))
            .append(Text.literal(" §c位置: "))
            .append(Text.literal("§d" + dimension))
            .append(Text.literal(" §c的 "))
            .append(posText)
            .append(Text.literal(" §c§l[点击坐标立即传送]"));
        
        // 广播给所有玩家
        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            if (!serverPlayer.equals(player)) { // 不发送给血量低的玩家自己
                serverPlayer.sendMessage(alertMessage, false);
            }
        }
        
        // 给血量低的玩家发送一条不同的消息
        MutableText selfMessage = Text.literal("§c§l[HELP] §r")
            .append(Text.literal("§c你的血量危险！已向其他玩家发送求助信息"));
        player.sendMessage(selfMessage, false);
        
        // 记录日志
        UuzFabricTestProj.LOGGER.info("已发送玩家 {} 的濒死通知，当前血量: {}", playerName, health);
    }
    
    /**
     * 获取玩家所在维度的名称
     * @param player 玩家
     * @return 维度名称
     */
    private static String getDimensionName(PlayerEntity player) {
        String dimensionKey = player.getWorld().getRegistryKey().getValue().toString();
        
        switch (dimensionKey) {
            case "minecraft:overworld":
                return "主世界";
            case "minecraft:the_nether":
                return "下界";
            case "minecraft:the_end":
                return "末地";
            default:
                return dimensionKey;
        }
    }
} 