package com.satyacodes.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author satyakaveti on 14/10/26
 * Spring AI auto-configures a ChatModel bean for each starter.
 * Instead of relying on ChatClient.Builder auto-config (which needs exactly one ChatModel),
 * we create each ChatClient bean ourselves.
 */


@Configuration
public class ModelConfig {

    /**
     * Ollama — marked @Primary, so it is the default injection target.
     */
    @Bean
    @Primary
    @Qualifier("ollamaChatClient")
    public ChatClient ollamaChatClient(OllamaChatModel model) {
        return ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * OpenAI — injected explicitly via @Qualifier.
     */
    @Bean
    @Qualifier("openAiChatClient")
    public ChatClient openAiChatClient(OpenAiChatModel model) {
        return ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * Hugging Face — injected explicitly via @Qualifier.
     */
    @Bean
    @Qualifier("huggingFaceChatClient")
    public ChatClient huggingFaceChatClient(HuggingfaceChatModel model) {
        return ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
