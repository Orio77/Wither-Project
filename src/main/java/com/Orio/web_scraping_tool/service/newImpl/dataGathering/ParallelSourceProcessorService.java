package com.Orio.web_scraping_tool.service.newImpl.dataGathering;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.exception.DataSourceUnavailableException;
import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataGathering.ISourceProcessorService;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;
import com.Orio.web_scraping_tool.util.ThreadUtil;

@Service
@Primary
public class ParallelSourceProcessorService implements ISourceProcessorService {

    private static final Logger logger = getLogger(ParallelSourceProcessorService.class);

    @Override
    public List<DataModel> collectData(List<IDataSource> dataSources, String query) {
        int numThreads = ThreadUtil.getNumAvailableThreads();
        logger.info("Starting data collection with {} threads for query: {}", numThreads, query);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Callable<List<DataModel>>> tasks = new ArrayList<>();

        for (IDataSource dataSource : dataSources) {
            logger.debug("Creating task for data source: {}", dataSource.getClass().getName());
            tasks.add(() -> dataSource.getData(query));
        }

        List<DataModel> results = new ArrayList<>();
        List<DataModel> data = null;
        List<Future<List<DataModel>>> futures = null;

        try {
            futures = executorService.invokeAll(tasks);
            logger.info("Successfully started {} tasks for data collection", tasks.size());
            for (Future<List<DataModel>> future : futures) {
                try {
                    data = future.get();
                    if (data != null && !data.isEmpty()) {
                        results.addAll(data);
                        logger.debug("Successfully retrieved data from a data source: {}", data);
                    } else {
                        logger.warn("Received null or empty data from a data source: {}", data);
                    }
                } catch (InterruptedException e) {
                    logger.error(
                            "Exception while trying to get data from a data source. Exception: {}. Data: {}",
                            e, data);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof DataSourceUnavailableException) {
                        logger.error("Data Source unavailable: {}. Cause: {}", cause.getMessage(), cause);
                    } else {
                        logger.error("Exception while trying to get data from a data source. Exception: {}. Data: {}",
                                e, data);
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.error("Exception while trying to start multithreaded tasks. Exception: {}. futures: {}",
                    e, futures);
        } finally {
            executorService.shutdown();
            logger.info("Executor service shut down");
        }

        logger.info("Data collection completed with {} results", results.size());
        return results;
    }
}