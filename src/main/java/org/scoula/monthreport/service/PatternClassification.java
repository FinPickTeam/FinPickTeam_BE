package org.scoula.monthreport.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.scoula.monthreport.enums.SpendingPatternType;

import java.util.Set;

@Getter
@AllArgsConstructor
public class PatternClassification {
    private final SpendingPatternType overall;     // FRUGAL/STABLE/OVERSPENDER/VOLATILE
    private final Set<SpendingPatternType> patterns; // 행동/카테고리 패턴 집합
}
