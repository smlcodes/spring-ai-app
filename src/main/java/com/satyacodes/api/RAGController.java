package com.satyacodes.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author satyakaveti on 15/10/26
 */
@RestController
@RequestMapping("/api/rag")
@Slf4j
public class RAGController {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

    // 1. STORE: Embed documents and save them
    @PostMapping("/save")
    public String load() {
        Document doc1 = new Document("Spring AI supports OpenAI and Ollama.", Map.of("source", "docs"));
        Document doc2 = new Document("VectorStore stores embeddings for search.", Map.of("source", "docs"));
        vectorStore.add(List.of(doc1, doc2));   // EmbeddingModel runs inside
        return "Saved!";
    }

    // 2. ASK: Retrieve + generate answer
    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        // Advisor: automatically fetches relevant docs and adds them to the prompt
        return chatClient.prompt()
                .user(question)
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .content();
    }

}