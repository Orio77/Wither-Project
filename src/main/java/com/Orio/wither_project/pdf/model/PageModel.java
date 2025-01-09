package com.Orio.wither_project.pdf.model;

import com.Orio.wither_project.pdf.summary.model.PageSummaryModel;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class PageModel extends BaseContentModel<PageSummaryModel> {
    private int pageNumber;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private ChapterModel chapter;
}
