package com.Orio.web_scraping_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.web_scraping_tool.model.DataModel;

@Repository
public interface SQLRepo extends JpaRepository<DataModel, Long> {

}
