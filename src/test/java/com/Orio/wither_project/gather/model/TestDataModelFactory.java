package com.Orio.wither_project.gather.model;

import java.util.ArrayList;
import java.util.List;

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;

/**
 * Factory class to create test data models
 */
public class TestDataModelFactory {

    /**
     * Creates a test data model with various QA content
     */
    public static DataModel createSampleDataModel() {
        DataModel dataModel = DataModel.builder().query("Sample query").build();
        List<ScrapeItem> items = new ArrayList<>();

        // Create sample scrape items with QA pairs
        items.add(createScrapeItem(
                "https://example.com/programming",
                "Programming languages overview. Question: What is Java? Answer: Java is a class-based, object-oriented programming language."));

        items.add(createScrapeItem(
                "https://example.com/frameworks",
                "Popular frameworks. Question: What is Spring Boot? Answer: Spring Boot is a Java-based framework for building microservices."));

        items.add(createScrapeItem(
                "https://example.com/languages",
                "Modern languages. Question: What is Python? Answer: Python is a high-level, interpreted programming language with dynamic semantics."));

        dataModel.setItems(items);
        return dataModel;
    }

    private static ScrapeItem createScrapeItem(String url, String content) {
        ScrapeItem item = ScrapeItem.builder().build();
        item.setLink(url);
        item.setContent(content);
        return item;
    }
}
