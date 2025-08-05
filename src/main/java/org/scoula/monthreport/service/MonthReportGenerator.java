package org.scoula.monthreport.service;

public interface MonthReportGenerator {
    void generate(Long userId, String monthStr);
}
