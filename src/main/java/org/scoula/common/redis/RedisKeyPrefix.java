package org.scoula.common.redis;

import java.util.concurrent.TimeUnit;

public class RedisKeyPrefix {
    public static final String REFRESH_TOKEN = "RT:";
    public static final long REFRESH_TOKEN_TTL = 7L; // 7일
    public static final TimeUnit REFRESH_TOKEN_UNIT = TimeUnit.DAYS;

    public static final String VERIFY_TOKEN = "VT:";
    public static final long VERIFY_TOKEN_TTL = 10L; // 10분
    public static final TimeUnit VERIFY_TOKEN_UNIT = TimeUnit.MINUTES;

    public static final String BLACKLIST_TOKEN = "BT:";

    // ===== 이메일 인증 전용 키들 =====
    public static final String EV_CODE      = "EV:code:";      // 6자리 코드
    public static final String EV_TRIES     = "EV:tries:";     // 검증 시도횟수
    public static final String EV_COOLDOWN  = "EV:cooldown:";  // 재전송 쿨타임
    public static final String EV_OK        = "EV:ok:";        // 인증성공 플래그
    public static final String EV_DAILY     = "EV:daily:";     // 일일 전송 횟수

    public static final long EV_CODE_TTL_SEC     = 300;   // 5분
    public static final long EV_TRIES_TTL_SEC    = 300;   // 5분(코드와 동일)
    public static final long EV_COOLDOWN_TTL_SEC = 60;    // 60초
    public static final long EV_OK_TTL_SEC       = 1800;  // 30분(가입까지 허용 창)
    public static final long EV_DAILY_TTL_SEC    = 86400; // 24시간(또는 자정까지 로직)
}