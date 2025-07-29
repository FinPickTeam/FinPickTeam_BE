package org.scoula.nhapi.controller;


import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.service.AccountRegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nh")
@Api(tags = "nhapi-controller")
public class AccountRegisterController {

    private final AccountRegisterService accountRegisterService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponseDTO<Map<String, Object>>> register(@RequestBody FinAccountRequestDto requestDto) {
        Map<String, Object> result = accountRegisterService.registerAccount(requestDto);
        return ResponseEntity.ok(CommonResponseDTO.success("핀어카운트 발급 및 계좌 등록 완료", result));
    }

}
