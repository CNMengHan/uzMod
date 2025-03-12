package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.client.ExplosionArrowManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

public class ExplosionArrowCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("explosionarrow")
                .then(literal("on")
                    .executes(ExplosionArrowCommand::turnOn))
                .then(literal("off")
                    .executes(ExplosionArrowCommand::turnOff))));
    }

    private static int turnOn(CommandContext<ServerCommandSource> context) {
        ExplosionArrowManager.setEnabled(true);
        context.getSource().sendFeedback(() -> Text.literal("爆炸箭矢已开启"), false);
        return 1;
    }

    private static int turnOff(CommandContext<ServerCommandSource> context) {
        ExplosionArrowManager.setEnabled(false);
        context.getSource().sendFeedback(() -> Text.literal("爆炸箭矢已关闭"), false);
        return 1;
    }
} 