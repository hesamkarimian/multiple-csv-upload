package com.platomics.hiring.springboot.util;

import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@UtilityClass
public class TestUtil {
    public static MockMultipartFile getCsvMultipartFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file", file.getName(), "text/csv", input);
    }
}
