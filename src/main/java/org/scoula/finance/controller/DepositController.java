package org.scoula.finance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.deposit.DepositDetailDto;
import org.scoula.finance.dto.deposit.DepositFilterDto;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.service.deposit.DepositService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @GetMapping("/depositdetail")
    public ResponseEntity<DepositDetailDto> getDepositList(@RequestParam String depositProductName)
    {return ResponseEntity.ok(depositService.selectDepositByProductName(depositProductName));}

    //나중에 pathvariable로 수정 예정
//    예금 추천
    @GetMapping("/recommend")
    public ResponseEntity<List<Map<String, Object>>> recommend(@RequestParam int userId, @RequestParam int amount, @RequestParam int period){

        //유저 투자 성향 정보 받기

        return ResponseEntity.ok(depositService.getAllDepositRecommendations(amount, period));
    }
}
