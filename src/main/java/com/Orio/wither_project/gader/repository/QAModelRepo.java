package com.Orio.wither_project.gader.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.gader.model.QAModel;

@Repository
public interface QAModelRepo extends JpaRepository<QAModel, Long> {

    QAModel findByQuestion(String question);

    QAModel findByAnswer(String answer);

    List<QAModel> findBySource(String source);

}
