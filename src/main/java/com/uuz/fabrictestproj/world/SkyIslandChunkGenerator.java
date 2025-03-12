package com.uuz.fabrictestproj.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.List;

public class SkyIslandChunkGenerator extends ChunkGenerator {
    public static final Codec<SkyIslandChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed)
            ).apply(instance, SkyIslandChunkGenerator::new)
    );

    private final long seed;
    // 空岛高度范围
    private static final int MIN_ISLAND_HEIGHT = 100;
    private static final int MAX_ISLAND_HEIGHT = 150;
    // 底部山脉高度范围
    private static final int MIN_MOUNTAIN_HEIGHT = 10;
    private static final int MAX_MOUNTAIN_HEIGHT = 60;
    // 连接柱高度范围
    private static final int MIN_PILLAR_HEIGHT = 70;
    private static final int MAX_PILLAR_HEIGHT = 90;

    public SkyIslandChunkGenerator(BiomeSource biomeSource, long seed) {
        super(biomeSource);
        this.seed = seed;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
        // 不进行洞穴生成
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
        // 表面处理在generateFeatures中完成
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            // 生成底部山脉和空岛
            generateTerrain(chunk);
            return chunk;
        }, executor);
    }

    private void generateTerrain(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        Random random = Random.create(chunkX * 341873128712L + chunkZ * 132897987541L + this.seed);

        // 遍历区块中的每个x,z坐标
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                // 生成底部山脉
                generateMountain(chunk, x, z, worldX, worldZ, random);
                
                // 使用改进的噪声函数决定是否生成岛屿
                double islandValue = getIslandValue(worldX, worldZ);
                
                if (islandValue > 0.4) {
                    // 确定岛屿的中心高度 (100-150范围)
                    int baseHeight = MIN_ISLAND_HEIGHT + (int)(getHeightValue(worldX, worldZ) * (MAX_ISLAND_HEIGHT - MIN_ISLAND_HEIGHT));
                    
                    // 确定岛屿的厚度
                    int thickness = 10 + (int)(getThicknessValue(worldX, worldZ) * 20);
                    
                    // 生成岛屿
                    generateIsland(chunk, x, z, baseHeight, thickness, islandValue, random);
                    
                    // 有一定概率生成连接柱
                    if (random.nextDouble() < 0.02 && islandValue > 0.6) {
                        generateConnectionPillar(chunk, x, z, baseHeight, random);
                    }
                }
            }
        }
    }

    private void generateMountain(Chunk chunk, int x, int z, int worldX, int worldZ, Random random) {
        // 使用噪声函数确定山脉高度
        double mountainNoise = getMountainValue(worldX, worldZ);
        int mountainHeight = MIN_MOUNTAIN_HEIGHT + (int)(mountainNoise * (MAX_MOUNTAIN_HEIGHT - MIN_MOUNTAIN_HEIGHT));
        
        // 生成基岩层
        chunk.setBlockState(new BlockPos(x, 0, z), Blocks.BEDROCK.getDefaultState(), false);
        
        // 生成山体
        for (int y = 1; y <= mountainHeight; y++) {
            BlockState blockState;
            
            // 顶层放置草方块
            if (y == mountainHeight) {
                blockState = Blocks.GRASS_BLOCK.getDefaultState();
            } 
            // 顶层下方3层放置泥土
            else if (y > mountainHeight - 4) {
                blockState = Blocks.DIRT.getDefaultState();
            } 
            // 其余部分放置石头和矿物
            else {
                blockState = Blocks.STONE.getDefaultState();
                
                // 随机添加矿物
                if (random.nextDouble() < 0.05) {
                    if (y < 20) {
                        blockState = random.nextDouble() < 0.3 ? 
                            Blocks.DIAMOND_ORE.getDefaultState() : 
                            Blocks.DEEPSLATE_DIAMOND_ORE.getDefaultState();
                    } else if (y < 40) {
                        blockState = Blocks.IRON_ORE.getDefaultState();
                    } else {
                        blockState = Blocks.COAL_ORE.getDefaultState();
                    }
                }
            }
            
            chunk.setBlockState(new BlockPos(x, y, z), blockState, false);
        }
        
        // 在山顶添加植被
        if (random.nextDouble() < 0.3 && mountainHeight > 30) {
            if (random.nextDouble() < 0.7) {
                // 放置草
                chunk.setBlockState(new BlockPos(x, mountainHeight + 1, z), Blocks.GRASS.getDefaultState(), false);
            } else {
                // 放置花
                BlockState flowerState = random.nextBoolean() ? 
                    Blocks.DANDELION.getDefaultState() : 
                    Blocks.POPPY.getDefaultState();
                chunk.setBlockState(new BlockPos(x, mountainHeight + 1, z), flowerState, false);
            }
            
            // 在较高的山顶生成树
            if (mountainHeight > 45 && random.nextDouble() < 0.1) {
                placeTree(chunk, x, mountainHeight, z, random);
            }
        }
    }

    private void generateConnectionPillar(Chunk chunk, int x, int z, int islandBaseHeight, Random random) {
        // 获取山脉高度
        int worldX = chunk.getPos().x * 16 + x;
        int worldZ = chunk.getPos().z * 16 + z;
        double mountainNoise = getMountainValue(worldX, worldZ);
        int mountainHeight = MIN_MOUNTAIN_HEIGHT + (int)(mountainNoise * (MAX_MOUNTAIN_HEIGHT - MIN_MOUNTAIN_HEIGHT));
        
        // 确定连接柱的起点和终点
        int pillarBottom = mountainHeight;
        int pillarTop = islandBaseHeight - 5; // 连接到岛屿底部
        
        // 生成连接柱
        for (int y = pillarBottom; y <= pillarTop; y++) {
            // 添加一些变化，使连接柱不是完全垂直的
            double offset = Math.sin(y * 0.1) * 0.5;
            int offsetX = (int) offset;
            int offsetZ = (int) (offset * 1.5);
            
            // 确保偏移后的坐标仍在区块内
            int newX = Math.max(0, Math.min(15, x + offsetX));
            int newZ = Math.max(0, Math.min(15, z + offsetZ));
            
            // 放置石柱方块
            BlockState blockState;
            if (random.nextDouble() < 0.7) {
                blockState = Blocks.STONE.getDefaultState();
            } else if (random.nextDouble() < 0.5) {
                blockState = Blocks.COBBLESTONE.getDefaultState();
            } else {
                blockState = Blocks.MOSSY_COBBLESTONE.getDefaultState();
            }
            
            chunk.setBlockState(new BlockPos(newX, y, newZ), blockState, false);
            
            // 在石柱周围随机添加一些方块，使其看起来更自然
            if (random.nextDouble() < 0.2) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        if (random.nextDouble() < 0.3) {
                            int nx = Math.max(0, Math.min(15, newX + dx));
                            int nz = Math.max(0, Math.min(15, newZ + dz));
                            chunk.setBlockState(new BlockPos(nx, y, nz), blockState, false);
                        }
                    }
                }
            }
        }
    }

    private void generateIsland(Chunk chunk, int x, int z, int baseHeight, int thickness, double islandValue, Random random) {
        // 岛屿的顶部和底部
        int top = baseHeight + thickness / 2;
        int bottom = baseHeight - thickness / 2;
        
        // 使用改进的算法生成更自然的岛屿形状
        double islandRadius = 4 + islandValue * 8; // 岛屿半径基于岛屿值
        
        // 生成岛屿核心
        for (int y = bottom; y <= top; y++) {
            // 计算与岛屿中心的垂直距离比例
            double verticalDistRatio = Math.abs(y - baseHeight) / (double)(thickness / 2);
            
            // 根据垂直距离调整岛屿半径，创建椭球形状
            double adjustedRadius = islandRadius * (1 - verticalDistRatio * 0.8);
            
            // 添加一些随机性，使岛屿边缘不规则
            double edgeNoise = (random.nextDouble() * 0.4 - 0.2) * adjustedRadius;
            double finalRadius = adjustedRadius + edgeNoise;
            
            // 如果在岛屿范围内，放置方块
            if (finalRadius > 0) {
                // 计算岛屿中心到当前点的水平距离
                double centerX = x + random.nextDouble() * 2 - 1; // 轻微偏移中心点
                double centerZ = z + random.nextDouble() * 2 - 1;
                double distFromCenter = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                
                if (distFromCenter < finalRadius) {
                    BlockState blockState;
                    
                    // 顶层放置草方块
                    if (y == top) {
                        blockState = Blocks.GRASS_BLOCK.getDefaultState();
                    } 
                    // 顶层下方3层放置泥土
                    else if (y > top - 4) {
                        blockState = Blocks.DIRT.getDefaultState();
                    } 
                    // 其余部分放置石头
                    else {
                        blockState = Blocks.STONE.getDefaultState();
                        
                        // 随机添加矿物
                        if (random.nextDouble() < 0.04) {
                            if (y < 30) {
                                blockState = Blocks.DIAMOND_ORE.getDefaultState();
                            } else if (y < 50) {
                                blockState = Blocks.IRON_ORE.getDefaultState();
                            } else if (y < 80) {
                                blockState = Blocks.GOLD_ORE.getDefaultState();
                            } else {
                                blockState = Blocks.COAL_ORE.getDefaultState();
                            }
                        }
                    }
                    
                    chunk.setBlockState(new BlockPos(x, y, z), blockState, false);
                }
            }
        }
        
        // 在岛屿顶部添加更多植被
        if (chunk.getBlockState(new BlockPos(x, top, z)).isIn(BlockTags.DIRT)) {
            if (random.nextDouble() < 0.4) {
                if (random.nextDouble() < 0.6) {
                    // 放置草
                    chunk.setBlockState(new BlockPos(x, top + 1, z), Blocks.GRASS.getDefaultState(), false);
                } else {
                    // 放置花
                    BlockState flowerState;
                    double flowerRoll = random.nextDouble();
                    if (flowerRoll < 0.3) {
                        flowerState = Blocks.DANDELION.getDefaultState();
                    } else if (flowerRoll < 0.6) {
                        flowerState = Blocks.POPPY.getDefaultState();
                    } else if (flowerRoll < 0.8) {
                        flowerState = Blocks.BLUE_ORCHID.getDefaultState();
                    } else {
                        flowerState = Blocks.ALLIUM.getDefaultState();
                    }
                    chunk.setBlockState(new BlockPos(x, top + 1, z), flowerState, false);
                }
            }
            
            // 增加树木生成概率
            if (islandValue > 0.5 && random.nextDouble() < 0.15 && islandRadius > 5) {
                placeTree(chunk, x, top, z, random);
            }
        }
    }
    
    private void placeTree(Chunk chunk, int x, int y, int z, Random random) {
        // 确保有足够的空间生成树
        if (x > 2 && x < 13 && z > 2 && z < 13) {
            // 选择树木类型
            BlockState saplingState;
            double treeType = random.nextDouble();
            
            if (treeType < 0.6) {
                saplingState = Blocks.OAK_SAPLING.getDefaultState();
            } else if (treeType < 0.8) {
                saplingState = Blocks.BIRCH_SAPLING.getDefaultState();
            } else if (treeType < 0.95) {
                saplingState = Blocks.SPRUCE_SAPLING.getDefaultState();
            } else {
                saplingState = Blocks.JUNGLE_SAPLING.getDefaultState();
            }
            
            // 放置树苗
            chunk.setBlockState(new BlockPos(x, y + 1, z), saplingState, false);
            
            // 在树苗周围放置一些草和花，创造更自然的环境
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    if (random.nextDouble() < 0.3) {
                        int nx = x + dx;
                        int nz = z + dz;
                        if (nx >= 0 && nx < 16 && nz >= 0 && nz < 16) {
                            if (chunk.getBlockState(new BlockPos(nx, y, nz)).isIn(BlockTags.DIRT)) {
                                if (random.nextDouble() < 0.7) {
                                    chunk.setBlockState(new BlockPos(nx, y + 1, nz), Blocks.GRASS.getDefaultState(), false);
                                } else {
                                    chunk.setBlockState(new BlockPos(nx, y + 1, nz), Blocks.DANDELION.getDefaultState(), false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 改进的噪声函数，使用柏林噪声或简化版本
    private double getIslandValue(int x, int z) {
        double scale1 = 0.01;
        double scale2 = 0.05;
        double scale3 = 0.002;
        
        // 使用多个不同频率的噪声叠加，创造更自然的分布
        double noise1 = Math.sin(x * scale1) * Math.cos(z * scale1);
        double noise2 = Math.sin(x * scale2 + 0.5) * Math.cos(z * scale2 + 0.5) * 0.5;
        double noise3 = Math.sin(x * scale3 + 1.0) * Math.cos(z * scale3 + 1.0) * 0.25;
        
        // 添加一些随机偏移，打破规则性
        double offset = (Math.sin(x * 0.1) + Math.cos(z * 0.1)) * 0.1;
        
        return (noise1 + noise2 + noise3 + offset + 2) / 4.0;
    }

    // 改进的高度噪声函数
    private double getHeightValue(int x, int z) {
        double scale1 = 0.015;
        double scale2 = 0.03;
        
        double noise1 = Math.sin(x * scale1 + 0.1) * Math.cos(z * scale1 + 0.1);
        double noise2 = Math.sin(x * scale2 + 1.5) * Math.cos(z * scale2 + 1.5) * 0.5;
        
        return (noise1 + noise2 + 2) / 4.0;
    }

    // 改进的厚度噪声函数
    private double getThicknessValue(int x, int z) {
        double scale1 = 0.02;
        double scale2 = 0.04;
        
        double noise1 = Math.sin(x * scale1 + 0.7) * Math.cos(z * scale1 + 0.7);
        double noise2 = Math.sin(x * scale2 + 2.0) * Math.cos(z * scale2 + 2.0) * 0.3;
        
        return (noise1 + noise2 + 2) / 4.0;
    }
    
    // 山脉高度噪声函数
    private double getMountainValue(int x, int z) {
        double scale1 = 0.008;
        double scale2 = 0.02;
        
        double noise1 = Math.sin(x * scale1 + 0.3) * Math.cos(z * scale1 + 0.3);
        double noise2 = Math.sin(x * scale2 + 1.2) * Math.cos(z * scale2 + 1.2) * 0.4;
        
        return (noise1 + noise2 + 2) / 4.0;
    }

    @Override
    public int getWorldHeight() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return -64; // 没有海平面
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        // 返回空岛或山脉的高度，取决于位置
        double islandValue = getIslandValue(x, z);
        if (islandValue > 0.4) {
            // 空岛高度
            double heightValue = getHeightValue(x, z);
            return MIN_ISLAND_HEIGHT + (int)(heightValue * (MAX_ISLAND_HEIGHT - MIN_ISLAND_HEIGHT));
        } else {
            // 山脉高度
            double mountainValue = getMountainValue(x, z);
            return MIN_MOUNTAIN_HEIGHT + (int)(mountainValue * (MAX_MOUNTAIN_HEIGHT - MIN_MOUNTAIN_HEIGHT));
        }
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[world.getHeight()];
        // 这个方法用于调试，我们可以简单地返回空气
        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.getDefaultState();
        }
        return new VerticalBlockSample(world.getBottomY(), states);
    }
    
    @Override
    public void populateEntities(ChunkRegion region) {
        // 空岛世界不需要额外的实体生成
        ChunkPos chunkPos = region.getCenterPos();
        UuzFabricTestProj.LOGGER.debug("处理实体生成: " + chunkPos.x + ", " + chunkPos.z);
        // 默认实现为空，让游戏自然生成实体
    }
    
    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        // 添加调试信息到F3屏幕
        text.add("空岛生成器: " + UuzFabricTestProj.MOD_ID + ":sky_island");
        text.add("位置: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        text.add("岛屿值: " + String.format("%.2f", getIslandValue(pos.getX(), pos.getZ())));
        text.add("高度值: " + String.format("%.2f", getHeightValue(pos.getX(), pos.getZ())));
        text.add("厚度值: " + String.format("%.2f", getThicknessValue(pos.getX(), pos.getZ())));
        text.add("山脉值: " + String.format("%.2f", getMountainValue(pos.getX(), pos.getZ())));
    }
} 