package com.Orio.wither_project.service.dataSaving;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Orio.wither_project.config.DataBaseConfig;
import com.Orio.wither_project.exception.UnauthorizedException;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.repository.SQLDataModelRepo;
import com.Orio.wither_project.service.data.managing.repoService.impl.PostgreSQLService;

@SpringBootTest
public class PostgreSQLServiceTest {

    @Autowired
    private SQLDataModelRepo sqlRepo;

    @Autowired
    private DataBaseConfig dbConfig;

    private PostgreSQLService postgreSQLService;
    private List<DataModel> data;
    private List<Long> ids;

    @BeforeEach
    public void setUp() {
        postgreSQLService = new PostgreSQLService(sqlRepo, dbConfig);
        data = Arrays.asList(new DataModel("source1", "data1"), new DataModel("source2", "data2"));
    }

    @AfterEach
    public void tearDown() {
        sqlRepo.deleteAllById(ids);
    }

    @Test
    public void testSave() {
        // Save the new data and flush to ensure IDs are generated
        postgreSQLService.saveAll(data);
        sqlRepo.flush();

        // Collect the IDs from the data objects after they have been saved
        ids = data.stream().map(DataModel::getId).collect(Collectors.toList());

        // Assertions
        assertEquals(2, ids.size());
        List<DataModel> retrievedData = sqlRepo.findAllById(ids);
        System.out.println("Expected Data: " + data);
        System.out.println("Retrieved Data: " + retrievedData);
        // Because of a transient field, I assert equality that way
        assertTrue(retrievedData.get(0).getSource().equals(data.get(0).getSource()));
    }

    @Test
    public void testGet() {
        // Save the new data and flush to ensure IDs are generated
        postgreSQLService.saveAll(data);
        sqlRepo.flush();

        // Collect the IDs from the data objects after they have been saved
        ids = data.stream().map(DataModel::getId).collect(Collectors.toList());

        List<DataModel> retrievedData = postgreSQLService.get(ids);
        assertEquals(2, retrievedData.size());
        // Because of a transient field, I assert equality that way
        assertTrue(retrievedData.get(0).getSource().equals(data.get(0).getSource()));
    }

    @Test
    public void testRemove() throws UnauthorizedException {
        // Save the new data and flush to ensure IDs are generated
        postgreSQLService.saveAll(data);
        sqlRepo.flush();

        // Collect the IDs from the data objects after they have been saved
        ids = data.stream().map(DataModel::getId).collect(Collectors.toList());

        postgreSQLService.remove(ids, dbConfig.getSqlPassword());

        List<DataModel> remainingData = sqlRepo.findAllById(ids);
        assertTrue(remainingData.isEmpty());
    }

    @Test
    public void testRemoveWithWrongPassword() {
        // Save the new data and flush to ensure IDs are generated
        postgreSQLService.saveAll(data);
        sqlRepo.flush();

        // Collect the IDs from the data objects after they have been saved
        ids = data.stream().map(DataModel::getId).collect(Collectors.toList());

        assertThrows(UnauthorizedException.class, () -> {
            postgreSQLService.remove(ids, "wrongPassword");
        });

        List<DataModel> remainingData = sqlRepo.findAllById(ids);
        assertEquals(2, remainingData.size());
    }
}