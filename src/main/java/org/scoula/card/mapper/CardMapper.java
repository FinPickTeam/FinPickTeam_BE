package org.scoula.card.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.card.domain.Card;

import java.util.List;

@Mapper
public interface CardMapper {
    void insertCard(Card card);
    Card findById(Long cardId);
    List<Card> findByUserId(Long userId);
}
