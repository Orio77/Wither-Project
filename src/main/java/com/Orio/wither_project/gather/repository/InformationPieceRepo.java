package com.Orio.wither_project.gather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.gather.model.InformationPiece;

@Repository
public interface InformationPieceRepo extends JpaRepository<InformationPiece, Long> {

    InformationPiece findBySource(String source);
}
