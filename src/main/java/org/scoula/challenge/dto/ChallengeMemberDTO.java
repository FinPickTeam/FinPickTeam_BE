package org.scoula.challenge.dto;

import lombok.Data;

@Data
public class ChallengeMemberDTO {
    private Long userId;
    private String nickname;
    private Double progress;

    // 레이어드 아바타용 파츠 ID들
    private Long levelId;
    private Long topId;
    private Long shoesId;
    private Long accessoryId;
    private Long giftCardId;
}
