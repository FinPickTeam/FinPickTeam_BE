package org.scoula.finance.dto.fund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FundRequestDto {
    private String fundProductName;
    private String fundRiskAversion;
    private String fundReturnsData;
}
