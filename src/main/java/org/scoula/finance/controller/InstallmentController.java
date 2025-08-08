package org.scoula.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentFilterDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.installment.InstallmentUserConditionDto;
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
            @ModelAttribute InstallmentFilterDto filterDto) {
        List<InstallmentListDto> dto = installmentService.getInstallmentList(filterDto);

        if(dto != null) {
            return CommonResponseDTO.success("적금 리스트를 불러오는데 성공했습니다.", dto);
        }
        else{
            return CommonResponseDTO.error("적금 리스트를 불러올 수 없습니다.", 404);
        }

    }

    @ApiOperation(value = "적금 상세 조회", notes = "상품명을 기반으로 적금 상세 정보를 조회합니다.")
    @GetMapping("/installmentDetail/")
    public CommonResponseDTO<InstallmentDetailDto> getInstallmentDetail(
            @RequestParam int installmentProductId){
        InstallmentDetailDto dto = installmentService.getInstallmentDetail(installmentProductId);

        if(dto != null) {
            return CommonResponseDTO.success(dto.getInstallmentProductName() + "의 데이터를 불러오는데 성공했습니다.", dto);
        }
        else{
            return CommonResponseDTO.error("상세정보를 불러오는데 실패했습니다.",404);
        }
    }

    @ApiOperation(value = "적금 추천", notes = "사용자의 투자성향을 참고하여 적금 상품을 추천합니다.")
    @PostMapping("/recommend")
    public CommonResponseDTO<List<InstallmentListDto>> getRecommendInstallmentList(
            @RequestParam int amount,
            @RequestParam int period,
            @RequestBody InstallmentUserConditionDto userConditionDto){

        if (amount <= 0 || period <= 0) {
            return CommonResponseDTO.error("유효하지 않은 요청 값입니다. 금액과 기간은 0보다 커야 합니다.", 400);
        }
        if (userConditionDto == null) {
            return CommonResponseDTO.error("사용자 조건 정보가 누락되었습니다.", 400);
        }

        return CommonResponseDTO.success("추천 상품을 불러오는데 성공했습니다", installmentService.getInstallmentRecommendationList(amount, period, userConditionDto));
    }

}
