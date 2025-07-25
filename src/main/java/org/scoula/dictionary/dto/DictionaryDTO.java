package org.scoula.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.dictionary.domain.DictionaryVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DictionaryDTO {
    private int id;
    private String word;
    private String definition;

    public static DictionaryDTO of(DictionaryVO dictionaryVO) {
        return DictionaryDTO.builder()
                .id(dictionaryVO.getId())
                .word(dictionaryVO.getTerm())
                .definition(dictionaryVO.getDefinition())
                .build();
    }

    public DictionaryVO toVO() {
        DictionaryVO dictionaryVO = new DictionaryVO();
        dictionaryVO.setId(id);
        dictionaryVO.setTerm(word);
        dictionaryVO.setDefinition(definition);
        return dictionaryVO;
    }
}
