package com.Orio.wither_project.summary.summary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.Orio.wither_project.summary.model.ChapterModel;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
@NoArgsConstructor
public class ChapterSummaryModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(columnDefinition = "text")
        private String content;

        @JsonBackReference
        @OneToOne
        private ChapterModel chapter;
        private String chapterTitle;
        private int chapterNumber;

        public ChapterSummaryModel(String content) {
                this.content = content;
        }
}
