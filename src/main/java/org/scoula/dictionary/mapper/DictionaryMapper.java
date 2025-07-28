package org.scoula.dictionary.mapper;

import org.scoula.dictionary.domain.DictionaryVO;

import java.util.List;

public interface DictionaryMapper {
    //단어리스트 조회
    List<DictionaryVO> getList();

    //단어 상세조회
    DictionaryVO getById(int DictionaryId);

    //단어명으로 검색하기
    List<DictionaryVO> getByWord(String word);
}
