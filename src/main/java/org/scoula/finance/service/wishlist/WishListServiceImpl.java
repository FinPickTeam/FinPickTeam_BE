package org.scoula.finance.service.wishlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.dto.fund.FundListDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.stock.StockCardListDto;
import org.scoula.finance.dto.wishlist.WishListDto;
import org.scoula.finance.dto.wishlist.WishListResponseDto;
import org.scoula.finance.mapper.WishListMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {
    private final WishListMapper wishListMapper;

    // 위시리스트 목록 조회하기
    @Override
    public WishListResponseDto getWishList(Long userId){
        //예금 위시리스트 목록 가져오기
        List<String> depositProductNameList = wishListMapper.getProductNameByProductType(userId, "DEPOSIT");
        System.out.println("예금 상품 이름: " + depositProductNameList);
        List<DepositListDto> depositList = wishListMapper.getDepositListByProductName(depositProductNameList);
        
        //적금 위시리스트 목록 가져오기
        List<String> installmentProductNameList = wishListMapper.getProductNameByProductType(userId, "INSTALLMENT");
        System.out.println("적금 상품 이름: " + installmentProductNameList);
        List<InstallmentListDto> installmentList = wishListMapper.getInstallmentListByProductName(installmentProductNameList);

        //펀드 위시리스트 목록 가져오기
        List<String> fundProductNameList = wishListMapper.getProductNameByProductType(userId, "FUND");
        System.out.println("펀드 상품 이름: " + fundProductNameList);
        List<FundListDto> fundList = wishListMapper.getFundListByProductName(fundProductNameList);
        
        //주식 위시리스트 목록 가져오기
        List<String> stockNameList = wishListMapper.getProductNameByProductType(userId, "STOCK");
        System.out.println("주식 상품 이름: " + stockNameList);
        List<StockCardListDto> stockList = wishListMapper.getStockListByProductName(stockNameList);

        //위시리스트 통합 목록 가져오기 (카드 데이터)
        WishListResponseDto wishListResponseDto = new WishListResponseDto(depositList, installmentList, fundList, stockList);
        return wishListResponseDto;
    }

    // 위시리스트에 있는 상품 이름 목록 가져오기 (하트 UI 용)
    @Override
    public List<String> getWishListByProductType(Long userId, String productType){
        return wishListMapper.getProductNameByProductType(userId, productType);
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
