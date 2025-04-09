package com.Orio.wither_project.process.qa.service.format;

import java.util.List;

import com.Orio.wither_project.gather.model.ContentWithSource;
import com.Orio.wither_project.gather.model.TextBatch;

public interface ITextSplitService {

    List<TextBatch> splitContent(List<ContentWithSource> items);

    List<TextBatch> splitContent(ContentWithSource item);
}
