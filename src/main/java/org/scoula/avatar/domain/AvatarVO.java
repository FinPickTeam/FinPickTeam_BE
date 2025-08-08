package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class AvatarVO {
    private Long userId;
    private Long levelId;
    private Long topId;
    private Long shoesId;
    private Long accessoryId;
    private Long giftCardId;
}
