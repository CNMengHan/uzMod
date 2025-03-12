package com.uuz.fabrictestproj.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PingCommand {
    private static final int DEFAULT_PORT = 80;
    private static final int PING_COUNT = 4;
    private static final int TIMEOUT_MS = 3000;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("uuz")
                .then(CommandManager.literal("ping")
                    .requires(source -> source.hasPermissionLevel(0))
                    .then(CommandManager.argument("target", StringArgumentType.greedyString())
                        .executes(context -> executePing(context, StringArgumentType.getString(context, "target"))))
                    .executes(context -> {
                        context.getSource().sendFeedback(
                            () -> Text.literal("用法: /uuz ping <目标地址[:端口]>")
                                .formatted(Formatting.YELLOW),
                            false
                        );
                        return 1;
                    })
                )
        );
    }
    
    private static int executePing(CommandContext<ServerCommandSource> context, String target) {
        final ServerCommandSource source = context.getSource();
        
        // 解析目标地址和端口
        final String host;
        final int port;
        
        if (target.contains(":")) {
            String[] parts = target.split(":");
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                source.sendError(Text.literal("无效的端口号: " + parts[1]));
                return 0;
            }
        } else {
            host = target;
            port = DEFAULT_PORT;
        }
        
        // 发送初始消息
        source.sendFeedback(
            () -> Text.literal("正在 Ping " + host + ":" + port + " ...")
                .formatted(Formatting.YELLOW),
            false
        );
        
        // 异步执行ping操作
        CompletableFuture.runAsync(() -> {
            final List<Long> times = new ArrayList<>();
            int success = 0;
            
            try {
                // 解析IP地址
                final InetAddress address = InetAddress.getByName(host);
                final String ip = address.getHostAddress();
                
                // 发送DNS解析结果
                source.sendFeedback(
                    () -> Text.literal("DNS解析: " + host + " -> " + ip)
                        .formatted(Formatting.GREEN),
                    false
                );
                
                // 执行多次ping
                for (int i = 1; i <= PING_COUNT; i++) {
                    final int currentAttempt = i;
                    long startTime = System.nanoTime();
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(address, port), TIMEOUT_MS);
                        long endTime = System.nanoTime();
                        long timeMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                        times.add(timeMs);
                        success++;
                        
                        source.sendFeedback(
                            () -> Text.literal(String.format("来自 %s 的回复: 端口=%d 时间=%dms",
                                ip, port, timeMs))
                                .formatted(Formatting.GREEN),
                            false
                        );
                    } catch (IOException e) {
                        source.sendFeedback(
                            () -> Text.literal(String.format("请求超时 (%d/%d)",
                                currentAttempt, PING_COUNT))
                                .formatted(Formatting.RED),
                            false
                        );
                    }
                    
                    // 等待一秒再发送下一个ping
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                // 发送统计信息
                if (!times.isEmpty()) {
                    final double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0);
                    final long minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);
                    final long maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
                    final int finalSuccess = success;
                    
                    source.sendFeedback(
                        () -> Text.literal(String.format("\n%s:%d 的 Ping 统计信息:",
                            host, port))
                            .formatted(Formatting.YELLOW),
                        false
                    );
                    source.sendFeedback(
                        () -> Text.literal(String.format("    数据包: 已发送 = %d，已接收 = %d，丢失 = %d (%.1f%% 丢失)",
                            PING_COUNT, finalSuccess, PING_COUNT - finalSuccess,
                            (PING_COUNT - finalSuccess) * 100.0 / PING_COUNT))
                            .formatted(Formatting.YELLOW),
                        false
                    );
                    if (success > 0) {
                        source.sendFeedback(
                            () -> Text.literal(String.format("往返行程的估计时间(以毫秒为单位):\n    最短 = %dms，最长 = %dms，平均 = %.1fms",
                                minTime, maxTime, avgTime))
                                .formatted(Formatting.YELLOW),
                            false
                        );
                    }
                }
            } catch (UnknownHostException e) {
                source.sendError(Text.literal("无法解析主机名: " + host));
            }
        });
        
        return 1;
    }
} 