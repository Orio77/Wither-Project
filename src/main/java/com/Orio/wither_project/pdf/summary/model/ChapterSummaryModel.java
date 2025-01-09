package com.Orio.wither_project.pdf.summary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ChapterSummaryModel extends AbstractSummaryModel implements ProgressivelySummarizable<PageSummaryModel> {

        @OneToOne
        private ChapterModel chapter;

        private String chapterTitle;
        private int chapterNumber;
        private static final int PART_LENGTH = 3;

        public ChapterSummaryModel(String content) {
                super.setContent(content);
        }

        @Override
        public String getText(List<PageSummaryModel> pageSummaries) {
                return pageSummaries.stream()
                                .map(PageSummaryModel::getContent)
                                .collect(Collectors.joining("\n"));
        }

        @Override
        public List<PageSummaryModel> split() {
                List<PageModel> pages = chapter.getPages();
                return partition(pages, PART_LENGTH).stream()
                                .map(pageGroup -> {
                                        String combinedContent = pageGroup.stream()
                                                        .map(PageModel::getContent)
                                                        .collect(Collectors.joining("\n"));
                                        return new PageSummaryModel(combinedContent);
                                })
                                .toList();
        }

        private <T> List<List<T>> partition(List<T> list, int size) {
                return list.stream()
                                .collect(Collectors.groupingBy(item -> list.indexOf(item) / size))
                                .values()
                                .stream()
                                .toList();
        }
}
