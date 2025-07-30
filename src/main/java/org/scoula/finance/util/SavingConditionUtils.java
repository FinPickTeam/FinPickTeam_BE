package org.scoula.finance.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SavingConditionUtils {

    //    가입기간 확인
    public boolean checkPeriod(String periodStr, int period){
        try{
            String raw = periodStr.replaceAll("-","");

            if(periodStr.contains("이상") && periodStr.contains("이하")){
                String[] parts= raw.split("이상|이하");
                int min = parsePeriodToMonth(parts[0]);
                return min <= period;
            }
            else if(raw.contains(",")){
                int target = parsePeriodToMonth(raw.split(",")[0]);
                return target <= period;
            }else{
                int target = parsePeriodToMonth(raw);
                return target <= period;
            }
        } catch (Exception e) {
            log.error("계약기간 파싱 오류: {}", periodStr, e);
        }
        return false;
    }

    // 가입기간 단위 변경
    private int parsePeriodToMonth(String input) {
        input = input.trim().replaceAll(",", "").replaceAll("제", "");

        if (input.contains("년")) {
            return Integer.parseInt(input.replaceAll("[^0-9]", "")) * 12;
        } else if (input.contains("개월")) {
            return Integer.parseInt(input.replaceAll("[^0-9]", ""));
        } else {
            // 단위 없을 때는 월 단위로 가정
            return Integer.parseInt(input.replaceAll("[^0-9]", ""));
        }
    }

    //  가입금액 범위 확인
    public boolean checkAmount(String amountStr, int amount){

        try{
            String raw = amountStr.replaceAll("[\\s\"]", "");

            if(raw.contains("이상") && raw.contains("이하")){
                String[] parts = raw.split("이상|이하");
                int min = parseMoney(parts[0]);
                return amount >= min;
            }
            else if(raw.contains("이상")){
                int min = parseMoney(raw.split("이상")[0]);
                return amount >= min;
            }
            else if(raw.contains("이하")){
                return true;
            }
            else{
                log.warn("처리 안된 가입금액 형식: {}", amountStr);
            }
        } catch (Exception e) {
            log.error("가입금액 파싱 오류: {}", amountStr, e);
        }
        return false;
    }

    //    금액 단위 변경
    private int parseMoney(String moneyStr){
        moneyStr = moneyStr.replaceAll(",","");
        if(moneyStr.contains("억원")){
            return Integer.parseInt(moneyStr.replaceAll("[^0-9]","")) * 100_000_000;
        }
        else if(moneyStr.contains("천만원")){
            return Integer.parseInt(moneyStr.replaceAll("[^0-9]","")) * 10_000_000;
        }
        else if(moneyStr.contains("만원")){
            return Integer.parseInt(moneyStr.replaceAll("[^0-9]","")) * 10_000;
        }
        else if(moneyStr.contains("천원")){
            return Integer.parseInt(moneyStr.replaceAll("[^0-9]","")) * 1_000;
        }
        return Integer.parseInt(moneyStr);
    }
}
