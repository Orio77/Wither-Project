package com.Orio.wither_project.gader.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.gather.controller.GatherController;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.dto.QueryRequest;
import com.Orio.wither_project.gather.service.orchestration.IWitherOrchestrationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class GatherControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IWitherOrchestrationService witherOrchestrationService;

    @InjectMocks
    private GatherController gatherController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gatherController).build();
    }

    @Test
    void testGatherInformation_Success() throws Exception {
        // Prepare test data
        String queryText = "test query";
        QueryRequest request = new QueryRequest();
        request.setQuery(queryText);

        InformationPiece mockResponse = new InformationPiece();
        mockResponse.setAuthor("Test content response");

        // Execute and verify
        mockMvc.perform(post(ApiPaths.BASE + ApiPaths.GATHER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.author").value("Test content response"));

        // Verify service was called
        verify(witherOrchestrationService, times(1)).orchestrate(queryText);
    }

    @Test
    void testGatherInformation_Error() throws Exception {
        // Prepare test data
        String queryText = "test query";
        QueryRequest request = new QueryRequest();
        request.setQuery(queryText);

        InformationPiece mockResponse = new InformationPiece();
        mockResponse.setAuthor("Test content response");

        // Execute and verify
        mockMvc.perform(post(ApiPaths.BASE + ApiPaths.GATHER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.author").value("Test content response"));

        // Verify service was called
        verify(witherOrchestrationService, times(1)).orchestrate(queryText);
    }
}
