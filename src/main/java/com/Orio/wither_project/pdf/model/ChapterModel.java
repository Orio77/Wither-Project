package com.Orio.wither_project.pdf.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import com.Orio.wither_project.pdf.summary.model.ChapterSummaryModel;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ChapterModel extends BaseContentModel<ChapterSummaryModel> {
    private String title;
    private int chapterNumber;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private DocumentModel doc;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chapter")
    private List<PageModel> pages;
}
