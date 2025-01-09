package com.Orio.wither_project.pdf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import com.Orio.wither_project.pdf.summary.model.BookSummaryModel;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentModel extends BaseContentModel<BookSummaryModel> {
    @NotBlank
    @Size(max = 255)
    @Column(unique = true)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String author;

    @OneToMany(mappedBy = "doc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChapterModel> chapters;
}