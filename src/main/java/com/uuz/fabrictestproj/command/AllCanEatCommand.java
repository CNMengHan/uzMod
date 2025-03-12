package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.manager.AllCanEatManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

public class AllCanEatCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("allcaneat")
                .then(literal("on")
                    .executes(AllCanEatCommand::turnOn))
                .then(literal("off")
                    .executes(AllCanEatCommand::turnOff))));
    }

    private static int turnOn(CommandContext<ServerCommandSource> context) {
        AllCanEatManager.setEnabled(true);
        context.getSource().sendFeedback(() -> Text.literal("全物品可食用功能已开启"), false);
        return 1;
    }

    private static int turnOff(CommandContext<ServerCommandSource> context) {
        AllCanEatManager.setEnabled(false);
        context.getSource().sendFeedback(() -> Text.literal("全物品可食用功能已关闭"), false);
        return 1;
    }
} 