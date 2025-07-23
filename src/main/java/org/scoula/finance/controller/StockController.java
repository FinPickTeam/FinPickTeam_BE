package org.scoula.finance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;
import org.scoula.finance.service.stock.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock")
public class StockController {
    private final StockService stockService;

    @PostMapping("/token/{userId}")
    public ResponseEntity<StockAccessTokenDto> issueAndSaveToken(@PathVariable Long userId){
        return ResponseEntity.ok(stockService.issueAndSaveToken(userId));
    }

    @GetMapping("/account/{userId}")
    public ResponseEntity<StockAccountDto> getAccountInfo(@PathVariable Long userId){
        return ResponseEntity.ok(stockService.getAccountReturnRate(userId));
    }
}
