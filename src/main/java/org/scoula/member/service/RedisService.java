package org.scoula.member.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redis;

    public void saveRefreshToken(String email, String token) {
        redis.opsForValue().set("RT:" + email, token, 7, TimeUnit.DAYS);
    }

    public String getRefreshToken(String email){
        return redis.opsForValue().get("RT:" + email);
    }

    public void deleteRefreshToken(String email){
        redis.delete("RT:" + email);
    }


    // 이메일 인증시에 사용하려고 했던 것들
    public void saveVerifyToken(String email, String token) {
        redis.opsForValue().set("VT:" + email, token, 10, TimeUnit.MINUTES); // 인증 메일 유효시간 10분
    }

    public String getVerifyToken(String email) {
        return redis.opsForValue().get("VT:" + email);
    }

    public void deleteVerifyToken(String email) {
        redis.delete("VT:" + email);
    }

}
