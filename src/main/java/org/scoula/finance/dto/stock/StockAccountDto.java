package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockAccountDto {
    private Long id;
    String stockAccount;
    String totalAccountReturnRate;
}
