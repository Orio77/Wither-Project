package com.Orio.wither_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.model.DataModel;

@Repository
public interface SQLRepo extends JpaRepository<DataModel, Long> {

}
