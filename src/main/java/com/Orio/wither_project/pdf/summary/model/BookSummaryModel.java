package com.Orio.wither_project.pdf.summary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
@NoArgsConstructor
public class BookSummaryModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(columnDefinition = "text")
        private String content;

        @JsonBackReference
        @OneToOne
        @JoinColumn(name = "book_id")
        private DocumentModel book;

        public BookSummaryModel(String content) {
                this.content = content;
        }
}