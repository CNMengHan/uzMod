package com.uuz.fabrictestproj.world;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * 空岛数据生成器
 */
public class SkyIslandDataGenerator implements DataGeneratorEntrypoint {
    
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        UuzFabricTestProj.LOGGER.info("空岛数据生成器已初始化");
    }
} 