package com.aqualifeplus.aqualifeplus.config;

import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@NoLogging
public class RabbitMQConfig {

    // 주 큐 생성 및 DLX 속성 설정
    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable("websocketMessageQueue")
                .withArgument("x-dead-letter-exchange", "dlxExchange") // DLX 설정
                .withArgument("x-dead-letter-routing-key", "dlxRoutingKey") // DLX 라우팅 키 설정
                .build();
    }

    // RabbitAdmin 빈 생성
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // 재시도 설정이 포함된 RabbitListenerContainerFactory 빈
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    // 재시도 인터셉터 설정
    public RetryOperationsInterceptor retryInterceptor() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 최대 재시도 횟수 설정 (예: 3회)
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 재시도 간격 설정 (예: 2초 간격)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000);  // 2000ms = 2초
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // RetryInterceptorBuilder로 RetryOperationsInterceptor 생성
        return RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    // DLX용 Exchange 생성
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlxExchange");
    }

    // DLX 큐 생성
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("deadLetterQueue", true); // 메시지 지속성을 위해 durable=true 설정
    }

    // DLX 큐와 DLX Exchange 바인딩
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("dlxRoutingKey");
    }

}