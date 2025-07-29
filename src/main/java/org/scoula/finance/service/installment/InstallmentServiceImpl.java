package org.scoula.finance.service.installment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.mapper.InstallmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {
    private final InstallmentMapper installmentMapper;

    // 적금 리스트 조회하기
    @Override
    public List<InstallmentListDto> getInstallmentList(){
        return installmentMapper.getInstallmentList();
    }

    // 적금 상품명으로 상세 정보 조회하기
    @Override
    public InstallmentDetailDto getInstallmentDetail(String installmentProductName){
        return installmentMapper.getInstallmentDetail(installmentProductName);
    }
}
