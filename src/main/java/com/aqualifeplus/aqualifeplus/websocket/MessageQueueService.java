package com.aqualifeplus.aqualifeplus.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.MessageProperties;

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

    @RabbitListener(queues = "websocketMessageQueue")
    public void receiveMessageFromQueue(String message) throws IOException {
        System.out.println("Received message from queue: " + message);

        try {
            // ObjectMapper를 사용하여 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();

            // 메시지를 ':'로 나눈 후 JSON 부분만 추출
            String[] parts = message.split(":", 2);
            String sessionPath = parts[0]; // sessionId 등 경로 정보
            String jsonPayload = parts[1]; // JSON 데이터
            System.out.println(jsonPayload);

            Pattern pattern = Pattern.compile("Type: (.*?), Message: (.*)");
            Matcher matcher = pattern.matcher(jsonPayload);

            // Firebase 저장
            String[] sessionIdArr = sessionPath.split("/");
            String channel = sessionIdArr[2];

            if (matcher.find()) {
                String type = matcher.group(1); // "업데이트"
                String messages = matcher.group(2); // "ㄱㄱ"
                firebaseSaveService.saveData(channel, type, messages);
                System.out.println("Data saved to Firebase successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            throw new RuntimeException("Failed to save data to Firebase", e);
        }
    }
}


