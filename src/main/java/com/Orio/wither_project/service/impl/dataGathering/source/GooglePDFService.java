package com.Orio.wither_project.service.impl.dataGathering.source;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.dataGathering.IFileDownloadService;
import com.Orio.wither_project.service.dataGathering.IPDFProcessorService;
import com.Orio.wither_project.service.dataGathering.source.IDataSource;
import com.Orio.wither_project.service.dataGathering.webSearch.IWebSearchService;

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
