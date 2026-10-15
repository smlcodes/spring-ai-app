package com.satyacodes.api;

import com.satyacodes.tools.CalculatorTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author satyakaveti on 15/10/26
 */
@RestController
@RequestMapping("/api/tools")
@Slf4j
public class ToolsController {

    public final ChatClient chatClient;
    public ToolsController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/calculator/add")
    public String ask(@RequestParam("a") int a,  @RequestParam("b") int b) {
        String response = chatClient.prompt()
                .user("What is "+a+" + "+b+"?")
                .tools(new CalculatorTool())
                .call()
                .content();
        log.info("addition is: ",response);
        return response;
    }
}
