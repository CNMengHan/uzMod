package com.uuz.fabrictestproj.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.uuz.fabrictestproj.UuzFabricTestProj;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DeepSeekAIManager {
    private static final String API_KEY = "sk-6e458cb116d743a99b47ef9f699e7c90";
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    
    public static final String MODEL_V3 = "deepseek-chat";
    public static final String MODEL_R1 = "deepseek-reasoner";
    
    private static DeepSeekAIManager instance;
    private final HttpClient httpClient;
    private final Gson gson;
    
    // 存储每个玩家的对话历史
    private final Map<String, Map<String, List<Message>>> conversationHistory;
    
    private DeepSeekAIManager() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.conversationHistory = new HashMap<>();
    }
    
    public static DeepSeekAIManager getInstance() {
        if (instance == null) {
            instance = new DeepSeekAIManager();
        }
        return instance;
    }
    
    /**
     * 发送消息到DeepSeekAI并获取回复
     * @param playerName 玩家名称
     * @param model 使用的模型
     * @param message 消息内容
     * @param callback 回调函数
     */
    public void sendMessage(String playerName, String model, String message, Consumer<String> callback) {
        // 确保玩家有对话历史记录
        if (!conversationHistory.containsKey(playerName)) {
            conversationHistory.put(playerName, new HashMap<>());
        }
        
        // 确保玩家对特定模型有对话历史记录
        Map<String, List<Message>> playerHistory = conversationHistory.get(playerName);
        if (!playerHistory.containsKey(model)) {
            playerHistory.put(model, new ArrayList<>());
        }
        
        // 添加用户消息到历史记录
        List<Message> history = playerHistory.get(model);
        history.add(new Message("user", message));
        
        // 创建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        
        JsonArray messagesArray = new JsonArray();
        for (Message msg : history) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.role);
            msgObj.addProperty("content", msg.content);
            messagesArray.add(msgObj);
        }
        
        requestBody.add("messages", messagesArray);
        
        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        // 异步发送请求
        CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofString());
        
        // 处理响应
        responseFuture.thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                        JsonObject choice = responseJson.getAsJsonArray("choices").get(0).getAsJsonObject();
                        JsonObject responseMessage = choice.getAsJsonObject("message");
                        
                        String content = responseMessage.get("content").getAsString();
                        String reasoningContent = null;
                        
                        // 如果是推理模型，获取推理内容
                        if (MODEL_R1.equals(model) && responseMessage.has("reasoning_content")) {
                            reasoningContent = responseMessage.get("reasoning_content").getAsString();
                        }
                        
                        // 添加AI回复到历史记录
                        history.add(new Message("assistant", content));
                        
                        // 如果是推理模型，构建包含推理过程的回复
                        if (reasoningContent != null) {
                            // 调用回调函数，返回包含推理过程的回复
                            callback.accept("思考过程：\n" + reasoningContent + "\n\n最终回答：\n" + content);
                        } else {
                            // 调用回调函数，返回普通回复
                            callback.accept(content);
                        }
                    } catch (Exception e) {
                        UuzFabricTestProj.LOGGER.error("处理AI回复时发生错误", e);
                        callback.accept("AI回复处理失败，请稍后再试。错误信息：" + e.getMessage());
                    }
                })
                .exceptionally(e -> {
                    UuzFabricTestProj.LOGGER.error("发送AI请求时发生错误", e);
                    callback.accept("无法连接到AI服务，请稍后再试。错误信息：" + e.getMessage());
                    return null;
                });
    }
    
    /**
     * 消息类，用于存储对话历史
     */
    private static class Message {
        private final String role;
        private final String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
} 