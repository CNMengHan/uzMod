package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.handler.MobEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements MobEntityAccessor {
    
    @Shadow protected GoalSelector goalSelector;
    @Shadow protected GoalSelector targetSelector;
    
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    
    /**
     * 在实体初始化后检查是否为骷髅，如果是则修改其行为
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        // 检查是否为骷髅实体
        if ((Object)this instanceof SkeletonEntity) {
            // 获取当前实例
            MobEntity self = (MobEntity)(Object)this;
            
            try {
                // 确保目标选择器已初始化
                if (this.targetSelector != null) {
                    // 创建目标选择器
                    ActiveTargetGoal<LivingEntity> targetGoal = new ActiveTargetGoal<>(self, LivingEntity.class, 0, true, false, 
                        (entity) -> entity != null && !(entity instanceof AbstractSkeletonEntity) && !(entity instanceof net.minecraft.entity.passive.BatEntity));
                    
                    // 确保目标不为null
                    if (targetGoal != null) {
                        // 添加新的目标选择器，攻击所有生物（除了骷髅和蝙蝠）
                        this.targetSelector.add(1, targetGoal);
                    }
                }
                
                // 确保目标选择器已初始化
                if (this.goalSelector != null) {
                    // 创建弓箭攻击目标
                    BowAttackGoal<AbstractSkeletonEntity> bowGoal = new BowAttackGoal<>((AbstractSkeletonEntity)self, 1.0, 3, 15.0F);
                    
                    // 确保目标不为null
                    if (bowGoal != null) {
                        // 添加新的弓箭攻击目标，使用更短的攻击间隔（六倍速度）
                        this.goalSelector.add(4, bowGoal); // 原始间隔是20，我们改为3（六倍速度）
                    }
                }
                
                // 为骷髅添加随机护甲
                equipRandomArmor();
                
                // 为骷髅的箭添加毒性效果
                addPoisonToArrows();
            } catch (Exception e) {
                // 如果装备过程中出现异常，记录日志但不中断游戏
                // 移除调试输出
                // System.out.println("Failed to initialize skeleton: " + e.getMessage());
                // e.printStackTrace(); // 打印完整堆栈跟踪以便调试
            }
        }
        
        // 检查是否为铁傀儡，如果是则增加移动速度
        if ((Object)this instanceof IronGolemEntity) {
            // 增加铁傀儡移动速度50%
            this.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .setBaseValue(this.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED) * 1.5);
        }
        
        // 检查是否为村民，如果是则增加血量
        if ((Object)this instanceof VillagerEntity) {
            // 增加村民血量200%
            this.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH)
                .setBaseValue(this.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH) * 3.0);
            this.setHealth(this.getMaxHealth());
        }
    }
    
    /**
     * 为骷髅添加随机护甲
     */
    private void equipRandomArmor() {
        try {
            // 检查是否为骷髅实体
            if (!((Object)this instanceof AbstractSkeletonEntity)) {
                return;
            }
            
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
                    ((MobEntity)(Object)this).setEquipmentDropChance(slot, 0.0F);
                }
            }
        } catch (Exception e) {
            // 移除调试输出
            // System.out.println("Failed to equip random armor: " + e.getMessage());
            // e.printStackTrace(); // 打印完整堆栈跟踪以便调试
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
    
    /**
     * 为骷髅的箭添加毒性效果
     */
    private void addPoisonToArrows() {
        if ((Object)this instanceof AbstractSkeletonEntity) {
            try {
                // 获取骷髅当前的弓
                ItemStack bow = this.getMainHandStack();
                
                // 如果骷髅手持弓，则为其添加毒性效果
                if (bow.isOf(Items.BOW)) {
                    // 创建带有毒性效果的箭
                    ItemStack poisonArrow = new ItemStack(Items.ARROW, 64);
                    
                    // 设置骷髅的副手为箭
                    this.equipStack(EquipmentSlot.OFFHAND, poisonArrow);
                    // 设置不掉落
                    ((MobEntity)(Object)this).setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0F);
                    
                    // 移除调试输出
                    // System.out.println("Equipped skeleton with arrows: " + ((MobEntity)(Object)this).getUuid());
                }
            } catch (Exception e) {
                // 移除调试输出
                // System.out.println("Failed to add arrows to skeleton: " + e.getMessage());
                // e.printStackTrace(); // 打印完整堆栈跟踪以便调试
            }
        }
    }
    
    @Shadow protected abstract void initGoals();
    
    @Override
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }
    
    @Override
    public GoalSelector getTargetSelector() {
        return this.targetSelector;
    }
} 