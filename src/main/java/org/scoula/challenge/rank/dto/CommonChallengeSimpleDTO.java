// org/scoula/challenge/rank/dto/CommonChallengeSimpleDTO.java
package org.scoula.challenge.rank.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommonChallengeSimpleDTO {
    private Long id;
    private String title;
    private Long goalValue;         // 스키마 타입에 맞춰 Long/Integer/BigDecimal 조정
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
