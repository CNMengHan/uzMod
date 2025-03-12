package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.manager.BoatFlyManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BoatFlyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("uuz")
                .then(literal("boatfly")
                    .then(literal("on")
                        .executes(context -> enableBoatFly(context, true)))
                    .then(literal("off")
                        .executes(context -> enableBoatFly(context, false)))
                    .then(literal("status")
                        .executes(BoatFlyCommand::getStatus))
                    .then(literal("set")
                        .then(literal("forwardspeed")
                            .then(argument("value", DoubleArgumentType.doubleArg(0.05, 10.0))
                                .executes(context -> setForwardSpeed(context, DoubleArgumentType.getDouble(context, "value")))))
                        .then(literal("upwardspeed")
                            .then(argument("value", DoubleArgumentType.doubleArg(0.0, 10.0))
                                .executes(context -> setUpwardSpeed(context, DoubleArgumentType.getDouble(context, "value")))))
                        .then(literal("changeforwardspeed")
                            .then(argument("value", BoolArgumentType.bool())
                                .executes(context -> setChangeForwardSpeed(context, BoolArgumentType.getBool(context, "value")))))
                        .then(argument("param", StringArgumentType.word())
                            .executes(context -> {
                                String param = StringArgumentType.getString(context, "param");
                                ServerCommandSource source = context.getSource();
                                source.sendFeedback(() -> Text.literal("未知参数: " + param + "。可用参数: forwardspeed, upwardspeed, changeforwardspeed"), false);
                                return 0;
                            }))
                    )
                    .executes(BoatFlyCommand::getStatus)
                )
        );
    }

    private static int enableBoatFly(CommandContext<ServerCommandSource> context, boolean enable) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendError(Text.literal("此命令只能由玩家执行"));
            return 0;
        }

        BoatFlyManager.setEnabled(source.getPlayer().getUuid(), enable);
        source.sendFeedback(() -> Text.literal("BoatFly 已" + (enable ? "启用" : "禁用")), false);
        return 1;
    }

    private static int getStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendError(Text.literal("此命令只能由玩家执行"));
            return 0;
        }

        boolean enabled = BoatFlyManager.isEnabled(source.getPlayer().getUuid());
        double forwardSpeed = BoatFlyManager.getForwardSpeed();
        double upwardSpeed = BoatFlyManager.getUpwardSpeed();
        boolean changeForwardSpeed = BoatFlyManager.isChangeForwardSpeed();

        source.sendFeedback(() -> Text.literal("BoatFly 状态:"), false);
        source.sendFeedback(() -> Text.literal("- 启用状态: " + (enabled ? "已启用" : "已禁用")), false);
        source.sendFeedback(() -> Text.literal("- 前进速度: " + forwardSpeed), false);
        source.sendFeedback(() -> Text.literal("- 上升速度: " + upwardSpeed), false);
        source.sendFeedback(() -> Text.literal("- 修改前进速度: " + (changeForwardSpeed ? "是" : "否")), false);
        return 1;
    }

    private static int setForwardSpeed(CommandContext<ServerCommandSource> context, double value) {
        ServerCommandSource source = context.getSource();
        BoatFlyManager.setForwardSpeed(value);
        source.sendFeedback(() -> Text.literal("BoatFly 前进速度已设置为: " + value), false);
        return 1;
    }

    private static int setUpwardSpeed(CommandContext<ServerCommandSource> context, double value) {
        ServerCommandSource source = context.getSource();
        BoatFlyManager.setUpwardSpeed(value);
        source.sendFeedback(() -> Text.literal("BoatFly 上升速度已设置为: " + value), false);
        return 1;
    }

    private static int setChangeForwardSpeed(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        BoatFlyManager.setChangeForwardSpeed(value);
        source.sendFeedback(() -> Text.literal("BoatFly 修改前进速度已设置为: " + (value ? "是" : "否")), false);
        return 1;
    }
} 