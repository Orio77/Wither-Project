package com.Orio.wither_project.summary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(exclude = { "pages", "doc" })
public class ChapterModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @OneToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    private ChapterSummaryModel summary;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    private PDFType type;

    private String title;
    private int chapterNumber;

    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name = "book_id")
    private DocumentModel doc;

    @JsonManagedReference
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<PageModel> pages;

    public void addPages(List<PageModel> pages) {
        if (this.pages == null) {
            this.pages = new ArrayList<>();
        }
        this.pages.addAll(pages);
        pages.forEach(page -> page.setChapter(this));
    }
}