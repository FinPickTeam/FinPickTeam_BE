package org.scoula.account.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.account.dto.AccountListWithTotalDto;
import org.scoula.account.dto.AccountRegisterResponseDto;
import org.scoula.account.service.AccountService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 계좌 등록 및 핀어카운트 발급
    @PostMapping("/register")
    public ResponseEntity<CommonResponseDTO<String>> registerAccount(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody FinAccountRequestDto dto) {

        Long userId = user.getUserId();
        AccountRegisterResponseDto responseDto = accountService.registerFinAccount(userId, dto);
        String finAcno = responseDto.getFinAccount();
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 등록 성공 및 핀어카운트 발급 성공", finAcno));
    }

    // 잔액 및 거래내역 동기화
    @PostMapping("/{accountId}/sync")
    public ResponseEntity<CommonResponseDTO<Void>> syncAccountData(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long accountId) {

        Long userId = user.getUserId();
        accountService.syncAccountById(userId, accountId);
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 동기화 성공", null));
    }

    // 전체 계좌 동기화
    @PostMapping("/sync-all")
    public ResponseEntity<CommonResponseDTO<Void>> syncAllAccounts(
            @AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getUserId();
        accountService.syncAllAccountsByUserId(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("사용자 전체 계좌 동기화 성공", null));
    }

    // 계좌 삭제 (비활성화)
    @DeleteMapping("/{accountId}")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long accountId) {

        Long userId = user.getUserId();
        accountService.deactivateAccount(accountId, userId);
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 비활성화 완료"));
    }

    // 계좌 목록 조회
    @GetMapping("/list")
    public ResponseEntity<CommonResponseDTO<AccountListWithTotalDto>> getAccountsWithTotal(
            @AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getUserId();
        AccountListWithTotalDto result = accountService.getAccountsWithTotal(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 목록 조회 성공", result));
    }
}
