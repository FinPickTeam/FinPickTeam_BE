package org.scoula.avatar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.avatar.domain.UserClothesVO;

//착장 전체조회 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserClothesDTO {
    private int itemId;
    private int cost;
    private String type;
    private String imageUrl;
    private boolean isWearing;


    public static UserClothesDTO of(UserClothesVO vo){
        return UserClothesDTO.builder()
                .itemId(vo.getItemId())
                .cost(vo.getCost())
                .type(vo.getType())
                .imageUrl(vo.getImageUrl())
                .isWearing(vo.isWearing())
                .build();
    }

    public UserClothesVO toVO(){
        UserClothesVO vo=new UserClothesVO();
        vo.setItemId(this.itemId);
        vo.setWearing(this.isWearing);
        vo.setType(this.type);
        vo.setCost(this.cost);
        vo.setImageUrl(this.imageUrl);
        return vo;
    }
}
