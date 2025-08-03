package org.scoula.nhapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinAccountRequestDto {
    private String accountNumber; // 사용자 입력
    private String birthday;      // 사용자 입력

    private static final int EXPECTED_LENGTH = 13; // 계좌번호 길이

    public void validate() {
        if (accountNumber == null || accountNumber.length() != EXPECTED_LENGTH) {
            throw new IllegalArgumentException("Invalid account number.");
        }
    }
}