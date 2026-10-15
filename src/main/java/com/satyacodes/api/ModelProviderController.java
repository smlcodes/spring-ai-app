package com.satyacodes.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * @author satyakaveti on 15/10/26
 */
@RestController
@RequestMapping("/api/models")
@Slf4j
public class ModelProviderController {

    private final Map<String, ChatModel> models;

    public ModelProviderController(OllamaChatModel ollama,
                                   OpenAiChatModel openAi,
                                   HuggingfaceChatModel huggingFace) {
        this.models = Map.of("ollama", ollama,
                             "openai", openAi,
                             "huggingface", huggingFace);
    }

    /**
     * GET /api/providers/ollama/ask?question=What is Spring AI?
     * GET /api/providers/openai/ask?question=What is Spring AI?
     * GET /api/providers/huggingface/ask?question=What is Spring AI?
     *
     */
    @GetMapping("/{provider}/ask")
    public String ask(@PathVariable String provider, @RequestParam String question) {
        ChatModel model = models.get(provider.toLowerCase());
        log.info("asking for model: {}", model);

        PromptTemplate promptTemplate = new PromptTemplate("What is {question} in 2-3 lines?");

        Prompt prompt = new Prompt(List.of(
                new SystemMessage("Answer in one short sentence."),
                new UserMessage(promptTemplate.render(Map.of("question", question)))));
        log.info("prompt :{}", prompt.getContents());


        ChatResponse response = model.call(prompt);
        log.info("response :{}", response);
        return response.getResult().getOutput().getText();
    }


}