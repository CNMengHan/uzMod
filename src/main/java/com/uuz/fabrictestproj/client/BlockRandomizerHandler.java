package com.uuz.fabrictestproj.client;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.network.BlockRandomizePacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class BlockRandomizerHandler {
    private static boolean wasMiddleMousePressed = false;
    private static final int COOLDOWN_TICKS = 6000; // 5分钟 = 300秒 = 6000 ticks
    private static int remainingTicks = 0;

    public static void register() {
        // 注册客户端tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // 更新冷却时间
            if (remainingTicks > 0) {
                remainingTicks--;
            }

            // 检查玩家是否在蹲着
            boolean isPlayerSneaking = client.player.isSneaking();
            
            // 检查鼠标中键是否被按下
            boolean isMiddleMousePressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_3) == GLFW.GLFW_PRESS;
            
            // 只在鼠标中键刚被按下时触发
            if (isMiddleMousePressed && !wasMiddleMousePressed && isPlayerSneaking) {
                // 检查是否在冷却中
                if (remainingTicks > 0) {
                    // 计算剩余时间
                    int remainingSeconds = remainingTicks / 20; // 转换为秒
                    int minutes = remainingSeconds / 60;
                    int seconds = remainingSeconds % 60;
                    
                    // 发送冷却提示
                    client.player.sendMessage(
                        Text.literal(String.format(
                            "§c方块随机化能力还在冷却中！剩余时间：§e%d分%d秒",
                            minutes, seconds
                        )),
                        false
                    );
                } else {
                    // 获取玩家看着的方块
                    HitResult hitResult = client.crosshairTarget;
                    if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) hitResult;
                        // 发送数据包到服务器
                        BlockRandomizePacket.sendToServer(blockHit.getBlockPos());
                        // 重置冷却时间
                        remainingTicks = COOLDOWN_TICKS;
                    }
                }
            }
            
            wasMiddleMousePressed = isMiddleMousePressed;
        });
        
        UuzFabricTestProj.LOGGER.info("方块随机化处理器已注册");
    }
    
    /**
     * 获取冷却剩余时间（秒）
     */
    public static int getRemainingCooldown() {
        return remainingTicks / 20;
    }
    
    /**
     * 检查是否在冷却中
     */
    public static boolean isOnCooldown() {
        return remainingTicks > 0;
    }
} 