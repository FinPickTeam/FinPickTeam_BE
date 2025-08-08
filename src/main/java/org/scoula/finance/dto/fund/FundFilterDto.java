package org.scoula.finance.dto.fund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FundFilterDto {
    private String fundType;
    private String fundRiskLevel;
}
