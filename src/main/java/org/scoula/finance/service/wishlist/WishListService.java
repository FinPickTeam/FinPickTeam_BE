package org.scoula.finance.service.wishlist;

import org.scoula.finance.dto.wishlist.WishListDto;
import org.scoula.finance.dto.wishlist.WishListResponseDto;

import java.util.List;

public interface WishListService {

    // 위시리스트 조회 (찜목록용)
    WishListResponseDto getWishList(Long userId);

    // 위시리스트에 있는 상품 이름 목록 가져오기 (하트 UI 용)
    List<String> getWishListByProductType(Long userId, String productType);

    // 위시리스트 추가
    int insertWishList(WishListDto wishListDto);

    // 위시리스트 삭제
    int deleteWishList(WishListDto wishListDto);
}
