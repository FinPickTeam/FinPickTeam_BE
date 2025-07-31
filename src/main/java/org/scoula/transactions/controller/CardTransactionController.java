package org.scoula.transactions.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.transactions.dto.CardTransactionDto;
import org.scoula.transactions.service.CardTransactionService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "transaction-controller")
@RestController
@RequestMapping("/api/users/{userId}/cards/{cardId}/transactions")
@RequiredArgsConstructor
public class CardTransactionController {

    private final CardTransactionService cardTransactionService;

    @GetMapping
    public CommonResponseDTO<List<CardTransactionDto>> getCardTransactions(
            @PathVariable Long userId,
            @PathVariable Long cardId) {
        List<CardTransactionDto> result = cardTransactionService.getCardTransactions(userId, cardId);
        return CommonResponseDTO.success("카드 거래내역 조회 성공", result);
    }
}
