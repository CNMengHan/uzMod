package com.uuz.fabrictestproj.manager;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AfkManager {
    // 玩家AFK检测时间（30分钟 = 1800秒 = 36000 ticks）
    private static final long AFK_TIME_THRESHOLD = TimeUnit.MINUTES.toMillis(30);
    
    // 检查间隔（每10秒检查一次 = 200 ticks）
    private static final int CHECK_INTERVAL = 200;
    
    // 存储玩家上次活动的时间和位置
    private static final Map<UUID, PlayerActivity> playerActivities = new HashMap<>();
    
    // 存储玩家AFK状态和时长
    private static final Map<UUID, AfkStatus> playerAfkStatus = new HashMap<>();
    
    // 计数器，用于控制检查频率
    private static int tickCounter = 0;
    
    // 有趣的AFK消息列表
    private static final List<String> AFK_MESSAGES = Arrays.asList(
        "好像是睡着了呢，已经AFK了%d分钟啦~",
        "可能去泡咖啡了，已经离开了%d分钟~",
        "也许在撸猫咪呢，已经发呆%d分钟了~",
        "可能去找薯片了，已经消失%d分钟了~",
        "大概是去上厕所了，已经AFK了%d分钟~",
        "可能被猫咪绑架了，已经不见踪影%d分钟了~",
        "也许在思考人生的意义，已经发呆%d分钟了~",
        "可能在研究如何养更多的村民，已经AFK了%d分钟~",
        "大概是去吃饭了，已经离开了%d分钟~",
        "可能在看直播，已经AFK了%d分钟~",
        "也许在听音乐，已经发呆%d分钟了~",
        "可能在刷手机，已经AFK了%d分钟~",
        "大概是被现实生活召唤走了，已经离开%d分钟了~",
        "可能在和朋友聊天，已经AFK了%d分钟~",
        "也许在做作业，已经消失%d分钟了~",
        "可能在看电影，已经AFK了%d分钟~",
        "大概是去遛狗了，已经离开%d分钟了~",
        "可能在打瞌睡，已经AFK了%d分钟~",
        "也许在思考如何建造更好的房子，已经发呆%d分钟了~",
        "可能在给植物浇水，已经AFK了%d分钟~"
    );
    
    /**
     * 初始化AFK管理器
     */
    public static void initialize() {
        // 注册服务器tick事件
        ServerTickEvents.END_SERVER_TICK.register(AfkManager::onServerTick);
        UuzFabricTestProj.LOGGER.info("AFK提示系统已初始化");
    }
    
    /**
     * 服务器tick事件处理
     * @param server Minecraft服务器实例
     */
    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        
        // 每CHECK_INTERVAL ticks检查一次
        if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            checkPlayersActivity(server);
        }
    }
    
    /**
     * 检查所有玩家的活动状态
     * @param server Minecraft服务器实例
     */
    private static void checkPlayersActivity(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();
        
        // 检查每个在线玩家
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID playerId = player.getUuid();
            Vec3d currentPos = player.getPos();
            
            // 获取玩家上次活动记录，如果没有则创建新的
            PlayerActivity activity = playerActivities.computeIfAbsent(playerId, 
                id -> new PlayerActivity(currentTime, currentPos));
            
            // 检查玩家是否移动或执行了动作
            boolean hasMoved = !currentPos.equals(activity.lastPosition);
            
            if (hasMoved) {
                // 玩家移动了，更新活动时间和位置
                activity.lastActiveTime = currentTime;
                activity.lastPosition = currentPos;
                
                // 如果玩家之前是AFK状态，现在回来了
                if (playerAfkStatus.containsKey(playerId)) {
                    AfkStatus afkStatus = playerAfkStatus.remove(playerId);
                    broadcastPlayerReturn(player, afkStatus.afkDuration);
                }
            } else {
                // 玩家没有移动，检查是否达到AFK时间阈值
                long inactiveTime = currentTime - activity.lastActiveTime;
                
                if (inactiveTime >= AFK_TIME_THRESHOLD) {
                    // 计算AFK时长（分钟）
                    int afkMinutes = (int) (inactiveTime / TimeUnit.MINUTES.toMillis(1));
                    
                    // 检查玩家是否已经在AFK状态，以及是否需要更新通知
                    AfkStatus afkStatus = playerAfkStatus.get(playerId);
                    
                    if (afkStatus == null) {
                        // 玩家刚刚进入AFK状态
                        afkStatus = new AfkStatus(afkMinutes);
                        playerAfkStatus.put(playerId, afkStatus);
                        broadcastPlayerAfk(player, afkMinutes);
                    } else if (afkMinutes >= afkStatus.afkDuration + 30) {
                        // 玩家AFK时间增加了30分钟，更新通知
                        afkStatus.afkDuration = afkMinutes;
                        broadcastPlayerAfk(player, afkMinutes);
                    }
                }
            }
        }
        
        // 清理已离线玩家的数据
        cleanupOfflinePlayers(server);
    }
    
    /**
     * 广播玩家进入AFK状态的消息
     * @param player AFK的玩家
     * @param afkMinutes AFK的分钟数
     */
    private static void broadcastPlayerAfk(ServerPlayerEntity player, int afkMinutes) {
        String playerName = player.getName().getString();
        
        // 随机选择一条AFK消息
        Random random = new Random();
        String messageTemplate = AFK_MESSAGES.get(random.nextInt(AFK_MESSAGES.size()));
        String message = String.format(messageTemplate, afkMinutes);
        
        // 创建广播消息
        Text broadcastMessage = Text.literal(playerName)
            .formatted(Formatting.YELLOW)
            .append(Text.literal(" " + message).formatted(Formatting.GRAY));
        
        // 广播给所有玩家
        player.getServer().getPlayerManager().broadcast(broadcastMessage, false);
        
        // 记录日志
        UuzFabricTestProj.LOGGER.info("玩家 {} AFK了 {} 分钟", playerName, afkMinutes);
    }
    
    /**
     * 广播玩家从AFK状态返回的消息
     * @param player 返回的玩家
     * @param afkMinutes AFK的分钟数
     */
    private static void broadcastPlayerReturn(ServerPlayerEntity player, int afkMinutes) {
        String playerName = player.getName().getString();
        
        // 创建广播消息
        Text broadcastMessage = Text.literal(playerName)
            .formatted(Formatting.YELLOW)
            .append(Text.literal(" 回来了，之前AFK了 " + afkMinutes + " 分钟").formatted(Formatting.GRAY));
        
        // 广播给所有玩家
        player.getServer().getPlayerManager().broadcast(broadcastMessage, false);
        
        // 记录日志
        UuzFabricTestProj.LOGGER.info("玩家 {} 从AFK状态返回，之前AFK了 {} 分钟", playerName, afkMinutes);
    }
    
    /**
     * 清理已离线玩家的数据
     * @param server Minecraft服务器实例
     */
    private static void cleanupOfflinePlayers(MinecraftServer server) {
        Set<UUID> onlinePlayerIds = new HashSet<>();
        
        // 收集所有在线玩家的ID
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            onlinePlayerIds.add(player.getUuid());
        }
        
        // 移除已离线玩家的数据
        playerActivities.keySet().removeIf(id -> !onlinePlayerIds.contains(id));
        playerAfkStatus.keySet().removeIf(id -> !onlinePlayerIds.contains(id));
    }
    
    /**
     * 玩家活动记录类
     */
    private static class PlayerActivity {
        long lastActiveTime;
        Vec3d lastPosition;
        
        PlayerActivity(long time, Vec3d position) {
            this.lastActiveTime = time;
            this.lastPosition = position;
        }
    }
    
    /**
     * 玩家AFK状态类
     */
    private static class AfkStatus {
        int afkDuration; // 以分钟为单位
        
        AfkStatus(int minutes) {
            this.afkDuration = minutes;
        }
    }
} 