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

    // 사용자 키움증권 rest api 접근 토큰 발급 및 저장
    @PostMapping("/token/{userId}")
    public CommonResponseDTO<StockAccessTokenDto> issueAndSaveToken(@PathVariable Long userId) {
        StockAccessTokenDto token = stockService.issueAndSaveToken(userId);
        if (token == null) {
            return CommonResponseDTO.error("토큰 발급에 실패했습니다.", 500);
        }
        return CommonResponseDTO.success("토큰 발급 성공", token);
    }

    // 사용자 계좌 수익률 정보 가져오기
    @GetMapping("/account/{userId}")
    public CommonResponseDTO<StockAccountDto> getAccountInfo(@PathVariable Long userId) {
        StockAccountDto dto = stockService.getAccountReturnRate(userId);
        if (dto == null) {
            return CommonResponseDTO.error("계좌 수익률 정보를 찾을 수 없습니다.", 404);
        }
        return CommonResponseDTO.success("계좌 수익률 조회 성공", dto);
    }

    // 주식 목록 가져오기 (필터 포함)
    @GetMapping("/stocks")
    public CommonResponseDTO<List<StockListDto>> getAllStocks(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String sortName,
            @RequestParam(required = false) String sortPrice) {

        List<StockListDto> stocks = stockService.getStockList(userId, market, sortName, sortPrice);
        return CommonResponseDTO.success("주식 목록 조회 성공", stocks);
    }

    // 차트 데이터 저장
    @PutMapping("/stocks/chart-data")
    public CommonResponseDTO<String> updateChartData(@RequestParam(name = "userId") Long userId) {
        try {
            stockService.fetchAndCacheChartData(userId);
            return CommonResponseDTO.success("차트 데이터 업데이트 성공");
        } catch (Exception e) {
            return CommonResponseDTO.error("차트 업데이트 실패: " + e.getMessage(), 500);
        }
    }

    //주식 상세 정보 가져오기
    @GetMapping("/stocks/{stockCode}")
    public CommonResponseDTO<StockDetailDto> getStockDetail(
            @RequestParam(name = "userId") Long userId,
            @PathVariable String stockCode) {

        StockDetailDto detail = stockService.getStockDetail(userId, stockCode);
        if (detail == null) {
            return CommonResponseDTO.error("주식 상세 정보를 찾을 수 없습니다.", 404);
        }
        return CommonResponseDTO.success("주식 상세 정보 조회 성공", detail);
    }

    //사용자 맞춤 추천 주식 가져오기
    @GetMapping("/recommend")
    public CommonResponseDTO<List<StockListDto>> getRecommend(@RequestParam(name = "id") Long id) {
        List<StockListDto> recommendData = stockService.getStockRecommendationList(id);

        if (recommendData == null || recommendData.isEmpty()) {
            return CommonResponseDTO.error("추천 가능한 주식이 없습니다.", 404);
        }

        return CommonResponseDTO.success("사용자 맞춤 주식 추천 성공", recommendData);
    }

    @GetMapping("/test")
    public CommonResponseDTO<Long> test(@AuthenticationPrincipal User user) {
        log.info(" 유저 데이터: "+  user.toString());
        Long userId = user.getId();
        return CommonResponseDTO.success("유저 아이디 반환", userId);
    }
}
