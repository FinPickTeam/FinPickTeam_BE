package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class AvatarVO {
    private Long id;
    private String avatarImage;
    private Integer topId;
    private Integer shoesId;
    private Integer accessoryId;
    private Integer giftCardId;
}
