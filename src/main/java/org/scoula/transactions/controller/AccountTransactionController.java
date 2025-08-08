package org.scoula.transactions.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.transactions.dto.AccountTransactionDto;
import org.scoula.transactions.service.AccountTransactionService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "transaction-controller")
@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class AccountTransactionController {

    private final AccountTransactionService accountTransactionService;

    @GetMapping
    public CommonResponseDTO<List<AccountTransactionDto>> getAccountTransactions(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long accountId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        Long userId = user.getUserId();
        List<AccountTransactionDto> result = accountTransactionService.getTransactions(userId, accountId, from, to);
        return CommonResponseDTO.success("계좌 거래내역 조회 성공", result);
    }
}
