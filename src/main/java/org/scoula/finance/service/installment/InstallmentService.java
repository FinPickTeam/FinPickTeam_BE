package org.scoula.finance.service.installment;

import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentFilterDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.installment.InstallmentUserConditionDto;

import java.util.List;

public interface InstallmentService {
    // 적금 리스트 정보 가져오기
    List<InstallmentListDto> getInstallmentList(InstallmentFilterDto filter);

    // 적금 상세 정보 가져오기
    InstallmentDetailDto getInstallmentDetail(String installmentProductName);

    // 적금 추천 리스트 가져오기
    List<InstallmentListDto> getInstallmentRecommendationList(int amount, int period, InstallmentUserConditionDto condition);
}
