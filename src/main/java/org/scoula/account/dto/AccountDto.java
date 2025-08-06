package org.scoula.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.account.domain.Account;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private Long id;
    private String accountNumber;
    private String productName;
    private String accountType;
    private BigDecimal balance;

    public static AccountDto from(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .productName(account.getProductName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .build();
    }
}
