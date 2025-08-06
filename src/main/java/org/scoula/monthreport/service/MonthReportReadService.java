package org.scoula.monthreport.service;

import org.scoula.monthreport.dto.MonthReportDetailDto;

public interface MonthReportReadService {
    MonthReportDetailDto getReport(Long userId, String month);
}
