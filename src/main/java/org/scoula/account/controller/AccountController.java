package org.scoula.account.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.account.dto.AccountRegisterResponseDto;
import org.scoula.account.service.AccountService;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponseDTO<AccountRegisterResponseDto>> register(
            @RequestBody FinAccountRequestDto dto
    ) {
        AccountRegisterResponseDto response = accountService.registerFinAccount(dto);
        return ResponseEntity.ok(CommonResponseDTO.success("계좌 등록 완료", response));
    }
}
