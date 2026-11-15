# Spring AI Series — Build AI Agents Step by Step

A hands-on, progressive series that takes you from **your first LLM call** to a **fully fledged agent that talks to an MCP server**.

Each part is a self-contained, runnable Spring Boot project. You can start from Part 1 and work your way down, or jump straight to the part you need.

| Stack | Version |
|---|---|
| **Framework** | Spring Boot 4.1.x |
| **AI library** | Spring AI 2.0.1 |
| **Language / JDK** | Java 21 |
| **Build tool** | Maven |
| **LLM provider** | Groq (OpenAI-compatible API) |

---

## Series at a glance

| Part | Topic | Project | What you will build |
|:----:|-------|---------|---------------------|
| [1](#part-1--connecting-to-an-llm-with-spring-ai) | Connecting to an LLM with Spring AI | `part-1/AgentApplicationDemo` | A REST API that talks to an LLM using `ChatClient` |
| [2](#part-2--tool-calling-and-memory-management) | Tool calling for agents + memory management | `part-2/springai-agent-demo` | An agent that calls your own Java methods as tools and remembers each user |
| [3](#part-3--mcp-client-and-server-with-spring-ai) | MCP client & server creation with Spring AI | `part-3/task-manager-mcp-server` + `part-3/springai-mcp-client-demo` | An MCP server exposing tools/prompts/resources and a client consuming them |

---

## Part 1 — Connecting to an LLM with Spring AI

**Project:** `part-1/AgentApplicationDemo`

The foundation. No agents, no tools — just a clean, typed way to talk to a large language model from Spring Boot.

### What you learn

- Wiring the OpenAI starter (`spring-ai-starter-model-openai`) against any OpenAI-compatible endpoint (here: Groq)
- Injecting `ChatClient.Builder` and building a `ChatClient` bean
- Sending a **system prompt** and a **user prompt**
- Exposing the LLM behind a simple REST endpoint

### Key code

```java
@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatClient chatClient;

    ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return chatClient.prompt()
                .system("You are java expert, provide answers related to java topics, ...")
                .user(question)
                .call().content();
    }
}
```

### Try it

```bash
curl "http://localhost:8080/api/ask?question=What%20is%20a%20functional%20interface"
```

---

## Part 2 — Tool calling and memory management

**Project:** `part-2/springai-agent-demo`

Now the LLM gets *capabilities*. Your application exposes Java methods as tools, the model decides when to call them, and the result flows back into the conversation.

### What you learn

- Turning plain methods into agent tools with `@Tool` and `@ToolParam`
- Registering tools with `ChatClient.defaultTools(...)` via the auto-configured `ToolCallbackProvider`
- Persisting domain data with Spring Data JPA + H2 (file-based, so data survives restarts)
- **Memory management** with `MessageChatMemoryAdvisor` — a separate conversation history per `userId`
- Structuring a chat API with `userId` + `message` request/response records
- Prompt engineering for tool discipline: *when* **not** to call a tool

### Key code

```java
@Component
public class TaskTools {

    @Tool(description = "Create a new task with title and user id")
    Task createTask(@ToolParam(description = "The title of the task") String title,
                    @ToolParam(description = "The user id of the task") String userId) { ... }

    @Tool(description = "Get all incomplete tasks for a user")
    List<Task> getIncompleteTasks(@ToolParam(description = "The user id of the tasks") String userId) { ... }
}
```

```java
// Per-user conversation memory
String response = chatClient.prompt()
        .user("user id :" + userId + ", message: " + message)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
        .call().content();
```

### Try it

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"userId": "u1", "message": "create a task named Prepare spring ai blog"}'
```

---

## Part 3 — MCP client and server with Spring AI

**Projects:**

- `part-3/task-manager-mcp-server` — the MCP **server** (port `8081`)
- `part-3/springai-mcp-client-demo` — the MCP **client** (port `8080`)

Part 3 moves your tools behind the **Model Context Protocol** — an open standard that lets an AI application discover and use tools, prompts and resources exposed by *any* MCP-compatible server. In Part 2 the tools were wired in-process; here they live in a separate, independently deployable application.

### What you learn

**On the server side (`task-manager-mcp-server`)**

- Building an MCP server with `spring-ai-starter-mcp-server-webmvc`
- Exposing **tools** with `@McpTool` / `@McpToolParam`
- Exposing **prompts** with `@McpPrompt` / `@McpArg` (reusable prompt templates)
- Exposing **resources** with `@McpResource` (read-only data, e.g. `tasks://summary`)
- The `STREAMABLE` transport protocol and server metadata (`name`, `version`, `instructions`)
- Serving an MCP server from a normal Spring Boot app, with H2 + JPA underneath

**On the client side (`springai-mcp-client-demo`)**

- Connecting to a remote MCP server with `spring-ai-starter-mcp-client`
- Configuring the server URL via `spring.ai.mcp.client.streamable-http.connections.*`
- Consuming remote tools through the same `ToolCallbackProvider` abstraction used in Part 2
- Reusing chat memory and advisors on top of remotely discovered tools

### Key code — server

```java
@Component
public class TaskTools {
    @McpTool(description = "Create a new task with title and user id")
    Task createTask(@McpToolParam(description = "The title of the task") String title,
                    @McpToolParam(description = "The user id of the task") String userId) { ... }
}

@Component
public class TaskResources {
    @McpResource(uri = "tasks://summary", name = "Tasks Summary",
            description = "Counts of open and completed tasks", mimeType = "text/plain")
    public String summary() { ... }
}
```

### Key code — client

```properties
spring.ai.mcp.client.type=SYNC
spring.ai.mcp.client.request-timeout=30s
spring.ai.mcp.client.streamable-http.connections.task-server.url=http://localhost:8081
```

```java
// Remote MCP tools are injected as a ToolCallbackProvider, exactly like local ones
@Bean
public ChatClient chatClient(ChatClient.Builder builder,
                             ToolCallbackProvider taskTools,
                             ChatMemory chatMemory) {
    return builder
            .defaultSystem("You are a helpful personal task assistant. ...")
            .defaultTools(taskTools)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                             SimpleLoggerAdvisor.builder().build())
            .build();
}
```

### Run it

Start the **server first**, then the **client** (the client needs the server to be up in order to discover its tools).

```bash
# 1. MCP server -> http://localhost:8081
cd part-3/task-manager-mcp-server
./mvnw spring-boot:run

# 2. MCP client -> http://localhost:8080
cd part-3/springai-mcp-client-demo
./mvnw spring-boot:run
```

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"userId": "u1", "message": "list my pending tasks"}'
```

---

## Prerequisites

1. **JDK 21+** — `java -version`
2. **A Groq API key** — the demos call Groq's OpenAI-compatible endpoint

   ```bash
   # macOS / Linux
   export GROQ_API_KEY=your-key-here

   # Windows (PowerShell)
   $env:GROQ_API_KEY="your-key-here"
   ```

   The key is never hard-coded. Every project reads it from the environment:

   ```properties
   spring.ai.openai.api-key=${GROQ_API_KEY}
   spring.ai.openai.base-url=https://api.groq.com/openai/v1
   ```

> Want to use a different provider? Point `spring.ai.openai.base-url` at your own OpenAI-compatible endpoint and swap `spring.ai.openai.chat.model` — the code does not change.

---

## Project layout

```
spring-ai-series/
├── README.md
├── part-1/
│   └── AgentApplicationDemo/        # Talk to an LLM with ChatClient
├── part-2/
│   └── springai-agent-demo/         # @Tool calling + chat memory
└── part-3/
    ├── task-manager-mcp-server/     # MCP server (tools, prompts, resources)
    └── springai-mcp-client-demo/    # MCP client consuming the server
```

---

## Running any part

Every module is an independent Maven project:

```bash
cd part-<n>/<project>
./mvnw spring-boot:run          # macOS / Linux
mvnw.cmd spring-boot:run        # Windows
```

---

## Tech stack

| Concern | Technology |
|---|---|
| AI framework | Spring AI 2.0.1 |
| Runtime | Spring Boot 4.1.x, Java 21 |
| LLM access | `spring-ai-starter-model-openai` (Groq, OpenAI-compatible) |
| Tools (local) | `@Tool` / `@ToolParam` + `ToolCallbackProvider` |
| Tools (remote) | `@McpTool` / `@McpResource` / `@McpPrompt` + `spring-ai-starter-mcp-client` |
| Memory | `MessageChatMemoryAdvisor` + `ChatMemory` (conversation id per user) |
| Persistence | Spring Data JPA + H2 (file-based) |
| Protocol | MCP over the `STREAMABLE` transport |

---

## Useful references

- [Spring AI documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI ChatClient API](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Spring AI Tool Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [Spring AI MCP Server Boot Starters](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starters-docs.html)
- [Spring AI MCP Client Boot Starters](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starters-docs.html)
- [Model Context Protocol](https://modelcontextprotocol.io)

---

## License

This repository is provided for learning purposes.
