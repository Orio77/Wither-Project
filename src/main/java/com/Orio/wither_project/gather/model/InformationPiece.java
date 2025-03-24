package com.Orio.wither_project.gather.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
public class InformationPiece {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String query;

    private String title;

    private String link;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String description;

    private String author;

    private String publishDate;

    @Transient
    private List<Exception> error;
}
