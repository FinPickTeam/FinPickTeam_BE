package org.scoula.avatar.controller;

public class AvatarController {

    //아바타 착장 조회(get, 파라미터 : Long userId, 반환값 : AvatarDTO)

    //아바타 착장 수정(put, 파라미터 : Long userId, int topId, int shoesId, int accessoryId, int giftCardId, 반환값 : 성공여부)
    //각 아이템들마다 해당하는 타입파악(select type from item where id=#{itemId}) 후, 해당하는 컬럼에 각각 반영

    //의상 전체 조회(get, 파라미터 : Long userId, 반환값 : List<ItemDTO>)

    //구매처리(post, 파라미터 : Long userId, int itemId, 반환값 : 성공여부)
    //아이템 cost 검색하여 유저의 보유재화와 비교, 구매가능한지 판단 -> 가능할 경우 cost만큼 보유재화 차감하고 clothes 테이블에 해당 아이템 삽입


}
