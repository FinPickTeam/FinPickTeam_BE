package org.scoula.monthreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingPatternDto {
    private String label; // ex) "감정적 소비형"
    private String desc;  // ex) "카페/간식, 외식 등 즉흥 소비가 많아요"
}

