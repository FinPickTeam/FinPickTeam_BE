package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class ItemsVO {
    private Long id;
    private ItemType type;
    private Integer cost;
    private String imageUrl;

    public enum ItemType {
        TOP,
        SHOES,
        ACCESSORY,
        GIFTCARD,
    }
}
