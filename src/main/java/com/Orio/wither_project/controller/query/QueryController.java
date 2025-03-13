// package com.Orio.wither_project.controller.query;

// import static org.slf4j.LoggerFactory.getLogger;

// import java.util.List;

// import org.slf4j.Logger;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.Orio.wither_project.constants.ApiPaths;
// import com.Orio.wither_project.model.DataModel;
// import com.Orio.wither_project.query.IQueryService;

// import lombok.RequiredArgsConstructor;

// /**
// * REST controller handling query endpoints.
// */
// @RestController
// @RequestMapping(ApiPaths.BASE + ApiPaths.QUERY)
// @RequiredArgsConstructor
// public class QueryController {
// private static final Logger logger = getLogger(QueryController.class);
// private static final int DEFAULT_RESULTS = 5;

// private final IQueryService queryService;

// @GetMapping
// public ResponseEntity<List<DataModel>> query(
// @RequestParam(required = true) String question,
// @RequestParam(defaultValue = "" + DEFAULT_RESULTS) int numResult) {

// logger.info("Received query request - question: '{}', numResults: {}",
// question, numResult);

// List<DataModel> response = queryService.handle(question, numResult);

// if (response.isEmpty()) {
// logger.debug("No results found for query");
// return ResponseEntity.noContent().build();
// }

// logger.debug("Returning {} results", response.size());
// return ResponseEntity.ok(response);
// }
// }
