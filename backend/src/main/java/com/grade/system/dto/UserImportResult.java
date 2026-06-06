package com.grade.system.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserImportResult {
    private int total;
    private int successCount;
    private int failCount;
    private List<ImportError> errors = new ArrayList<>();

    @Data
    public static class ImportError {
        private int rowNumber;
        private String username;
        private String name;
        private String errorMessage;

        public ImportError(int rowNumber, String username, String name, String errorMessage) {
            this.rowNumber = rowNumber;
            this.username = username;
            this.name = name;
            this.errorMessage = errorMessage;
        }
    }
}
