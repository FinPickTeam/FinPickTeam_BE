package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockCompareDataDto {
    private String stockCode;
    private String stockBps;
    private String stockEps;
    private String stockDiv;
    private String stockRoe;
    private String stockPbr;
}
