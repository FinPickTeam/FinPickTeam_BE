package org.scoula.dictionary.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryVO {
    private int id;
    private String term;
    private String definition;
}
