---

Spring AI - Part 1: Foundations & Core Components
Spring AI is an open-source project in the Spring ecosystem that simplifies the integration of AI capabilities into Java applications.
Instead of implementing every AI provider's API from scratch, we can use Spring AI's programming model to interact with supported AI models and other AI components.
Spring AI vs. LangChain : Both Spring AI and LangChain help developers build applications powered by LLMs. They provide abstractions and integrations that reduce the amount of provider-specific code developers need to write.

1.1 Core Components / Layers
Model Abstraction Layer: Defines interfaces like ChatModel, EmbeddingModel, and ImageModel. Each AI provider (OpenAI, Anthropic, Ollama, etc.) supplies its own implementation, so switching models only requires changing dependencies and configuration-not code.
Client Layer: ChatClient is the API developers interact with most. It offers a Fluent-style call, similar to WebClient and RestClient, with support for synchronous and streaming responses.
Advisor Layer: Encapsulates reusable generative AI patterns. The RAG Advisor automatically injects retrieved context into the prompt, and the Chat Memory Advisor manages conversation history. This layer is Spring AI's primary mechanism for handling "cross-cutting concerns."
Vector Store Layer: A unified VectorStore interface supporting PostgreSQL/PGVector, Chroma, Pinecone, Milvus, Neo4j, Redis, and other mainstream vector databases.

1.2 Key Module Structure
Spring AI has split its core into finer-grained domain modules:
spring-ai-model: AI capability abstractions (ChatModel, EmbeddingModel, Tool Calling)
spring-ai-client-chat: ChatClient, ChatMemory, OutputConverter
spring-ai-rag: Complete RAG framework, including RetrievalAugmentationAdvisor
spring-ai-vector-store: Unified vector database abstraction
spring-ai-commons: Base domain models (Document, TextSplitter)

1.3 Project Setup

---

2. Model Providers
   Spring AI allows you to use multiple model providers such as OpenAI, Anthropic, and Ollama without changing core logic. You can plug in different AI services (OpenAI, Anthropic, Ollama) by changing configuration or the dependency, not your business logic
   Below are Model providers differ in capabilities. Here's a key comparison:
   🔑 Important Classes to Know
   ChatClient: Used to send questions to the AI model and get answers.
   ChatModel generates AI responses. 
EmbeddingModel converts text into numerical values for searching similar content.
   VectorStore: Stores document embeddings and helps find relevant information based on a user's question.
   Advisor: Helps add extra information or instructions to a prompt before sending it to the AI model. For example, QuestionAnswerAdvisor helps provide relevant documents for RAG.
   ChatMemory: Stores previous conversation messages so the AI.

Example on Model Providers

---

3.Core Abstractions
Spring AI provides a set of core interfaces and classes that let Java developers interact with AI models without depending on a specific AI model provider. Below are most important classes & interfaces. 
3.1 Core Components
ChatModel(low-level): Provides a lower-level interface for interacting with AI chat models.
ChatClient: Provides a fluent API to send prompts and receive AI responses.
ChatResponse: Contains the AI-generated response and associated metadata.

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatClient chatClient;

    // Low-level: uses ChatModel directly
    @GetMapping("/model")
    public String askLowLevel(@RequestParam String question) {
        Prompt prompt = new Prompt(question);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    // Fluent: uses ChatClient
    @GetMapping("/client")
    public String askFluent(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}

3.2. Prompt Engineering
PromptTemplate: Creates reusable prompts with dynamic values.Holds a reusable string "Explain {topic}..." and fills in {topic} at runtime.
Message: Represents a system instruction, user message, or assistant response.SystemMessage = the AI's role/rule. UserMessage = the actual question.
Prompt: wrap one or more messages & sent to AI.

@RestController
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @GetMapping("/ask")
    public String ask() {

        // PromptTemplate: reusable prompt with {topic}
        PromptTemplate template = new PromptTemplate("Explain {topic} in one line.");

        // Message: system + user
        Message system = new SystemMessage("You are a teacher.");
        Message user   = new UserMessage(template.render(Map.of("topic", "Spring AI")));

        // Prompt: bundle messages
        Prompt prompt = new Prompt(List.of(system, user));

        // Send
        return chatClient.prompt(prompt).call().content();
    }
}

3.3. RAG (Retrieval-Augmented Generation)
Document : Holds the text content+ metadata (e.g. source=docs) 
EmbeddingModel: Converts text into numerical vectors called embeddings to support semantic search -  text ➜ numbers (vectors)
VectorStore: Stores embeddings & retrieves relevant documents based on similarity - save & search vectors.
Advisors: get document context and injects them into the prompt before calling the AI -  auto-add retrieved context to the prompt

[Simple flow]
/save  →  Document → EmbeddingModel → VectorStore (saved)
/ask   →  Qsn → VectorStore search → Advisor adds context → AI answer
@RestController
public class RagController {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

