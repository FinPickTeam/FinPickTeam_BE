package org.scoula.transactions.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.transactions.dto.LedgerDetailDto;
import org.scoula.transactions.dto.LedgerDto;
import org.scoula.transactions.service.LedgerService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "transaction-controller")
@RestController
@RequestMapping("/api/users/{userId}/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping
    public CommonResponseDTO<List<LedgerDto>> getLedgerList(@PathVariable Long userId) {
        List<LedgerDto> result = ledgerService.getLedgerByUserId(userId);
        return CommonResponseDTO.success("거래내역 조회 성공", result);
    }

    @GetMapping("/{ledgerId}")
    public CommonResponseDTO<LedgerDetailDto> getLedgerDetail(
            @PathVariable Long userId,
            @PathVariable Long ledgerId) {
        LedgerDetailDto result = ledgerService.getLedgerDetail(userId, ledgerId);
        return CommonResponseDTO.success("거래 상세 조회 성공", result);
    }

}
