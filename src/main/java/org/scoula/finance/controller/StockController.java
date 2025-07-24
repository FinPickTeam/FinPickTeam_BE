package org.scoula.finance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;
import org.scoula.finance.dto.stock.StockDetailDto;
import org.scoula.finance.dto.stock.StockListDto;
import org.scoula.finance.service.stock.StockService;
import org.scoula.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock")
public class StockController {
    private final StockService stockService;

    // 사용자 키움증권 rest api 접근 토큰
    @PostMapping("/token/{userId}")
    public ResponseEntity<StockAccessTokenDto> issueAndSaveToken(@PathVariable Long userId){
        stockService.issueAndSaveToken(userId);
        return ResponseEntity.ok(stockService.issueAndSaveToken(userId)); //백엔드 발급 확인용
//        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    // 계좌 수익률 정보 가져오기
    @GetMapping("/account/{userId}")
    public ResponseEntity<StockAccountDto> getAccountInfo(@PathVariable Long userId){
        return ResponseEntity.ok(stockService.getAccountReturnRate(userId));
    }

    // 주식 목록 가져오기 (필터 포함)
    @GetMapping("/stocks")
    public ResponseEntity<List<StockListDto>> getAllStocks(@RequestParam Long id,
                                                           @RequestParam(required = false) String market,
                                                           @RequestParam(required = false) String sortName,
                                                           @RequestParam(required = false) String sortPrice
                                                           ){
        return ResponseEntity.ok(stockService.getStockList(id, market, sortName, sortPrice));
    }

    // 차트 데이터 저장
    @PutMapping("/chart_data")
    public CommonResponseDTO<String> updateChatData(@RequestParam Long id){
        stockService.fetchAndCacheChartData(id);
        return CommonResponseDTO.success("차트 업데이트에 성공했습니다.", null);
    }
    //주식 상세 정보 가져오기
    @GetMapping("stocks/{stockCode}")
    public ResponseEntity<StockDetailDto> getStockDetail(
            @RequestParam Long id, // 임시(JWT 토큰 대용)
//            @AuthenticationPrincipal User user,
            @PathVariable String stockCode){

//        Long userID = user.getId();
        return ResponseEntity.ok(stockService.getStockDetail(id, stockCode));
    }
}
