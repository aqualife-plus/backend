package com.aqualifeplus.aqualifeplus.websocket;


import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.FAIL_FIREBASE_SAVE;
import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.FAIL_UPDATE_NAME;
import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.RABBITMQ_BASIC_REJECT_ERROR;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageQueueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Lazy
    private FirebaseSaveService firebaseSaveService;

    // 메시지를 RabbitMQ 큐에 송신
    public void sendMessageToQueue(String message) {
        System.out.println("push queue");
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 메시지 지속성 설정
        Message m = MessageBuilder.withBody(message.getBytes())
                .andProperties(messageProperties)
                .build();

        rabbitTemplate.convertAndSend("websocketMessageQueue", m);
        System.out.println("Sent persistent message to RabbitMQ: " + message);
    }

    /**
     * RabbitMQ 큐에서 메시지 수신 및 처리
     */
    @RabbitListener(queues = "websocketMessageQueue")
    public void receiveMessageFromQueue(String message) {
        log.info("Received message from queue: {}", message);
        processMessage(message);
    }

    public void processMessage(String message) {
        log.info("Received message from queue: " + message);

        try {
            // 메시지를 ':'로 나눈 후 JSON 부분만 추출
            String[] parts = message.split("<>", 3);
            String sessionPath = parts[1]; // sessionId 등 경로 정보
            String jsonPayload = parts[2]; // JSON 데이터

            Pattern pattern = Pattern.compile("Type: (.*?), Message: (.*)");
            Matcher matcher = pattern.matcher(jsonPayload);

            // Firebase 저장
            String[] sessionIdArr = sessionPath.split("/");

            if (matcher.find()) {
                String type = matcher.group(1); // "업데이트"
                String messages = matcher.group(2); // "ㄱㄱ"
                firebaseSaveService.updateOnOff(
                        sessionIdArr[0], sessionIdArr[1],
                        type, Boolean.parseBoolean(messages));
                log.info("Data saved to Firebase successfully.(구현x)");
            }
        } catch (Exception e) {
            log.error("Error processing message: " + e.getMessage());
            throw new CustomException(FAIL_FIREBASE_SAVE);
        }
    }
}


