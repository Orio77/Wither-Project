package com.Orio.wither_project.service.summary;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.config.OllamaConfig;
import com.Orio.wither_project.model.BookSummaryModel;
import com.Orio.wither_project.model.ChapterSummaryModel;
import com.Orio.wither_project.model.PageModel;
import com.Orio.wither_project.model.PageSummaryModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaBookSummaryService implements IBookSummaryService {
    private final OllamaConfig ollamaConfig;
    private static final Logger logger = LoggerFactory.getLogger(OllamaBookSummaryService.class);

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createSummary(String content, ISummaryType type) {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(type, "type must not be null");

        logger.info("Creating summary for content of type {}", type);

        ChatRequest request = buildChatRequest(content, type);
        ChatResponse response = ollamaConfig.getOllamaApi().chat(request);

        if (response == null || response.message() == null) {
            logger.error("Failed to get valid response from Ollama API");
            throw new RuntimeException("Failed to get valid response from Ollama API");
        }

        try {
            T summary = (T) type.createSummary(response.message().content());
            logger.info("Successfully created summary");
            return summary;
        } catch (Exception e) {
            logger.error("Failed to create summary instance", e);
            throw new RuntimeException("Failed to create summary instance", e);
        }
    }

    @Override
    public <T, E extends ProgressivelySummarizable<T>> E createProgressiveSummary(
            List<T> parts,
            ISummaryType type,
            E container) {
        Objects.requireNonNull(parts, "parts must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(container, "container must not be null");

        if (parts.isEmpty()) {
            logger.warn("Parts list is empty, returning container");
            return container;
        }

        String combinedText = container.getText(parts);
        String summarizedContent = createSummary(combinedText, type);
        container.setContent(summarizedContent);

        return container;
    }

    @Override
    public List<PageSummaryModel> getPageSummaries(List<PageModel> pages) {
        return pages.stream()
                .<PageSummaryModel>map(page -> createSummary(page.getContent(), SummaryType.PAGE))
                .toList();
    }

    @Override
    public ChapterSummaryModel generateChapterSummary(List<PageSummaryModel> pageSummaries) {
        ChapterSummaryModel container = new ChapterSummaryModel();
        return createProgressiveSummary(pageSummaries, SummaryType.CHAPTER, container);
    }

    @Override
    public BookSummaryModel getBookSummary(List<ChapterSummaryModel> chapterSummaries) {
        BookSummaryModel container = new BookSummaryModel();
        return createProgressiveSummary(chapterSummaries, SummaryType.BOOK, container);
    }

    private ChatRequest buildChatRequest(String content, ISummaryType type) {
        logger.debug("Building chat request for content of type {}", type);

        OllamaOptions options = new OllamaOptions()
                .withTemperature(ollamaConfig.getTemperature())
                .withNumCtx(ollamaConfig.getNumCTX());

        Message systemMessage = Message.builder(Role.SYSTEM)
                .withContent(type.getSystemPrompt())
                .build();

        Message userMessage = Message.builder(Role.USER)
                .withContent(content)
                .build();

        return ChatRequest.builder(ollamaConfig.getModel())
                .withMessages(List.of(systemMessage, userMessage))
                .withOptions(options)
                .build();
    }
}