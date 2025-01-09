package com.Orio.wither_project.pdf.summary.model;

import com.Orio.wither_project.pdf.model.PageModel;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageSummaryModel extends AbstractSummaryModel {

    @OneToOne
    @JoinColumn(name = "page_id")
    private PageModel page;

    public PageSummaryModel(String content) {
        super.setContent(content);
    }
}
