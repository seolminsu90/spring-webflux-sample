package com.webflux.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.RedisSerializationContextBuilder;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.webflux.model.Test;

@Configuration
public class RedisConfigurer {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(2)).shutdownTimeout(Duration.ZERO).build();

        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379), clientConfig);
    }

    @Bean
    public ReactiveRedisTemplate<String, Test> testRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {

        Jackson2JsonRedisSerializer<Test> serializer = new Jackson2JsonRedisSerializer<>(Test.class);
        RedisSerializationContextBuilder<String, Test> builder = RedisSerializationContext
                .newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, Test> serializationContext = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
