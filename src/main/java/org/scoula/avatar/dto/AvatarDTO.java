package org.scoula.avatar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.avatar.domain.AvatarVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvatarDTO {
    private Long id;
    private String avatarImage;
    private Integer topId;
    private Integer shoesId;
    private Integer accessoryId;
    private Integer giftCardId;

    public static AvatarDTO of(AvatarVO vo){
        return AvatarDTO.builder()
                .id(vo.getId())
                .avatarImage(vo.getAvatarImage())
                .topId(vo.getTopId())
                .shoesId(vo.getShoesId())
                .accessoryId(vo.getAccessoryId())
                .giftCardId(vo.getGiftCardId())
                .build();
    }

    public AvatarVO toVO(){
        AvatarVO vo = new AvatarVO();
        vo.setId(this.id);
        vo.setAvatarImage(this.avatarImage);
        vo.setTopId(this.topId);
        vo.setShoesId(this.shoesId);
        vo.setAccessoryId(this.accessoryId);
        vo.setGiftCardId(this.giftCardId);
        return vo;
    }
}
