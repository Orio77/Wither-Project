package com.Orio.wither_project.service.data.processing.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.config.OllamaConfig;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.model.OllamaThreeWordsResponseModel;
import com.Orio.wither_project.service.data.processing.IAIQAService;
import com.Orio.wither_project.util.TextUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaQAService implements IAIQAService {

        private final OllamaConfig ollamaConfig;
        private final ObjectMapper objMapper;
        private static final Logger logger = LoggerFactory.getLogger(OllamaQAService.class);

        private static final int DEFAULT_NUM_CTX = 13000;
        private static final float DEFAULT_TEMPERATURE = 0.0f;
        private static final String JSON_FORMAT = "json";
        private static final String SYSTEM_PROMPT = """
                        Act as an analyst who examines the text to identify and extract valuable fragments that fully explain ideas.
                        Ignore any text that doesn't encapsulate the idea fully. Ignore any text that doesn't contain a meaningful idea at all.
                        Avoid technical metadata or irrelevant content such as URLs, image captions, or locations. Only return content that adds value to understanding.
                        Be strict and don't hesitate to respond with "null" if no such fragment is present.
                        Provide a brief analysis of the text in the search for a valuable fragment (3 sentences).
                        If such fragment is found, provide first and last 3 words of the fragment to indicate where it can be found. The words must be exactly quoted from the text.
                        If such fragment found, provide an up-to-5-word question the fragment answers. Make the question related to the idea itself, not the text.
                        """;
        private static final String JSON_FORMAT_PROMPT = """
                        Respond in the following json format: {
                            "analysis": "A brief analysis of the text",
                            "first_three_words": "First three words that indicate where the valuable fragment begins",
                            "last_three_words": "Last three words that indicate where the valuable fragment ends",
                            "question": "up-to-5-word question the fragment answers"
                        }""";

        /**
         * Generates questions for the provided list of data models using Ollama AI.
         * 
         * @param dataList List of DataModel objects to process
         * @throws IllegalArgumentException if dataList is null or empty
         */
        @Override
        public void generateQuestions(List<DataModel> dataList) {
                if (dataList == null || dataList.isEmpty()) {
                        throw new IllegalArgumentException("Data list cannot be null or empty");
                }

                OllamaOptions options = createOptions();
                dataList.parallelStream()
                                .filter(data -> data != null && data.getContent() != null)
                                .forEach(dataPiece -> {
                                        try {
                                                processDataPiece(dataPiece, options);
                                        } catch (Exception e) {
                                                logger.error("Error processing data piece: {}", dataPiece.getId(), e);
                                        }
                                });
        }

        private OllamaOptions createOptions() {
                return new OllamaOptions()
                                .withTemperature(DEFAULT_TEMPERATURE)
                                .withNumCtx(DEFAULT_NUM_CTX);
        }

        private void processDataPiece(DataModel dataPiece, OllamaOptions options) {
                try {
                        ChatRequest request = createChatRequest(dataPiece, options);
                        ChatResponse response = executeRequest(request);
                        processResponse(dataPiece, response);
                } catch (Exception e) {
                        logger.error("Failed to process data piece: {}", dataPiece.getId(), e);
                }
        }

        private ChatRequest createChatRequest(DataModel dataPiece, OllamaOptions options) {
                if (dataPiece == null || dataPiece.getContent() == null) {
                        throw new IllegalArgumentException("DataModel or its content cannot be null");
                }

                return ChatRequest.builder(ollamaConfig.getModel())
                                .withMessages(getQuestionMessages(dataPiece))
                                .withFormat(JSON_FORMAT)
                                .withOptions(options)
                                .build();
        }

        private ChatResponse executeRequest(ChatRequest request) {
                logger.debug("Executing request with model: {}", ollamaConfig.getModel());
                try {
                        return ollamaConfig.getOllamaApi().chat(request);
                } catch (Exception e) {
                        logger.error("Failed to execute chat request", e);
                        throw new RuntimeException("Failed to execute chat request", e);
                }
        }

        private void processResponse(DataModel dataPiece, ChatResponse response) {
                if (response == null || response.message() == null) {
                        logger.warn("Received null response for data piece: {}", dataPiece.getId());
                        return;
                }

                String content = response.message().content();
                logger.debug("Processing response for data piece: {}", dataPiece.getId());

                OllamaThreeWordsResponseModel responseModel = parseJson(content);
                if (responseModel == null) {
                        logger.warn("Failed to parse JSON response for data piece: {}", dataPiece.getId());
                        return;
                }

                updateDataModel(dataPiece, responseModel);
        }

        private void updateDataModel(DataModel dataPiece, OllamaThreeWordsResponseModel responseModel) {
                String first = responseModel.getFirst_three_words();
                String last = responseModel.getLast_three_words();
                String question = responseModel.getQuestion();

                if (first == null || last == null || question == null) {
                        logger.warn("Incomplete response model for data piece: {}", dataPiece.getId());
                        return;
                }

                String fragment = TextUtil.parse(dataPiece.getContent(), first, last);
                if (fragment == null) {
                        logger.warn("Could not extract fragment for data piece: {}", dataPiece.getId());
                        return;
                }

                logger.debug("Generated question: '{}' for fragment: '{}'", question, fragment);
                dataPiece.setQuestion(question);
                dataPiece.setAnswer(fragment);
        }

        private OllamaThreeWordsResponseModel parseJson(String content) {
                try {
                        return objMapper.readValue(content, OllamaThreeWordsResponseModel.class);
                } catch (JsonProcessingException e) {
                        logger.error("Error parsing JSON response: {}", e.getMessage());
                        return null;
                }
        }

        private List<Message> getQuestionMessages(DataModel dataPiece) {
                Message systemBiggerFragment = Message.builder(Role.SYSTEM)
                                .withContent(SYSTEM_PROMPT)
                                .build();

                Message systemThreeWordsFormat = Message.builder(Role.SYSTEM)
                                .withContent(JSON_FORMAT_PROMPT)
                                .build();

                Message contextMessage = Message.builder(Role.USER)
                                .withContent(dataPiece.getContent())
                                .build();

                return List.of(systemBiggerFragment, systemThreeWordsFormat, contextMessage);
        }

        @Override
        public void generateAnswers(List<DataModel> data) {
                // Not implemented
        }
}
