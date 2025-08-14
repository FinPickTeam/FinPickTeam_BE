package org.scoula.monthreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.monthreport.enums.SpendingPatternType;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingPatternDto {
    // FRUGAL/STABLE/OVERSPENDER/VOLATILE
    private SpendingPatternType overall;
    // 행동/카테고리 서브 패턴 모음 (IMPULSE, CAFE_OVER 등)
    private Set<SpendingPatternType> patterns;

    /** PDF/뷰에서 쓰기 쉬운 표시용 라벨(거시 라벨 한글) */
    public String getLabel() {
        return overall != null ? overall.getLabel() : "없음";
    }

    /** PDF/뷰에서 쓰는 상세 설명(세부 패턴 라벨 목록) */
    public String getDesc() {
        if (patterns == null || patterns.isEmpty()) return "특징적 세부 패턴 없음";
        return patterns.stream()
                .map(SpendingPatternType::getLabel)
                .collect(Collectors.joining(", "));
    }
}
