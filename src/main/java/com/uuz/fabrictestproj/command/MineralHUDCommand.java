package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.client.hud.MineralHUD;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

public class MineralHUDCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("MineralHUD")
                .then(literal("on")
                    .executes(MineralHUDCommand::turnOn))
                .then(literal("off")
                    .executes(MineralHUDCommand::turnOff))));
    }

    private static int turnOn(CommandContext<ServerCommandSource> context) {
        MineralHUD.setEnabled(true);
        context.getSource().sendFeedback(() -> Text.literal("§a矿物HUD已开启"), false);
        return 1;
    }

    private static int turnOff(CommandContext<ServerCommandSource> context) {
        MineralHUD.setEnabled(false);
        context.getSource().sendFeedback(() -> Text.literal("§c矿物HUD已关闭"), false);
        return 1;
    }
} 