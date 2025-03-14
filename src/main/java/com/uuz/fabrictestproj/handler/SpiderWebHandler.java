package com.uuz.fabrictestproj.handler;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 处理蜘蛛攻击玩家时生成蜘蛛网的功能
 */
public class SpiderWebHandler {
    
    // 蜘蛛网存在的时间（tick）
    private static final int WEB_DURATION_TICKS = 200; // 10秒
    
    // 存储蜘蛛网位置和剩余时间
    private static final Map<BlockPos, WebInfo> webPositions = new HashMap<>();
    
    /**
     * 注册蜘蛛网处理器
     */
    public static void register() {
        // 注册实体伤害事件
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // 检查被伤害的实体是否是玩家
            if (entity instanceof PlayerEntity player && !entity.getWorld().isClient) {
                // 获取伤害来源实体
                Entity attacker = source.getAttacker();
                
                // 检查攻击者是否是蜘蛛或剧毒蜘蛛
                if (attacker instanceof SpiderEntity || attacker instanceof CaveSpiderEntity) {
                    // 在玩家位置生成蜘蛛网
                    placeWebAtPlayer(player);
                }
            }
            
            // 允许伤害
            return true;
        });
        
        // 注册服务器tick事件，用于处理蜘蛛网的自动消失
        ServerTickEvents.END_SERVER_TICK.register(SpiderWebHandler::onServerTick);
    }
    
    /**
     * 在玩家位置生成蜘蛛网
     */
    private static void placeWebAtPlayer(PlayerEntity player) {
        World world = player.getWorld();
        BlockPos playerPos = player.getBlockPos();
        
        // 检查玩家位置是否为空气或可替换的方块
        if (world.getBlockState(playerPos).isAir() || 
            world.getBlockState(playerPos).isReplaceable()) {
            
            // 设置蜘蛛网方块
            world.setBlockState(playerPos, Blocks.COBWEB.getDefaultState());
            
            // 添加到蜘蛛网位置映射中
            webPositions.put(playerPos.toImmutable(), new WebInfo((ServerWorld) world, WEB_DURATION_TICKS));
        }
    }
    
    /**
     * 服务器tick事件处理
     */
    private static void onServerTick(MinecraftServer server) {
        // 处理蜘蛛网的自动消失
        Iterator<Map.Entry<BlockPos, WebInfo>> iterator = webPositions.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, WebInfo> entry = iterator.next();
            BlockPos pos = entry.getKey();
            WebInfo webInfo = entry.getValue();
            
            // 减少剩余时间
            webInfo.remainingTicks--;
            
            // 如果时间到了，移除蜘蛛网
            if (webInfo.remainingTicks <= 0) {
                ServerWorld world = webInfo.world;
                
                // 检查该位置是否仍然是蜘蛛网
                if (world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                    // 移除蜘蛛网
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
                
                // 从映射中移除
                iterator.remove();
            }
        }
    }
    
    /**
     * 蜘蛛网信息类
     */
    private static class WebInfo {
        public final ServerWorld world;
        public int remainingTicks;
        
        public WebInfo(ServerWorld world, int remainingTicks) {
            this.world = world;
            this.remainingTicks = remainingTicks;
        }
    }
} 