package com.uuz.fabrictestproj.world;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;

/**
 * 注册空岛区块生成器
 */
public class SkyIslandChunkGeneratorType {
    
    /**
     * 注册空岛区块生成器
     */
    public static void register() {
        // 注册区块生成器编解码器
        Registry.register(Registries.CHUNK_GENERATOR, 
                new Identifier(UuzFabricTestProj.MOD_ID, "sky_island"), 
                SkyIslandChunkGenerator.CODEC);
        
        UuzFabricTestProj.LOGGER.info("空岛区块生成器已注册");
    }
} 