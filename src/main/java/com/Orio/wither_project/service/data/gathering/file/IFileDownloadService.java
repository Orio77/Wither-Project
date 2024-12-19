package com.Orio.wither_project.service.data.gathering.file;

import java.util.List;

public interface IFileDownloadService {

    void download(List<String> links, String query);
}
