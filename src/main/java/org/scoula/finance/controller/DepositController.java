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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposit")
public class DepositController {
    private final DepositService depositService;

    @GetMapping("/depositlist")
    public ResponseEntity<List<DepositDetailDto>> getDepositList() {return ResponseEntity.ok(depositService.getAllDepositDetails());}

//    @GetMapping("/recommend")
//    public ResponseEntity<List<DepositRecommendationDto>> recommend(@RequestParam int userId, @RequestParam int amount, @RequestParam int period){
//
//        //유저 투자 정보 받기
//
//        return ResponseEntity.ok(depositService.getAllDepositRecommendations(userId, amount, period));
//    }
}
