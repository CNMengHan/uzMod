package com.uuz.fabrictestproj.network;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ItemEntity;
import java.util.Random;

public class CatDropPacket {
    private static final Random random = new Random();
    public static final Identifier CAT_DROP_PACKET_ID = new Identifier(UuzFabricTestProj.MOD_ID, "cat_drop");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CAT_DROP_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            int catId = buf.readInt();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();

            server.execute(() -> {
                Entity entity = player.getWorld().getEntityById(catId);
                if (!(entity instanceof CatEntity cat)) return;

                // 获取随机物品
                Item randomItem = getRandomItem();
                ItemStack itemStack = new ItemStack(randomItem);

                // 在猫的位置生成掉落物
                ItemEntity itemEntity = new ItemEntity(
                    cat.getWorld(),
                    x,
                    y + 0.5,
                    z,
                    itemStack
                );

                // 添加一些随机速度
                itemEntity.setVelocity(
                    (random.nextDouble() - 0.5) * 0.2,
                    random.nextDouble() * 0.2,
                    (random.nextDouble() - 0.5) * 0.2
                );

                cat.getWorld().spawnEntity(itemEntity);
            });
        });
    }

    public static void send(int catId, double x, double y, double z) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(catId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        ClientPlayNetworking.send(CAT_DROP_PACKET_ID, buf);
    }

    private static Item getRandomItem() {
        // 获取所有已注册的物品
        var items = Registries.ITEM.stream()
            .filter(item -> item != Items.AIR) // 排除空气
            .toList();
        
        return items.get(random.nextInt(items.size()));
    }
} 