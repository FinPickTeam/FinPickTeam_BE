package org.scoula.finance.mapper;

import org.scoula.finance.dto.fund.FundFilterDto;
import org.scoula.finance.dto.fund.FundListDto;

import java.util.List;

public interface FundMapper {

    //펀드 리스트 가져오기 (필터 O)
    List<FundListDto> getFundList(FundFilterDto fundFilterDto);
}
