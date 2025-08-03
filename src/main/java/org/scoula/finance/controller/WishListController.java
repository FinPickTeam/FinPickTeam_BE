package org.scoula.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.wishlist.WishListDto;
import org.scoula.finance.dto.wishlist.WishListResponseDto;
import org.scoula.finance.service.wishlist.WishListService;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"위시리스트 API"})
@RequestMapping("v1/api/wishlist")
public class WishListController {
    private final WishListService wishListService;

    @ApiOperation(value= "사용자 위시리스트 조회", notes = "사용자 ID로 사용자 위시리스트를 조회합니다.")
    @GetMapping
    public CommonResponseDTO<WishListResponseDto> getWishList(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();

        WishListResponseDto wishList = wishListService.getWishList(userId);
        return CommonResponseDTO.success("위시리스트 조회에 성공했습니다.", wishList);
    }
    
    @ApiOperation(value= "위시리스트 상품명 조회", notes = "찜목록 UI에 사용할 위시리스트 상품명을 조회합니다.")
    @GetMapping("/{productType}/names")
    public CommonResponseDTO<List<String>> getWishListByProductType(@AuthenticationPrincipal CustomUserDetails user, @PathVariable String productType){
        if (!List.of("DEPOSIT", "INSTALLMENT", "FUND", "STOCK").contains(productType.toUpperCase())) {
            return CommonResponseDTO.error("타입이 올바르지 않습니다.", 400);
        }

        List<String> result = wishListService.getWishListByProductType(user.getUserId(), productType.toUpperCase());
        return CommonResponseDTO.success("위시리스트 상품명 조회에 성공했습니다.", result);
    }

    @ApiOperation(value= "위시리스트 추가", notes = "위시리스트에 상품을 추가합니다.")
    @PostMapping
    public CommonResponseDTO<Integer> insertWishList(@AuthenticationPrincipal CustomUserDetails user, @RequestBody WishListDto wishListDto){
        wishListDto.setUserId(user.getUserId());
        int result = wishListService.insertWishList(wishListDto);
        if(result > 0){
            return CommonResponseDTO.success("위시리스트 추가에 성공했습니다.");
        }
        else{
            return CommonResponseDTO.error("위시리스트 추가에 실패했습니다.", 400);
        }
    }

    @ApiOperation(value= "위시리스트 제거", notes = "위시리스트에서 상품을 제거합니다.")
    @DeleteMapping
    public CommonResponseDTO<Integer> deleteWishList(@AuthenticationPrincipal CustomUserDetails user, @RequestBody WishListDto wishListDto){
        wishListDto.setUserId(user.getUserId());
        int result = wishListService.deleteWishList(wishListDto);

        if(result > 0){
            return CommonResponseDTO.success("위시리스트에서 삭제되었습니다.");
        }
        else{
            return CommonResponseDTO.error("삭제할 항목이 존재하지 않습니다.", 404);
        }
    }
}
