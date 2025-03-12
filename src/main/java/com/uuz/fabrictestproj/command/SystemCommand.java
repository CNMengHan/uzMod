package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.uuz.fabrictestproj.UuzFabricTestProj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class SystemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("cmd")
                    .requires(source -> source.hasPermissionLevel(0))
                    .then(CommandManager.argument("command", StringArgumentType.greedyString())
                        .executes(context -> {
                            String command = StringArgumentType.getString(context, "command");
                            ServerCommandSource source = context.getSource();
                            
                            // 创建异步任务执行系统命令
                            CompletableFuture.runAsync(() -> {
                                try {
                                    // 构建命令
                                    ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
                                    processBuilder.redirectErrorStream(true); // 合并错误流和输出流
                                    
                                    // 启动进程
                                    Process process = processBuilder.start();
                                    
                                    // 读取输出
                                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            final String output = line;
                                            // 在主线程中发送消息
                                            source.getServer().execute(() -> {
                                                source.sendFeedback(() -> Text.literal(output)
                                                    .formatted(Formatting.GRAY), false);
                                            });
                                        }
                                    }
                                    
                                    // 等待进程结束
                                    int exitCode = process.waitFor();
                                    
                                    // 发送执行结果
                                    source.getServer().execute(() -> {
                                        if (exitCode == 0) {
                                            source.sendFeedback(() -> Text.literal("命令执行完成")
                                                .formatted(Formatting.GREEN), false);
                                        } else {
                                            source.sendFeedback(() -> Text.literal("命令执行失败，退出代码: " + exitCode)
                                                .formatted(Formatting.RED), false);
                                        }
                                    });
                                    
                                } catch (Exception e) {
                                    // 发送错误消息
                                    source.getServer().execute(() -> {
                                        source.sendFeedback(() -> Text.literal("执行命令时出错: " + e.getMessage())
                                            .formatted(Formatting.RED), false);
                                        UuzFabricTestProj.LOGGER.error("执行系统命令时出错", e);
                                    });
                                }
                            });
                            
                            return 1;
                        }))
                )
        );
    }
} 