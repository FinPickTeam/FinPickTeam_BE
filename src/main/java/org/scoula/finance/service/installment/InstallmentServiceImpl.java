package org.scoula.finance.service.installment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentFilterDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.installment.InstallmentUserConditionDto;
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
    public List<InstallmentListDto> getInstallmentList(InstallmentFilterDto filter){
        return installmentMapper.getInstallmentList(filter);
    }

    // 적금 상품명으로 상세 정보 조회하기
    @Override
    public InstallmentDetailDto getInstallmentDetail(String installmentProductName){
        return installmentMapper.getInstallmentDetail(installmentProductName);
    }

    //사용자 맞춤 적금 추천 상품 리스트 조회하기
    @Override
    public List<InstallmentListDto> getInstallmentRecommendationList(int amount, int period, InstallmentUserConditionDto conditionDto){
        return null;
    }

}
