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
import com.Orio.wither_project.gader.model.InformationPiece;
import com.Orio.wither_project.gader.model.dto.QueryRequest;
import com.Orio.wither_project.gader.service.orchestration.IWitherOrchestrationService;
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
        mockResponse.setSomeField(new String("Test content response"));

        // Configure mock
        when(witherOrchestrationService.orchestrate(queryText)).thenReturn(mockResponse);

        // Execute and verify
        mockMvc.perform(post(ApiPaths.BASE + ApiPaths.GATHER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.someField").value(new String("Test content response")));

        // Verify service was called
        verify(witherOrchestrationService, times(1)).orchestrate(queryText);
    }

    @Test
    void testGatherInformation_Error() throws Exception {
        // Prepare test data
        String queryText = "test query";
        QueryRequest request = new QueryRequest();
        request.setQuery(queryText);

        // Configure mock to throw exception
        when(witherOrchestrationService.orchestrate(queryText)).thenThrow(new RuntimeException("Test error"));

        // Execute and verify
        mockMvc.perform(post(ApiPaths.BASE + ApiPaths.GATHER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        // Verify service was called
        verify(witherOrchestrationService, times(1)).orchestrate(queryText);
    }
}
