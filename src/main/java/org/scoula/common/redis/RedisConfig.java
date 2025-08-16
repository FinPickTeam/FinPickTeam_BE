package org.scoula.common.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
@PropertySource("classpath:application.properties")
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.password:}")
    private String password;

    /**
     * Redis 연결 팩토리 생성
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (!password.isBlank()) {
            config.setPassword(RedisPassword.of(password));
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * 문자열 기반 RedisTemplate
     */
    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    // 선택: 블랙리스트 용 별칭 템플릿(같은 팩토리)
    @Bean(name = "blackListRedisTemplate")
    public StringRedisTemplate blackListRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * 객체 저장용 RedisTemplate (JSON 직렬화 기반)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 연결 설정
        template.setConnectionFactory(factory);

        // 키는 String, 값은 JSON 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 해시도 같은 방식 적용
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
