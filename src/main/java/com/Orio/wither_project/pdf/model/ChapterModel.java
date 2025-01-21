package com.Orio.wither_project.pdf.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

import com.Orio.wither_project.pdf.summary.model.ChapterSummaryModel;

@Data
@Entity
public class ChapterModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @OneToOne(cascade = CascadeType.ALL)
    private ChapterSummaryModel summary;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    private PDFType type;

    private String title;
    private int chapterNumber;

    @JsonBackReference
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "book_id")
    private DocumentModel doc;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chapter", fetch = FetchType.EAGER)
    private List<PageModel> pages;
}