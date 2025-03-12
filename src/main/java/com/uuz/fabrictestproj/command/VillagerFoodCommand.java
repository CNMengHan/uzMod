package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.manager.VillagerFoodManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 村民食物命令
 * 允许玩家手动触发村民食物生成功能
 */
public class VillagerFoodCommand {
    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("givevillagerfood")
                    .requires(source -> source.hasPermissionLevel(0)) // 所有玩家都可以执行
                    .executes(VillagerFoodCommand::executeVillagerFood)
                )
        );
    }
    
    /**
     * 执行村民食物命令
     */
    private static int executeVillagerFood(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 手动触发食物生成，不广播消息
        boolean success = VillagerFoodManager.spawnFoodForVillagersManually(source.getServer(), false);
        
        if (success) {
            source.sendFeedback(() -> Text.literal("成功为村民生成食物！").formatted(Formatting.GREEN), false);
        } else {
            source.sendFeedback(() -> Text.literal("未找到任何已加载区块中的村民！").formatted(Formatting.RED), false);
        }
        
        return 1;
    }
} 