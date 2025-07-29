package org.scoula.finance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.service.installment.InstallmentService;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.user.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/installment")
public class InstallmentController {
    private final InstallmentService installmentService;

    @GetMapping("/list")
    public CommonResponseDTO<List<InstallmentListDto>> getInstallmentList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        System.out.println("유저 데이터: " + userDetails.toString());
        System.out.println("유저 ID: " + userDetails.getUserId());
        return CommonResponseDTO.success(
                "적금 리스트를 불러오는데 성공했습니다.",
                installmentService.getInstallmentList());
    }
}
