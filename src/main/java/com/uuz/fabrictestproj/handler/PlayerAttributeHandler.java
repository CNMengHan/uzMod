package com.uuz.fabrictestproj.handler;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;
import java.util.function.Predicate;

public class PlayerAttributeHandler {
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("d5d0d878-b04d-4c1b-b1c3-91f2d1c9c321");
    private static final UUID HUNGER_MODIFIER_ID = UUID.fromString("a7d7c878-b04d-4c1b-b1c3-91f2d1c9c322");
    private static final double HEALTH_MULTIPLIER = 1.0; // 增加100%血量（最终血量为原版的2倍）
    private static final double HUNGER_MULTIPLIER = 1.0; // 增加100%饱食度（最终饱食度为原版的2倍）
    private static final double PLAYER_WALK_SPEED = 0.23; // 玩家行走速度
    private static final double PLAYER_SPRINT_SPEED = 0.3; // 玩家跑步速度
    private static final double PLAYER_JUMP_HEIGHT = 0.42; // 玩家跳跃高度
    private static final double ZOMBIE_WANDER_SPEED_MULTIPLIER = 0.3; // 僵尸游荡速度倍率

    public static void register() {
        // 注册玩家加入事件
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    applyAttributeModifiers(player);
                }
            }
        });

        // 注册玩家重生事件
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            applyAttributeModifiers(newPlayer);
        });

        // 注册实体加载事件
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                applyAttributeModifiers(player);
            } else if (entity instanceof ZombieEntity zombie) {
                modifyZombieAI(zombie);
            }
        });
    }

    private static void applyAttributeModifiers(ServerPlayerEntity player) {
        // 修改最大生命值
        EntityAttributeInstance healthAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            // 移除旧的修改器（如果存在）
            healthAttribute.removeModifier(HEALTH_MODIFIER_ID);
            // 添加新的修改器
            EntityAttributeModifier healthModifier = new EntityAttributeModifier(
                HEALTH_MODIFIER_ID,
                "Health boost",
                HEALTH_MULTIPLIER,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
            );
            healthAttribute.addPersistentModifier(healthModifier);
            // 恢复满血
            player.setHealth(player.getMaxHealth());
        }

        // 修改最大饱食度
        modifyHungerManager(player);
    }

    private static void modifyHungerManager(PlayerEntity player) {
        HungerManager hungerManager = player.getHungerManager();
        // 设置饱食度上限为40（原版20的两倍）
        if (hungerManager.getFoodLevel() <= 20) {
            hungerManager.setFoodLevel(40);
            hungerManager.setSaturationLevel(20.0f);
        }
    }

    private static void modifyZombieAI(ZombieEntity zombie) {
        // 设置僵尸的基础移动速度属性
        EntityAttributeInstance moveSpeedAttribute = zombie.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (moveSpeedAttribute != null) {
            moveSpeedAttribute.setBaseValue(PLAYER_WALK_SPEED);
        }

        // 获取AI选择器
        GoalSelector goalSelector = ((MobEntityAccessor)zombie).getGoalSelector();
        GoalSelector targetSelector = ((MobEntityAccessor)zombie).getTargetSelector();

        // 确保goalSelector不为null
        if (goalSelector != null) {
            // 清除现有的AI目标
            goalSelector.clear(goal -> true); // 清除所有目标

            // 添加自定义的追击AI（优先级2）
            goalSelector.add(2, new CustomZombieAttackGoal(zombie, 1.0D));
            
            // 添加自定义的游荡AI（优先级7）
            goalSelector.add(7, new CustomZombieWanderGoal(zombie, 0.5D));
            
            // 添加看向玩家的AI（优先级8）
            goalSelector.add(8, new LookAtEntityGoal(zombie, PlayerEntity.class, 8.0F));
            
            // 添加随机看向的AI（优先级9）
            goalSelector.add(9, new LookAroundGoal(zombie));
        } else {
            System.out.println("Warning: Zombie goalSelector is null, cannot modify AI");
        }

        // 重新添加目标选择器（让僵尸能够发现并追踪玩家）
        if (targetSelector != null) {
            targetSelector.clear(goal -> true); // 清除所有目标选择器
            targetSelector.add(2, new ActiveTargetGoal<>(zombie, PlayerEntity.class, true));
        } else {
            System.out.println("Warning: Zombie targetSelector is null, cannot modify AI");
        }
    }

    // 自定义僵尸追击AI
    private static class CustomZombieAttackGoal extends ZombieAttackGoal {
        private final ZombieEntity zombie;
        private int jumpDelay = 0;
        private boolean isSprinting = false;
        private int attackTick = 0;

        public CustomZombieAttackGoal(ZombieEntity zombie, double speed) {
            super(zombie, speed, false);
            this.zombie = zombie;
        }

        @Override
        public void tick() {
            LivingEntity target = this.zombie.getTarget();
            if (target != null && target.isAlive()) {
                // 获取到目标的距离
                double distanceToTarget = this.zombie.squaredDistanceTo(target);
                
                // 更新移动速度（根据距离决定是否冲刺）
                EntityAttributeInstance moveSpeedAttribute = this.zombie.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (moveSpeedAttribute != null) {
                    if (distanceToTarget > 16.0) { // 4格以上距离时冲刺
                        moveSpeedAttribute.setBaseValue(PLAYER_SPRINT_SPEED);
                        isSprinting = true;
                    } else {
                        moveSpeedAttribute.setBaseValue(PLAYER_WALK_SPEED);
                        isSprinting = false;
                    }
                }

                // 在追击目标时跳跃
                if (--jumpDelay <= 0 && this.zombie.isOnGround()) {
                    // 检查前方是否有障碍物或高度差
                    if (this.zombie.horizontalCollision || this.shouldJump()) {
                        Vec3d velocity = this.zombie.getVelocity();
                        double forward = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
                        if (forward > 0.1) { // 只在有前进速度时跳跃
                            this.zombie.setVelocity(velocity.x, PLAYER_JUMP_HEIGHT, velocity.z);
                            jumpDelay = isSprinting ? 10 : 20; // 冲刺时更频繁跳跃
                        }
                    }
                }
            }
            
            // 调用原版AI的tick方法来处理基础的追击逻辑
            super.tick();
        }

        private boolean shouldJump() {
            LivingEntity target = this.zombie.getTarget();
            if (target == null) return false;
            
            // 检查目标是否在更高的位置
            boolean heightDifference = target.getY() > this.zombie.getY() + 0.5;
            
            // 检查与目标的距离
            double distance = this.zombie.squaredDistanceTo(target);
            
            // 只在距离适中且需要向上移动时跳跃
            return heightDifference && distance < 36.0; // 6格以内
        }
    }

    // 自定义僵尸游荡AI
    private static class CustomZombieWanderGoal extends WanderAroundFarGoal {
        private final ZombieEntity zombie;

        public CustomZombieWanderGoal(ZombieEntity zombie, double speed) {
            super(zombie, speed);
            this.zombie = zombie;
        }

        @Override
        public void tick() {
            super.tick();
            // 游荡时使用较慢的速度，并且不跳跃
            EntityAttributeInstance moveSpeedAttribute = this.zombie.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (moveSpeedAttribute != null) {
                moveSpeedAttribute.setBaseValue(PLAYER_WALK_SPEED * ZOMBIE_WANDER_SPEED_MULTIPLIER);
            }
            
            // 抑制任何向上的速度（防止跳跃）
            if (this.zombie.getVelocity().y > 0) {
                this.zombie.setVelocity(this.zombie.getVelocity().multiply(1.0, 0.5, 1.0));
            }
        }
    }
} 