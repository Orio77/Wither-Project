package com.Orio.wither_project.process.summary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
@NoArgsConstructor
public class DocumentSummaryModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(columnDefinition = "text")
        private String content;

        @JsonBackReference
        @OneToOne
        @JoinColumn(name = "book_id")
        private DocumentModel book; // TODO change to document

        public DocumentSummaryModel(String content) {
                this.content = content;
        }

        public void addDocument(DocumentModel book) {
                this.book = book;
                book.setSummary(this);
        }
}