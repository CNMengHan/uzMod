package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.manager.UnbreakingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

public class UnbreakingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uuz")
            .then(literal("unbreaking")
                .executes(UnbreakingCommand::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getEntity() instanceof PlayerEntity player) {
            ItemStack heldItem = player.getMainHandStack();
            
            if (heldItem.isEmpty()) {
                source.sendFeedback(() -> Text.literal("§c你的手上没有拿任何物品"), false);
                return 0;
            }
            
            if (UnbreakingManager.addUnbreakingItem(heldItem)) {
                source.sendFeedback(() -> Text.literal("§a已将你手中的物品设置为不会损坏"), false);
                return 1;
            } else {
                source.sendFeedback(() -> Text.literal("§a该物品已经是不会损坏的了"), false);
                return 0;
            }
        } else {
            source.sendFeedback(() -> Text.literal("§c该命令只能由玩家执行"), false);
            return 0;
        }
    }
} 