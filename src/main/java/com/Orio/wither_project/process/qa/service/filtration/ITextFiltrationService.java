package com.Orio.wither_project.process.qa.service.filtration;

import java.util.List;

import com.Orio.wither_project.gather.model.TextBatch;

public interface ITextFiltrationService {

    List<TextBatch> filter(List<TextBatch> textBatches);
}
