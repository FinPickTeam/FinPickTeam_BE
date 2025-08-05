package org.scoula.card.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.card.domain.Card;

import java.util.List;

@Mapper
public interface CardMapper {
    void insertCard(Card card);
    Card findById(Long cardId);
    List<Card> findByUserId(Long userId);
    void updateIsActive(@Param("id") Long id, @Param("isActive") boolean isActive);
}
