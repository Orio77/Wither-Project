// package com.Orio.wither_project.pdf2.controller;

// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// @RestController
// public class PDF3Controller {

// private static final Logger logger =
// LoggerFactory.getLogger(PDF3Controller.class);

// @PutMapping("save/pdf")
// public String save(@RequestBody MultipartFile pdf) {
// logger.info("Received a request to save a PDF file");

// return "PDF saved successfully";
// }

// @PutMapping("save/processed")
// public String saveProcessed(@PathVariable String id, @RequestBody String
// entity) {
// // TODO: process PUT request

// return entity;
// }

// @PutMapping("process")
// public String putMethodName(@PathVariable String id, @RequestBody String
// entity) {
// // TODO: process PUT request

// return entity;
// }
// }
