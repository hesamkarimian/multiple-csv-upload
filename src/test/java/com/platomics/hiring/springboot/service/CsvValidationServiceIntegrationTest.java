package com.platomics.hiring.springboot.service;

import com.platomics.hiring.springboot.IntegrationTestBase;
import com.platomics.hiring.springboot.models.ValidationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.platomics.hiring.springboot.util.TestUtil.getCsvMultipartFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class CsvValidationServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CsvValidationService csvValidationService;

    @Test
    void validCsvReturnsNoErrors() throws IOException {
        // Load valid CSV file
        var validFile = getCsvMultipartFile("src/test/resources/valid/valid.csv");

        // Validate CSV
        List<ValidationError> errors = new ArrayList<>(csvValidationService.validateCsv(validFile.getInputStream()));

        // Assert no errors
        assertEquals(0, errors.size());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTestParams")
    void invalidCsvFilesReturnErrors(TestParams testParams) throws IOException {

        // Load invalid CSV file
        var inValidFile = getCsvMultipartFile("src/test/resources/invalid/" + testParams.getInvalidCsvFileName());

        // Validate CSV
        List<ValidationError> errors = new ArrayList<>(csvValidationService.validateCsv(inValidFile.getInputStream()));

        // Assert errors
        assertFalse(errors.isEmpty(), "Expected at least one error");

        // Assert each error corresponds with the expected error type, row number and column name
        for (ValidationError error : errors) {
            assertEquals(testParams.getErrorType(), error.getErrorType(), "Error type mismatch");
            assertEquals(testParams.getRowNumber(), error.getRowNumber(), "Row number mismatch");
            assertEquals(testParams.getColumnName(), error.getColumnName(), "Column name mismatch");
        }
    }
}

