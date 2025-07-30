package org.scoula.challenge.dto;

import lombok.Data;
import org.scoula.challenge.enums.ChallengeType;

import java.time.LocalDate;

@Data
public class ChallengeCreateRequestDTO {
    private String title; // 챌린지 제목
    private Long categoryId; // 카테고리 ID
    private String description; // 설명
    private LocalDate startDate; // 시작일
    private LocalDate endDate; // 종료일
    private ChallengeType type; // Enum
    private Integer goalValue; // 목표 금액
    private Boolean usePassword; // 비밀번호 사용 여부
    private Integer password; // 비밀번호 (nullable)
}
