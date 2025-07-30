package org.scoula.finance.service.fund;

import org.scoula.finance.dto.fund.FundFilterDto;
import org.scoula.finance.dto.fund.FundListDto;

import java.util.List;

public interface FundService {
    
    // 펀드 전체 조회하기
    List<FundListDto> getFundList(FundFilterDto filter);
}
