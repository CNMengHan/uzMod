package com.uuz.fabrictestproj.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;

public class MineralHUD implements HudRenderCallback {
    private static final int SEARCH_RANGE = 120; // 垂直搜索范围
    private static final int MARGIN = 5; // 边距
    private static final List<Block> ORES = new ArrayList<>();
    
    // 添加开关状态变量，默认为关闭
    private static boolean enabled = false;

    static {
        ORES.add(Blocks.DIAMOND_ORE);
        ORES.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        ORES.add(Blocks.IRON_ORE);
        ORES.add(Blocks.DEEPSLATE_IRON_ORE);
        ORES.add(Blocks.GOLD_ORE);
        ORES.add(Blocks.DEEPSLATE_GOLD_ORE);
        ORES.add(Blocks.COPPER_ORE);
        ORES.add(Blocks.DEEPSLATE_COPPER_ORE);
        ORES.add(Blocks.COAL_ORE);
        ORES.add(Blocks.DEEPSLATE_COAL_ORE);
        ORES.add(Blocks.EMERALD_ORE);
        ORES.add(Blocks.DEEPSLATE_EMERALD_ORE);
        ORES.add(Blocks.REDSTONE_ORE);
        ORES.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        ORES.add(Blocks.LAPIS_ORE);
        ORES.add(Blocks.DEEPSLATE_LAPIS_ORE);
    }
    
    /**
     * 初始化MineralHUD
     */
    public static void initialize() {
        // 初始化时不做任何事情，保持默认关闭状态
    }
    
    /**
     * 设置HUD的启用状态
     * @param value 是否启用
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }
    
    /**
     * 获取HUD的启用状态
     * @return 是否启用
     */
    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        // 如果HUD未启用，不渲染任何内容
        if (!enabled) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null || client.world == null) return;

        BlockPos playerPos = player.getBlockPos();
        List<String> displayText = new ArrayList<>();
        
        for (int y = -SEARCH_RANGE; y <= SEARCH_RANGE; y++) {
            BlockPos checkPos = playerPos.add(0, y, 0);
            Block block = client.world.getBlockState(checkPos).getBlock();
            
            if (ORES.contains(block)) {
                String blockName = block.getName().getString();
                int distance = Math.abs(y);
                String direction = y < 0 ? "Down" : "Up";
                displayText.add(String.format("%s: %d blocks (%s)", blockName, distance, direction));
            }
        }

        int y = MARGIN;
        for (String text : displayText) {
            drawContext.drawText(client.textRenderer, Text.literal(text), MARGIN, y, 0xFFFFFF, true);
            y += client.textRenderer.fontHeight + 2;
        }
    }
} 