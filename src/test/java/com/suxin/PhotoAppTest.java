package com.suxin;

import com.suxin.app.PhotoApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.UUID;

@SpringBootTest
class PhotoAppTest {

    @Resource
    private PhotoApp photoApp;

    /**
     * 流式输出：边生成边打印到控制台
     */
    @Test
    void doChatWithTools() {
        testMessage("你好，我拍了一张海边日落的照片，帮我写一段小红书文案");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        Flux<String> flux = photoApp.doChatWithTools(message, chatId);

        StringBuilder answer = new StringBuilder();
        flux.doOnNext(chunk -> {
            // 实时输出，不换行，保证流式效果
            System.out.print(chunk);
            System.out.flush();
            answer.append(chunk);
        }).blockLast(); // 阻塞到流结束，最后一次性拿到完整回答

        System.out.println();
        Assertions.assertNotEquals(0, answer.length(), "回答不应为空");
    }
}
