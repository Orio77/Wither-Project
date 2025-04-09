package com.Orio.wither_project.process.qa.service.persistance.impl;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.repository.InformationPieceRepo;
import com.Orio.wither_project.process.qa.exception.InformationPieceNotFoundException;
import com.Orio.wither_project.process.qa.model.dto.QAProcessRequestDTO;
import com.Orio.wither_project.process.qa.service.persistance.ISQLInformationPieceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgresInformationPieceService implements ISQLInformationPieceService {

    private final InformationPieceRepo repo;

    @Override
    public InformationPiece getInformationPiece(QAProcessRequestDTO request) {
        log.info("Getting information piece from SQL for request: {}", request);

        Long id = request.getId();
        String source = request.getSource();

        InformationPiece result = null;

        if (id != null) {
            log.debug("Searching for information piece by ID: {}", id);
            result = repo.findById(id).orElse(null);
            if (result == null) {
                log.warn("Information piece with ID {} not found", id);
                throw new InformationPieceNotFoundException("Information piece not found with ID: " + id);
            }
        } else if (source != null && !source.trim().isEmpty()) {
            log.debug("Searching for information piece by source: {}", source);
            result = repo.findBySource(source);
            if (result == null) {
                log.warn("Information piece with source '{}' not found", source);
                throw new InformationPieceNotFoundException("Information piece not found with source: " + source);
            }
        } else {
            log.error("Cannot retrieve information piece: both ID and source are missing or invalid");
            throw new IllegalArgumentException("Either ID or source must be provided to retrieve an information piece");
        }

        return result;
    }
}
