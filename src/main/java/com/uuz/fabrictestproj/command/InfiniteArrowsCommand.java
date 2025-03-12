package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.client.InfiniteArrowsManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

public class InfiniteArrowsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("infinitearrows")
                .then(literal("on")
                    .executes(InfiniteArrowsCommand::turnOn))
                .then(literal("off")
                    .executes(InfiniteArrowsCommand::turnOff))));
    }

    private static int turnOn(CommandContext<ServerCommandSource> context) {
        InfiniteArrowsManager.setEnabled(true);
        context.getSource().sendFeedback(() -> Text.literal("无限箭矢已开启"), false);
        return 1;
    }

    private static int turnOff(CommandContext<ServerCommandSource> context) {
        InfiniteArrowsManager.setEnabled(false);
        context.getSource().sendFeedback(() -> Text.literal("无限箭矢已关闭"), false);
        return 1;
    }
} 