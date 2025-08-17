package org.scoula.card.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.card.domain.Card;
import org.scoula.card.dto.CardDto;

import java.util.List;

@Mapper
public interface CardMapper {
    void insertCard(Card card);
    Card findById(Long cardId);
    void updateIsActive(@Param("id") Long id, @Param("isActive") boolean isActive);
    List<Card> findActiveByUserId(Long userId);
    List<Card> findByIdList(List<Long> ids);
    int countByUser(@Param("userId") Long userId);
}
