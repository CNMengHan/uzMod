package com.uuz.fabrictestproj.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HomeManager {
    private static final Map<String, HomeLocation> homes = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SAVE_FILE_NAME = "uuz_homes.json";
    private static Path saveFilePath;
    
    public static void initialize(MinecraftServer server) {
        saveFilePath = server.getSavePath(WorldSavePath.ROOT).resolve(SAVE_FILE_NAME);
        loadHomes();
        
        // 注册服务器关闭事件，保存家的数据
        ServerLifecycleEvents.SERVER_STOPPING.register(server1 -> saveHomes());
    }
    
    /**
     * 设置一个新的家
     * @param name 家的名称
     * @param player 设置家的玩家
     * @return 是否成功设置
     */
    public static boolean setHome(String name, ServerPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        String dimension = getDimensionKey(player.getWorld());
        
        HomeLocation home = new HomeLocation(
            name,
            player.getName().getString(),
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            dimension,
            System.currentTimeMillis()
        );
        
        homes.put(name.toLowerCase(), home);
        saveHomes();
        
        // 广播消息
        broadcastHomeSet(player, home);
        
        return true;
    }
    
    /**
     * 传送玩家到指定的家
     * @param name 家的名称
     * @param player 要传送的玩家
     * @return 是否成功传送
     */
    public static boolean teleportToHome(String name, ServerPlayerEntity player) {
        HomeLocation home = homes.get(name.toLowerCase());
        if (home == null) {
            return false;
        }
        
        // 获取目标维度
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        
        ServerWorld targetWorld = null;
        for (ServerWorld world : server.getWorlds()) {
            if (getDimensionKey(world).equals(home.dimension)) {
                targetWorld = world;
                break;
            }
        }
        
        if (targetWorld == null) return false;
        
        player.teleport(
            targetWorld,
            home.x,
            home.y,
            home.z,
            player.getYaw(),
            player.getPitch()
        );
        
        return true;
    }
    
    /**
     * 获取所有家的列表
     * @return 家的列表
     */
    public static List<HomeLocation> getAllHomes() {
        return new ArrayList<>(homes.values());
    }
    
    /**
     * 获取指定名称的家
     * @param name 家的名称
     * @return 家的位置信息，如果不存在则返回null
     */
    public static HomeLocation getHome(String name) {
        return homes.get(name.toLowerCase());
    }
    
    /**
     * 保存所有家的数据到文件
     */
    private static void saveHomes() {
        try {
            if (!Files.exists(saveFilePath)) {
                Files.createDirectories(saveFilePath.getParent());
            }
            
            String json = GSON.toJson(homes);
            Files.writeString(saveFilePath, json);
        } catch (IOException e) {
            UuzFabricTestProj.LOGGER.error("保存家数据时出错", e);
        }
    }
    
    /**
     * 从文件加载所有家的数据
     */
    private static void loadHomes() {
        try {
            if (!Files.exists(saveFilePath)) {
                return;
            }
            
            String json = Files.readString(saveFilePath);
            Type type = new TypeToken<HashMap<String, HomeLocation>>(){}.getType();
            Map<String, HomeLocation> loadedHomes = GSON.fromJson(json, type);
            
            if (loadedHomes != null) {
                homes.clear();
                homes.putAll(loadedHomes);
            }
        } catch (IOException e) {
            UuzFabricTestProj.LOGGER.error("加载家数据时出错", e);
        }
    }
    
    /**
     * 获取维度的标识符
     * @param world 世界
     * @return 维度标识符
     */
    private static String getDimensionKey(World world) {
        return world.getRegistryKey().getValue().toString();
    }
    
    /**
     * 广播家设置的消息
     * @param player 设置家的玩家
     * @param home 家的位置信息
     */
    private static void broadcastHomeSet(ServerPlayerEntity player, HomeLocation home) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        
        String dimensionName;
        switch (home.dimension) {
            case "minecraft:overworld":
                dimensionName = "主世界";
                break;
            case "minecraft:the_nether":
                dimensionName = "下界";
                break;
            case "minecraft:the_end":
                dimensionName = "末地";
                break;
            default:
                dimensionName = home.dimension;
                break;
        }
        
        // 创建可点击的坐标文本
        MutableText posText = Text.literal(String.format("§e[%d, %d, %d]", (int)home.x, (int)home.y, (int)home.z))
            .styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uuz home " + home.name))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§a点击传送到这个家")))
            );
        
        // 创建广播消息
        MutableText message = Text.literal("")
            .append(Text.literal("§a玩家 ").append(player.getName().copy().formatted(Formatting.GREEN)))
            .append(Text.literal(" §a设置了一个新的家: "))
            .append(Text.literal("§b" + home.name))
            .append(Text.literal(" §a在 "))
            .append(Text.literal("§d" + dimensionName))
            .append(Text.literal(" §a的 "))
            .append(posText)
            .append(Text.literal(" §a(点击坐标可传送)"));
        
        // 广播给所有玩家
        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            serverPlayer.sendMessage(message, false);
        }
    }
    
    /**
     * 家的位置信息类
     */
    public static class HomeLocation {
        public final String name;
        public final String owner;
        public final double x;
        public final double y;
        public final double z;
        public final String dimension;
        public final long timestamp;
        
        public HomeLocation(String name, String owner, double x, double y, double z, String dimension, long timestamp) {
            this.name = name;
            this.owner = owner;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
            this.timestamp = timestamp;
        }
    }
} 