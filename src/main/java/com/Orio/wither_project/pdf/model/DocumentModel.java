package com.Orio.wither_project.pdf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.Orio.wither_project.pdf.summary.model.BookSummaryModel;

import java.util.List;

@Entity
@Data
public class DocumentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @OneToOne(cascade = CascadeType.ALL)
    private BookSummaryModel summary;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    private PDFType type;

    @NotBlank
    @Size(max = 255)
    @Column()
    private String title;

    @NotBlank
    @Size(max = 255)
    private String author;

    @JsonManagedReference
    @OneToMany(mappedBy = "doc", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ChapterModel> chapters;
}