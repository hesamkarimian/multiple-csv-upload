package com.platomics.hiring.springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platomics.hiring.springboot.models.ValidationError;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CsvValidationService {

    @Value("classpath:survey.json")
    private Resource surveyJson;

    private JsonNode surveyDefinition;

    // Initialize the survey definition using the JSON file
    @PostConstruct
    public void init() throws IOException {
        var objectMapper = new ObjectMapper();
        surveyDefinition = objectMapper.readTree(surveyJson.getInputStream());
        log.info("Survey definition loaded successfully.");
    }

    public List<ValidationError> validateCsv(InputStream csvInputStream) throws IOException {
        List<ValidationError> errors = new ArrayList<>();

        var csvParser = new CSVParser(
                new InputStreamReader(csvInputStream),
                CSVFormat.DEFAULT.builder().setHeader().build()
        );

        // Validate each row in the CSV
        for (CSVRecord record : csvParser.getRecords()) {
            validateRecord(record, errors);
        }

        return errors;
    }

    private void validateRecord(CSVRecord record, List<ValidationError> errors) {
        JsonNode elements = surveyDefinition.path("pages").get(0).path("elements");
        int addHeaderRow = 1;
        int rowNum = (int) record.getRecordNumber() + addHeaderRow;

        // Validate each field in the survey for the current CSV row
        for (JsonNode element : elements) {
            validateRecordForEachSurveyField(record, rowNum, element, errors);
        }
    }

    private void validateRecordForEachSurveyField(CSVRecord record, int rowNum,
                                                  JsonNode element, List<ValidationError> errors) {
        if (element.get("isRequired").asBoolean() && element.has("visibleIf")) {
            validateVisibleIfElement(record, rowNum, element, errors);
        } else if (element.get("isRequired").asBoolean() && !element.has("visibleIf")) {
            validateNonVisibleIfElement(record, rowNum, element, errors);
        }
    }

    private void validateVisibleIfElement(CSVRecord record, int rowNum,
                                          JsonNode element, List<ValidationError> errors) {
        boolean visibleIfExEvaluatesTrue = evaluateVisibleIf(element, record);
        boolean hasChoices = element.has("choices");
        if (visibleIfExEvaluatesTrue) {
            if (hasChoices) {
                validateFieldWithChoices(record, rowNum, element, errors);
            } else {
                validateFieldWithoutChoices(record, rowNum, element, errors);
            }
        }
    }

    private void validateNonVisibleIfElement(CSVRecord record, int rowNum,
                                             JsonNode element, List<ValidationError> errors) {
        boolean hasChoices = element.has("choices");
        if (hasChoices) {
            validateFieldWithChoices(record, rowNum, element, errors);
        } else {
            validateFieldWithoutChoices(record, rowNum, element, errors);
        }
    }

    private void validateFieldWithChoices(CSVRecord record, int rowNum, JsonNode element, List<ValidationError> errors) {
        var fieldName = element.get("name").asText();
        var choicesNode = element.get("choices");
        var value = record.get(fieldName);

        boolean isValid = false;
        for (JsonNode choice : choicesNode) {
            String choiceValue = choice.has("value") ? choice.get("value").asText() : choice.asText();
            if (value.equals(choiceValue)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            if (value.isEmpty()) {
                errors.add(new ValidationError(record.get(getVisibleIfField(element)) + " missing fields",
                        rowNum, element.get("name").asText()));
            } else {
                errors.add(new ValidationError("Invalid " + fieldName.replace("_", " "),
                        rowNum, fieldName));
            }
        }
    }

    private void validateFieldWithoutChoices(CSVRecord record, int rowNum,
                                             JsonNode element, List<ValidationError> errors) {
        var fieldName = element.get("name").asText();
        var value = record.get(fieldName);

        if (value.isEmpty()) {
            errors.add(new ValidationError("Missing " + fieldName, rowNum, fieldName));
        }
    }

    private boolean evaluateVisibleIf(JsonNode element, CSVRecord record) {
        return Optional.ofNullable(element.get("visibleIf"))
                .map(JsonNode::asText)
                .map(visibleIf -> visibleIf.split("="))
                .map(parts -> {
                    String fieldName = parts[0].trim().replace("{", "").replace("}", "");
                    String expectedValue = parts[1].trim().replace("'", "");
                    return expectedValue.equals(record.get(fieldName));
                })
                .orElse(false);
    }

    private String getVisibleIfField(JsonNode element) {
        return Optional.ofNullable(element.get("visibleIf"))
                .map(JsonNode::asText)
                .map(visibleIf -> visibleIf.split("="))
                .map(parts -> parts[0].trim().replace("{", "").replace("}", ""))
                .orElse("");
    }
}
