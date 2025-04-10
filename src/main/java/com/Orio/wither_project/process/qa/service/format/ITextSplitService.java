package com.Orio.wither_project.process.qa.service.format;

import java.util.List;

import com.Orio.wither_project.gather.model.Content;
import com.Orio.wither_project.gather.model.TextBatch;

public interface ITextSplitService {

    List<TextBatch> splitContent(List<Content> items);

    List<TextBatch> splitContent(Content item);
}
