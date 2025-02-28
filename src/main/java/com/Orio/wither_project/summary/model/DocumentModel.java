package com.Orio.wither_project.summary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
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
    @ToString.Exclude
    private DocumentSummaryModel summary;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    private PDFType type;

    @NotBlank
    @Size(max = 255)
    @Column(unique = true)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String author;

    @Column(name = "summary_completion_percentage")
    private Double summaryCompletionPercentage = 0.0;

    @JsonManagedReference
    @ToString.Exclude
    @OneToMany(mappedBy = "doc", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<ChapterModel> chapters;

    public void addChapters(List<ChapterModel> chapters) {
        if (this.chapters == null) {
            this.chapters = new ArrayList<>();
        }
        this.chapters.addAll(chapters);
        chapters.forEach(chapter -> chapter.setDoc(this));
    }
}