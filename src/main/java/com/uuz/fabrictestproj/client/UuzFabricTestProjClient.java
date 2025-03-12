package com.uuz.fabrictestproj.client;

import com.uuz.fabrictestproj.client.hud.DebugHUD;
import com.uuz.fabrictestproj.client.hud.MineralHUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;

public class UuzFabricTestProjClient implements ClientModInitializer {
    private static final MineralHUD MINERAL_HUD = new MineralHUD();
    
    @Override
    public void onInitializeClient() {
        // 初始化HUD
        DebugHUD.initialize();
        MineralHUD.initialize();
        
        // 注册HUD渲染
        HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
            DebugHUD.getInstance().onHudRender(context, tickDelta);
            MINERAL_HUD.onHudRender(context, tickDelta);
        });
        
        // 注册猫咪掉落物品功能
        ClientTickEvents.END_CLIENT_TICK.register(CatDropManager::onTick);
        
        // 注册方块随机化处理器
        BlockRandomizerHandler.register();
    }
} 