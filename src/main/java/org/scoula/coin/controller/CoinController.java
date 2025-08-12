package org.scoula.coin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.coin.dto.CoinMonthlyResponseDTO;
import org.scoula.coin.dto.CoinStatusResponseDTO;
import org.scoula.coin.service.CoinService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coin")
public class CoinController {

    private final CoinService coinService;

    // 이달 누적 포인트
    @GetMapping("/monthly")
    public CommonResponseDTO<CoinMonthlyResponseDTO> getMyMonthlyCoin(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        CoinMonthlyResponseDTO dto = coinService.getMyMonthlyCoin(userId);
        return CommonResponseDTO.success("월별 누적 포인트 조회 성공", dto);
    }

    @GetMapping("/status")
    public CommonResponseDTO<CoinStatusResponseDTO> getMyCoinStatus(
            @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        return CommonResponseDTO.success("코인 상태 조회 성공",
                coinService.getMyCoinStatus(userId));
    }
}
