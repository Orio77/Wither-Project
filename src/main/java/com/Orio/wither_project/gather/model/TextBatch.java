package com.Orio.wither_project.gather.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TextBatch {
    @Builder.Default
    private List<String> content = new ArrayList<>();
    private String source;

    public void addContent(String content) {
        this.content.add(content);
    }
}
