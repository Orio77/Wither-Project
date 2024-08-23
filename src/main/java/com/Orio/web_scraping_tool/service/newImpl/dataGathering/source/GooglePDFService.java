package com.Orio.web_scraping_tool.service.newImpl.dataGathering.source;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataGathering.IFileDownloadService;
import com.Orio.web_scraping_tool.service.dataGathering.IPDFProcessorService;
import com.Orio.web_scraping_tool.service.dataGathering.search.IWebSearchService;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GooglePDFService implements IDataSource {

    private final IWebSearchService webSearchService;
    private final IFileDownloadService fileDownloadService;
    private final IPDFProcessorService pdfProcessorService;

    @Override
    public List<DataModel> getData(String query) { // TODO handle nulls and empties

        String pdfQuery = query + " pdf";

        List<String> links = new ArrayList<>();
        try {
            links = webSearchService.getLinks(pdfQuery);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // TODO handle nulls and empties

        fileDownloadService.download(links, query); // TODO Use FileUtil.savePDFs() inside

        return pdfProcessorService.getData(query); // TODO receives the folder name
    }

}
