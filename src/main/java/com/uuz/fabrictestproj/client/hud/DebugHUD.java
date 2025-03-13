package com.uuz.fabrictestproj.client.hud;

import com.uuz.fabrictestproj.UuzFabricTestProj;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DebugHUD implements HudRenderCallback {
    private static final int MAX_LINES = 50; // 最大显示行数
    private static final int LINE_HEIGHT = 9; // 行高
    private static final int MARGIN_LEFT = 2; // 左边距
    private static final int MARGIN_TOP = 20; // 顶部边距
    private static final int MAX_LINE_LENGTH = 220; // 每行最大字符数
    private static final float SCALE = 1.0f; // 缩放比例
    
    private static final DebugHUD INSTANCE = new DebugHUD();
    // 使用线程安全的CopyOnWriteArrayList替代ArrayList
    private final List<String> debugMessages = new CopyOnWriteArrayList<>();
    private boolean visible = false;
    
    private DebugHUD() {
        // 私有构造函数，防止外部实例化
    }
    
    public static DebugHUD getInstance() {
        return INSTANCE;
    }
    
    public static void initialize() {
        UuzFabricTestProj.LOGGER.info("调试HUD已初始化");
        // 创建并注册日志追加器
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        Layout<String> layout = PatternLayout.newBuilder()
                .withConfiguration(config)
                .withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg")
                .build();
        
        DebugLogAppender logAppender = INSTANCE.new DebugLogAppender("DebugHUDAppender", layout);
        logAppender.start();
        context.getConfiguration().getRootLogger().addAppender(logAppender, null, null);
    }

    public void setVisible(boolean value) {
        this.visible = value;
        if (!value) {
            clearMessages();
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void addMessage(String message) {
        if (!visible) return;
        
        // 如果消息太长，进行截断
        if (message.length() > MAX_LINE_LENGTH) {
            message = message.substring(0, MAX_LINE_LENGTH - 3) + "...";
        }
        
        // CopyOnWriteArrayList是线程安全的，不需要额外的同步
        debugMessages.add(message);
        
        // 保持消息数量在限制之内
        while (debugMessages.size() > MAX_LINES) {
            try {
                // 安全地移除第一个元素
                if (!debugMessages.isEmpty()) {
                    debugMessages.remove(0);
                }
            } catch (IndexOutOfBoundsException e) {
                // 忽略可能的并发修改异常
                break;
            }
        }
    }
    
    public void clearMessages() {
        debugMessages.clear();
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (!visible) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        
        context.getMatrices().push();
        context.getMatrices().scale(SCALE, SCALE, 1.0f);
        
        int y = (int)(MARGIN_TOP / SCALE);
        
        // 创建一个消息列表的快照，避免在渲染过程中发生并发修改
        List<String> messagesToRender = new ArrayList<>(debugMessages);
        
        for (String message : messagesToRender) {
            if (message != null) {  // 添加空值检查
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal(message),
                    (int)(MARGIN_LEFT / SCALE),
                    y,
                    0xffffff  // 白色
                );
                y += LINE_HEIGHT;
            }
        }
        
        context.getMatrices().pop();
    }

    private class DebugLogAppender extends AbstractAppender {
        protected DebugLogAppender(String name, Layout<String> layout) {
            super(name, null, layout, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            String message = new String(getLayout().toByteArray(event));
            addMessage(message.trim());
        }
    }
} 