// transactions.dto.TransactionSyncRequestDto
package org.scoula.transactions.dto;

import lombok.Data;

@Data
public class TransactionSyncRequestDto {
    private String finAccount;
    private String fromDate;
    private String toDate;
}
