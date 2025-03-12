package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SummonCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("uuz")
            .then(CommandManager.literal("summon")
                .then(CommandManager.argument("entity", IdentifierArgumentType.identifier())
                    .executes(context -> summon(context, IdentifierArgumentType.getIdentifier(context, "entity"), 
                            context.getSource().getPosition(), new NbtCompound(), true))
                    .then(CommandManager.argument("pos", Vec3ArgumentType.vec3())
                        .executes(context -> summon(context, IdentifierArgumentType.getIdentifier(context, "entity"), 
                                Vec3ArgumentType.getVec3(context, "pos"), new NbtCompound(), true))
                        .then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound())
                            .executes(context -> summon(context, IdentifierArgumentType.getIdentifier(context, "entity"), 
                                    Vec3ArgumentType.getVec3(context, "pos"), 
                                    NbtCompoundArgumentType.getNbtCompound(context, "nbt"), true)))))));
    }
    
    private static int summon(CommandContext<ServerCommandSource> context, Identifier entity, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        
        try {
            // 获取实体类型
            EntityType<?> entityType = world.getRegistryManager().get(RegistryKeys.ENTITY_TYPE).get(entity);
            if (entityType == null) {
                source.sendError(Text.translatable("entity.notFound", entity));
                return 0;
            }
            
            // 直接使用EntityType创建实体
            Entity summonedEntity = entityType.create(world);
            if (summonedEntity == null) {
                source.sendError(Text.translatable("command.uuzfabrictestproj.summon.failed"));
                return 0;
            }
            
            // 设置实体位置
            summonedEntity.refreshPositionAndAngles(pos.x, pos.y, pos.z, summonedEntity.getYaw(), summonedEntity.getPitch());
            
            // 应用NBT数据
            if (!nbt.isEmpty()) {
                NbtCompound entityNbt = summonedEntity.writeNbt(new NbtCompound());
                entityNbt.copyFrom(nbt);
                summonedEntity.readNbt(entityNbt);
            }
            
            // 如果是生物，设置生成原因
            if (initialize && summonedEntity instanceof MobEntity mobEntity) {
                mobEntity.initialize(world, world.getLocalDifficulty(new BlockPos((int)pos.x, (int)pos.y, (int)pos.z)), SpawnReason.COMMAND, null, null);
            }
            
            // 将实体添加到世界中
            if (!world.spawnEntity(summonedEntity)) {
                source.sendError(Text.translatable("command.uuzfabrictestproj.summon.failed"));
                return 0;
            }
            
            // 发送成功消息
            source.sendFeedback(() -> Text.translatable("command.uuzfabrictestproj.summon.success", summonedEntity.getDisplayName()), true);
            
            return 1;
        } catch (Exception e) {
            UuzFabricTestProj.LOGGER.error("执行summon命令时发生错误", e);
            e.printStackTrace();
            source.sendError(Text.translatable("command.uuzfabrictestproj.summon.failed"));
            return 0;
        }
    }
} 