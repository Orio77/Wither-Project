package com.Orio.wither_project.service.data.managing.repoService.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.config.DataBaseConfig;
import com.Orio.wither_project.exception.UnauthorizedException;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.repository.SQLDataModelRepo;
import com.Orio.wither_project.service.data.managing.repoService.ISQLDataModelService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostgreSQLDataModelService implements ISQLDataModelService {

    private final SQLDataModelRepo sqlRepo;
    private final DataBaseConfig dbConfig;
    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLDataModelService.class);

    @Override
    public void save(List<DataModel> data) {
        logger.debug("Received data to save: {}", data);
        List<DataModel> res = sqlRepo.saveAll(data);
        logger.debug("Saved data to the SQL database: {}", res);
    }

    @Override
    public List<DataModel> get(List<Long> ids) {
        return sqlRepo.findAllById(ids);
    }

    @Override
    public void remove(List<Long> ids, String removePassword) throws UnauthorizedException {
        if (removePassword.equals(dbConfig.getSqlPassword())) {
            sqlRepo.deleteAllById(ids);
        } else
            throw new UnauthorizedException("User failed to provide the correct password to the sql database");
    }

    public void saveAll(List<DataModel> dataList) {
        sqlRepo.saveAllAndFlush(dataList);
    }

    @Override
    public List<DataModel> findByQuestionIn(List<String> questions) {
        logger.debug("Searching for DataModels with questions (case-sensitive): {}", questions);
        List<DataModel> results = sqlRepo.findByQuestionIn(questions);

        if (results.isEmpty()) {
            logger.debug("No case-sensitive matches found, trying case-insensitive search");
            results = findByQuestionInIgnoreCase(questions);
        }

        return results;
    }

    @Override
    public List<DataModel> findByQuestionInIgnoreCase(List<String> questions) {
        List<String> lowerCaseQuestions = questions.stream()
                .map(String::toLowerCase)
                .toList();
        logger.debug("Searching for DataModels with questions (case-insensitive): {}", lowerCaseQuestions);
        return sqlRepo.findByQuestionInIgnoreCase(lowerCaseQuestions);
    }

}
