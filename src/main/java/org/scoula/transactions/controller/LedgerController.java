package org.scoula.transactions.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation("통합 거래내역 조회 (카테고리, 기간 필터 지원)")
    @GetMapping
    public CommonResponseDTO<List<LedgerDto>> getLedgerList(
            @PathVariable Long userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String category) {

        List<LedgerDto> result = ledgerService.getLedgers(userId, from, to, category);
        return CommonResponseDTO.success("거래내역 조회 성공", result);
    }

    @ApiOperation("거래 상세 내역 조회")
    @GetMapping("/{ledgerId}")
    public CommonResponseDTO<LedgerDetailDto> getLedgerDetail(
            @PathVariable Long userId,
            @PathVariable Long ledgerId) {
        LedgerDetailDto result = ledgerService.getLedgerDetail(userId, ledgerId);
        return CommonResponseDTO.success("거래 상세 조회 성공", result);
    }
}
