package org.scoula.nhapi.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.nhapi.dto.BalanceResponseDto;
import org.scoula.nhapi.service.BalanceInquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nh")
@Api(tags = "nhapi-controller")
public class BalanceInquiryController {

    private final BalanceInquiryService balanceInquiryService;

    @PostMapping("/balance")
    public ResponseEntity<CommonResponseDTO<BalanceResponseDto>> getBalance(@RequestParam String finAccount) {
        BalanceResponseDto balance = balanceInquiryService.getBalance(finAccount);
        return ResponseEntity.ok(CommonResponseDTO.success("잔액 조회 완료", balance));
    }

}
