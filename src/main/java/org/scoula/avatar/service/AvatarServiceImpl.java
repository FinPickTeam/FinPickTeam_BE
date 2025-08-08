package org.scoula.avatar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.avatar.domain.AvatarVO;
import org.scoula.avatar.domain.ItemsVO;
import org.scoula.avatar.domain.UserClothesVO;
import org.scoula.avatar.dto.AvatarDTO;
import org.scoula.avatar.dto.UserClothesDTO;
import org.scoula.avatar.exception.InsufficientCoinException;
import org.scoula.avatar.mapper.AvatarMapper;
import org.scoula.coin.mapper.CoinMapper;
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
    final private CoinMapper coinMapper;

    @Override
    public void insertAvatar(Long userId) {
        mapper.insertAvatar(userId);
    }

    //아바타 조회
    @Override
    public AvatarDTO getAvatar(Long userId) {
        AvatarVO avatarVO = mapper.getAvatar(userId);
        return AvatarDTO.of(avatarVO);
    }

    //특정 아이템으로만 아바타 수정
    public void updateAvatarByItemId(Long userId, Long itemId) {
        AvatarVO curAvatarVO = mapper.getAvatar(userId);
        Long[] curItems = {curAvatarVO.getLevelId(),
                curAvatarVO.getTopId(),
                curAvatarVO.getShoesId(),
                curAvatarVO.getAccessoryId(),
                curAvatarVO.getGiftCardId()
        };

        ItemsVO vo=mapper.getItem(itemId);

        mapper.updateClotheByItemId(userId, false, curItems[0]); //기존 아이템 is_wearing=false처리
        mapper.updateClotheByItemId(userId, true, itemId); //기존 아이템 is_wearing=true처리
        mapper.updateAvatarByItemId(userId, vo.getType(), itemId); //새로운 아이템 업데이트

    }

    //아바타 한번에 수정
    @Override
    public void updateAvatar(Long userId, Long[] items) {

        // 현재 아바타 착장 저장
        AvatarVO curAvatarVO = mapper.getAvatar(userId);
        Long[] curItems = {curAvatarVO.getLevelId(),
                curAvatarVO.getTopId(),
                curAvatarVO.getShoesId(),
                curAvatarVO.getAccessoryId(),
                curAvatarVO.getGiftCardId()
        };

        // 현재 아바타 착장들을 옷장에서 '착용하지 않음(is_wearing=false)'으로 변경
        mapper.updateClothe(userId, false, curItems);

        //착용하려는 아이템들을 타입별로 분류
        Map<String,Long> itemsByType=new HashMap<>();
        for (Long item : items) {
            String itemType = mapper.getItemType(item);
            itemsByType.put(itemType, item);
        }

        //각 타입에 해당하는 아이템코드들을 avatarVO에 삽입
        AvatarVO avatarVO = new AvatarVO();
        avatarVO.setUserId(userId);
        avatarVO.setLevelId(itemsByType.get("level"));
        avatarVO.setTopId(itemsByType.get("top"));
        avatarVO.setShoesId(itemsByType.get("shoes"));
        avatarVO.setAccessoryId(itemsByType.get("accessory"));
        avatarVO.setGiftCardId(itemsByType.get("giftCard"));
        log.info(avatarVO);

        //아바타 착장 및 옷장 착용여부 수정
        mapper.updateAvatar(avatarVO);
        mapper.updateClothe(userId, true, items);
    }

    //유저 전체 의상 조회
    @Override
    public List<UserClothesDTO> getUserClothes(Long userId) {
        List<UserClothesVO> VOs=mapper.getUserClothes(userId);
        List<UserClothesDTO> userClothesDTO=new ArrayList<>();

        for (UserClothesVO vo : VOs){
            userClothesDTO.add(UserClothesDTO.of(vo));
        }

        return userClothesDTO;
    }

    //의상 구매 시 옷장삽입
    @Override
    @Transactional
    public void insertClothe(Long userId, Long itemId) {
        //유저 재화와 아이템 가격 조회
        ItemsVO itemsVO=mapper.getItem(itemId);
//        Long curAmount=mapper.getUserCoin(userId);
        int curAmount=coinMapper.getUserCoin(userId);
        int itemCost=itemsVO.getCost();

        //재화가 아이템 가격보다 적으면 예외
        if(itemCost>curAmount) {
            throw new InsufficientCoinException();
        }

        //예외 통과하면 유저재화를 갱신하고 옷장에 의상 삽입
        mapper.insertClothe(userId, itemId); //의상소유내역 삽입
        coinMapper.subtractCoin(userId, itemCost);
        coinMapper.insertCoinHistory(userId,itemCost,"minus","AVATAR");

    }

    @Override
    public int getCoin(Long userId) {
        return coinMapper.getUserCoin(userId);
    }
}

