package com.platomics.hiring.springboot.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationError {
    private String errorType;
    private int rowNumber;
    private String columnName;
}
