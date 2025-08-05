package org.scoula.account.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.account.dto.AccountDto;
import org.scoula.account.dto.AccountRegisterResponseDto;
import org.scoula.account.service.AccountService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 계좌 등록 및 핀어카운트 발급
    @PostMapping("/register")
    public ResponseEntity<CommonResponseDTO<String>> registerAccount(@RequestBody FinAccountRequestDto dto) {
        // AccountRegisterResponseDto 반환
        AccountRegisterResponseDto responseDto = accountService.registerFinAccount(dto);

        // 핀어카운트 정보를 응답으로 반환
        String finAcno = responseDto.getFinAccount();
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 등록 성공 및 핀어카운트 발급 성공", finAcno));
    }

    // 잔액 및 거래내역 동기화
    @PostMapping("/{accountId}/sync")
    public ResponseEntity<CommonResponseDTO<Void>> syncAccountData(@PathVariable Long accountId) {
        accountService.syncAccountById(accountId);
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 동기화 성공", null));
    }

    // 전체 계좌 동기화 (userId 기준)
    @PostMapping("/sync-all/{userId}")
    public ResponseEntity<CommonResponseDTO<Void>> syncAllAccounts(@PathVariable Long userId) {
        accountService.syncAllAccountsByUserId(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("사용자 전체 계좌 동기화 성공", null));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAccount(
            @PathVariable Long accountId,
            @RequestParam Long userId //TODO 나중에 @AuthenticationPrincipal로 바꾸기
    ) {
        accountService.deactivateAccount(accountId, userId);
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 비활성화 완료"));
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<CommonResponseDTO<List<AccountDto>>> getActiveAccounts(@PathVariable Long userId) {
        List<AccountDto> accounts = accountService.getActiveAccounts(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 목록 조회 성공", accounts));
    }

}