package com.uuz.fabrictestproj.handler;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.client.InfiniteArrowsManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 无限箭矢处理器
 */
public class InfiniteArrowsHandler {
    
    /**
     * 注册无限箭矢处理器
     */
    public static void register() {
        // 使用UseItemCallback监听物品使用事件
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            
            // 检查是否启用了无限箭矢功能
            if (InfiniteArrowsManager.isEnabled()) {
                // 检查物品是否是远程武器（如弓）
                if (stack.getItem() instanceof RangedWeaponItem) {
                    // 确保玩家有箭矢可用
                    ensurePlayerHasArrow(player);
                }
            }
            
            // 不修改原始行为，继续传递事件
            return TypedActionResult.pass(stack);
        });
        
        UuzFabricTestProj.LOGGER.info("无限箭矢处理器已注册");
    }
    
    /**
     * 确保玩家有箭矢可用
     */
    private static void ensurePlayerHasArrow(PlayerEntity player) {
        // 检查玩家是否已经有箭矢
        boolean hasArrow = player.getInventory().contains(new ItemStack(Items.ARROW));
        
        // 如果没有箭矢，添加一个隐形的箭矢
        if (!hasArrow) {
            ItemStack arrow = new ItemStack(Items.ARROW);
            // 设置特殊NBT标签，标记为无限箭矢
            arrow.getOrCreateNbt().putBoolean("InfiniteArrow", true);
            player.getInventory().insertStack(arrow);
        }
    }
} 