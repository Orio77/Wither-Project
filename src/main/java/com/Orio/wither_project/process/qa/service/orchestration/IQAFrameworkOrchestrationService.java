package com.Orio.wither_project.process.qa.service.orchestration;

import java.util.List;

import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.process.qa.model.dto.QAProcessRequestDTO;

public interface IQAFrameworkOrchestrationService {

    default void processAndSave(QAProcessRequestDTO request) {
        InformationPiece data = getData(request);

        List<TextBatch> batches = split(data);

        List<TextBatch> filteredBatches = filter(batches);

        setSources(filteredBatches, data.getSource());

        orchestrateGeneration(filteredBatches);

    }

    default void setSources(List<TextBatch> batches, String source) {
        batches.forEach(batch -> batch.setSource(source));
    }

    InformationPiece getData(QAProcessRequestDTO request);

    List<TextBatch> split(InformationPiece info);

    List<TextBatch> filter(List<TextBatch> batches);

    void orchestrateGeneration(List<TextBatch> batches);

}
