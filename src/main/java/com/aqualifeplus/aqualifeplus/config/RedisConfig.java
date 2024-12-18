package com.aqualifeplus.aqualifeplus.config;

import com.aqualifeplus.aqualifeplus.common.redis.ExpiredEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis의 전반적인 설정을 다루는 클래스
 * redisTemplateForTokens : token관련 메소드
 * redisTemplateForFishbowlSettings : 예약관련 메소드
 * */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplateForTokens() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory(0)); // DB 0번
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplateForFishbowlSettings() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory(1)); // DB 1번
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            ExpiredEventListener expiredEventListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 키 만료 이벤트 리스너 추가
        container.addMessageListener(
                expiredEventListener,
                new PatternTopic("__keyevent@1__:expired"));
        return container;
    }

    private LettuceConnectionFactory redisConnectionFactory(int databaseIndex) {
        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration();
        redisConfig.setHostName("127.0.0.1");
        redisConfig.setPort(6379);
        redisConfig.setDatabase(databaseIndex); // 사용할 DB 설정

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
        factory.afterPropertiesSet(); // 초기화 추가
        return factory;
    }
}

