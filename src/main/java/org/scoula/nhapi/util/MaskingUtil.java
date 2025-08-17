package org.scoula.nhapi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 카드/계좌 공통 마스킹 유틸:
 * - 카드: 1234-****-****-5678
 * - 계좌: 020-****-180882 (첫 그룹/마지막 그룹만 노출, 중간 전부 마스킹)
 */
public final class MaskingUtil {
    private MaskingUtil() {}

    /** 문자열 안의 숫자 그룹 기준으로 첫/마지막만 남기고 가운데 그룹은 전부 '*' 처리 */
    public static String maskMiddleGroups(String s) {
        if (s == null || s.isBlank()) return s;
        var m = Pattern.compile("\\d+").matcher(s);
        List<int[]> groups = new ArrayList<>();
        while (m.find()) groups.add(new int[]{m.start(), m.end()});
        if (groups.isEmpty()) return s;

        StringBuilder sb = new StringBuilder(s);
        int n = groups.size();

        if (n == 1) { // 그룹 1개면 가운데 일부만 마스킹
            int a = groups.get(0)[0], b = groups.get(0)[1];
            int len = b - a;
            int keep = Math.max(2, len / 4); // 앞/뒤 최소 2자리 유지
            for (int i = a + keep; i < b - keep; i++) sb.setCharAt(i, '*');
            return sb.toString();
        }

        // n >= 2 : 가운데 그룹(1..n-2) 전부 마스킹. n==2면 두 번째만 마스킹
        int startIdx = 1, endIdx = (n >= 3 ? n - 2 : 1);
        for (int gi = startIdx; gi <= endIdx; gi++) {
            int a = groups.get(gi)[0], b = groups.get(gi)[1];
            for (int i = a; i < b; i++) {
                if (Character.isDigit(sb.charAt(i))) sb.setCharAt(i, '*');
            }
        }
        return sb.toString();
    }

    /** 카드 번호를 4-4-4-4로 재그룹하고 2/3번째 그룹을 마스킹 */
    public static String maskCard(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < 12) { // 너무 짧으면 일반 마스킹
            return maskMiddleGroups(raw);
        }
        // 16~19자리까지 커버: 앞4 / 중간8 / 마지막4만 사용해 보기 좋게 표시
        String first4 = digits.substring(0, 4);
        String last4  = digits.substring(digits.length() - 4);
        return first4 + "-****-****-" + last4;
    }

    /** 계좌번호는 구분자가 있으면 가운데 그룹 마스킹, 없으면 일반 마스킹 */
    public static String maskAccount(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        if (raw.contains("-") || raw.contains(" ")) {
            return maskMiddleGroups(raw);
        }
        // 구분자가 없을 때는 통일된 규칙이 없으니 일반 마스킹
        return maskMiddleGroups(raw);
    }
}
