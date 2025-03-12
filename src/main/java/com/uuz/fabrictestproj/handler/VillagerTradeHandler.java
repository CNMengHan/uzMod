package com.uuz.fabrictestproj.handler;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理村民随机交易
 */
public class VillagerTradeHandler {
    // 存储每个村民的随机交易
    private static final Map<Integer, List<Item>> villagerRandomItems = new HashMap<>();
    
    // 不应该作为随机物品的物品列表
    private static final List<Item> EXCLUDED_ITEMS = List.of(
        Items.AIR, Items.BARRIER, Items.STRUCTURE_VOID, Items.COMMAND_BLOCK, 
        Items.CHAIN_COMMAND_BLOCK, Items.REPEATING_COMMAND_BLOCK, Items.COMMAND_BLOCK_MINECART,
        Items.DEBUG_STICK, Items.JIGSAW, Items.STRUCTURE_BLOCK, Items.LIGHT
    );
    
    // 每个村民提供的随机交易数量
    private static final int RANDOM_TRADES_COUNT = 100;
    
    /**
     * 为村民添加随机交易
     * @param villager 村民实体
     */
    public static void addRandomTrades(VillagerEntity villager) {
        // 如果村民没有职业，不添加随机交易
        if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
            return;
        }
        
        // 获取村民的ID
        int villagerId = villager.getId();
        
        // 获取村民的交易列表
        TradeOfferList tradeOffers = villager.getOffers();
        
        // 如果已经添加了随机交易，不再添加
        if (hasRandomTrades(tradeOffers)) {
            return;
        }
        
        // 获取或生成该村民的随机物品
        List<Item> randomItems = getRandomItemsForVillager(villagerId, villager.getRandom());
        
        // 添加随机交易
        for (int i = 0; i < RANDOM_TRADES_COUNT && i < randomItems.size(); i++) {
            Item item = randomItems.get(i);
            
            // 创建交易：2个绿宝石换1个随机物品
            ItemStack price = new ItemStack(Items.EMERALD, 2);
            ItemStack result = new ItemStack(item, 1);
            
            // 创建交易，设置无限使用次数
            TradeOffer offer = new TradeOffer(price, result, Integer.MAX_VALUE, 0, 0.0f);
            
            // 添加到交易列表
            tradeOffers.add(offer);
        }
    }
    
    /**
     * 检查交易列表是否已经包含随机交易
     */
    private static boolean hasRandomTrades(TradeOfferList tradeOffers) {
        // 如果交易数量大于正常职业村民的交易数量，认为已添加随机交易
        return tradeOffers.size() > 2;
    }
    
    /**
     * 获取指定村民的随机物品列表
     */
    private static List<Item> getRandomItemsForVillager(int villagerId, Random random) {
        // 如果已经有为该村民生成的随机物品，直接返回
        if (villagerRandomItems.containsKey(villagerId)) {
            return villagerRandomItems.get(villagerId);
        }
        
        // 否则生成新的随机物品列表
        List<Item> randomItems = generateRandomItems(random);
        villagerRandomItems.put(villagerId, randomItems);
        return randomItems;
    }
    
    /**
     * 生成随机物品列表
     */
    private static List<Item> generateRandomItems(Random random) {
        List<Item> allItems = new ArrayList<>();
        
        // 收集所有可用的物品
        for (RegistryEntry<Item> entry : Registries.ITEM.streamEntries().toList()) {
            Item item = entry.value();
            
            // 排除不应该作为随机物品的物品
            if (!EXCLUDED_ITEMS.contains(item) && !item.toString().contains("spawn_egg")) {
                allItems.add(item);
            }
        }
        
        // 随机选择不同的物品
        List<Item> selectedItems = new ArrayList<>();
        for (int i = 0; i < RANDOM_TRADES_COUNT && !allItems.isEmpty(); i++) {
            int index = random.nextInt(allItems.size());
            selectedItems.add(allItems.get(index));
            allItems.remove(index);
        }
        
        return selectedItems;
    }
} 