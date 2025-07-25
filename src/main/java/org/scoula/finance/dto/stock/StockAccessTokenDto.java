package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockAccessTokenDto {
    private Long id;
    private String stockAccount;
    private String stockAccessToken;
    private String stockTokenExpiresDt;
}
