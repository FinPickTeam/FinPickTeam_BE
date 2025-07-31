package org.scoula.avatar.service;

import org.scoula.avatar.dto.AvatarDTO;
import org.scoula.avatar.dto.UserClothesDTO;

import java.util.List;

public interface AvatarService{

    AvatarDTO getAvatar(Long userId);

    void updateAvatar(Long userId, int[] items);

    List<UserClothesDTO> getUserClothes(Long userId);

    void insertClothe(Long userId, Long itemId);
}
