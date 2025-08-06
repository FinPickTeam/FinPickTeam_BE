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
}