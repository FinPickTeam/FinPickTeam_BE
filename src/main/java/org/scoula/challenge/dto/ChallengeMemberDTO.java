package org.scoula.challenge.dto;

import lombok.Builder;
import lombok.Data;

@Data // 기본 생성자 + setter 제공
public class ChallengeMemberDTO {
    private Long userId;
    private String nickname;
    private Double progress;
    private String avatarImage;
}