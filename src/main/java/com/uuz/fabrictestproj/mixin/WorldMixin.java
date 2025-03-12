package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.world.SkyIslandChunkGenerator;
import com.uuz.fabrictestproj.world.SkyIslandWorldType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class WorldMixin {
    
    @Shadow public abstract MinecraftServer getServer();
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // 使用this作为ServerWorld，因为我们已经在ServerWorld的Mixin中
        ServerWorld world = (ServerWorld)(Object)this;
        MinecraftServer server = this.getServer();
        
        // 检查是否是主世界
        if (world.getRegistryKey() == World.OVERWORLD) {
            // 获取世界生成器类型 - 使用更安全的方式检查
            String levelName = server.getSaveProperties().getLevelName();
            
            // 检查是否是我们的空岛世界类型 - 通过世界名称或其他方式判断
            if (levelName.contains("skyisland") || levelName.contains("空岛")) {
                UuzFabricTestProj.LOGGER.info("检测到空岛世界，替换区块生成器");
                
                // 获取当前的生物群系源
                ChunkGenerator currentGenerator = world.getChunkManager().getChunkGenerator();
                BiomeSource biomeSource = currentGenerator.getBiomeSource();
                
                // 创建我们的空岛区块生成器
                ChunkGenerator skyIslandGenerator = new SkyIslandChunkGenerator(biomeSource, world.getSeed());
                
                // 替换区块生成器
                try {
                    // 使用反射替换区块生成器
                    java.lang.reflect.Field chunkManagerField = ServerWorld.class.getDeclaredField("chunkManager");
                    chunkManagerField.setAccessible(true);
                    Object chunkManager = chunkManagerField.get(world);
                    
                    // 适用于所有1.20.x版本的区块生成器获取方法
                    java.lang.reflect.Field threadedAnvilChunkStorageField = chunkManager.getClass().getDeclaredField("threadedAnvilChunkStorage");
                    threadedAnvilChunkStorageField.setAccessible(true);
                    Object threadedAnvilChunkStorage = threadedAnvilChunkStorageField.get(chunkManager);
                    
                    // 尝试从threadedAnvilChunkStorage中获取区块生成器
                    java.lang.reflect.Field generatorField = null;
                    try {
                        generatorField = threadedAnvilChunkStorage.getClass().getDeclaredField("chunkGenerator");
                    } catch (NoSuchFieldException e) {
                        // 如果找不到，尝试获取所有字段
                        UuzFabricTestProj.LOGGER.error("无法在threadedAnvilChunkStorage中找到区块生成器字段，可用字段有：");
                        for (java.lang.reflect.Field field : threadedAnvilChunkStorage.getClass().getDeclaredFields()) {
                            UuzFabricTestProj.LOGGER.error(" - " + field.getName());
                        }
                        
                        // 尝试直接使用反射调用getChunkGenerator方法
                        try {
                            java.lang.reflect.Method getChunkGeneratorMethod = chunkManager.getClass().getMethod("getChunkGenerator");
                            getChunkGeneratorMethod.setAccessible(true);
                            Object generatorFromMethod = getChunkGeneratorMethod.invoke(chunkManager);
                            
                            // 如果成功获取到当前生成器，尝试使用反射设置它
                            java.lang.reflect.Field[] fields = generatorFromMethod.getClass().getDeclaredFields();
                            for (java.lang.reflect.Field field : fields) {
                                field.setAccessible(true);
                                UuzFabricTestProj.LOGGER.info("尝试复制字段: " + field.getName());
                                try {
                                    Object value = field.get(generatorFromMethod);
                                    java.lang.reflect.Field targetField = skyIslandGenerator.getClass().getDeclaredField(field.getName());
                                    targetField.setAccessible(true);
                                    targetField.set(skyIslandGenerator, value);
                                } catch (Exception ex) {
                                    UuzFabricTestProj.LOGGER.error("复制字段 " + field.getName() + " 失败: " + ex.getMessage());
                                }
                            }
                            
                            // 尝试使用反射设置区块生成器
                            try {
                                java.lang.reflect.Field generatorField2 = chunkManager.getClass().getDeclaredField("generator");
                                generatorField2.setAccessible(true);
                                generatorField2.set(chunkManager, skyIslandGenerator);
                            } catch (NoSuchFieldException ex) {
                                // 如果找不到generator字段，尝试使用反射调用setChunkGenerator方法
                                try {
                                    java.lang.reflect.Method setChunkGeneratorMethod = chunkManager.getClass().getMethod("setChunkGenerator", ChunkGenerator.class);
                                    setChunkGeneratorMethod.setAccessible(true);
                                    setChunkGeneratorMethod.invoke(chunkManager, skyIslandGenerator);
                                } catch (Exception ex2) {
                                    throw new RuntimeException("无法设置区块生成器", ex2);
                                }
                            }
                            
                            UuzFabricTestProj.LOGGER.info("成功替换为空岛区块生成器");
                            return;
                        } catch (Exception ex) {
                            throw new RuntimeException("无法设置区块生成器", ex);
                        }
                    }
                    
                    generatorField.setAccessible(true);
                    generatorField.set(threadedAnvilChunkStorage, skyIslandGenerator);
                    
                    UuzFabricTestProj.LOGGER.info("成功替换为空岛区块生成器");
                } catch (Exception e) {
                    UuzFabricTestProj.LOGGER.error("替换区块生成器失败", e);
                }
            }
        }
    }
} 