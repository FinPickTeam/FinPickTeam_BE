package org.scoula.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentFilterDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.service.installment.InstallmentService;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.user.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"적금 API"})
@RequestMapping("/v1/api/installment")
public class InstallmentController {
    private final InstallmentService installmentService;

    @ApiOperation(value= "적금 리스트 조회", notes = "적금 리스트를 조회합니다.")
    @GetMapping("/list")
    public CommonResponseDTO<List<InstallmentListDto>> getInstallmentList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute InstallmentFilterDto filterDto) {
        System.out.println("유저 ID: " + userDetails.getUserId());

        List<InstallmentListDto> dto = installmentService.getInstallmentList(filterDto);

        if(dto != null) {
            return CommonResponseDTO.success("적금 리스트를 불러오는데 성공했습니다.", dto);
        }
        else{
            return CommonResponseDTO.error("적금 리스트를 불러올 수 없습니다.", 404);
        }

    }

    @ApiOperation(value = "적금 상세 조회", notes = "상품명을 기반으로 적금 상세 정보를 조회합니다.")
    @GetMapping("/installmentDetail/{installmentProductName}")
    public CommonResponseDTO<InstallmentDetailDto> getInstallmentDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String installmentProductName){
        System.out.println("유저 ID: " + userDetails.getUserId());

        InstallmentDetailDto dto = installmentService.getInstallmentDetail(installmentProductName);

        if(dto != null) {
            return CommonResponseDTO.success(dto.getInstallmentProductName() + "의 데이터를 불러오는데 성공했습니다.", dto);
        }
        else{
            return CommonResponseDTO.error("상세정보를 불러오는데 실패했습니다.",404);
        }
    }

}
