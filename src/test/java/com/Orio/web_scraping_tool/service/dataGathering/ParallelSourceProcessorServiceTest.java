package com.Orio.web_scraping_tool.service.dataGathering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;
import com.Orio.web_scraping_tool.service.impl.dataGathering.ParallelSourceProcessorService;
import com.Orio.web_scraping_tool.util.ThreadUtil;

public class ParallelSourceProcessorServiceTest {

    @Mock
    private IDataSource dataSource1;

    @Mock
    private IDataSource dataSource2;

    @InjectMocks
    private ParallelSourceProcessorService parallelSourceProcessorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCollectData_success() throws Exception {
        try (MockedStatic<ThreadUtil> threadUtilMock = mockStatic(ThreadUtil.class)) {
            threadUtilMock.when(ThreadUtil::getNumAvailableThreads).thenReturn(2);

            DataModel dataModel1 = mock(DataModel.class);
            DataModel dataModel2 = mock(DataModel.class);

            when(dataSource1.getData("query")).thenReturn(Arrays.asList(dataModel1));
            when(dataSource2.getData("query")).thenReturn(Arrays.asList(dataModel2));

            List<DataModel> results = parallelSourceProcessorService.collectData(
                    Arrays.asList(dataSource1, dataSource2),
                    "query");

            assertEquals(2, results.size());
            verify(dataSource1, times(1)).getData("query");
            verify(dataSource2, times(1)).getData("query");
        }
    }

    @Test
    public void testCollectData_withNullData() throws Exception {
        try (MockedStatic<ThreadUtil> threadUtilMock = mockStatic(ThreadUtil.class)) {
            threadUtilMock.when(ThreadUtil::getNumAvailableThreads).thenReturn(2);

            when(dataSource1.getData("query")).thenReturn(null);
            when(dataSource2.getData("query")).thenReturn(Collections.emptyList());

            List<DataModel> results = parallelSourceProcessorService.collectData(
                    Arrays.asList(dataSource1, dataSource2),
                    "query");

            assertEquals(0, results.size());
            verify(dataSource1, times(1)).getData("query");
            verify(dataSource2, times(1)).getData("query");
        }
    }

    @Test
    public void testCollectData_withException() throws Exception {
        try (MockedStatic<ThreadUtil> threadUtilMock = mockStatic(ThreadUtil.class)) {
            threadUtilMock.when(ThreadUtil::getNumAvailableThreads).thenReturn(2);

            when(dataSource1.getData("query")).thenThrow(new RuntimeException("Test Exception"));
            when(dataSource2.getData("query")).thenReturn(Collections.emptyList());

            List<DataModel> results = parallelSourceProcessorService.collectData(
                    Arrays.asList(dataSource1, dataSource2),
                    "query");

            assertEquals(0, results.size());
            verify(dataSource1, times(1)).getData("query");
            verify(dataSource2, times(1)).getData("query");
        }
    }
}