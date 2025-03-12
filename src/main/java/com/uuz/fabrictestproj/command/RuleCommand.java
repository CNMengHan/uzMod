package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

public class RuleCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("rule")
                    .requires(source -> source.hasPermissionLevel(0))
                    .then(CommandManager.literal("randomTickSpeed")
                        .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 500))
                            .executes(context -> setRandomTickSpeed(context, IntegerArgumentType.getInteger(context, "value"))))
                        .executes(context -> getRandomTickSpeed(context)))
                    .then(CommandManager.literal("doDaylightCycle")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setDaylightCycle(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getDaylightCycle(context)))
                    .then(CommandManager.literal("doWeatherCycle")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setWeatherCycle(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getWeatherCycle(context)))
                    .then(CommandManager.literal("doMobSpawning")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setMobSpawning(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getMobSpawning(context)))
                    .then(CommandManager.literal("doMobAI")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setMobAI(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getMobAI(context)))
                    .then(CommandManager.literal("doEntityDrops")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setEntityDrops(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getEntityDrops(context)))
                    .then(CommandManager.literal("doFireTick")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setFireTick(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getFireTick(context)))
                    .then(CommandManager.literal("pvp")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setPvP(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getPvP(context)))
                    .then(CommandManager.literal("doImmediateRespawn")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setImmediateRespawn(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getImmediateRespawn(context)))
                    .then(CommandManager.literal("keepInventory")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setKeepInventory(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getKeepInventory(context)))
                    .then(CommandManager.literal("fallDamage")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setFallDamage(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getFallDamage(context)))
                    .then(CommandManager.literal("drowningDamage")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setDrowningDamage(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getDrowningDamage(context)))
                    .then(CommandManager.literal("fireDamage")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setFireDamage(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getFireDamage(context)))
                    .then(CommandManager.literal("mobGriefing")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setMobGriefing(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getMobGriefing(context)))
                    .then(CommandManager.literal("naturalRegeneration")
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(context -> setNaturalRegeneration(context, BoolArgumentType.getBool(context, "value"))))
                        .executes(context -> getNaturalRegeneration(context)))
                    .executes(RuleCommand::showHelp)
                )
        );
    }

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(() -> Text.literal("=== 游戏规则命令帮助 ===").formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("/uuz rule randomTickSpeed [0-500] - 设置随机刻速度").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule doDaylightCycle [true/false] - 设置昼夜循环").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule doWeatherCycle [true/false] - 设置天气循环").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule doMobSpawning [true/false] - 设置生物生成").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule doMobAI [true/false] - 设置生物AI").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule doEntityDrops [true/false] - 设置实体掉落").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule doFireTick [true/false] - 设置火焰蔓延").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule pvp [true/false] - 设置PVP").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule doImmediateRespawn [true/false] - 设置立即重生").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule keepInventory [true/false] - 设置死亡不掉落").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule fallDamage [true/false] - 设置摔落伤害").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule drowningDamage [true/false] - 设置溺水伤害").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule fireDamage [true/false] - 设置火焰伤害").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule mobGriefing [true/false] - 设置生物破坏方块").formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("/uuz rule naturalRegeneration [true/false] - 设置自然生命恢复").formatted(Formatting.YELLOW), false);
        return 1;
    }

    private static int setRandomTickSpeed(CommandContext<ServerCommandSource> context, int value) {
        ServerCommandSource source = context.getSource();
        GameRules.IntRule rule = source.getServer().getGameRules().get(GameRules.RANDOM_TICK_SPEED);
        rule.set(value, source.getServer());
        broadcastChange(source, "随机刻速度", String.valueOf(value));
        return 1;
    }

    private static int getRandomTickSpeed(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        int value = source.getServer().getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        source.sendFeedback(() -> Text.literal("当前随机刻速度: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setDaylightCycle(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE);
        rule.set(value, source.getServer());
        broadcastChange(source, "昼夜循环", String.valueOf(value));
        return 1;
    }

    private static int getDaylightCycle(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE);
        source.sendFeedback(() -> Text.literal("当前昼夜循环: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setWeatherCycle(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_WEATHER_CYCLE);
        rule.set(value, source.getServer());
        broadcastChange(source, "天气循环", String.valueOf(value));
        return 1;
    }

    private static int getWeatherCycle(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE);
        source.sendFeedback(() -> Text.literal("当前天气循环: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setMobSpawning(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_MOB_SPAWNING);
        rule.set(value, source.getServer());
        broadcastChange(source, "生物生成", String.valueOf(value));
        return 1;
    }

    private static int getMobSpawning(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
        source.sendFeedback(() -> Text.literal("当前生物生成: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setMobAI(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_MOB_GRIEFING);
        rule.set(value, source.getServer());
        broadcastChange(source, "生物AI", String.valueOf(value));
        return 1;
    }

    private static int getMobAI(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
        source.sendFeedback(() -> Text.literal("当前生物AI: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setEntityDrops(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_ENTITY_DROPS);
        rule.set(value, source.getServer());
        broadcastChange(source, "实体掉落", String.valueOf(value));
        return 1;
    }

    private static int getEntityDrops(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS);
        source.sendFeedback(() -> Text.literal("当前实体掉落: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setFireTick(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_FIRE_TICK);
        rule.set(value, source.getServer());
        broadcastChange(source, "火焰蔓延", String.valueOf(value));
        return 1;
    }

    private static int getFireTick(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_FIRE_TICK);
        source.sendFeedback(() -> Text.literal("当前火焰蔓延: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setPvP(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_LIMITED_CRAFTING);
        rule.set(value, source.getServer());
        broadcastChange(source, "PVP", String.valueOf(value));
        return 1;
    }

    private static int getPvP(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING);
        source.sendFeedback(() -> Text.literal("当前PVP: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setImmediateRespawn(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN);
        rule.set(value, source.getServer());
        broadcastChange(source, "立即重生", String.valueOf(value));
        return 1;
    }

    private static int getImmediateRespawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
        source.sendFeedback(() -> Text.literal("当前立即重生: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setKeepInventory(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.KEEP_INVENTORY);
        rule.set(value, source.getServer());
        broadcastChange(source, "死亡不掉落", String.valueOf(value));
        return 1;
    }

    private static int getKeepInventory(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
        source.sendFeedback(() -> Text.literal("当前死亡不掉落: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setFallDamage(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.FALL_DAMAGE);
        rule.set(value, source.getServer());
        broadcastChange(source, "摔落伤害", String.valueOf(value));
        return 1;
    }

    private static int getFallDamage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.FALL_DAMAGE);
        source.sendFeedback(() -> Text.literal("当前摔落伤害: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setDrowningDamage(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DROWNING_DAMAGE);
        rule.set(value, source.getServer());
        broadcastChange(source, "溺水伤害", String.valueOf(value));
        return 1;
    }

    private static int getDrowningDamage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DROWNING_DAMAGE);
        source.sendFeedback(() -> Text.literal("当前溺水伤害: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setFireDamage(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.FIRE_DAMAGE);
        rule.set(value, source.getServer());
        broadcastChange(source, "火焰伤害", String.valueOf(value));
        return 1;
    }

    private static int getFireDamage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.FIRE_DAMAGE);
        source.sendFeedback(() -> Text.literal("当前火焰伤害: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setMobGriefing(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.DO_MOB_GRIEFING);
        rule.set(value, source.getServer());
        broadcastChange(source, "生物破坏方块", String.valueOf(value));
        return 1;
    }

    private static int getMobGriefing(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
        source.sendFeedback(() -> Text.literal("当前生物破坏方块: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setNaturalRegeneration(CommandContext<ServerCommandSource> context, boolean value) {
        ServerCommandSource source = context.getSource();
        GameRules.BooleanRule rule = source.getServer().getGameRules().get(GameRules.NATURAL_REGENERATION);
        rule.set(value, source.getServer());
        broadcastChange(source, "自然生命恢复", String.valueOf(value));
        return 1;
    }

    private static int getNaturalRegeneration(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean value = source.getServer().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
        source.sendFeedback(() -> Text.literal("当前自然生命恢复: " + value).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static void broadcastChange(ServerCommandSource source, String ruleName, String value) {
        Text message = Text.literal("游戏规则已更新: " + ruleName + " = " + value)
            .formatted(Formatting.GREEN);
        source.getServer().getPlayerManager().broadcast(message, false);
    }
} 