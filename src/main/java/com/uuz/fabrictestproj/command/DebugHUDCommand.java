package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.uuz.fabrictestproj.client.hud.DebugHUD;

public class DebugHUDCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("debug")
                    .requires(source -> source.hasPermissionLevel(0))
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            DebugHUD.getInstance().setVisible(enabled);
                            context.getSource().sendFeedback(
                                () -> Text.literal("调试HUD已" + (enabled ? "启用" : "禁用"))
                                    .formatted(enabled ? Formatting.GREEN : Formatting.RED),
                                false
                            );
                            return 1;
                        }))
                    .executes(context -> {
                        boolean currentState = DebugHUD.getInstance().isVisible();
                        DebugHUD.getInstance().setVisible(!currentState);
                        context.getSource().sendFeedback(
                            () -> Text.literal("调试HUD已" + (!currentState ? "启用" : "禁用"))
                                .formatted(!currentState ? Formatting.GREEN : Formatting.RED),
                            false
                        );
                        return 1;
                    })
                )
        );
    }
} 