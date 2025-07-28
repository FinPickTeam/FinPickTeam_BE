package org.scoula.finance.dto.installment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class InstallmentUserConditionDto {
    private boolean autoTransfer; // 자동이체 실적
    private boolean cardUsage; // 카드 사용 여부
    private boolean openBanking; // 오픈뱅킹 가입
    private boolean salaryTransfer; // 급여 이체
    private boolean utilityPayment; // 공과금 자동이체
    private boolean marketingConsent; // 마케팅 수신 동의
    private boolean housingSubscription; // 청약통장 가입 여부
    private boolean internetMobileBanking; // 모바일 뱅킹 이용 여부
    private boolean ageGroup; // 나이대
    private boolean greenMission; // 친환경 미션 여부
    private boolean incomeTransfer; // 소득이체 실적
    private boolean newCustomer; // 신규 고객 여부
}
