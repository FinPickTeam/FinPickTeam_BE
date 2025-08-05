package org.scoula.monthreport.service;

import java.util.List;

public interface MonthReportInitService {
    List<String> generateAllMissingReports(Long userId);
}
