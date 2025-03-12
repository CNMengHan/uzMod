package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeleportCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("tpto")
                .then(argument("player", EntityArgumentType.player())
                    .executes(TeleportCommand::teleportToPlayer))));
    }

    private static int teleportToPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity sourcePlayer = source.getPlayerOrThrow();
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
        
        if (sourcePlayer == targetPlayer) {
            source.sendFeedback(() -> Text.literal("§c你不能传送到自己的位置"), false);
            return 0;
        }
        
        // 执行传送
        sourcePlayer.teleport(
            targetPlayer.getServerWorld(),
            targetPlayer.getX(),
            targetPlayer.getY(),
            targetPlayer.getZ(),
            targetPlayer.getYaw(),
            targetPlayer.getPitch()
        );
        
        // 发送反馈消息
        source.sendFeedback(() -> Text.literal("§a已传送到玩家 " + targetPlayer.getName().getString() + " 的位置"), false);
        targetPlayer.sendMessage(Text.literal("§e玩家 " + sourcePlayer.getName().getString() + " 传送到了你的位置"), false);
        
        return 1;
    }
} 