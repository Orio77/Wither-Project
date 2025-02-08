package com.Orio.wither_project.pdf.summary.model;

import com.Orio.wither_project.pdf.model.PageModel;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageSummaryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @JsonBackReference
    @OneToOne(mappedBy = "summary")
    private PageModel page;

    public PageSummaryModel(String content) {
        this.content = content;
    }

    public void addPage(PageModel page) {
        this.page = page;
        page.setSummary(this);
    }
}
