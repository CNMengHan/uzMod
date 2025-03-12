package com.uuz.fabrictestproj.world;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.util.Identifier;

/**
 * 空岛世界类型
 */
public class SkyIslandWorldType {
    
    // 空岛世界类型的ID
    public static final String SKY_ISLAND_ID = SkyIslandWorldPreset.SKY_ISLAND_ID;
    
    /**
     * 注册空岛世界类型
     */
    public static void register() {
        // 注册区块生成器
        SkyIslandChunkGeneratorType.register();
        
        UuzFabricTestProj.LOGGER.info("空岛世界类型已注册");
    }
} 