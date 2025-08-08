package org.scoula.transactions.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.transactions.dto.LedgerCategoryUpdateDto;
import org.scoula.transactions.dto.LedgerDetailDto;
import org.scoula.transactions.dto.LedgerDto;
import org.scoula.transactions.dto.LedgerMemoUpdateDto;
import org.scoula.transactions.service.LedgerEditService;
import org.scoula.transactions.service.LedgerService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "transaction-controller")
@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;
    private final LedgerEditService ledgerEditService;

    @ApiOperation("통합 거래내역 조회 (카테고리, 기간 필터 지원)")
    @GetMapping
    public CommonResponseDTO<List<LedgerDto>> getLedgerList(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String category) {

        Long userId = user.getUserId();
        List<LedgerDto> result = ledgerService.getLedgers(userId, from, to, category);
        return CommonResponseDTO.success("거래내역 조회 성공", result);
    }

    @ApiOperation("거래 상세 내역 조회")
    @GetMapping("/{ledgerId}")
    public CommonResponseDTO<LedgerDetailDto> getLedgerDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long ledgerId) {
        Long userId = user.getUserId();
        LedgerDetailDto result = ledgerService.getLedgerDetail(userId, ledgerId);
        return CommonResponseDTO.success("거래 상세 조회 성공", result);
    }

    @ApiOperation("거래 카테고리 수정")
    @PatchMapping("/{ledgerId}/category")
    public CommonResponseDTO<Void> updateLedgerCategory(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long ledgerId,
            @RequestBody LedgerCategoryUpdateDto dto) {
        Long userId = user.getUserId();
        // 권한 검증이 필요한 경우 userId 넘겨서 서비스에서 처리
        ledgerEditService.updateCategory(ledgerId, dto.getCategoryId());
        return CommonResponseDTO.success("카테고리 수정 완료", null);
    }

    @ApiOperation("거래 메모 수정")
    @PatchMapping("/{ledgerId}/memo")
    public CommonResponseDTO<Void> updateLedgerMemo(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long ledgerId,
            @RequestBody LedgerMemoUpdateDto dto) {
        Long userId = user.getUserId();
        // 권한 검증이 필요한 경우 userId 넘겨서 서비스에서 처리
        ledgerEditService.updateMemo(ledgerId, dto.getMemo());
        return CommonResponseDTO.success("메모 수정 완료", null);
    }
}
