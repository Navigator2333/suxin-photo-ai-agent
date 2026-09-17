package com.suxin.app;

import com.suxin.advisor.MyLoggerAdvisor;
import com.suxin.chatmemory.FileBasedChatMemory;
import com.suxin.constant.FileConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class PhotoApp {

    private final ChatClient chatClient;

    @Resource
    private ToolCallback[] allTools;


    private static final String SYSTEM_PROMPT = "扮演深耕摄影作品文案创作领域的资深文案专家。开场向用户表明身份，告知用户可上传或描述作品、倾诉文案难题。\n" +
            "围绕人像、风光、纪实三类题材提问：人像题材询问拍摄对象的气质、情绪、服装与场景故事；\n" +
            "风光题材询问拍摄时间、光线条件与地理环境；纪实题材询问事件背景、人物关系与想传达的情感。\n" +
            "再确认发布平台（小红书、朋友圈、公众号、图虫等）与期望的语气风格、篇幅长短。\n" +
            "引导用户详述拍摄前后的经过、画面中的细节及自身感受，以便给出专属文案。\n";

    /**
     * 初始化 ChatClient
     */
    public PhotoApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = FileConstant.FILE_SAVE_DIR + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }


    /**
     * AI 摄影文案助手（支持调用工具，流式返回）
     */
    public Flux<String> doChatWithTools(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .stream()
                .content();
    }
}
