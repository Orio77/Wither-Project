package com.Orio.web_scraping_tool.service.newImpl.dataSaving;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.config.VectorDbConfig;
import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataSaving.IVectorStoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpringVectorDbService implements IVectorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(SpringVectorDbService.class);

    private final VectorStore vectorStore;
    private final VectorDbConfig vectorDbConfig;

    public void save(List<DataModel> data) {

        if (data == null || data.isEmpty()) {
            logger.error("Received questions was null or empty: {}", data);
            throw new IllegalArgumentException("Question list cannot be null or empty");
        }
        List<String> questions = data.stream().map(DataModel::getQuestion).toList();
        List<Document> questionDocs = questions.stream().filter(Objects::nonNull).map(q -> new Document(q)).toList();
        if (questionDocs.isEmpty()) {
            logger.warn("All questions were null, resulting in an empty document list. Received questions: {}",
                    questions);
            throw new IllegalArgumentException("All questions were null, resulting in an empty document list.");
        }
        logger.debug("Documents: {}", questionDocs);
        vectorStore.add(questionDocs);
        logger.debug("Documents saved successfully");
    }

    public List<String> search(String question) {
        Objects.requireNonNull(question, "Search query must not be null");
        if (question.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query must not be empty");
        }
        return this.searchDocs(question).stream()
                .map(doc -> doc.getContent()).toList();
    }

    public List<Document> searchDocs(String str) {
        try {
            return vectorStore.similaritySearch(SearchRequest.defaults()
                    .withQuery(str)
                    .withTopK(vectorDbConfig.getTopK()));
        } catch (Exception e) {
            logger.error("Error occurred during document search", e);
            throw new RuntimeException("Vector Search operation failed", e);
        }
        // .withSimilarityThreshold(vectorDbConfig.getSimilarityThreshold()))
    }

    @Override
    public boolean remove(List<String> ids, String removePassword) {
        if (ids == null || ids.isEmpty()) {
            logger.warn("Id list is null or empty: {}", ids);
            throw new IllegalArgumentException("Element ID cannot be null or empty");
        }
        Optional<Boolean> deleteSuccess = vectorStore.delete(ids);
        if (deleteSuccess.isPresent() && deleteSuccess.get()) {
            logger.info("ids {} were successfully deleted.", ids);
            return true;
        } else {
            logger.warn("Failed to delete ids {}", ids);
            return false;
        }
    }
}