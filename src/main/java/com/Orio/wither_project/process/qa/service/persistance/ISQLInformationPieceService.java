package com.Orio.wither_project.process.qa.service.persistance;

import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.process.qa.model.dto.QAProcessRequestDTO;

public interface ISQLInformationPieceService {

    InformationPiece getInformationPiece(QAProcessRequestDTO request);
}
