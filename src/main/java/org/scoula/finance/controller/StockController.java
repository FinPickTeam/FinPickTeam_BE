package org.scoula.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.stock.*;
import org.scoula.finance.service.stock.StockService;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"주식 API"})
@RequestMapping("v1/api/stock")
public class StockController {
    private final StockService stockService;

    // 사용자 키움증권 rest api 접근 토큰 발급 및 저장
    @PostMapping("/token/user")
    public CommonResponseDTO<StockAccessTokenDto> issueAndSaveToken(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();

        StockAccessTokenDto token = stockService.issueAndSaveToken(userId);
        if (token == null) {
            return CommonResponseDTO.error("토큰 발급에 실패했습니다.", 500);
        }
        return CommonResponseDTO.success("토큰 발급 성공", token);
    }

    // 사용자 계좌 수익률 정보 가져오기
    @GetMapping("/account/user")
    public CommonResponseDTO<StockAccountDto> getAccountInfo(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();

        StockAccountDto dto = stockService.getAccountReturnRate(userId);
        if (dto == null) {
            return CommonResponseDTO.error("계좌 수익률 정보를 찾을 수 없습니다.", 404);
        }
        return CommonResponseDTO.success("계좌 수익률 조회 성공", dto);
    }

    // 주식 목록 가져오기 (필터 포함)
    @ApiOperation(value = "주식 목록 가져오기", notes = "주식 목록을 가져옵니다.")
    @GetMapping("/list")
    public CommonResponseDTO<List<StockListDto>> getAllStocks(
            @AuthenticationPrincipal CustomUserDetails user,
            @ModelAttribute StockFilterDto filterDto) {

        Long userId = user.getUserId();

        List<StockListDto> stocks = stockService.getStockList(userId, filterDto);
        return CommonResponseDTO.success("주식 목록 조회 성공", stocks);
    }

    // 차트 데이터 저장
    @ApiOperation(value = "차트 데이터 업데이트", notes = "차트 데이터를 새로 가져옵니다.")
    @PutMapping("/stocks/chartData")
    public CommonResponseDTO<String> updateChartData() {
        try {
            stockService.updateChartData();
            return CommonResponseDTO.success("차트 데이터 업데이트 성공");
        } catch (Exception e) {
            return CommonResponseDTO.error("차트 업데이트 실패: " + e.getMessage(), 500);
        }
    }
    
    // 팩터 리밸런싱
    @ApiOperation(value = "팩터 리밸런싱", notes = "팩터값을 리밸런싱 합니다.")
    @PostMapping("/factor/calculate")
    public CommonResponseDTO<Double> calculateFactor(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String analyzeDate,
            @RequestParam String resultDate,
            @RequestParam String startDate
    ){
        try{
            stockService.updateFactor(analyzeDate, resultDate, startDate);
            return  CommonResponseDTO.success("팩터 리밸런싱 성공");
        } catch(Exception e){
            return CommonResponseDTO.error("팩터 리밸런싱 실패 " + e.getMessage(), 500);
        }
    }

    //주식 상세 정보 가져오기
    @ApiOperation(value = "주식 상세 정보 조회", notes = "주식코드로 상세 정보를 조회합니다.")
    @GetMapping("/stocks/{stockCode}")
    public CommonResponseDTO<StockDetailDto> getStockDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String stockCode) {
        Long userId = user.getUserId();

        StockDetailDto detail = stockService.getStockDetail(userId, stockCode);
        if (detail == null) {
            return CommonResponseDTO.error("주식 상세 정보를 찾을 수 없습니다.", 404);
        }
        return CommonResponseDTO.success("주식 상세 정보 조회 성공", detail);
    }

    //추천 주식 가져오기
    @ApiOperation(value = "추천 주식 가져오기", notes = "추천된 주식을 가져옵니다.")
    @GetMapping("/recommend")
    public CommonResponseDTO<List<StockListDto>> getRecommend(@RequestParam(required = false) Integer priceLimit,
                                                              @RequestParam int limit,
                                                              @AuthenticationPrincipal CustomUserDetails user){
        Long userId = user.getUserId();

        List<StockListDto> recommendData = stockService.getStockRecommendationList(userId, limit, priceLimit);
        if (recommendData == null || recommendData.isEmpty()) {
            return CommonResponseDTO.error("추천 가능한 주식이 없습니다.", 404);
        }

        return CommonResponseDTO.success("사용자 맞춤 주식 추천 성공", recommendData);
    }
}
