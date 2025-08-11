package org.scoula.finance.service.wishlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.dto.fund.FundListDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.stock.StockCardListDto;
import org.scoula.finance.dto.stock.StockDetailDto;
import org.scoula.finance.dto.stock.StockListDataDto;
import org.scoula.finance.dto.stock.StockListDto;
import org.scoula.finance.dto.wishlist.WishListDto;
import org.scoula.finance.dto.wishlist.WishListResponseDto;
import org.scoula.finance.mapper.StockMapper;
import org.scoula.finance.mapper.WishListMapper;
import org.scoula.finance.service.stock.StockServiceImpl;
import org.scoula.finance.util.KiwoomApiUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {
    private final WishListMapper wishListMapper;
    private final StockMapper stockMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 위시리스트 목록 조회하기
    @Override
    public WishListResponseDto getWishList(Long userId){
        //예금 위시리스트 목록 가져오기
        List<Integer> depositProductIdList = wishListMapper.getProductIdByProductType(userId, "DEPOSIT");
        System.out.println("예금 상품 ID: " + depositProductIdList);
        List<DepositListDto> depositList = wishListMapper.getDepositListByProductId(depositProductIdList);
        
        //적금 위시리스트 목록 가져오기
        List<Integer> installmentProductIdList = wishListMapper.getProductIdByProductType(userId, "INSTALLMENT");
        System.out.println("적금 상품 ID: " + installmentProductIdList);
        List<InstallmentListDto> installmentList = wishListMapper.getInstallmentListByProductId(installmentProductIdList);

        //펀드 위시리스트 목록 가져오기
        List<Integer> fundProductIdList = wishListMapper.getProductIdByProductType(userId, "FUND");
        System.out.println("펀드 상품 ID: " + fundProductIdList);
        List<FundListDto> fundList = wishListMapper.getFundListByProductId(fundProductIdList);

        //주식 위시리스트 목록 가져오기
        List<Integer> stockIdList = wishListMapper.getProductIdByProductType(userId, "STOCK");
        List<StockListDto> stockList = new ArrayList<>();
        if(!stockIdList.isEmpty()){
            String token = stockMapper.getUserToken(userId);

            System.out.println("주식 코드: " + stockIdList);

            for(int intStockCode : stockIdList){
                StockListDto dto = new StockListDto();
                String stockCode = String.format("%06d", intStockCode);

                try{
                    String response = KiwoomApiUtils.sendPostRequest("/api/dostk/stkinfo", token,
                            String.format("{\"stk_cd\" : \"%s\"}", stockCode), "ka10001");

                    JsonNode root = objectMapper.readTree(response);

                    StockListDataDto dataDto = wishListMapper.getStockDataByStockCode(stockCode);

                    dto.setStockCode(stockCode);
                    dto.setStockName(dataDto.getStockName());
                    dto.setStockReturnsData(dataDto.getStockReturnsData());
                    dto.setStockMarketType(dataDto.getStockMarketType());
                    dto.setStockPredictedPrice(root.path("pred_pre").asText());
                    dto.setStockChangeRate(root.path("flu_rt").asText());
                    dto.setStockSummary(dataDto.getStockSummary());

                    String curPriceRaw = root.path("cur_prc").asText();
                    String curPrice = curPriceRaw.replaceAll("[^0-9]", "");
                    int currentPrice = Integer.parseInt(curPrice);
                    dto.setStockPrice(currentPrice);

                    stockList.add(dto);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //위시리스트 통합 목록 가져오기 (카드 데이터)
        return new WishListResponseDto(depositList, installmentList, fundList, stockList);
    }

    // 위시리스트에 있는 상품 이름 목록 가져오기 (하트 UI 용)
    @Override
    public List<Integer> getWishListByProductType(Long userId, String productType){
        return wishListMapper.getProductIdByProductType(userId, productType);
    }

    // 위시리스트에 상품 추가하기
    @Override
    public int insertWishList(WishListDto wishListDto){
        return wishListMapper.insertWishList(wishListDto);
    }

    // 위시리스트에서 삭제
    @Override
    public int deleteWishList(WishListDto wishListDto){
        return wishListMapper.deleteWishList(wishListDto);
    }

}
