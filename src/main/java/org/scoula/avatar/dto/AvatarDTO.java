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
    private Long userId;
    private Long avatarImage;
    private Long topId;
    private Long shoesId;
    private Long accessoryId;
    private Long giftCardId;

    public static AvatarDTO of(AvatarVO vo){
        return AvatarDTO.builder()
                .userId(vo.getUserId())
                .avatarImage(vo.getAvatarImage())
                .topId(vo.getTopId())
                .shoesId(vo.getShoesId())
                .accessoryId(vo.getAccessoryId())
                .giftCardId(vo.getGiftCardId())
                .build();
    }

    public AvatarVO toVO(){
        AvatarVO vo = new AvatarVO();
        vo.setUserId(this.userId);
        vo.setAvatarImage(this.avatarImage);
        vo.setTopId(this.topId);
        vo.setShoesId(this.shoesId);
        vo.setAccessoryId(this.accessoryId);
        vo.setGiftCardId(this.giftCardId);
        return vo;
    }
}
