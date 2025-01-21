package com.Orio.wither_project.pdf.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

public class TestUtils {
    public static final String TEST_PDF_PATH = "/test.pdf";

    public static PDDocument loadTestPdf() throws IOException {
        InputStream inputStream = TestUtils.class.getResourceAsStream(TEST_PDF_PATH);
        if (inputStream == null) {
            throw new IOException("Test PDF not found in resources: " + TEST_PDF_PATH);
        }
        return Loader.loadPDF(inputStream.readAllBytes());
    }
}
