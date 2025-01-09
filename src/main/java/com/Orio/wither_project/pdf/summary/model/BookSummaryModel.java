package com.Orio.wither_project.pdf.summary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BookSummaryModel extends AbstractSummaryModel implements ProgressivelySummarizable<ChapterSummaryModel> {
        @OneToOne
        @JoinColumn(name = "book_id")
        private DocumentModel book;

        private static final int PART_LENGTH = 3;

        public BookSummaryModel(String content) {
                super.setContent(content);
        }

        @Override
        public String getText(List<ChapterSummaryModel> chapterSummaries) {
                return chapterSummaries.stream()
                                .map(ChapterSummaryModel::getContent)
                                .collect(Collectors.joining("\n"));
        }

        @Override
        public List<ChapterSummaryModel> split() {
                List<ChapterModel> chapters = book.getChapters();
                return partition(chapters, PART_LENGTH).stream()
                                .map(chapterGroup -> {
                                        String combinedContent = chapterGroup.stream()
                                                        .map(ChapterModel::getContent)
                                                        .collect(Collectors.joining("\n"));
                                        return new ChapterSummaryModel(combinedContent);
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