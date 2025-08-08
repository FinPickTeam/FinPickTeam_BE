package org.scoula.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.deposit.DepositDetailDto;
import org.scoula.finance.dto.deposit.DepositFilterDto;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.dto.deposit.DepositUserConditionDto;
import org.scoula.finance.service.deposit.DepositService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"예금 API"})
@RequestMapping("v1/api/deposit")
public class DepositController {
    private final DepositService depositService;

    //  예금 목록 조회
    @ApiOperation(value= "예금 리스트 조회", notes = "예금 리스트를 조회합니다.")
    @GetMapping("/list")
    public CommonResponseDTO<List<DepositListDto>> getDeposits(@ModelAttribute DepositFilterDto filterDto) {
        List<DepositListDto> dto = depositService.getDeposits(filterDto);
        return CommonResponseDTO.success("예금 목록 조회에 성공했습니다.", dto);
    }

//    예금 상세 조회
    @ApiOperation(value = "예금 상세 정보 조회", notes = "예금 상품명으로 상세 정보를 조회합니다.")
    @GetMapping("/depositDetail/")
    public CommonResponseDTO<DepositDetailDto> getDepositList(@RequestParam Long productId)
    {
        DepositDetailDto dto = depositService.selectDepositByProductName(productId);

        if(dto != null){
            return CommonResponseDTO.success("예금 상세 정보를 가져오는데 성공했습니다.", dto);
        }
        else{
            return CommonResponseDTO.error("예금 상세 정보를 불러올 수 없습니다.", 400);
        }
    }

//    예금 추천
    @PostMapping("/recommend")
    public CommonResponseDTO<List<DepositListDto>> recommend(
            @RequestParam int amount,
            @RequestParam int period,
            @RequestBody DepositUserConditionDto depositUserConditionDto){
        if (amount <= 0 || period <= 0) {
            return CommonResponseDTO.error("유효하지 않은 요청 값입니다. 금액과 기간은 0보다 커야 합니다.", 400);
        }
        if (depositUserConditionDto == null) {
            return CommonResponseDTO.error("사용자 조건 정보가 누락되었습니다.", 400);
        }


        return CommonResponseDTO.success("추천 상품을 불러오는데 성공했습니다", depositService.getAllDepositRecommendations(amount, period, depositUserConditionDto));
    }
}
