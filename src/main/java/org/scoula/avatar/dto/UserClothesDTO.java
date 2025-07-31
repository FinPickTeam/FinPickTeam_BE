package org.scoula.avatar.dto;

import lombok.Data;

//착장 전체조회 DTO
@Data
public class UserClothesDTO {
    private int itemId;
    private enum type{TOP, SHOES, ACCESSORY, GIFTCARD};
    private int cost;
    private String imageUrl;
    private boolean isWearing;
}