    // 1. STORE: Embed documents and save them
    @PostMapping("/save")
    public String load() {
        Document doc1 = new Document("Spring AI supports OpenAI and Ollama.",
                Map.of("source", "docs"));
        Document doc2 = new Document("VectorStore stores embeddings for search.",
                Map.of("source", "docs"));

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

3.4 Tool Calling and MCP
Spring AI allows an AI model to use tools, such as a calculator, to perform tasks and return results.
1. Tool Calling : Tool Calling allows the AI model to request the execution of a Java method to perform a task, such as adding two numbers.
   public class CalculatorTools {

@Tool(description = "Add two numbers")
public int add(int a, int b) {
return a + b;
}
}
2. ChatClient with Tools : ChatClient sends the user's question to the AI model with calculator tool to use.
   String response = chatClient.prompt()
   .user("What is 25 + 35?")
   .tools(new CalculatorTools())
   .call()
   .content();
   System.out.println(response);
3. MCP Client : An MCP Client connects the Spring AI application to an external MCP Server and allows it to use the server's tools. For example, the application can connect to a Calculator MCP Server.
   ChatClient chatClient = ChatClient.builder(chatModel)
   .build();
   String response = chatClient.prompt()
   .user("Use the calculator to add 25 and 35.")
   .call()
   .content();
4. MCP Server : An MCP Server exposes tools that other applications can discover and use. Here is a simple calculator tool implemented using Spring AI's MCP Server support.
   @Service
   public class CalculatorTools {
   @Tool(description = "Add two numbers")
   public int add(int a, int b) {
   return a + b;
   }
   }
5. Tool Callback : A ToolCallback connects a tool request from the AI model to the Java method that executes it.
   For example, you can register a Java method as a tool callback:
   ToolCallback calculatorTool =
   MethodToolCallback.builder()
   .toolDefinition(ToolDefinition.builder()
   .name("add")
   .description("Add two numbers")
   .inputSchema("""
   {
   "type": "object",
   "properties": {
   "a": {"type": "integer"},
   "b": {"type": "integer"}
   },
   "required": ["a", "b"]
   }
   """)
   .build())
   .toolMethod(CalculatorTools.class
   .getMethod("add", int.class, int.class))
   .toolObject(new CalculatorTools())
   .build();
   You can then provide the callback to the ChatClient:
   String response = chatClient.prompt()
   .user("Add 25 and 35.")
   .toolCallbacks(calculatorTool)
   .call()
   .content();

---

4. AI Application Patterns
   An AI application pattern describes how an application uses an LLM to solve a particular business problem. Instead of building every AI feature from scratch, developers can reuse common patterns and customize them for their requirements.
   Below are Common AI Application Patterns
   Chatbots : Build conversational applications that answer questions, assist users, and provide interactive support. Example: A customer support chatbot that answers questions about orders, refunds, and delivery status.
   Document Q&A : Allow users to ask questions about documents, PDFs, manuals, and company knowledge bases. For large document collections, Retrieval-Augmented Generation (RAG) retrieves relevant content before generating an answer. Example: An employee asks, "What is our work-from-home policy?"
   Summarization: Convert long documents, reports, conversations, or articles into concise summaries. Example: Summarize a 20-page technical document into five key points.
   Classification: Categorize incoming text into predefined categories based on its content. Example: Classify support tickets as BILLING, TECHNICAL, or ACCOUNT.
   Sentiment Analysis : Identify whether a message expresses positive, negative, or neutral sentiment.Example: Analyze customer reviews to understand satisfaction levels.
   Code Generation: Generate code, SQL queries, unit tests, documentation, or code explanations from natural-language instructions. Example: Generate a JUnit test for a Spring Boot service.
   Content Generation: Create new content such as emails, product descriptions, blog posts, and marketing copy. Example: Generate a product description from a list of product specifications.
   Translation: Translate text from one language to another while preserving the intended meaning.
   Semantic Search: Find information based on meaning rather than exact keyword matches. Embeddings and vector stores can help identify semantically similar content. Example: Search for "How can I get my money back?" and retrieve a refund policy even when the document uses the phrase "return and reimbursement."
   Other Patterns

Information extraction: Extract names, dates, amounts, and other structured data.
Question answering: Answer questions using supplied context or retrieved knowledge.
Data transformation: Convert unstructured text into JSON or another structured format.
Recommendation: Suggest relevant products, articles, or actions.
Tool calling: Let an AI application invoke APIs or business functions.
Workflow automation: Combine AI decisions with predefined business processes.

---

5. Memory
   A basic LLM request usually processes the current prompt and any context explicitly supplied with it. A conversation that spans multiple requests therefore needs a way to preserve relevant history.
   Chat memory enables the application to use previous messages when generating a response.
   Types of Memory
   Message-window memory: Retains a limited number of recent messages. Useful for short conversations.
   Persistent chat history: Stores conversation messages in a durable database or another persistent store. Useful when conversations must survive application restarts.
   Summary memory: Maintains a concise summary of older conversation history to reduce token usage.
   Long-term knowledge retrieval: Uses embeddings and a vector store to retrieve relevant information from a larger collection of historical information or user-approved knowledge.

Chat memory and RAG serve different purposes. Chat memory preserves conversational context, while RAG retrieves relevant information from a knowledge source.

Chat Memory in Spring AI
Spring AI provides chat memory abstractions and advisors that integrate conversation history into model requests.
A common approach is to use MessageChatMemoryAdvisor with a ChatMemory implementation.
@Configuration
public class ChatConfig {
@Bean
ChatMemory chatMemory() {
return MessageWindowChatMemory.builder()
.maxMessages(20)
.build();
}
@Bean
ChatClient chatClient( ChatClient.Builder builder, ChatMemory chatMemory) {
return builder.defaultAdvisors(
MessageChatMemoryAdvisor.builder(chatMemory)
.build()
)
.build();
}
}
The memory window limits the number of messages retained for the conversation. The exact limit should be selected based on context size, token usage, and the application's requirements.

Maintain Separate Conversations
In a multi-user application, messages must be associated with the correct conversation.
@Service
public class ConversationService {
private final ChatClient chatClient;
public ConversationService(ChatClient chatClient) {
this.chatClient = chatClient;
}
public String chat(
String conversationId,
String message) {
return chatClient.prompt()
.user(message)
.advisors(a -> a.param(
ChatMemory.CONVERSATION_ID,
conversationId
))
.call()
.content();
}
}
chat("user-101-session-1", "My name is Rahul");
chat("user-101-session-1", "What is my name?");
The second request uses the same conversation identifier, allowing the configured memory advisor to retrieve the corresponding history.
Production consideration: The conversation ID should be derived from a validated session or authorized application context. Never let users access another user's conversation merely by supplying an arbitrary ID.

---

6. Advisors
   Spring AI advisors provide a way to intercept, customize, and extend the interaction between an application and an AI model.
   Common uses include:
   Adding conversation history to prompts.
   Retrieving relevant documents before a model call.
   Applying reusable request and response processing.
   Enforcing prompt-related policies.
   Adding logging and other cross-cutting behavior.

Common Spring AI Advisors
MessageChatMemoryAdvisor : Adds relevant conversation messages to a request and updates the chat memory.
PromptChatMemoryAdvisor: Incorporates conversation history into the prompt using a prompt-oriented approach.
QuestionAnswerAdvisor: Retrieves relevant documents from a configured vector store and uses them to provide context for answering questions.
RetrievalAugmentationAdvisor : Supports retrieval-augmented generation pipelines that combine retrieval and generation components.

---

Spring AI - Part 2: RAG & Vector Stores
Large Language Models are powerful, but they have a fundamental limitation: they only know what they were trained on. Ask about your company's refund policy, and the model will either hallucinate or admit ignorance.
Retrieval Augmented Generation (RAG) solves this by giving the LLM a memory - an external knowledge base it can search before answering. Spring AI provides a clean, modular architecture for building RAG pipelines in Java.

RAG Architecture
RAG operates in two distinct phases.
Phase 1: Document Ingestion
Docs → Split → Embed → Store
Docs : Documenets such as - PDFs, Word,TXT etc., Spring AI represents each piece as a Document object containing text and metadata .
Split: TokenTextSplitter breaks documents into chunks sized by token count, preserving semantic boundaries where possible .
Embed: EmbeddingModel converts each chunk into a numerical vector
Store: VectorStore persists these vectors alongside the original text and metadata. The store indexes the vectors to support fast similarity searches later.

Phase 2: Query Retrieval
Query → Embed → Similarity → LLM
Query: The user asks a question in natural language.
Embed: The same EmbeddingModel converts the query into a vector.
Similarity: The VectorStore searches for vectors closest to the query vector, returning the most semantically relevant document chunks.
LLM: The retrieved chunks are injected into the prompt as context, and the LLM generates an answer grounded in that context .

Vector Stores
Spring AI supports many vector databases. The VectorStore interface means you can swap between them by changing configuration, not application code. Below are mostly used vector stores
PostgreSQL (pgvector) - Free & Open Source, self-hosted - vector search inside your existing Postgres with SQL support.
Chroma - Free & Open Source, self-hosted or cloud - lightweight and fastest to start for prototyping.
Milvus - Free & Open Source, self-hosted or cloud - distributed and built for massive scale.
Redis - Source-Available or Commercial, self-hosted or cloud - in-memory speed with HNSW.
Neo4j - Open Source or Commercial, self-hosted or cloud - vector search plus graph traversal.
SimpleVectorStore - Free, built into Spring AI, in-memory only - for testing, not production.

Lab: RAG over Amazon Refund Policy with ChromaDB & Ollama
https://chat.deepseek.com/a/chat/s/4ec55225-c967-45a7-872a-7a6882637a51

---