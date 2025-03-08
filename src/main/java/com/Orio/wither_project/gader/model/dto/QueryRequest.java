package com.Orio.wither_project.gader.model.dto;

import lombok.Data;

@Data
public class QueryRequest {
    private String query;
    private boolean websites;
    private boolean files;
    private boolean news;
    private boolean researchPapers;
}
