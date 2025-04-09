package com.Orio.wither_project.gather.service.persist.impl;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.config.VectorDbConfig;
import com.Orio.wither_project.gather.service.persist.IVectorStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Neo4jVectorService implements IVectorStoreService {

    private final VectorStore vectorStore;
    private final VectorDbConfig vectorDbConfig;

    @Override
    public List<String> getQuestions(String question) {

        SearchRequest request = SearchRequest.defaults().withQuery(question).withTopK(vectorDbConfig.getTopK());

        List<Document> result = vectorStore.similaritySearch(request);
        log.info("Found {} questions", result.size());
        return result.stream().map(Document::getContent).toList();
    }

    @Override
    public void save(List<String> questions) {
        List<Document> documents = questions.stream()
                .map(Document::new)
                .toList();
        vectorStore.add(documents);
    }

    @Override
    public void save(String question) {
        Document doc = Document.builder().withContent(question).build();
        vectorStore.add(List.of(doc));
    }
}
