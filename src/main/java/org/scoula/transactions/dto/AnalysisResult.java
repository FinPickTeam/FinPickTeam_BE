package org.scoula.transactions.dto;

import org.scoula.transactions.domain.analysis.AnalysisCode;

public class AnalysisResult {
    private AnalysisCode code;
    private String message;

    public AnalysisResult(AnalysisCode code, String message) {
        this.code = code;
        this.message = message;
    }
    public AnalysisCode getCode() { return code; }
    public String getMessage() { return message; }
}
