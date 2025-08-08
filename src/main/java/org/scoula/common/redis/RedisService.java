package org.scoula.common.redis;

import org.scoula.security.util.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redis;
    private final StringRedisTemplate blackList;
    private final JwtUtil jwtUtil;

    public void saveRefreshToken(Long userId, String token) {
        try {
            redis.opsForValue().set(
                    RedisKeyPrefix.REFRESH_TOKEN + userId,
                    token,
                    RedisKeyPrefix.REFRESH_TOKEN_TTL,
                    RedisKeyPrefix.REFRESH_TOKEN_UNIT
            );
            System.out.println("✅ Redis 저장 성공: " + userId);
        } catch (Exception e) {
            System.out.println("❌ Redis 저장 실패: " + e.getMessage());
        }
    }

    public String getRefreshToken(Long userId){
        return redis.opsForValue().get(RedisKeyPrefix.REFRESH_TOKEN + userId);
    }

    public void deleteRefreshToken(Long userId){
        redis.delete(RedisKeyPrefix.REFRESH_TOKEN + userId);
    }

    public void blacklistAccessToken(String accessToken) {

        // 토큰에서 만료 시간을 추출하여 남은 유효 시간을 계산
        Date expiration = jwtUtil.getExpirationDateFromToken(accessToken);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();

        if (remainingTime > 0) {
            // 토큰 자체를 키로 사용하여 블랙리스트에 저장
            blackList.opsForValue().set(
                    RedisKeyPrefix.BLACKLIST_TOKEN + accessToken,
                    accessToken,
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    //블랙리스트에 있는 access token인지 검증
    public boolean isTokenBlacklisted(String token) {
        String key = RedisKeyPrefix.BLACKLIST_TOKEN + token;
        return blackList.hasKey(key);
    }


    // 이메일 인증시에 사용하려고 했던 것들
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

}
