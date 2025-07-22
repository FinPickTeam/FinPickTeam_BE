package org.scoula.finance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositRecommendationDto;
import org.scoula.finance.service.DepositService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposit")
public class DepositController {
    private final DepositService depositService;

//    예금 목록 조회
    @GetMapping("/depositlist")
    public ResponseEntity<List<DepositDetailDto>> getDepositList() {return ResponseEntity.ok(depositService.getAllDepositDetails());}

//    예금 추천
    @GetMapping("/recommend")
    public ResponseEntity<List<Map<String, Object>>> recommend(@RequestParam int userId, @RequestParam int amount, @RequestParam int period){

        //유저 투자 성향 정보 받기

        return ResponseEntity.ok(depositService.getAllDepositRecommendations(amount, period));
    }
}
