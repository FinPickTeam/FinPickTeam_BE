package org.scoula.avatar.mapper;

import org.scoula.avatar.domain.AvatarVO;
import org.scoula.avatar.domain.UserClothesVO;

import java.util.List;

public interface AvatarMapper {
    //아바타 착장 조회(get, 파라미터 : Long userId, 반환값 : AvatarDTO)
    AvatarVO getAvatar(Long userId);

    //item type판별
    String getItemType(int itemId);

    //착용 중인 의상 수정
    void updateAvatar(AvatarVO avatarVO);

    //전체 의상 조회
    List<UserClothesVO> getUserClothes();

    //의상 구매
    void insertClothe(Long userId, Long itemId);

    //재화차감
    void updateMoney(int curAmount);

    //유저 코인 조회
    int getUserCoin(Long userId);

    //아이템 가격 조회
    int getItemCost(Long itemId);
}
