package org.scoula.transactions.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.transactions.dto.AccountTransactionDto;
import org.scoula.transactions.service.AccountTransactionService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "transaction-controller")
@RestController
@RequestMapping("/api/users/{userId}/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class AccountTransactionController {

    private final AccountTransactionService accountTransactionService;

    @GetMapping
    public CommonResponseDTO<List<AccountTransactionDto>> getAccountTransactions(
            @PathVariable Long userId,
            @RequestParam Long accountId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        List<AccountTransactionDto> result = accountTransactionService.getTransactions(userId, accountId, from, to);
        return CommonResponseDTO.success("계좌 거래내역 조회 성공", result);
    }

}
