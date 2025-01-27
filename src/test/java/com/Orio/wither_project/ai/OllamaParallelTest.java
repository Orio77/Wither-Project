package com.Orio.wither_project.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OllamaParallelTest {
    private static final Logger logger = LoggerFactory.getLogger(OllamaParallelTest.class);

    @Autowired
    private OllamaChatModel ollamaService;

    @Autowired
    private OllamaApi ollamaApi;

    @Autowired
    private OllamaOptions options;

    private final List<String> questionList = List.of(
            "What is Java?",
            "Explain polymorphism",
            "What are design patterns?",
            "Describe SOLID principles",
            "What is Spring Boot?",
            "Explain dependency injection",
            "What is JPA?",
            "Describe microservices",
            "What is REST API?",
            "Explain Docker containers");

    private List<Prompt> prompts;

    private List<Message> messages;

    @BeforeEach
    void init() {
        prompts = new ArrayList<>();
        messages = new ArrayList<>();
        for (String question : questionList) {
            UserMessage message = new UserMessage("Answer the following question in 3 sentences: " + question);
            Prompt prompt = new Prompt(message);

            Message message2 = Message.builder(Role.USER).withContent(message.getContent()).build();

            messages.add(message2);
            prompts.add(prompt);
        }
    }

    @Test
    void testSequentialCalls() {
        long startTime = System.nanoTime();

        prompts.forEach(prompt -> {
            logger.info("Processing question: {}", prompt.getContents());
            ChatResponse response = ollamaService.call(prompt);
            String content = response.getResult().getOutput().getContent();
            logger.info("Prompt: {}, Response: {}, Response length: {}", prompt.getContents(),
                    content,
                    content.length());
        });

        long endTime = System.nanoTime();
        logger.info("Sequential execution time: {} ms", (endTime - startTime) / 1_000_000);
    }

    @Test
    void testParallelCallsFutures() {
        long startTime = System.nanoTime();

        List<CompletableFuture<ChatResponse>> futures = prompts.stream()
                .map(prompt -> {
                    logger.info("Submitting question: {}", prompt.getContents());
                    return CompletableFuture.supplyAsync(() -> ollamaService.call(prompt));
                })
                .collect(Collectors.toList());

        List<ChatResponse> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long endTime = System.nanoTime();
        logger.info("Parallel execution time: {} ms", (endTime - startTime) / 1_000_000);

        for (int i = 0; i < questionList.size(); i++) {
            String content = responses.get(i).getResult().getOutput().getContent();
            logger.info("Question: {}, Response content: {}, Response length: {}", questionList.get(i), content,
                    content.length());
        }
    }

    @Test
    void testParallelCallsExecutor() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        long startTime = System.nanoTime();

        prompts.forEach(prompt -> {
            executor.submit(() -> {
                logger.info("Executing question: {}", prompt.getContents());
                ChatResponse response = ollamaService.call(prompt);
                String content = response.getResult().getOutput().getContent();
                logger.info("Prompt: {}, Response: {}, Response length: {}", prompt.getContents(),
                        content,
                        content.length());
            });
        });

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        long endTime = System.nanoTime();
        logger.info("Parallel execution time: {} ms", (endTime - startTime) / 1_000_000);
    }

    @Test
    void testParallelCallsForkJoin() {
        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        long startTime = System.nanoTime();

        List<CompletableFuture<ChatResponse>> futures = prompts.stream()
                .map(prompt -> CompletableFuture.supplyAsync(() -> {
                    logger.info("Executing question: {}", prompt.getContents());
                    return ollamaService.call(prompt);
                }, forkJoinPool))
                .collect(Collectors.toList());

        List<ChatResponse> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long endTime = System.nanoTime();
        logger.info("Parallel execution time with ForkJoinPool: {} ms", (endTime - startTime) / 1_000_000);

        for (int i = 0; i < questionList.size(); i++) {
            String content = responses.get(i).getResult().getOutput().getContent();
            logger.info("Question: {}, Response content: {}, Response length: {}", questionList.get(i), content,
                    content.length());
        }
    }

    @Test
    void testParallelCallsStream() {
        long startTime = System.nanoTime();

        List<ChatResponse> responses = prompts.parallelStream()
                .map(prompt -> {
                    logger.info("Processing question: {}", prompt.getContents());
                    return ollamaService.call(prompt);
                })
                .collect(Collectors.toList());

        long endTime = System.nanoTime();
        logger.info("Parallel execution time with parallelStream: {} ms", (endTime - startTime) / 1_000_000);

        for (int i = 0; i < questionList.size(); i++) {
            String content = responses.get(i).getResult().getOutput().getContent();
            logger.info("Question: {}, Response content: {}, Response length: {}", questionList.get(i), content,
                    content.length());
        }
    }

    @Test
    public void testOllamaApiSequential() {
        long startTime = System.nanoTime();

        for (int i = 0; i < messages.size(); i++) {
            logger.info("Processing message: {}", messages.get(i).content());
            ChatRequest request = ChatRequest.builder(options.getModel()).withOptions(options)
                    .withMessages(List.of(messages.get(i)))
                    .build();
            var response = ollamaApi.chat(request);
            logger.info("Response received: {}, length: {}", response.message().content(),
                    response.message().content().length());
        }

        long endTime = System.nanoTime();
        logger.info("Sequential API execution time: {} ms", (endTime - startTime) / 1_000_000);
    }

    @Test
    public void testOllamaApiParallelStream() {
        long startTime = System.nanoTime();

        messages.parallelStream()
                .map(message -> {
                    logger.info("Processing message: {}", message.content());
                    ChatRequest request = ChatRequest.builder(options.getModel())
                            .withOptions(options)
                            .withMessages(List.of(message))
                            .build();
                    var response = ollamaApi.chat(request);
                    logger.info("Response received: {}, length: {}", response.message().content(),
                            response.message().content().length());
                    return response.message().content();
                })
                .collect(Collectors.toList());

        long endTime = System.nanoTime();
        logger.info("Parallel API execution time: {} ms", (endTime - startTime) / 1_000_000);
    }

    @Test
    public void testOllamaApiCompletableFuture() {
        long startTime = System.nanoTime();

        List<CompletableFuture<String>> futures = messages.stream()
                .map(message -> CompletableFuture.supplyAsync(() -> {
                    logger.info("Processing message: {}", message.content());
                    ChatRequest request = ChatRequest.builder(options.getModel())
                            .withOptions(options)
                            .withMessages(List.of(message))
                            .build();
                    var response = ollamaApi.chat(request);
                    logger.info("Response received: {}, length: {}", response.message().content(),
                            response.message().content().length());
                    return response.message().content();
                }))
                .collect(Collectors.toList());

        futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long endTime = System.nanoTime();
        logger.info("CompletableFuture API execution time: {} ms", (endTime - startTime) / 1_000_000);
    }
}
