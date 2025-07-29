package org.scoula.finance.controller;

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
@RequestMapping("/api/deposit")
public class DepositController {
    private final DepositService depositService;

//  예금 목록 조회
    @GetMapping
    public ResponseEntity<List<DepositListDto>> getDeposits(@ModelAttribute DepositFilterDto filterDto) {
        return ResponseEntity.ok(depositService.getDeposits(filterDto));
    }

//    예금 상세 조회
    @GetMapping("/depositdetail/{depositProductName}")
    public ResponseEntity<DepositDetailDto> getDepositList(@PathVariable String depositProductName)
    {
        return ResponseEntity.ok(depositService.selectDepositByProductName(depositProductName));
    }

    //나중에 pathvariable로 수정 예정
//    예금 추천
    @PostMapping("/recommend")
    public CommonResponseDTO<List<DepositListDto>> recommend(
            @RequestParam int userId,
            @RequestParam int amount,
            @RequestParam int period,
            @RequestBody DepositUserConditionDto depositUserConditionDto){

        //유저 투자 성향 정보 받기

        return CommonResponseDTO.success("추천 성공",depositService.getAllDepositRecommendations(amount, period, depositUserConditionDto));
    }
}
