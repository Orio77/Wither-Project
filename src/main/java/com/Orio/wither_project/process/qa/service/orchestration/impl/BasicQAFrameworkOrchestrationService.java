package com.Orio.wither_project.process.qa.service.orchestration.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.ContentWithSource;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.gather.service.format.IModelFormatService;
import com.Orio.wither_project.process.qa.model.dto.QAProcessRequestDTO;
import com.Orio.wither_project.process.qa.service.filtration.ITextFiltrationService;
import com.Orio.wither_project.process.qa.service.format.ITextSplitService;
import com.Orio.wither_project.process.qa.service.orchestration.IQAFrameworkOrchestrationService;
import com.Orio.wither_project.process.qa.service.orchestration.IQAGenerationOrchestrationService;
import com.Orio.wither_project.process.qa.service.persistance.ISQLInformationPieceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicQAFrameworkOrchestrationService implements IQAFrameworkOrchestrationService {

    private final ISQLInformationPieceService sqlInformationPieceService;
    private final IModelFormatService modelFormatService;
    private final ITextSplitService textSplitService;
    private final ITextFiltrationService textFiltrationService;
    private final IQAGenerationOrchestrationService orchestrationService;

    @Override
    public InformationPiece getData(QAProcessRequestDTO request) {
        return sqlInformationPieceService.getInformationPiece(request);
    }

    @Override
    public List<TextBatch> split(InformationPiece info) {
        log.info("Splitting information piece: {}", info);
        ContentWithSource cws = modelFormatService.format(info);
        log.info("Formatted content with source: {}", cws);
        return textSplitService.splitContent(cws);
    }

    @Override
    public List<TextBatch> filter(List<TextBatch> batches) {
        return textFiltrationService.filter(batches);
    }

    @Override
    public void orchestrateGeneration(List<TextBatch> batches) {
        log.info("Starting orchestration of generation for {} batches", batches.size());
        for (TextBatch textBatch : batches) {
            orchestrationService.orchestrate(textBatch);
        }
    }

}
