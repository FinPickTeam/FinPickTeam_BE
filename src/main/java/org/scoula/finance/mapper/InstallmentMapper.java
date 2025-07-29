package org.scoula.finance.mapper;

import org.scoula.finance.dto.installment.InstallmentListDto;

import java.util.List;

public interface InstallmentMapper {
    List<InstallmentListDto> getInstallmentList();
}
