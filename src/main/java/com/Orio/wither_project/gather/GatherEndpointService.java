package com.Orio.wither_project.gather;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.acquisition.aggregation.ISourceAggregationService;
import com.Orio.wither_project.gather.acquisition.source.IDataSource;
import com.Orio.wither_project.gather.processing.qa.IAIQAService;
import com.Orio.wither_project.gather.storage.sql.service.ISQLDataModelService;
import com.Orio.wither_project.gather.storage.vector.service.IVectorStoreService;
import com.Orio.wither_project.model.DataModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GatherEndpointService {
    private final ISourceAggregationService sourceCollectorService;
    private final List<IDataSource> dataSources;
    // private final IAIQAService aiQAService;
    private final ISQLDataModelService sqlService;
    private final IVectorStoreService vectorDbService;

    private static final Logger logger = getLogger(GatherEndpointService.class);

    public List<DataModel> gatherData(String query) {
        logger.info("Gathering data for query: {}", query);
        List<DataModel> data = sourceCollectorService.collectData(dataSources, query);
        // processData(data);
        saveData(data);
        return data;
    }

    // private void processData(List<DataModel> data) {
    // aiQAService.generateQuestions(data);
    // aiQAService.generateAnswers(data);
    // }

    private void saveData(List<DataModel> data) {
        sqlService.save(data);
        vectorDbService.save(data);
    }
}
