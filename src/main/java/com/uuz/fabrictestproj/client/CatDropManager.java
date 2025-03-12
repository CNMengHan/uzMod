package com.uuz.fabrictestproj.client;

import com.uuz.fabrictestproj.network.CatDropPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ItemEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CatDropManager {
    private static final int REQUIRED_TICKS = 60; // 3秒 = 60 ticks
    private static final Map<CatEntity, Integer> observedCats = new HashMap<>();
    private static final Random random = new Random();
    private static CatEntity lastObservedCat = null;

    public static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        // 检查玩家是否在蹲着
        if (!client.player.isSneaking()) {
            observedCats.clear();
            lastObservedCat = null;
            return;
        }

        // 获取玩家视线所指的实体
        HitResult hitResult = client.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            observedCats.clear();
            lastObservedCat = null;
            return;
        }

        EntityHitResult entityHit = (EntityHitResult) hitResult;
        if (!(entityHit.getEntity() instanceof CatEntity cat)) {
            observedCats.clear();
            lastObservedCat = null;
            return;
        }

        // 更新观察时间
        if (lastObservedCat != cat) {
            observedCats.clear();
            lastObservedCat = cat;
        }

        int currentTicks = observedCats.getOrDefault(cat, 0) + 1;
        observedCats.put(cat, currentTicks);

        // 检查是否达到所需时间
        if (currentTicks >= REQUIRED_TICKS) {
            spawnRandomItem(cat);
            observedCats.clear();
            lastObservedCat = null;
        }
    }

    private static void spawnRandomItem(CatEntity cat) {
        if (cat.getWorld().isClient) {
            // 在客户端发送数据包到服务器
            Vec3d pos = cat.getPos();
            CatDropPacket.send(cat.getId(), pos.x, pos.y, pos.z);
            return;
        }

        // 获取随机物品
        Item randomItem = getRandomItem();
        ItemStack itemStack = new ItemStack(randomItem);

        // 在猫的位置生成掉落物
        Vec3d pos = cat.getPos();
        ItemEntity itemEntity = new ItemEntity(
            cat.getWorld(),
            pos.x,
            pos.y + 0.5,
            pos.z,
            itemStack
        );

        // 添加一些随机速度
        itemEntity.setVelocity(
            (random.nextDouble() - 0.5) * 0.2,
            random.nextDouble() * 0.2,
            (random.nextDouble() - 0.5) * 0.2
        );

        cat.getWorld().spawnEntity(itemEntity);
    }

    private static Item getRandomItem() {
        // 获取所有已注册的物品
        var items = Registries.ITEM.stream()
            .filter(item -> item != Items.AIR) // 排除空气
            .toList();
        
        return items.get(random.nextInt(items.size()));
    }
} 