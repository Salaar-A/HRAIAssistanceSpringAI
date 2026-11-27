package com.ai.hraiassistance.controller;

import com.ai.hraiassistance.service.HRAssistanceService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/HRAI")
public class HRAssistanceController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    private final HRAssistanceService hrAssistanceService;

    private final ChatMemory chatMemory;

    public HRAssistanceController(ChatClient.Builder builder, VectorStore vectorStore, HRAssistanceService hrAssistanceService, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultSystem("You are AI assistant to the HR team. so help them if they ask any question.Be Kind.")
                .build();
        this.vectorStore = vectorStore;
        this.hrAssistanceService = hrAssistanceService;
        this.chatMemory = chatMemory;
    }

    @GetMapping("/checkemployee")
    public String checkEmployeeInfo(@RequestParam String query, @RequestParam String userId){
        return chatClient.prompt()
                .user(query)
                .tools(hrAssistanceService)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(u-> u.param(ChatMemory.CONVERSATION_ID,userId))
                .advisors(new SimpleLoggerAdvisor())
                .call().content();
    }
}
