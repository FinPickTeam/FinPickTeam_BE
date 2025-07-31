package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class UserClothesVO {
    private int itemId;
    private int cost;
    private String type;
    private String imageUrl;
    private boolean isWearing;
}
