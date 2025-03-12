package com.uuz.fabrictestproj.item;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.enchantment.FireballEnchantment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 模组物品注册类
 */
public class ModItems {
    // 创建物品组
    public static final RegistryKey<ItemGroup> UUZ_ITEM_GROUP = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            new Identifier(UuzFabricTestProj.MOD_ID, "uuz_items")
    );
    
    // 注册煎蛋卷物品
    public static final Item COOKIE_EGG_JUAN = registerItem("cookie_egg_juan",
            new CookieEggJuanItem(new FabricItemSettings()));
    
    /**
     * 初始化物品
     */
    public static void initialize() {
        UuzFabricTestProj.LOGGER.info("注册模组物品...");
        
        // 创建物品组
        Registry.register(Registries.ITEM_GROUP, UUZ_ITEM_GROUP, FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.uuzfabrictestproj.uuz_items"))
                .icon(() -> new ItemStack(COOKIE_EGG_JUAN)) // 使用煎蛋卷作为图标
                .build());
        
        // 将物品添加到物品组
        ItemGroupEvents.modifyEntriesEvent(UUZ_ITEM_GROUP).register(content -> {
            content.add(COOKIE_EGG_JUAN);
            
            // 添加不同等级的火球术附魔书
            FireballEnchantment fireball = FireballEnchantment.getInstance();
            for (int level = 1; level <= fireball.getMaxLevel(); level++) {
                ItemStack enchantedBook = new ItemStack(net.minecraft.item.Items.ENCHANTED_BOOK);
                EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentLevelEntry(fireball, level));
                content.add(enchantedBook);
            }
        });
    }
    
    /**
     * 注册物品
     */
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(UuzFabricTestProj.MOD_ID, name), item);
    }
} 