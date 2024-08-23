package com.Orio.web_scraping_tool.service.dataGathering;

import java.util.List;

public interface IFileDownloadService {

    void download(List<String> links, String query);
}
