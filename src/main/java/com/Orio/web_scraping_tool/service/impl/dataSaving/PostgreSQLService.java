package com.Orio.web_scraping_tool.service.impl.dataSaving;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.config.DataBaseConfig;
import com.Orio.web_scraping_tool.exception.UnauthorizedException;
import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.repository.SQLRepo;
import com.Orio.web_scraping_tool.service.dataSaving.ISQLService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostgreSQLService implements ISQLService {

    private final SQLRepo sqlRepo;
    private final DataBaseConfig dbConfig;
    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLService.class);

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

}