package com.Orio.wither_project.pdf.service.orchestration;

import java.io.IOException;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.model.DocumentModel;

public interface IPDFProcessingOrchestrationService {
    default boolean processPDF(FileEntity file) throws IOException {
        DocumentModel doc = convert(file);
        if (doc == null)
            return false;

        return setMetadata(doc) &&
                setContents(doc) &&
                setSummaries(doc);
    }

    DocumentModel convert(FileEntity file) throws IOException;

    boolean setMetadata(DocumentModel model);

    boolean setContents(DocumentModel model);

    boolean setSummaries(DocumentModel model);
}
