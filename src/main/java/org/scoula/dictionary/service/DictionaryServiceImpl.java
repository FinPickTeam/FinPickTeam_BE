package org.scoula.dictionary.service;

import lombok.RequiredArgsConstructor;
import org.scoula.dictionary.domain.DictionaryVO;
import org.scoula.dictionary.dto.DictionaryDTO;
import org.scoula.dictionary.mapper.DictionaryMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService{

    final private DictionaryMapper dictionaryMapper;

    //단어세부조회
    @Override
    public DictionaryDTO getDetail(int id){
        DictionaryVO dictionaryVO = dictionaryMapper.getById(id);

        return DictionaryDTO.of(dictionaryVO);
    }

    //단어전체조회
    @Override
    public List<DictionaryDTO> getList(){
        List<DictionaryVO> dictionaryVOList = dictionaryMapper.getList();
        List<DictionaryDTO> dictionaryDTOList = new ArrayList<>();
        for(DictionaryVO dictionaryVO : dictionaryVOList){
            DictionaryDTO dictionaryDTO = DictionaryDTO.of(dictionaryVO);
            dictionaryDTOList.add(dictionaryDTO);
        }
        return dictionaryDTOList;
    }


    //단어검색조회
    @Override
    public List<DictionaryDTO> getBySearch(String word){
        List<DictionaryVO> dictionaryVO = dictionaryMapper.getByWord(word);
        List<DictionaryDTO> dictionaryDTOList = new ArrayList<>();
        for(DictionaryVO dictionaryVO1 : dictionaryVO){
            DictionaryDTO dictionaryDTO = DictionaryDTO.of(dictionaryVO1);
            dictionaryDTOList.add(dictionaryDTO);
        }
        return dictionaryDTOList;
    }
}
