package com.platomics.hiring.springboot;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.stream.Stream;

public abstract class IntegrationTestBase {

    public static Stream<TestParams> provideInvalidTestParams() {
        return Stream.of(
                new TestParams("ce-ivdd-missing-fields.csv", "CE IVDD missing fields",
                        23, "component_list_IVDD"),
                new TestParams("ce-ivdr-missing-fields.csv", "CE IVDR missing fields",
                        17, "component_risk_class_IVDR"),
                new TestParams("ce-mdd-missing-fields.csv", "CE MDD missing fields",
                        28, "component_risk_class_MDR_MDD"),
                new TestParams("ce-mdr-missing-fields.csv", "CE MDR missing fields",
                        25, "component_risk_class_MDR"),
                new TestParams("invalid-component-list-IVDD.csv", "Invalid component list IVDD",
                        22, "component_list_IVDD"),
                new TestParams("missing-name.csv", "Missing component_name",
                        19, "component_name")
        );
    }

    @Data
    @AllArgsConstructor
    public static class TestParams {
        private String invalidCsvFileName;
        private String errorType;
        private int rowNumber;
        private String columnName;
    }
}
