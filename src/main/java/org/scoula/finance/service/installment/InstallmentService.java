package org.scoula.finance.service.installment;

import org.scoula.finance.dto.installment.InstallmentListDto;

import java.util.List;

public interface InstallmentService {
    // 적금 리스트 정보 가져오기
    List<InstallmentListDto> getInstallmentList();
}
