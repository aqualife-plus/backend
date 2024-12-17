package com.aqualifeplus.aqualifeplus.websocket;


import static com.aqualifeplus.aqualifeplus.common.exception.ErrorCode.NOT_MATCH_NOW_DATA_FORMAT;

import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@NoLogging
@RequiredArgsConstructor
public class DlxMessageProcessor {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final ChatHandler chatHandler;

    // DLX 큐에서 메시지 수신 후 원래 큐로 다시 전송
    @RabbitListener(queues = "deadLetterQueue")
    public void processDlxMessage(Message message) throws IOException {
        String messageContent = new String(message.getBody());
        log.info("Processing DLX message: " + messageContent);

        try {
            // Firebase 상태 확인
            if (!checkFirebaseStatus()) {
                throw new CustomException(ErrorCode.FIREBASE_ERROR);
            }

            // 메시지 유효성 검증
            if (!isValidMessage(messageContent)) {
                log.warn("Invalid message content: " + messageContent);
                // 특정 메시지만 삭제
                throw new CustomException(NOT_MATCH_NOW_DATA_FORMAT);
            }

            // 원래 큐로 재전송
            rabbitTemplate.convertAndSend("websocketMessageQueue", message);
            log.info("Resent message to websocketMessageQueue: " + messageContent);
        } catch (Exception e) {
            log.error("Error processing DLX message: " + e.getMessage());
            // 실패한 메시지는 삭제하거나 재시도
            chatHandler.sendMessage(getSession(messageContent), "send Fail");
            rabbitTemplate.execute(channel -> {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false); // 재시도 하지 않음
                return null;
            });
        }
    }

    private String getSession(String messageContent) {
        return messageContent.split("<>", 3)[0];
    }


    // Firebase 상태를 확인하는 가상의 메서드
    private boolean checkFirebaseStatus() {
        // 실제 Firebase 상태 확인 로직을 여기에 추가 (예: HTTP 핑 테스트)
        return true; // 이 부분을 실제 Firebase 연결 상태 확인 로직으로 교체하세요.
    }
    private boolean isValidMessage(String messageContent) {
        Pattern pattern = Pattern.compile("Type: (.*?), Message: (.*)");
        Matcher matcher = pattern.matcher(messageContent);

        if (matcher.find()) {
            String type = matcher.group(1);
            String messages = matcher.group(2);

            log.info(String.valueOf((type.equals("co2State") || type.equals("lightState"))
                    && (messages.equals("true") || messages.equals("false"))));

            return (type.equals("co2State") || type.equals("lightState"))
                    && (messages.equals("true") || messages.equals("false"));
        }

        return false;
    }
}
