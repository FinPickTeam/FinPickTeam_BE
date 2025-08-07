package org.scoula.account.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class AccountListWithTotalDto {
    private BigDecimal accountTotal;
    private List<AccountDto> accounts;
}
