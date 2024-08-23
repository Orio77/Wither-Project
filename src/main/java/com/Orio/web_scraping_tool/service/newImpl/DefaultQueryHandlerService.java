package com.Orio.web_scraping_tool.service.newImpl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.IQueryHandlerService;
import com.Orio.web_scraping_tool.service.dataGathering.ISourceProcessorService;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;
import com.Orio.web_scraping_tool.service.dataProcessing.IAIQAService;
import com.Orio.web_scraping_tool.service.dataSaving.ISQLService;
import com.Orio.web_scraping_tool.service.dataSaving.IVectorStoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultQueryHandlerService implements IQueryHandlerService {
    private final ISourceProcessorService sourceProcessorService;
    private final List<IDataSource> dataSources;
    private final IAIQAService aiQAService;
    private final ISQLService sqlService;
    private final IVectorStoreService vectorDbService;

    private static final Logger logger = getLogger(DefaultQueryHandlerService.class);

    @Override
    public List<DataModel> getData(String query) {
        logger.info("Entering getData with query: {}", query);
        List<DataModel> data = null;
        try {
            data = sourceProcessorService.collectData(dataSources, query);
            logger.info("Data collected successfully for query: {}", query);
        } catch (Exception e) {
            logger.error("Error collecting data for query: {}", query, e);
        }
        logger.info("Exiting getData with query: {}", query);
        return data;
    }

    @Override
    public void processData(List<DataModel> data) {
        logger.info("Entering processData with data size: {}", data.size());
        try {
            aiQAService.generateQuestions(data);
            logger.info("Questions generated successfully");
            aiQAService.generateAnswers(data);
            logger.info("Answers generated successfully");
        } catch (Exception e) {
            logger.error("Error processing data", e);
        }
        logger.info("Exiting processData with data size: {}", data.size());
    }

    @Override
    public void saveData(List<DataModel> data) {
        logger.info("Entering saveData with data size: {}", data.size());
        try {
            sqlService.save(data);
            logger.info("Data saved to SQL successfully");
            vectorDbService.save(data);
            logger.info("Data saved to Vector DB successfully");
        } catch (Exception e) {
            logger.error("Error saving data", e);
        }
        logger.info("Exiting saveData with data size: {}", data.size());
    }
}