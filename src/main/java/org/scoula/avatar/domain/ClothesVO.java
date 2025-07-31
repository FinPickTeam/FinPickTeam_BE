package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class ClothesVO {
    private Long id;
    private Long userId;
    private Long itemId;
    private Boolean isWearing;
}
