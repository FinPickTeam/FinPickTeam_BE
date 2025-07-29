package org.scoula.finance.mapper;

import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentListDto;

import java.util.List;

public interface InstallmentMapper {
    // 적금 전체 리스트 가져오기
    List<InstallmentListDto> getInstallmentList();

    // 적금 상세정보 가져오기
    InstallmentDetailDto getInstallmentDetail(String installmentProductName);
}
