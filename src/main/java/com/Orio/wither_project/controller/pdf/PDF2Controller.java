// package com.Orio.wither_project.controller.pdf;

// import java.util.List;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;

// import com.Orio.wither_project.constants.ApiPaths;
// import com.Orio.wither_project.model.BookModel;
// import com.Orio.wither_project.service.impl.PDFEndpointService;

// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping(ApiPaths.BASE + ApiPaths.PDF)
// @RequiredArgsConstructor
// public class PDF2Controller {
// private static final Logger logger =
// LoggerFactory.getLogger(PDF2Controller.class);
// private final PDFEndpointService pdfEndpointService;

// @PostMapping("/upload")
// public ResponseEntity<Boolean> upload(@RequestParam(required = true)
// MultipartFile file,
// @RequestParam String name, @RequestParam List<Integer> startChapterPages) {
// logger.info("Received request to upload PDF file: {}",
// file.getOriginalFilename());
// boolean result = pdfEndpointService.uploadPDF(file, name, startChapterPages);
// return ResponseEntity.ok(result);
// }

// @GetMapping("/get/all")
// public ResponseEntity<List<BookModel>> getAllPdfDocuments() {
// logger.info("Received request to get all PDF documents");
// return ResponseEntity.ok(pdfEndpointService.getAllDocuments());
// }

// @GetMapping("/get")
// public ResponseEntity<BookModel> getPdfDocument(@RequestParam String
// fileName) {
// logger.info("Received request to get PDF document with fileName: {}",
// fileName);
// return pdfEndpointService.getDocumentByName(fileName)
// .map(ResponseEntity::ok)
// .orElseGet(() -> ResponseEntity.notFound().build());
// }

// }