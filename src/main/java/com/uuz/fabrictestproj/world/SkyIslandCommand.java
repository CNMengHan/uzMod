package com.uuz.fabrictestproj.world;

import com.mojang.brigadier.CommandDispatcher;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;

/**
 * 空岛世界命令
 */
public class SkyIslandCommand {
    
    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("skyisland")
                    .requires(source -> source.hasPermissionLevel(2)) // 需要权限等级2（OP）
                    .executes(context -> {
                        return convertToSkyIsland(context.getSource());
                    })
                )
        );
        
        UuzFabricTestProj.LOGGER.info("空岛世界命令已注册");
    }
    
    /**
     * 将当前世界转换为空岛世界
     */
    private static int convertToSkyIsland(ServerCommandSource source) {
        try {
            // 获取当前世界
            ServerWorld world = source.getWorld();
            
            // 检查是否是主世界
            if (world.getRegistryKey() != World.OVERWORLD) {
                source.sendFeedback(() -> Text.literal("只能在主世界使用此命令"), false);
                return 0;
            }
            
            // 获取当前的生物群系源
            ChunkGenerator currentGenerator = world.getChunkManager().getChunkGenerator();
            BiomeSource biomeSource = currentGenerator.getBiomeSource();
            
            // 创建空岛区块生成器
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
                        
                        // 使用反射设置区块生成器
                        java.lang.reflect.Field generatorField2 = chunkManager.getClass().getDeclaredField("generator");
                        generatorField2.setAccessible(true);
                        generatorField2.set(chunkManager, skyIslandGenerator);
                        
                        source.sendFeedback(() -> Text.literal("世界已转换为空岛世界！新生成的区块将是空岛地形。"), true);
                        UuzFabricTestProj.LOGGER.info("成功将世界转换为空岛世界");
                        return 1;
                    } catch (Exception ex) {
                        throw new RuntimeException("无法设置区块生成器", ex);
                    }
                }
                
                generatorField.setAccessible(true);
                generatorField.set(threadedAnvilChunkStorage, skyIslandGenerator);
                
                source.sendFeedback(() -> Text.literal("世界已转换为空岛世界！新生成的区块将是空岛地形。"), true);
                UuzFabricTestProj.LOGGER.info("成功将世界转换为空岛世界");
                return 1;
            } catch (Exception e) {
                source.sendError(Text.literal("转换失败：" + e.getMessage()));
                UuzFabricTestProj.LOGGER.error("替换区块生成器失败", e);
                return 0;
            }
        } catch (Exception e) {
            source.sendError(Text.literal("转换失败：" + e.getMessage()));
            UuzFabricTestProj.LOGGER.error("转换世界失败", e);
            return 0;
        }
    }
} 