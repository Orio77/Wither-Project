package com.Orio.wither_project.service.impl.dataGathering;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.exception.DataSourceUnavailableException;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.dataGathering.ISourceProcessorService;
import com.Orio.wither_project.service.dataGathering.source.IDataSource;
import com.Orio.wither_project.util.ThreadUtil;

@Service
@Primary
public class ParallelSourceProcessorService implements ISourceProcessorService {

    private static final Logger logger = getLogger(ParallelSourceProcessorService.class);

    @Override
    public List<DataModel> collectData(List<IDataSource> dataSources, String query) {
        if (dataSources == null) {
            logger.warn("Null dataSources provided");
            return new ArrayList<>();
        }

        final int numThreads = ThreadUtil.getNumAvailableThreads();
        logger.info("Starting data collection with {} threads for query: {}", numThreads, query);

        final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Create tasks using streams
        final List<Callable<List<DataModel>>> tasks = dataSources.stream()
                .filter(Objects::nonNull)
                .map(dataSource -> {
                    logger.debug("Creating task for data source: {}", dataSource.getClass().getName());
                    return (Callable<List<DataModel>>) () -> dataSource.getData(query);
                })
                .toList();

        try {
            // Execute tasks and collect results
            return executorService.invokeAll(tasks).stream()
                    .map(future -> getFutureResult(future))
                    .flatMap(Optional::stream)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (InterruptedException e) {
            logger.error("Exception while trying to start multithreaded tasks. Exception: {}", e);
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } finally {
            shutdownExecutor(executorService);
        }
    }

    private Optional<List<DataModel>> getFutureResult(Future<List<DataModel>> future) {
        try {
            List<DataModel> data = future.get();
            if (data != null && !data.isEmpty()) {
                logger.debug("Successfully retrieved data from a data source: {}", data);
                return Optional.of(data);
            } else {
                logger.warn("Received null or empty data from a data source");
                return Optional.empty();
            }
        } catch (InterruptedException e) {
            logger.error("Exception while trying to get data from a data source: {}", e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException e) {
            logExecutionException(e);
            return Optional.empty();
        }
    }

    private void logExecutionException(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof DataSourceUnavailableException) {
            logger.error("Data Source unavailable: {}. Cause: {}", cause.getMessage(), cause);
        } else {
            logger.error("Exception while trying to get data from a data source: {}", e);
        }
    }

    private void shutdownExecutor(ExecutorService executorService) {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            logger.info("Executor service shut down");
        } catch (InterruptedException e) {
            logger.warn("Executor service shutdown interrupted", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}