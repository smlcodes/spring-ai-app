package com.satyacodes.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 1b. UPLOAD: read a PDF, split it into chunks, embed and store in ChromaDB
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestParam("file") MultipartFile file) {
        List<Document> pages = new PagePdfDocumentReader(file.getResource()).get();
        List<Document> chunks = new TokenTextSplitter().apply(pages);
        chunks.forEach(c -> c.getMetadata().put("source", String.valueOf(file.getOriginalFilename())));
        vectorStore.add(chunks);   // EmbeddingModel runs inside
        log.info("Uploaded '{}': {} pages -> {} chunks", file.getOriginalFilename(), pages.size(), chunks.size());
        return "Stored %d chunks from %s".formatted(chunks.size(), file.getOriginalFilename());
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