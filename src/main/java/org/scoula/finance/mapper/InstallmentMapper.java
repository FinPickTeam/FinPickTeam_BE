package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.finance.dto.installment.InstallmentDetailDto;
import org.scoula.finance.dto.installment.InstallmentFilterDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.installment.InstallmentRecommendationDto;

import java.util.List;

public interface InstallmentMapper {

    // 적금 리스트 가져오기 (필터 O)
    List<InstallmentListDto> getInstallmentList(InstallmentFilterDto filter);

    // 적금 상세정보 가져오기
    InstallmentDetailDto getInstallmentDetail(String installmentProductName);
    
    // 추천 로직에 필요한 데이터 가져오기
    List<InstallmentRecommendationDto> getInstallmentRecommendationList();

    // 상품명으로 적금 리스트 가져오기
    List<InstallmentListDto> getInstallmentListByProductName(@Param("names") List<String> installmentProductName);
}
