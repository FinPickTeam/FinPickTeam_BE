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
    private Long itemId;
    private String name;
    private String type;
    private int cost;
    private String imageUrl;
    private boolean isOwned;
    private boolean isWearing;


    public static UserClothesDTO of(UserClothesVO vo){
        return UserClothesDTO.builder()
                .itemId(vo.getItemId())
                .name(vo.getName())
                .cost(vo.getCost())
                .type(vo.getType())
                .imageUrl(vo.getImageUrl())
                .isOwned(vo.isOwned())
                .isWearing(vo.isWearing())
                .build();
    }

    public UserClothesVO toVO(){
        UserClothesVO vo=new UserClothesVO();
        vo.setItemId(this.itemId);
        vo.setName(this.name);
        vo.setWearing(this.isWearing);
        vo.setType(this.type);
        vo.setCost(this.cost);
        vo.setImageUrl(this.imageUrl);
        vo.setOwned(this.isOwned);
        return vo;
    }
}
