package org.scoula.avatar.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.avatar.domain.AvatarVO;
import org.scoula.avatar.domain.ItemsVO;
import org.scoula.avatar.domain.UserClothesVO;

import java.util.List;

public interface AvatarMapper {

    //아바타 생성
    void insertAvatar(Long userId);

    //아바타 착장 조회(get, 파라미터 : Long userId, 반환값 : AvatarDTO)
    AvatarVO getAvatar(Long userId);

    //item type 판별
    String getItemType(Long itemId);

    //착용 중인 의상 수정
    void updateAvatar(AvatarVO avatarVO);

    //착용 중인 의상을 1개만 지정하여 수정
    void updateAvatarByItemId(@Param("userId")Long userId,@Param("type") String type, @Param("itemId") Long itemId);

    //전체 의상 조회(의상소유여부 포함)
    List<UserClothesVO> getUserClothes(Long userId);

    //의상 세부 조회(의상소유여부 미포함)
    ItemsVO getItem(Long itemId);

    //유저 옷장에 의상삽입
    void insertClothe(@Param("userId") Long userId,@Param("itemId") Long itemId);

    //clothe테이블의 is_wearing 한번에 갱신
    void updateClothe(@Param("userId") Long userId, @Param("isWearing") Boolean isWearing, @Param("itemId") Long[] items);

    //clothe테이블의 is_wearing, 아이템 하나만 갱신
    void updateClotheByItemId(@Param("userId") Long userId, @Param("isWearing") Boolean isWearing, @Param("itemId") Long item);

}
