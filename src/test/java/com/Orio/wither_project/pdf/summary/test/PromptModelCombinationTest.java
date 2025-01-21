package com.Orio.wither_project.pdf.summary.test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.config.OllamaConfig;
import com.Orio.wither_project.pdf.summary.config.SummaryPromptConfig;
import com.Orio.wither_project.pdf.summary.service.impl.OllamaSummaryGenerationService;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.Data;

@SpringBootTest
@ActiveProfiles("test")
class PromptModelCombinationTest {
        private static final Logger logger = LoggerFactory.getLogger(PromptModelCombinationTest.class);

        @Autowired
        private TestLargeLanguageModels models;

        @Autowired
        private SummaryPromptConfig promptConfig;

        @Autowired
        private TestTextConfiguration testText;

        @Autowired
        private OllamaConfig ollamaConfig;

        @Autowired
        private OllamaApi ollamaApi;

        private static final int GENERATIONS_PER_COMBINATION = 1;

        @Data
        private static class TestResult {
                private final String model;
                private final String promptType;
                private final int generation;
                private final String summary;
                private final long executionTime;
        }

        @Test
        void testAllCombinations() throws IOException {
                logger.info("Starting prompt-model combination test for models: {}", models.getAllModels());
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("prompt_model_results_%s.csv", timestamp);
                Path resultPath = Paths.get("test_results", fileName);
                resultPath.getParent().toFile().mkdirs();
                logger.debug("Writing results to: {}", resultPath);

                try (CSVWriter writer = new CSVWriter(new FileWriter(resultPath.toFile()))) {
                        writer.writeNext(new String[] { "Model", "Prompt Type", "Generation #", "Summary",
                                        "Execution Time (ms)" });

                        for (String model : models.getAllModels()) {
                                logger.info("Testing model: {}", model);
                                ollamaConfig.setModel(model);
                                OllamaChatModel chatModel = OllamaChatModel.builder().withOllamaApi(ollamaApi)
                                                .withDefaultOptions(new OllamaOptions()
                                                                .withTemperature(ollamaConfig.getTemperature())
                                                                .withModel(model).withNumCtx(ollamaConfig.getNumCTX()))
                                                .build();
                                OllamaSummaryGenerationService service = new OllamaSummaryGenerationService(chatModel,
                                                promptConfig);

                                // Test different prompt types
                                testPromptType(service, "Executive", promptConfig.getExecutiveSummarySystemMessage(),
                                                promptConfig.getExecutiveSummaryJsonSchema(), writer, model);
                                testPromptType(service, "Technical",
                                                promptConfig.getDetailedTechnicalSummarySystemMessage(),
                                                promptConfig.getDetailedTechnicalSummaryJsonSchema(), writer,
                                                model);
                                testPromptType(service, "Creative", promptConfig.getCreativeSummarySystemMessage(),
                                                promptConfig.getCreativeSummaryJsonSchema(), writer, model);
                                testPromptType(service, "Analytical", promptConfig.getAnalyticalSummarySystemMessage(),
                                                promptConfig.getAnalyticalSummaryJsonSchema(), writer, model);
                                testPromptType(service, "Narrative", promptConfig.getNarrativeSummarySystemMessage(),
                                                promptConfig.getNarrativeSummaryJsonSchema(), writer, model);
                        }
                } catch (IOException e) {
                        logger.error("Error writing test results: {}", e.getMessage(), e);
                        throw e;
                }
                logger.info("Completed prompt-model combination test");
        }

        private void testPromptType(OllamaSummaryGenerationService service, String promptType,
                        SystemMessage systemMessage, String responseFormat, CSVWriter writer, String model) {
                logger.debug("Testing prompt type '{}' with model '{}'", promptType, model);
                String testText = this.testText.getSingleParagraph();

                for (int i = 1; i <= GENERATIONS_PER_COMBINATION; i++) {
                        logger.debug("Generation {} of {} for prompt type '{}' with model '{}'",
                                        i, GENERATIONS_PER_COMBINATION, promptType, model);
                        long startTime = System.currentTimeMillis();
                        String summary = service.summarize(testText, systemMessage.getContent(), responseFormat);
                        long executionTime = System.currentTimeMillis() - startTime;
                        logger.debug("Generation completed in {}ms", executionTime);

                        TestResult result = new TestResult(model, promptType, i, summary, executionTime);
                        writer.writeNext(new String[] {
                                        result.getModel(),
                                        result.getPromptType(),
                                        String.valueOf(result.getGeneration()),
                                        result.getSummary(),
                                        String.valueOf(result.getExecutionTime())
                        });
                }
                logger.debug("Completed testing prompt type '{}' with model '{}'", promptType, model);
        }
}
