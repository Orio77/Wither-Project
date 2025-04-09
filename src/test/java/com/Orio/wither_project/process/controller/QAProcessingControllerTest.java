package com.Orio.wither_project.process.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.config.TestTextConfiguration;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.repository.InformationPieceRepo;
import com.Orio.wither_project.process.qa.controller.QAProcessingController;
import com.Orio.wither_project.process.qa.model.dto.QAProcessRequestDTO;

@SpringBootTest
@ActiveProfiles("test")
public class QAProcessingControllerTest {

    @Autowired
    private QAProcessingController qaProcessingController;

    @Autowired
    private InformationPieceRepo repo;

    @Autowired
    private TestTextConfiguration testTextConfig;

    private QAProcessRequestDTO request;

    private InformationPiece informationPiece;

    @BeforeEach
    void setUp() {
        request = createRequest();
        informationPiece = InformationPiece.builder()
                .id(request.getId())
                .content(testTextConfig.getWebPageContent())
                .source(request.getSource())
                .build();

        repo.save(informationPiece);
    }

    @AfterEach
    void tearDown() {
        repo.delete(informationPiece);
    }

    @Test
    void testController() {
        qaProcessingController.process(request);
    }

    private QAProcessRequestDTO createRequest() {
        return new QAProcessRequestDTO(Long.valueOf(1), "source");
    }
}
