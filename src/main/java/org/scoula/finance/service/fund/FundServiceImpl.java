package org.scoula.finance.service.fund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public List<FundListDto> getFundList(FundFilterDto filter){
        return fundMapper.getFundList(filter);
    }
}
