package com.aqualifeplus.aqualifeplus.websocket;


import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.FAIL_FIREBASE_SAVE;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.firebase.repository.FirebaseRealTimeRepository;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageQueueService {
    private final RabbitTemplate rabbitTemplate;

    @Lazy
    private final FirebaseRealTimeRepository firebaseRealTimeRepository;

    /**
     * 메시지를 RabbitMQ 큐에 송신
     * @param message   큐에 송신할 메세지
     * */
    public void sendMessageToQueue(String message) {
        log.info("push queue");
        MessageProperties messageProperties = new MessageProperties();
        // 메시지 지속성 설정
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        Message m = MessageBuilder.withBody(message.getBytes())
                .andProperties(messageProperties)
                .build();

        // 메시지가 websocketMessageQueue 큐에 전달
        rabbitTemplate.convertAndSend("websocketMessageQueue", m);
        log.info("Sent persistent message to RabbitMQ: " + message);
    }

    /**
     * websocketMessageQueue에서 메시지 수신 및 처리
     * @param message   큐에 수신된 메세지
     */
    @RabbitListener(queues = "websocketMessageQueue")
    public void receiveMessageFromQueue(String message) {
        log.info("Received message from queue: {}", message);
        processMessage(message);
    }

    /**
     * message를 정규화로 필터링 & firebase로 전송
     * */
    public void processMessage(String message) {
        log.info("process message " + message);

        try {
            // 메시지를 '<>'로 나눈 후 JSON 부분만 추출
            String[] parts = message.split("<>", 3);
            //session 정보
            String sessionData = parts[0];
            // 송신할 경로 정보
            String path = parts[1];
            // JSON 데이터
            String jsonPayload = parts[2];

            Pattern pattern = Pattern.compile("Type: (.*?), Message: (.*)");
            Matcher matcher = pattern.matcher(jsonPayload);

            // Firebase 경로 str -> 배열
            String[] pathArr = path.split("/");

            if (matcher.find()) {
                String type = matcher.group(1); // key
                String messages = matcher.group(2); // value

                switch (type) {
                    case "co2State", "lightState" ->
                            firebaseRealTimeRepository.updateOnOff(
                                    pathArr[0],
                                    pathArr[1],
                                    type,
                                    Boolean.parseBoolean(messages));
                    case "filter" ->
                        firebaseRealTimeRepository.updateFilter(
                                pathArr[0],
                                pathArr[1]);
                }

                log.info("Data saved to Firebase successfully.");
            }
        } catch (Exception e) {
            log.error("Error processing message: " + e.getMessage());
            throw new CustomException(FAIL_FIREBASE_SAVE);
        }
    }
}


