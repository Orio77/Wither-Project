package com.Orio.wither_project.gather.model;

import java.util.List;

import lombok.Data;

@Data
public class InformationPiece {
    private String query;
    private String title;
    private String link;
    private String content;
    private String description;
    private String author;
    private String publishDate;
    private List<Exception> error;
}
