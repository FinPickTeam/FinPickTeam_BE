package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockCompareDataDto {
    private String stockCode;
    private String stockDividendYield;
    private String stockRoe;
    private String stockRoa;
    private String stockDebtRatio;
    private String stockCurrentRatio;
}
