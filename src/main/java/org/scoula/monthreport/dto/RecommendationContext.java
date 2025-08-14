package org.scoula.monthreport.dto;

import org.scoula.monthreport.enums.SpendingPatternType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class RecommendationContext {
    public SpendingPatternType overall;            // FRUGAL/STABLE/OVERSPENDER/VOLATILE
    public Set<SpendingPatternType> patterns;      // 행동/카테고리 패턴 집합
    public Map<String, BigDecimal> categoryRatios; // "cafe" -> 22.5
    public Map<String, Integer> averageDiffByCat;  // "cafe" -> +18 (또래 대비)
}
