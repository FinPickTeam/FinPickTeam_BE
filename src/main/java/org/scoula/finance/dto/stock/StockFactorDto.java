package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockFactorDto {
    private String date;
    private double smb;
    private double hml;
    private double mom;
    private double kospi;
    private double kosdaq;
}
