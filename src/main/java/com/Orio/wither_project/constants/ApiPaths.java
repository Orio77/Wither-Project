package com.Orio.wither_project.constants;

public final class ApiPaths {
    public static final String BASE = "/api";
    public static final String GATHER = "/gather";
    public static final String QUERY = "/query";

    public static final String PDF = "/pdf";
    public static final String PDF_UPLOAD = PDF + "/upload";
    public static final String PDF_PROCESS = PDF + "/process";
    public static final String PDF_GET = PDF + "/get";
    public static final String PDF_DELETE = PDF + "/delete";
    public static final String PDF_GET_FILE = PDF_GET + "/file";
    public static final String PDF_GET_FILE_ALL = PDF_GET_FILE + "/all";
    public static final String PDF_GET_DOC = PDF_GET + "/doc";
    public static final String PDF_GET_DOC_ALL = PDF_GET_DOC + "/all";
    public static final String PDF_DELETE_DOC = PDF_DELETE + "/doc";

    private ApiPaths() {
        // Prevent instantiation
    }
}
