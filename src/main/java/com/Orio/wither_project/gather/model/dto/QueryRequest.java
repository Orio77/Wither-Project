package com.Orio.wither_project.gather.model.dto;

import lombok.Data;

@Data
public class QueryRequest {
    private String query;
    private boolean websites;
    private boolean files;
    private boolean news;
    private boolean researchPapers;
}
