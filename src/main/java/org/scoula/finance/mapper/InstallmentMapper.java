package org.scoula.finance.mapper;

import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentFilterDto;
import org.scoula.finance.dto.installment.InstallmentListDto;

import java.util.List;

public interface InstallmentMapper {

    // 적금 리스트 가져오기 (필터 O)
    List<InstallmentListDto> getInstallmentList(InstallmentFilterDto filter);

    // 적금 상세정보 가져오기
    InstallmentDetailDto getInstallmentDetail(String installmentProductName);
}
