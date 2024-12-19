package com.Orio.wither_project.service.dataSaving;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Orio.wither_project.config.DataBaseConfig;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.data.managing.repoService.impl.SpringVectorDbService;

@SpringBootTest
public class Neo4jDbServiceTest {

    @Autowired
    private SpringVectorDbService neo4jService;
    @Autowired
    private DataBaseConfig dbConfig;
    private static List<DataModel> list;

    @BeforeAll
    public static void setUp() {
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DataModel dataModel = new DataModel("Source" + i, "Element" + i);
            dataModel.setQuestion("Question" + i);
            dataModel.setAnswer("Answer" + i);
            list.add(dataModel);
        }
    }

    private void cleanUp() {
        list.stream().forEach(element -> {
            System.out.println("Cleaning up element: " + element);
            List<Document> searchRes = neo4jService.searchDocs(element.getQuestion());
            System.out.println("Found search results: " + searchRes);
            if (searchRes == null || searchRes.isEmpty()) {
                return;
            }
            List<String> idToRemove = searchRes.stream().map(Document::getId).limit(1).toList();
            neo4jService.remove(idToRemove, dbConfig.getVsPassword());
        });
    }

    @Test
    public void testAdd() {
        neo4jService.save(list);

        System.out.println("List: " + list);
        DataModel dataModel = list.get(3);
        System.out.println("Data model: " + dataModel);

        String question = dataModel.getQuestion();
        System.out.println("Question: " + question);

        List<String> searchResult = neo4jService.search(question);
        System.out.println(searchResult);

        assertNotNull(searchResult);
        assertTrue(!searchResult.isEmpty());

        assertDoesNotThrow(() -> this.cleanUp());
    }
}