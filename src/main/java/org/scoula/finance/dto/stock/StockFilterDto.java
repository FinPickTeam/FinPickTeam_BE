package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockFilterDto {
    private String marketType;
    private String sortByStockName;
    private String sortByStockPrice;
}
