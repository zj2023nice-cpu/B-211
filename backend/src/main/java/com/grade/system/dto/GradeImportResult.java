package com.grade.system.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class GradeImportResult {
    private int total;
    private int successCount;
    private int failCount;
    private List<ImportError> errors = new ArrayList<>();

    @Data
    public static class ImportError {
        private int rowNumber;
        private String studentName;
        private String courseName;
        private String term;
        private String errorMessage;

        public ImportError(int rowNumber, String studentName, String courseName, String term, String errorMessage) {
            this.rowNumber = rowNumber;
            this.studentName = studentName;
            this.courseName = courseName;
            this.term = term;
            this.errorMessage = errorMessage;
        }
    }
}
