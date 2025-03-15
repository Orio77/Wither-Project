package com.Orio.wither_project.gather.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ScrapedTextBatch {
    private List<String> content;
    private String source;
}
