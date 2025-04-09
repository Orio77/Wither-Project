package com.Orio.wither_project.query.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.process.qa.model.QAModel;

@Repository
public interface QAModelRepo extends JpaRepository<QAModel, Long> {

    QAModel findByQuestion(String question);

    List<QAModel> findByQuestionIn(List<String> questions);

    QAModel findByAnswer(String answer);

    List<QAModel> findBySource(String source);

}
