package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.dto.fund.FundListDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.stock.StockCardListDto;
import org.scoula.finance.dto.stock.StockListDataDto;
import org.scoula.finance.dto.wishlist.WishListDto;

import java.util.List;

public interface WishListMapper {
    // 타입에 해당하는 위시리스트 목록 조회
    List<Integer> getProductIdByProductType(@Param("userId") Long userId, @Param("productType") String productType);

    List<DepositListDto> getDepositListByProductId(@Param("productId") List<Integer> productId);

    List<InstallmentListDto> getInstallmentListByProductId(@Param("productId") List<Integer> productId);

    List<FundListDto> getFundListByProductId(@Param("productId") List<Integer> productId);

    List<StockCardListDto> getStockListByProductId(@Param("productId") List<Integer> productId);

    String getStockCodeByStockId(@Param("id") int id);

    StockListDataDto getStockDataByStockCode(@Param("stockCode") String stockCode);

    // 위시리스트 추가
    int insertWishList(@Param("wish") WishListDto wishListDto);

    // 위시리스트에서 삭제
    int deleteWishList(WishListDto wishListDto);
}
