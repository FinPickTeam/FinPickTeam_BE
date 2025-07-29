package org.scoula.finance.service.installment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.mapper.InstallmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {
    private final InstallmentMapper installmentMapper;

    //
    @Override
    public List<InstallmentListDto> getInstallmentList(){
        return installmentMapper.getInstallmentList();
    }
}
