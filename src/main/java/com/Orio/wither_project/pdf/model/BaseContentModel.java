package com.Orio.wither_project.pdf.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class BaseContentModel<T> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @OneToOne(cascade = CascadeType.ALL)
    private T summary;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    private PDFType type;
}
