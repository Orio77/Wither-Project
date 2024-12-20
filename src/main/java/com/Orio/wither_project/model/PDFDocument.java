package com.Orio.wither_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class PDFDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(unique = true)
    private String fileName;

    @NotBlank
    @Size(max = 255)
    private String author;

    @Lob
    @Column(length = 1000000) // Changed this line
    private byte[] data;

    @Column(columnDefinition = "text")
    private String summary;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    private List<String> chapterSummaries;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    private List<String> pageSummaries;
}