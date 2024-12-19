package com.Orio.wither_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.model.DataModel;

@Repository
public interface SQLDataModelRepo extends JpaRepository<DataModel, Long> {

    List<DataModel> findByQuestionIn(List<String> questions);

    @Query("SELECT d FROM DataModel d WHERE LOWER(d.question) IN :questions")
    List<DataModel> findByQuestionInIgnoreCase(List<String> questions);
}