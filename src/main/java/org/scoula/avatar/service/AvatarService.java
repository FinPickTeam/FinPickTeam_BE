package org.scoula.avatar.service;

import org.scoula.avatar.dto.AvatarDTO;
import org.scoula.avatar.dto.UserClothesDTO;

import java.util.List;

public interface AvatarService{

    void insertAvatar(Long userId);

    AvatarDTO getAvatar(Long userId);

    void updateAvatar(Long userId, Long[] items);

    List<UserClothesDTO> getUserClothes(Long userId);

    void insertClothe(Long userId, Long itemId);

    int getCoin(Long userId);

    void updateAvatarByItemId(Long userId, Long itemId);
}
