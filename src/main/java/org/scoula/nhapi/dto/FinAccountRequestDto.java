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
    private String accountNumber;
    private String birthday;
}
