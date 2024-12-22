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
public class BookSummaryModel extends AbstractSummaryModel implements ProgressivelySummarizable<ChapterSummaryModel> {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(columnDefinition = "text")
        private String content;

        @OneToOne
        @JoinColumn(name = "book_id")
        private BookModel book;

        public BookSummaryModel(String content) {
                this.content = content;
        }

        @Override
        public String getText(List<ChapterSummaryModel> chapterSummaries) {
                return chapterSummaries.stream()
                                .map(ChapterSummaryModel::getContent)
                                .collect(Collectors.joining("\n"));
        }

        @Override
        public List<ChapterSummaryModel> split() {
                return book.getChapters().stream()
                                .map(ChapterModel::getSummary)
                                .toList();
        }
}