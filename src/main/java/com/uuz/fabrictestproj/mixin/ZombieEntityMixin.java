package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.handler.ZombieEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity implements ZombieEntityAccessor {
    @Shadow
    protected void initGoals() {}

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * 在僵尸初始化时为其添加随机护甲
     */
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // 为僵尸添加随机护甲
        equipRandomArmor();
    }

    @Override
    public void invokeInitGoals() {
        this.initGoals();
    }

    /**
     * 为僵尸添加随机护甲
     */
    private void equipRandomArmor() {
        Random random = this.getRandom();
        
        // 定义可能的护甲材质
        ArmorMaterial[] materials = {
            ArmorMaterials.LEATHER,
            ArmorMaterials.CHAIN,
            ArmorMaterials.IRON,
            ArmorMaterials.GOLD,
            ArmorMaterials.DIAMOND,
            ArmorMaterials.NETHERITE
        };
        
        // 为每个装备槽位选择随机护甲
        for (EquipmentSlot slot : new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            // 随机选择一种材质
            ArmorMaterial material = materials[random.nextInt(materials.length)];
            
            // 根据槽位和材质获取对应的护甲物品
            Item armorItem = getArmorItem(slot, material);
            
            if (armorItem != null) {
                this.equipStack(slot, new ItemStack(armorItem));
                // 设置不掉落
                ((ZombieEntity)(Object)this).setEquipmentDropChance(slot, 0.0F);
            }
        }
    }
    
    /**
     * 根据槽位和材质获取对应的护甲物品
     */
    private Item getArmorItem(EquipmentSlot slot, ArmorMaterial material) {
        Map<ArmorMaterial, Map<EquipmentSlot, Item>> armorMap = new HashMap<>();
        
        // 皮革护甲
        Map<EquipmentSlot, Item> leatherMap = new HashMap<>();
        leatherMap.put(EquipmentSlot.HEAD, Items.LEATHER_HELMET);
        leatherMap.put(EquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE);
        leatherMap.put(EquipmentSlot.LEGS, Items.LEATHER_LEGGINGS);
        leatherMap.put(EquipmentSlot.FEET, Items.LEATHER_BOOTS);
        armorMap.put(ArmorMaterials.LEATHER, leatherMap);
        
        // 锁链护甲
        Map<EquipmentSlot, Item> chainMap = new HashMap<>();
        chainMap.put(EquipmentSlot.HEAD, Items.CHAINMAIL_HELMET);
        chainMap.put(EquipmentSlot.CHEST, Items.CHAINMAIL_CHESTPLATE);
        chainMap.put(EquipmentSlot.LEGS, Items.CHAINMAIL_LEGGINGS);
        chainMap.put(EquipmentSlot.FEET, Items.CHAINMAIL_BOOTS);
        armorMap.put(ArmorMaterials.CHAIN, chainMap);
        
        // 铁护甲
        Map<EquipmentSlot, Item> ironMap = new HashMap<>();
        ironMap.put(EquipmentSlot.HEAD, Items.IRON_HELMET);
        ironMap.put(EquipmentSlot.CHEST, Items.IRON_CHESTPLATE);
        ironMap.put(EquipmentSlot.LEGS, Items.IRON_LEGGINGS);
        ironMap.put(EquipmentSlot.FEET, Items.IRON_BOOTS);
        armorMap.put(ArmorMaterials.IRON, ironMap);
        
        // 金护甲
        Map<EquipmentSlot, Item> goldMap = new HashMap<>();
        goldMap.put(EquipmentSlot.HEAD, Items.GOLDEN_HELMET);
        goldMap.put(EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE);
        goldMap.put(EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS);
        goldMap.put(EquipmentSlot.FEET, Items.GOLDEN_BOOTS);
        armorMap.put(ArmorMaterials.GOLD, goldMap);
        
        // 钻石护甲
        Map<EquipmentSlot, Item> diamondMap = new HashMap<>();
        diamondMap.put(EquipmentSlot.HEAD, Items.DIAMOND_HELMET);
        diamondMap.put(EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE);
        diamondMap.put(EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS);
        diamondMap.put(EquipmentSlot.FEET, Items.DIAMOND_BOOTS);
        armorMap.put(ArmorMaterials.DIAMOND, diamondMap);
        
        // 下界合金护甲
        Map<EquipmentSlot, Item> netheriteMap = new HashMap<>();
        netheriteMap.put(EquipmentSlot.HEAD, Items.NETHERITE_HELMET);
        netheriteMap.put(EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE);
        netheriteMap.put(EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS);
        netheriteMap.put(EquipmentSlot.FEET, Items.NETHERITE_BOOTS);
        armorMap.put(ArmorMaterials.NETHERITE, netheriteMap);
        
        return armorMap.get(material).get(slot);
    }
} 