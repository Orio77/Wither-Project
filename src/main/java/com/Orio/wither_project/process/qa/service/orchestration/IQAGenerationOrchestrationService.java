package com.Orio.wither_project.process.qa.service.orchestration;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.springframework.ai.chat.model.ChatResponse;

import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.process.qa.model.QAModel;

public interface IQAGenerationOrchestrationService {

    default void orchestrate(TextBatch batch) {
        String source = batch.getSource();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            batch.getContent().forEach(content -> {
                // 1. Generate questions for the content
                ChatResponse questionsResponse = processForQuestions(content);
                var questions = parseQuestions(questionsResponse);

                // 2. Process each question in parallel
                var allProcessingFutures = questions.stream()
                        .map(question ->
                // 2a. Get answer for the question asynchronously
                CompletableFuture.supplyAsync(() -> processForAnswer(content, question), executor)
                        .thenComposeAsync(answersResponse -> {
                            // 2b. Parse the QAModel from the answer response
                            var qaModel = parseQAModel(answersResponse);
                            qaModel.setSource(source);

                            // 2c. Process QAModel (refine, notify, save) asynchronously
                            QAModel refinedModel = refine(qaModel, content);
                            notifyWS(refinedModel);
                            save(refinedModel);

                            // Return a future that completes when all processing for this question's
                            // answers is done
                            return CompletableFuture.allOf();
                        }, executor) // Ensure subsequent steps also use the executor
                )
                        .toList();

                // 3. Wait for all processing chains (for all questions) to complete
                CompletableFuture.allOf(allProcessingFutures.toArray(CompletableFuture[]::new)).join();
            });
        }
    }

    ChatResponse processForQuestions(String text);

    ChatResponse processForAnswer(String text, String question);

    List<String> parseQuestions(ChatResponse response);

    QAModel parseQAModel(ChatResponse response);

    QAModel refine(QAModel qaModel, String content);

    void notifyWS(QAModel qaModel);

    void save(QAModel qaModel);
}