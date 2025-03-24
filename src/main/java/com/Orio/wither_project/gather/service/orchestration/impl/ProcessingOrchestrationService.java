package com.Orio.wither_project.gather.service.orchestration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.config.ProcessingConfig;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.gather.model.ScrapedTextBatch;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.repository.QAModelRepo;
import com.Orio.wither_project.gather.service.format.IFormatService;
import com.Orio.wither_project.gather.service.persist.impl.Neo4jVectorService;
import com.Orio.wither_project.gather.service.process.IQAService;
import com.Orio.wither_project.socket.gather.service.IProcessingProgressNotifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingOrchestrationService {

    private final IQAService qaService;
    private final ProcessingConfig config;
    private final IProcessingProgressNotifier progressNotifier;
    private final IFormatService formatService;
    private final QAModelRepo repo;
    private final Neo4jVectorService neo4jVectorService;

    public void orchestrate(DataModel dataModel) {
        log.info("Starting processing orchestration...");

        List<ScrapeItem> items = dataModel.getItems();

        List<ScrapedTextBatch> parts = formatService.formatPartsToProcess(items);

        List<QAModel> result = process(parts);

        log.info("Orchestration process completed. Results count: {}", result.size());
    }

    private List<QAModel> process(List<ScrapedTextBatch> parts) {
        log.info("Processing {} parts", parts.size());
        List<QAModel> allResults = new ArrayList<>();
        int totalParts = parts.size();
        int processedParts = 0;

        // Process parts sequentially
        for (ScrapedTextBatch part : parts) {
            List<QAModel> partResults = processPartContentInParallel(part);
            allResults.addAll(partResults);
            processedParts++;
            progressNotifier.notifyProgress(processedParts, totalParts);
            log.debug("Part processed, current result count: {}", allResults.size());
        }

        log.info("All parts processed successfully with {} total results", allResults.size());
        return allResults;
    }

    private List<QAModel> processPartContentInParallel(ScrapedTextBatch part) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(part.getContent().size(), config.getThreadPoolSize()));

        try {
            List<CompletableFuture<List<QAModel>>> futures = part.getContent().stream()
                    .map(content -> CompletableFuture.supplyAsync(() -> processContent(content, part.getSource()),
                            executor))
                    .collect(Collectors.toList());

            // Wait for all futures to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            // Get all results once completed
            return allFutures.thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream).map(qaModel -> {
                        qaModel.setSource(part.getSource());
                        log.debug("Source {} set for QA model", part.getSource());
                        return qaModel;
                    }).collect(Collectors.toList())).join();

        } catch (Exception e) {
            log.error("Error processing content items within part", e);
            return new ArrayList<>();
        } finally {
            executor.shutdown();
        }
    }

    private List<QAModel> processContent(String content, String source) {
        try {
            log.debug("Processing content of size: {}", content.length());

            // Extract QA models
            List<QAModel> results = qaService.extract(content);

            // Set source only if not already set
            results.forEach(qaModel -> {
                if (qaModel.getSource() == null || qaModel.getSource().isEmpty()) {
                    qaModel.setSource(source);
                    log.debug("Source {} set for QA model", source);
                }
            });

            // Batch save all results in one operation
            if (!results.isEmpty()) {
                // First save to repository
                List<QAModel> savedResults = repo.saveAll(results);

                // Then save questions to vector service
                List<String> questions = savedResults.stream()
                        .map(QAModel::getQuestion)
                        .toList();
                neo4jVectorService.save(questions);

                // Notify about results after successful persistence
                savedResults.forEach(progressNotifier::notifyQAResult);
            }

            return results;
        } catch (Exception e) {
            log.error("Error processing content: {}", e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            log.info("Completed processing content of size: {}", content.length());
        }
    }

}
