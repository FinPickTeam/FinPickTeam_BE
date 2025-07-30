package org.scoula.finance.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class DepositUserConditionDto {
    private boolean newCustomer; // 신규 고객 여부
    private boolean salaryTransfer; // 급여 또는 연금 이체 실적 여부
    private boolean cardUsage; // 신용카드 또는 체크카드 사용 실적 여부
    private boolean internetMobileBanking; // 인터넷/모바일/폰뱅킹 사용 여부
    private boolean marketingConsent; // 마케팅 정보 수신 동의 여부
    private boolean housingSubscription; // 주택청약종합저축 또는 청약통장 보유 여부
    private boolean couponUsed; // 금리우대쿠폰 사용 여부
}
