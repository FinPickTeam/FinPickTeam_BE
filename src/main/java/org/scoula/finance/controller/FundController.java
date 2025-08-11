package org.scoula.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.fund.FundDetailDto;
import org.scoula.finance.dto.fund.FundFilterDto;
import org.scoula.finance.dto.fund.FundListDto;
import org.scoula.finance.service.fund.FundService;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"펀드 API"})
@RequestMapping("/api/v1/fund")
public class FundController {
    private final FundService fundService;

    @ApiOperation(value = "펀드 리스트 조회", notes = "펀드 리스트를 조회합니다.")
    @GetMapping("/list")
    public CommonResponseDTO<List<FundListDto>> getFundList(
            @ModelAttribute FundFilterDto filterDto) {
        List<FundListDto> dto = fundService.getFundList(filterDto);

        if(dto != null){
            return CommonResponseDTO.success("펀드 리스트를 불러오는데 성공했습니다", dto);
        }
        else{
            return CommonResponseDTO.error("펀드 리스트를 불러오는데 실패했습니다", 404);
        }
    }

    @ApiOperation(value = "펀드 상세 조회", notes = "상품명을 기반으로 펀드 상세 정보를 조회합니다.")
    @GetMapping("/fundDetail/")
    public CommonResponseDTO<FundDetailDto> FundDetail(@RequestParam Long productId) {
        if(productId == null){
            return CommonResponseDTO.error("펀드 상세정보를 불러오는데 실패했습니다.", 400);
        }

        FundDetailDto dto = fundService.getFundDetail(productId);

        if(dto != null){
            return CommonResponseDTO.success("펀드 상세정보를 불러오는데 성공했습니다.", dto);
        }
        else{
            return CommonResponseDTO.error("해당 펀드를 찾을 수 없습니다.", 404);
        }
    }

    @ApiOperation(value = "펀드 추천 리스트 조회", notes = "사용자의 투자성향과 펀드의 일간 수익률 데이터를 기반으로 상품을 추천합니다.")
    @GetMapping("/recommend")
    public CommonResponseDTO<List<FundListDto>> getRecommendFundList(){
        List<FundListDto> dto = fundService.getFundRecommendation();

        if(dto != null){
            return CommonResponseDTO.success("펀드 추천 목록을 불러오는데 성공했습니다.", dto);
        }
        else{
            return CommonResponseDTO.error("해당 펀드를 찾을 수 없습니다.", 404);
        }
    }
}
