package com.Orio.wither_project.gather.service.orchestration.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.repository.QAModelRepo;
import com.Orio.wither_project.gather.service.orchestration.impl.ProcessingOrchestrationService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Slf4j
public class ProcessingOrchestrationServiceTest {

    @Autowired
    private ProcessingOrchestrationService orchestrationService;

    @Autowired
    private QAModelRepo qaModelRepo;

    @BeforeEach
    void setUp() {
        // Clear any previous data
        qaModelRepo.deleteAll();
    }

    @Test
    void testOrchestrate() {
        // Create test data with multiple items
        DataModel dataModel = createTestDataModel();

        // Process the data
        orchestrationService.orchestrate(dataModel);

        // Verify results were saved to the database
        List<QAModel> savedResults = qaModelRepo.findAll();
        log.debug("Saved results: {}", savedResults);

        // Assertions
        assertFalse(savedResults.isEmpty(), "Results should not be empty");
        assertEquals(3, savedResults.size(), "Should have extracted 3 QA pairs");

        // Verify content of QA models
        boolean foundJavaQuestion = false;
        boolean foundSpringQuestion = false;
        boolean foundPythonQuestion = false;

        for (QAModel model : savedResults) {
            assertNotNull(model.getQuestion());
            assertNotNull(model.getAnswer());
            assertNotNull(model.getSource());

            if (model.getQuestion().contains("Java")) {
                foundJavaQuestion = true;
                assertTrue(model.getAnswer().contains("programming language"));
            }

            if (model.getQuestion().contains("Spring Boot")) {
                foundSpringQuestion = true;
                assertTrue(model.getAnswer().contains("framework"));
            }

            if (model.getQuestion().contains("Python")) {
                foundPythonQuestion = true;
                assertTrue(model.getAnswer().contains("high-level"));
            }
        }

        assertTrue(foundJavaQuestion, "Should find Java question");
        assertTrue(foundSpringQuestion, "Should find Spring Boot question");
        assertTrue(foundPythonQuestion, "Should find Python question");
    }

    private DataModel createTestDataModel() {
        DataModel dataModel = DataModel.builder().build();
        List<ScrapeItem> items = new ArrayList<>();

        ScrapeItem item1 = ScrapeItem.builder().build();
        item1.setContent(
                "This is a test content. Question: What is Java? Answer: Java is a popular programming language.");
        item1.setLink("https://test.com/java");
        items.add(item1);

        ScrapeItem item2 = ScrapeItem.builder().build();
        item2.setContent(
                "More test content. Question: What is Spring Boot? Answer: Spring Boot is a Java-based framework used to create microservices.");
        item2.setLink("https://test.com/spring");
        items.add(item2);

        ScrapeItem item3 = ScrapeItem.builder().build();
        item3.setContent("Question: What is Python? Answer: Python is a high-level, interpreted programming language.");
        item3.setLink("https://test.com/python");
        items.add(item3);

        dataModel.setItems(items);
        return dataModel;
    }
}
