package org.scoula.dictionary.service;

import org.scoula.dictionary.dto.DictionaryDTO;

import java.util.List;

public interface DictionaryService {

    public List<DictionaryDTO> getList();
    public DictionaryDTO getDetail(int id);
    public List<DictionaryDTO> getBySearch(String word);
}
