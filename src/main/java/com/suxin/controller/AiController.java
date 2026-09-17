package com.suxin.controller;

import com.suxin.app.PhotoApp;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private PhotoApp photoApp;

    /**
     * SSE 流式调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/photo_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return photoApp.doChatWithTools(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }


}
