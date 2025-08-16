package org.scoula.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.scoula.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {

    private final StringRedisTemplate redis;     // @Primary 로 주입
    private final StringRedisTemplate blackList; // 이름 지정 주입
    private final JwtUtil jwtUtil;

    public RedisService(
            StringRedisTemplate stringRedisTemplate, // ✅ @Primary
            @Qualifier("blackListRedisTemplate") StringRedisTemplate blackListRedisTemplate,
            JwtUtil jwtUtil
    ) {
        this.redis = stringRedisTemplate;
        this.blackList = blackListRedisTemplate;
        this.jwtUtil = jwtUtil;
    }

    // ===== 공용 유틸 =====
    public void setValueWithExpire(String key, String value, long ttlSeconds) {
        redis.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    public void delete(String key) {
        redis.delete(key);
    }

    /**
     * INCR. 새 키(값 1)가 만들어졌다면 TTL 부여
     */
    public Long incr(String key, long ttlSecondsIfNew) {
        Long v = redis.opsForValue().increment(key);
        if (v != null && v == 1L) {
            redis.expire(key, ttlSecondsIfNew, TimeUnit.SECONDS);
        }
        return v;
    }

    // ===== RT 저장/조회/삭제 =====
    public void saveRefreshToken(Long userId, String token) {
        redis.opsForValue().set(
                RedisKeyPrefix.REFRESH_TOKEN + userId,
                token,
                RedisKeyPrefix.REFRESH_TOKEN_TTL,
                RedisKeyPrefix.REFRESH_TOKEN_UNIT
        );
        log.debug("Redis RT 저장: {} -> {}", userId, token);
    }

    public String getRefreshToken(Long userId){
        return redis.opsForValue().get(RedisKeyPrefix.REFRESH_TOKEN + userId);
    }

    public void deleteRefreshToken(Long userId){
        redis.delete(RedisKeyPrefix.REFRESH_TOKEN + userId);
    }

    // ===== AT 블랙리스트 =====
    public void blacklistAccessToken(String accessToken) {
        Date expiration = jwtUtil.getExpirationDateFromToken(accessToken);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        if (remainingTime > 0) {
            blackList.opsForValue().set(
                    RedisKeyPrefix.BLACKLIST_TOKEN + accessToken,
                    accessToken,
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = RedisKeyPrefix.BLACKLIST_TOKEN + token;
        return Boolean.TRUE.equals(blackList.hasKey(key));
    }

    // ===== (기존) 단일 VERIFY_TOKEN도 남겨둠 – 필요시 사용 =====
    public void saveVerifyToken(String email, String token) {
        redis.opsForValue().set(
                RedisKeyPrefix.VERIFY_TOKEN + email,
                token,
                RedisKeyPrefix.VERIFY_TOKEN_TTL,
                RedisKeyPrefix.VERIFY_TOKEN_UNIT
        );
    }

    public String getVerifyToken(String email) {
        return redis.opsForValue().get(RedisKeyPrefix.VERIFY_TOKEN + email);
    }

    public void deleteVerifyToken(String email) {
        redis.delete(RedisKeyPrefix.VERIFY_TOKEN + email);
    }

    // ===== 이메일 인증 전용 키 헬퍼 =====
    public String evCodeKey(String email){ return RedisKeyPrefix.EV_CODE + email; }
    public String evTriesKey(String email){ return RedisKeyPrefix.EV_TRIES + email; }
    public String evCooldownKey(String email){ return RedisKeyPrefix.EV_COOLDOWN + email; }
    public String evOkKey(String email){ return RedisKeyPrefix.EV_OK + email; }
    public String evDailyKey(String email){ return RedisKeyPrefix.EV_DAILY + email; }

    // ===== 이메일 인증 전용 편의 메서드 =====
    public void saveEvCode(String email, String code) {
        setValueWithExpire(evCodeKey(email), code, RedisKeyPrefix.EV_CODE_TTL_SEC);
        setValueWithExpire(evTriesKey(email), "0", RedisKeyPrefix.EV_TRIES_TTL_SEC);
    }

    public String getEvCode(String email) {
        return get(evCodeKey(email));
    }

    public void incEvTries(String email) {
        int now = getEvTries(email) + 1;
        setValueWithExpire(evTriesKey(email), String.valueOf(now), RedisKeyPrefix.EV_TRIES_TTL_SEC);
    }

    public int getEvTries(String email) {
        String v = get(evTriesKey(email));
        return (v == null) ? 0 : Integer.parseInt(v);
    }

    public void setEvCooldown(String email) {
        setValueWithExpire(evCooldownKey(email), "1", RedisKeyPrefix.EV_COOLDOWN_TTL_SEC);
    }

    public boolean hasEvCooldown(String email) {
        return get(evCooldownKey(email)) != null;
    }

    public void setEvOk(String email) {
        setValueWithExpire(evOkKey(email), "1", RedisKeyPrefix.EV_OK_TTL_SEC);
    }

    public boolean hasEvOk(String email) {
        return get(evOkKey(email)) != null;
    }

    public void clearEvCodeState(String email) {
        delete(evCodeKey(email));
        delete(evTriesKey(email));
    }

    /**
     * 일일 전송 횟수 증가. 새 키면 TTL 부여
     */
    public long incrDailyAndSetExpireIfNew(String email) {
        return incr(evDailyKey(email), RedisKeyPrefix.EV_DAILY_TTL_SEC);
    }
}
