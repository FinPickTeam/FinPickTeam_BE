package org.scoula.finance.dto.installment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class InstallmentRequestPayload {
    private int period;
    private InstallmentUserConditionDto userCondition;
    private List<InstallmentConditionPayload> products;
}
