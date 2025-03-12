package com.uuz.fabrictestproj.manager;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoatFlyManager {
    private static final Map<UUID, Boolean> enabledPlayers = new HashMap<>();
    private static boolean initialized = false;
    
    // 默认设置
    private static double forwardSpeed = 1.0;
    private static double upwardSpeed = 0.3;
    private static boolean changeForwardSpeed = false;
    
    // 存储玩家的上升/下降状态
    private static final Map<UUID, Boolean> playerJumping = new HashMap<>();
    private static final Map<UUID, Boolean> playerSneaking = new HashMap<>();
    private static final Map<UUID, Boolean> playerForward = new HashMap<>();
    
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (isEnabled(player.getUuid()) && player.hasVehicle() && player.getVehicle() instanceof BoatEntity) {
                    handleBoatFly(player);
                }
            }
        });
        
        initialized = true;
    }
    
    private static void handleBoatFly(ServerPlayerEntity player) {
        Entity vehicle = player.getVehicle();
        if (vehicle == null) return;
        
        Vec3d velocity = vehicle.getVelocity();
        
        // 默认移动
        double motionX = velocity.x;
        double motionY = 0;
        double motionZ = velocity.z;
        
        // 获取玩家UUID
        UUID playerUuid = player.getUuid();
        
        // 上升/下降
        boolean isJumping = playerJumping.getOrDefault(playerUuid, false);
        if (isJumping) {
            motionY = upwardSpeed;
        } else if (playerSneaking.getOrDefault(playerUuid, false)) {
            motionY = velocity.y; // 保持当前垂直速度，允许下降
        }
        
        // 前进
        boolean isMovingForward = playerForward.getOrDefault(playerUuid, false);
        if (isMovingForward && changeForwardSpeed) {
            double speed = forwardSpeed;
            float yawRad = vehicle.getYaw() * MathHelper.RADIANS_PER_DEGREE;
            
            motionX = -MathHelper.sin(yawRad) * speed;
            motionZ = MathHelper.cos(yawRad) * speed;
        }
        
        // 应用移动
        vehicle.setVelocity(motionX, motionY, motionZ);
    }
    
    public static boolean isEnabled(UUID playerUuid) {
        return enabledPlayers.getOrDefault(playerUuid, false);
    }
    
    public static void setEnabled(UUID playerUuid, boolean enabled) {
        enabledPlayers.put(playerUuid, enabled);
        
        // 如果禁用，清除玩家的输入状态
        if (!enabled) {
            playerJumping.remove(playerUuid);
            playerSneaking.remove(playerUuid);
            playerForward.remove(playerUuid);
        }
    }
    
    public static void setPlayerJumping(UUID playerUuid, boolean jumping) {
        playerJumping.put(playerUuid, jumping);
    }
    
    public static void setPlayerSneaking(UUID playerUuid, boolean sneaking) {
        playerSneaking.put(playerUuid, sneaking);
    }
    
    public static void setPlayerForward(UUID playerUuid, boolean forward) {
        playerForward.put(playerUuid, forward);
    }
    
    public static double getForwardSpeed() {
        return forwardSpeed;
    }
    
    public static void setForwardSpeed(double speed) {
        forwardSpeed = speed;
    }
    
    public static double getUpwardSpeed() {
        return upwardSpeed;
    }
    
    public static void setUpwardSpeed(double speed) {
        upwardSpeed = speed;
    }
    
    public static boolean isChangeForwardSpeed() {
        return changeForwardSpeed;
    }
    
    public static void setChangeForwardSpeed(boolean change) {
        changeForwardSpeed = change;
    }
} 