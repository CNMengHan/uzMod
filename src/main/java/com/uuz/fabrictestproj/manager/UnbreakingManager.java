package com.uuz.fabrictestproj.manager;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import java.util.UUID;

public class UnbreakingManager {
    private static final String UNBREAKING_TAG = "UuzUnbreaking";
    
    /**
     * 将物品添加到不消耗耐久度的列表中
     * @param itemStack 要添加的物品
     * @return 如果物品成功添加返回true，如果物品已经在列表中返回false
     */
    public static boolean addUnbreakingItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        
        // 检查物品是否已经有不消耗耐久度的标签
        if (isUnbreakingItem(itemStack)) {
            return false;
        }
        
        // 获取物品的NBT数据，如果没有则创建
        NbtCompound nbt = itemStack.getOrCreateNbt();
        
        // 添加不消耗耐久度的标签
        nbt.putBoolean(UNBREAKING_TAG, true);
        
        // 设置物品为不可损坏
        nbt.putBoolean("Unbreakable", true);
        
        UuzFabricTestProj.LOGGER.info("物品 {} 已设置为不会损坏", itemStack.getName().getString());
        return true;
    }
    
    /**
     * 检查物品是否是不消耗耐久度的物品
     * @param itemStack 要检查的物品
     * @return 如果物品是不消耗耐久度的物品返回true，否则返回false
     */
    public static boolean isUnbreakingItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        
        NbtCompound nbt = itemStack.getNbt();
        return nbt != null && nbt.getBoolean(UNBREAKING_TAG);
    }
    
    /**
     * 初始化不消耗耐久度管理器
     */
    public static void initialize() {
        // 这里可以添加事件监听器或其他初始化代码
        UuzFabricTestProj.LOGGER.info("不消耗耐久度系统已初始化");
    }
} 