package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockListDataDto {
    String stockName;
    String stockCode;
    String stockMarketType;
    String stockSummary;
}
