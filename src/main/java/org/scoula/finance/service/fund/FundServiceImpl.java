package org.scoula.finance.service.fund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.fund.FundDetailDto;
import org.scoula.finance.dto.fund.FundFilterDto;
import org.scoula.finance.dto.fund.FundListDto;
import org.scoula.finance.mapper.FundMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundServiceImpl  implements FundService {
    private final FundMapper fundMapper;

    // 펀드 리스트 조회 (필터 포함)
    @Override
    public List<FundListDto> getFundList(FundFilterDto filter){
        return fundMapper.getFundList(filter);
    }

    // 펀드 상세 정보 조회
    @Override
    public FundDetailDto getFundDetail(String fundProductName){
        return fundMapper.getFundDetail(fundProductName);
    }
}
