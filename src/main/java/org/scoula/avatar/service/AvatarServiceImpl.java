package org.scoula.avatar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.avatar.domain.AvatarVO;
import org.scoula.avatar.domain.UserClothesVO;
import org.scoula.avatar.dto.AvatarDTO;
import org.scoula.avatar.dto.UserClothesDTO;
import org.scoula.avatar.exception.InsufficientCoinException;
import org.scoula.avatar.mapper.AvatarMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class AvatarServiceImpl implements AvatarService {

    final private AvatarMapper mapper;

    //아바타 조회
    @Override
    public AvatarDTO getAvatar(Long userId) {
        AvatarVO avatarVO = mapper.getAvatar(userId);
        return AvatarDTO.of(avatarVO);
    }

    //아바타 수정
    @Override
    public void updateAvatar(Long userId, int[] items) {
        Map<String,Integer> itemsByType=new HashMap<>();

        //아이템들을 타입별로 분류
        for (int item : items) {
            String itemType = mapper.getItemType(item);
            itemsByType.put(itemType, item);
        }

        //각 타입에 해당하는 아이템코드들을 avatarVO에 삽입
        AvatarVO avatarVO = new AvatarVO();
        avatarVO.setId(userId);
        avatarVO.setTopId(itemsByType.get("TOP"));
        avatarVO.setShoesId(itemsByType.get("SHOES"));
        avatarVO.setAccessoryId(itemsByType.get("ACCESSORY"));
        avatarVO.setGiftCardId(itemsByType.get("GIFTCARD"));

        mapper.updateAvatar(avatarVO);
    }

    //유저 전체 의상 조회
    @Override
    public List<UserClothesDTO> getUserClothes(Long userId) {
        List<UserClothesVO> VOs=mapper.getUserClothes();
        List<UserClothesDTO> userClothesDTOS=new ArrayList<>();

        for (UserClothesVO vo : VOs){
            userClothesDTOS.add(UserClothesDTO.of(vo));
        }

        return userClothesDTOS;
    }

    //의상 구매 시 삽입
    @Override
    @Transactional
    public void insertClothe(Long userId, Long itemId) {
        //유저 재화와 아이템 가격 조회
        int curAmount=mapper.getUserCoin(userId);
        int itemCost=mapper.getItemCost(itemId);

        //재화가 아이템 가격보다 적으면 예외
        if(itemCost>curAmount) {
            throw new InsufficientCoinException();
        }

        //예외 통과하면 유저재화를 갱신하고 옷장에 의상 삽입
        curAmount-=itemCost;
        mapper.updateMoney(curAmount);
        mapper.insertClothe(userId, itemId);
    }
}

