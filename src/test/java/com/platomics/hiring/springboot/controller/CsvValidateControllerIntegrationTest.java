package com.platomics.hiring.springboot.controller;

import com.platomics.hiring.springboot.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.platomics.hiring.springboot.util.TestUtil.getCsvMultipartFile;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CsvValidateControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validCsvReturns200() throws Exception {
        // Load valid CSV file
        var validFile = getCsvMultipartFile("src/test/resources/valid/valid.csv");

        // Assert API returns 200
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/validate-csv")
                        .file(validFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTestParams")
    void invalidCsvFilesReturn400WithErrors(TestParams testParams) throws Exception {
        // Load invalid CSV file
        var inValidFile = getCsvMultipartFile("src/test/resources/invalid/" + testParams.getInvalidCsvFileName());

        // Assert API returns 400
        // Assert error object corresponds with the expected error type, row number and column name
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/validate-csv")
                        .file(inValidFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].errorType").value(testParams.getErrorType()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].rowNumber").value(testParams.getRowNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].columnName").value(testParams.getColumnName()));
    }
}