package com.uuz.fabrictestproj;

import com.uuz.fabrictestproj.client.hud.MineralHUD;
import com.uuz.fabrictestproj.client.hud.DebugHUD;
import com.uuz.fabrictestproj.client.CatDropManager;
import com.uuz.fabrictestproj.handler.EnderTeleportHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.lwjgl.glfw.GLFW;

public class UuzFabricTestProjClient implements ClientModInitializer {
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // 注册矿物HUD
        MineralHUD.initialize();
        
        // 注册调试HUD
        DebugHUD.initialize();
        
        // 注册HUD渲染回调
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            // 渲染调试HUD
            DebugHUD.getInstance().onHudRender(drawContext, tickDelta);
            
            // 渲染矿物HUD
            if (MineralHUD.isEnabled()) {
                // MineralHUD 实现了 HudRenderCallback 接口，直接创建实例调用
                new MineralHUD().onHudRender(drawContext, tickDelta);
            }
        });
        
        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 处理猫掉落物品的tick事件
            CatDropManager.onTick(client);
        });
        
        // 注册末影传送处理器
        EnderTeleportHandler.registerClient();
        
        UuzFabricTestProj.LOGGER.info("客户端初始化完成");
    }
}
