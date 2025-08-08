package org.scoula.finance.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class PythonRequestPayload {
    private DepositUserConditionDto userCondition;
    private List<ProductConditionPayload> products;
}
