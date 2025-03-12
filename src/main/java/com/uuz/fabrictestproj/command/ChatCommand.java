package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.uuz.fabrictestproj.UuzFabricTestProj;
import com.uuz.fabrictestproj.ai.DeepSeekAIManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("uuz")
            .then(CommandManager.literal("chat")
                .then(CommandManager.literal("v3")
                    .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> execute(context, DeepSeekAIManager.MODEL_V3))))
                .then(CommandManager.literal("r1")
                    .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> execute(context, DeepSeekAIManager.MODEL_R1))))
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(context -> execute(context, DeepSeekAIManager.MODEL_V3)))));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context, String model) {
        try {
            ServerCommandSource source = context.getSource();
            String playerName = source.getName();
            String message = StringArgumentType.getString(context, "message");
            
            // 获取模型名称的显示文本
            String modelDisplayName = model.equals(DeepSeekAIManager.MODEL_R1) ? "R1" : "V3";
            
            // 向所有玩家广播用户的问题
            source.getServer().getPlayerManager().broadcast(
                Text.literal("<DeepSeek-" + modelDisplayName + "> ").formatted(Formatting.AQUA)
                    .append(Text.literal(playerName + ": ").formatted(Formatting.YELLOW))
                    .append(Text.literal(message).formatted(Formatting.WHITE)), 
                false);
            
            // 发送请求到DeepSeekAI并获取回复
            DeepSeekAIManager.getInstance().sendMessage(playerName, model, message, response -> {
                // 处理回复，移除markdown语法
                String cleanedResponse = cleanMarkdown(response);
                
                // 向所有玩家广播AI的回复
                source.getServer().getPlayerManager().broadcast(
                    Text.literal("<DeepSeek-" + modelDisplayName + "> ").formatted(Formatting.AQUA)
                        .append(Text.literal(cleanedResponse).formatted(Formatting.WHITE)), 
                    false);
                
                // 播放成功音效
                playSuccessSound(source);
            });
            
            return 1;
        } catch (Exception e) {
            UuzFabricTestProj.LOGGER.error("执行chat命令时发生错误", e);
            return 0;
        }
    }
    
    /**
     * 播放成功音效
     */
    private static void playSuccessSound(ServerCommandSource source) {
        try {
            // 获取服务器上的所有玩家
            source.getServer().getPlayerManager().getPlayerList().forEach(player -> {
                // 在每个玩家的位置播放音效
                player.playSound(
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, // 使用经验球拾取音效（successful_hit）
                    SoundCategory.PLAYERS,
                    0.5F, // 音量
                    1.0F  // 音调
                );
            });
        } catch (Exception e) {
            UuzFabricTestProj.LOGGER.error("播放音效时发生错误", e);
        }
    }
    
    /**
     * 清理Markdown语法，但保留代码块内容
     */
    private static String cleanMarkdown(String markdown) {
        if (markdown == null) return "";
        
        // 保留代码块内容，但移除代码块标记
        Pattern codeBlockPattern = Pattern.compile("```(?:[a-zA-Z]*)?\\s*([\\s\\S]*?)```");
        Matcher codeBlockMatcher = codeBlockPattern.matcher(markdown);
        StringBuffer sb = new StringBuffer();
        while (codeBlockMatcher.find()) {
            String codeContent = codeBlockMatcher.group(1).trim();
            codeBlockMatcher.appendReplacement(sb, codeContent);
        }
        codeBlockMatcher.appendTail(sb);
        markdown = sb.toString();
        
        // 移除行内代码标记，但保留内容
        markdown = markdown.replaceAll("`([^`]*)`", "$1");
        
        // 移除标题
        markdown = markdown.replaceAll("#{1,6}\\s+(.*)", "$1");
        
        // 移除粗体和斜体
        markdown = markdown.replaceAll("\\*\\*([^*]*)\\*\\*", "$1");
        markdown = markdown.replaceAll("__([^_]*)__", "$1");
        markdown = markdown.replaceAll("\\*([^*]*)\\*", "$1");
        markdown = markdown.replaceAll("_([^_]*)_", "$1");
        
        // 移除链接
        markdown = markdown.replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1");
        
        // 移除图片
        markdown = markdown.replaceAll("!\\[([^\\]]*)\\]\\([^)]*\\)", "");
        
        // 移除引用
        markdown = markdown.replaceAll(">\\s+(.*)", "$1");
        
        // 移除水平线
        markdown = markdown.replaceAll("---", "");
        markdown = markdown.replaceAll("\\*\\*\\*", "");
        markdown = markdown.replaceAll("___", "");
        
        // 移除列表标记
        markdown = markdown.replaceAll("^\\s*[*+-]\\s+", "");
        markdown = markdown.replaceAll("^\\s*\\d+\\.\\s+", "");
        
        // 移除表格
        markdown = markdown.replaceAll("\\|[^|]*\\|", "");
        markdown = markdown.replaceAll("[-:]+", "");
        
        return markdown.trim();
    }
} 