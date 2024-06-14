package com.platomics.hiring.springboot.controller;

import com.platomics.hiring.springboot.models.ValidationError;
import com.platomics.hiring.springboot.service.CsvValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CsvValidateController {
    private final CsvValidationService csvValidationService;

    @PostMapping("/validate-csv")

    public ResponseEntity<?> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        try {
            List<ValidationError> errors = csvValidationService.validateCsv(file.getInputStream());
            if (errors.isEmpty()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process file");
        }
    }
}
