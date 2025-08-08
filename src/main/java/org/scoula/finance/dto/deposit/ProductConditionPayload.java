package org.scoula.finance.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProductConditionPayload {
    private Long id;
    private String conditionText;
}