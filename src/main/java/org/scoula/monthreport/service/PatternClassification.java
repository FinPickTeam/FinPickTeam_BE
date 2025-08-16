package org.scoula.monthreport.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.scoula.monthreport.enums.SpendingPatternType;

import java.util.Set;

@Getter
@AllArgsConstructor
public class PatternClassification {
    private final SpendingPatternType overall;          // FRUGAL/STABLE/OVERSPENDER/VOLATILE
    private final Set<SpendingPatternType> patterns;    // IMPULSE / *_OVER 등 서브 패턴
}
