package org.scoula.summary.service;

import org.scoula.summary.dto.MonthlySnapshotDto;

import java.time.YearMonth;

public interface MonthlySnapshotService {
    MonthlySnapshotDto getOrCompute(Long userId, YearMonth month);
    MonthlySnapshotDto recomputeAndUpsert(Long userId, YearMonth month);
}
