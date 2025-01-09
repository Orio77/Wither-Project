package com.Orio.wither_project.pdf.summary.model;

import java.util.List;

public interface ProgressivelySummarizable<T> {

    public String getText(List<T> pageSummaries);

    public List<T> split();
}
