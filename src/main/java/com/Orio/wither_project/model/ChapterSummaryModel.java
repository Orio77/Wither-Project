package com.Orio.wither_project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.Orio.wither_project.service.summary.ProgressivelySummarizable;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ChapterSummaryModel extends AbstractSummaryModel implements ProgressivelySummarizable<PageSummaryModel> {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(columnDefinition = "text")
        private String content;

        @OneToOne
        private ChapterModel chapter;

        private String chapterTitle;
        private int chapterNumber;
        private static final int PART_LENGTH = 3;

        public ChapterSummaryModel(String content) {
                this.content = content;
        }

        @Override
        public String getText(List<PageSummaryModel> pageSummaries) {
                return pageSummaries.stream()
                                .map(PageSummaryModel::getContent)
                                .collect(Collectors.joining("\n"));
        }

        @Override
        public List<PageSummaryModel> split() {
                return chapter.getPages().stream()
                                .map(PageModel::getSummary)
                                .toList();
        }
}
