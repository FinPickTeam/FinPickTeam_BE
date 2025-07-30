package org.scoula.finance.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.finance.dto.fund.FundFilterDto;
import org.scoula.finance.dto.fund.FundListDto;
import org.scoula.finance.service.fund.FundService;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"펀드 API"})
@RequestMapping("v1/api/fund")
public class FundController {
    private final FundService fundService;

    @GetMapping("/list")
    public CommonResponseDTO<List<FundListDto>> getFundList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @ModelAttribute FundFilterDto filterDto) {
        List<FundListDto> dto = fundService.getFundList(filterDto);

        if(dto != null){
            return CommonResponseDTO.success("펀드 리스트를 불러오는데 성공했습니다", dto);
        }
        else{
            return CommonResponseDTO.error("펀드 리스트를 불러오는데 실패했습니다", 404);
        }
    }
}
