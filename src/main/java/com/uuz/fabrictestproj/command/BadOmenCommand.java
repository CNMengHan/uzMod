package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.handler.HourlyBadOmenHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BadOmenCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("villageattack")
                    .executes(BadOmenCommand::executeSelf) // 不带参数时给自己施加效果
                    .then(CommandManager.literal("allplayer")
                        .executes(BadOmenCommand::executeAll) // 给所有玩家施加效果
                    )
                    .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 5))
                        .executes(context -> executeSelfWithLevel(context, IntegerArgumentType.getInteger(context, "level"))) // 指定等级
                    )
                )
        );
    }
    
    private static int executeSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendError(Text.literal("该命令只能由玩家执行"));
            return 0;
        }
        
        // 给玩家施加不祥之兆效果
        applyBadOmen(player, 5);
        
        source.sendFeedback(() -> Text.literal("已给自己施加不祥之兆效果").formatted(Formatting.GREEN), false);
        return 1;
    }
    
    private static int executeSelfWithLevel(CommandContext<ServerCommandSource> context, int level) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendError(Text.literal("该命令只能由玩家执行"));
            return 0;
        }
        
        // 给玩家施加指定等级的不祥之兆效果
        applyBadOmen(player, level);
        
        source.sendFeedback(() -> Text.literal("已给自己施加" + level + "级不祥之兆效果").formatted(Formatting.GREEN), false);
        return 1;
    }
    
    private static int executeAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 给所有玩家施加不祥之兆效果
        HourlyBadOmenHandler.applyBadOmenToAllPlayers(source.getServer());
        
        source.sendFeedback(() -> Text.literal("已给所有玩家施加不祥之兆效果").formatted(Formatting.GREEN), true);
        return 1;
    }
    
    /**
     * 给玩家施加不祥之兆效果
     */
    private static void applyBadOmen(ServerPlayerEntity player, int level) {
        // 创建不祥之兆效果实例
        StatusEffectInstance badOmen = new StatusEffectInstance(
            StatusEffects.BAD_OMEN,          // 效果类型：不祥之兆
            10 * 20,                         // 持续时间：10秒
            level - 1,                       // 效果等级（索引从0开始）
            false,                           // 是否环境效果：否
            false,                           // 是否显示粒子：否
            false                            // 是否显示图标：否
        );
        
        // 给玩家施加效果
        player.addStatusEffect(badOmen);
    }
} 