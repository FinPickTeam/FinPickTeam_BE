package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class UserClothesVO {
    private Long itemId;
    private String name;
    private String type;
    private int cost;
    private String imageUrl;
    private boolean isOwned;
    private boolean isWearing;
}
