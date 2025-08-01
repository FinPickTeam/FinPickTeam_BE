package org.scoula.challenge.domain;

import lombok.Data;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;

import java.time.LocalDate;

@Data
public class Challenge {
    private Long id;
    private String title;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private ChallengeType type; // enum: PERSONAL, GROUP, (유저에게는 보여지지 않지만 common 타입도 존재)
    private Integer maxParticipants; // 6 고정
    private Integer password; //  숫자 4자리
    private Boolean usePassword; // 비밀번호 사용 여부
    private Long writerId;
    private ChallengeStatus status; // ENUM('RECRUITING', 'IN_PROGRESS', 'COMPLETED')
    private String goalType; // enum: 소비, 횟수 등
    private Integer goalValue;
    private Integer participantCount;

}
