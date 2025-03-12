package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.uuz.fabrictestproj.manager.HomeManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("sethome")
                .then(argument("name", StringArgumentType.word())
                    .executes(HomeCommand::setHome)))
            .then(literal("home")
                .then(argument("name", StringArgumentType.word())
                    .suggests(HomeCommand::suggestHomes)
                    .executes(HomeCommand::teleportToHome)))
            .then(literal("homes")
                .executes(HomeCommand::listHomes)));
    }

    private static int setHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String homeName = StringArgumentType.getString(context, "name");
        
        if (HomeManager.setHome(homeName, player)) {
            source.sendFeedback(() -> Text.literal("§a已设置家: §b" + homeName), false);
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("§c设置家失败"), false);
            return 0;
        }
    }

    private static int teleportToHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String homeName = StringArgumentType.getString(context, "name");
        
        HomeManager.HomeLocation home = HomeManager.getHome(homeName);
        if (home == null) {
            source.sendFeedback(() -> Text.literal("§c找不到名为 §b" + homeName + " §c的家"), false);
            return 0;
        }
        
        if (HomeManager.teleportToHome(homeName, player)) {
            source.sendFeedback(() -> Text.literal("§a已传送到家: §b" + homeName), false);
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("§c传送失败"), false);
            return 0;
        }
    }

    private static int listHomes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        
        List<HomeManager.HomeLocation> homes = HomeManager.getAllHomes();
        if (homes.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§c还没有设置任何家"), false);
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("§a所有家的列表:"), false);
        
        for (HomeManager.HomeLocation home : homes) {
            String dimensionName;
            switch (home.dimension) {
                case "minecraft:overworld":
                    dimensionName = "主世界";
                    break;
                case "minecraft:the_nether":
                    dimensionName = "下界";
                    break;
                case "minecraft:the_end":
                    dimensionName = "末地";
                    break;
                default:
                    dimensionName = home.dimension;
                    break;
            }
            
            // 创建可点击的家名文本
            MutableText homeText = Text.literal("§b" + home.name)
                .styled(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uuz home " + home.name))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§a点击传送到这个家")))
                );
            
            // 创建可点击的坐标文本
            MutableText posText = Text.literal(String.format("§e[%d, %d, %d]", (int)home.x, (int)home.y, (int)home.z))
                .styled(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uuz home " + home.name))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§a点击传送到这个家")))
                );
            
            // 创建消息
            MutableText message = Text.literal("")
                .append(homeText)
                .append(Text.literal(" §7- 由 "))
                .append(Text.literal("§a" + home.owner))
                .append(Text.literal(" §7创建于 "))
                .append(Text.literal("§d" + dimensionName))
                .append(Text.literal(" §7的 "))
                .append(posText);
            
            source.sendFeedback(() -> message, false);
        }
        
        return 1;
    }
    
    private static CompletableFuture<Suggestions> suggestHomes(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> homeNames = HomeManager.getAllHomes().stream()
            .map(home -> home.name)
            .collect(Collectors.toList());
        
        String input = builder.getRemaining().toLowerCase();
        for (String name : homeNames) {
            if (name.toLowerCase().startsWith(input)) {
                builder.suggest(name);
            }
        }
        
        return builder.buildFuture();
    }
} 