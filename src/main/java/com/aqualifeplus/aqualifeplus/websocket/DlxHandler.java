package com.aqualifeplus.aqualifeplus.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DlxHandler {
    private final RabbitTemplate rabbitTemplate;

    public void sendMessageToDlx(String message) {
        log.info("push dlx queue");

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        Message m = MessageBuilder.withBody(message.getBytes())
                .andProperties(messageProperties)
                .build();

        // 메시지가 dlx 큐에 전달
        rabbitTemplate.convertAndSend("deadLetterQueue", message);
        log.info("Sent persistent message to dlx queue: " + message);
    }
}
