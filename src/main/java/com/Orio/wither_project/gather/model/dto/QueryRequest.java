package com.Orio.wither_project.gather.model.dto;

import java.util.List;

import com.Orio.wither_project.pdf.model.entity.FileEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    private String query;
    private WebRequest url;
    private List<WebRequest> urls;
    private FileEntity file;
    private String content;
}
