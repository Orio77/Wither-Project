package com.Orio.wither_project.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;
    private int pageNumber;

    @OneToOne(mappedBy = "page", cascade = CascadeType.ALL)
    private PageSummaryModel summary;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private ChapterModel chapter;
}
