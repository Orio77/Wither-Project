package com.Orio.wither_project.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class ChapterModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int chapterNumber;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookModel book;

    @OneToOne(cascade = CascadeType.ALL)
    private ChapterSummaryModel summary;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chapter")
    private List<PageModel> pages;
}
