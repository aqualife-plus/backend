package com.aqualifeplus.aqualifeplus.websocket;


import com.aqualifeplus.aqualifeplus.common.aop.NoLogging;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoLogging
public class DlxMessageProcessor {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    // DLX 큐에서 메시지 수신 후 원래 큐로 다시 전송
    @RabbitListener(queues = "deadLetterQueue")
    public void processDlxMessage(Message message) {
        String messageContent = new String(message.getBody());
        System.out.println("Processing DLX message: " + messageContent);

        // Firebase 서버가 복구되었는지 확인하는 로직을 추가할 수 있음
        boolean isFirebaseAvailable = checkFirebaseStatus();
        if (isFirebaseAvailable) {
            // 메시지를 원래 큐로 전송
            rabbitTemplate.convertAndSend("websocketMessageQueue", message);
            System.out.println("Resent message to websocketMessageQueue: " + messageContent);
        } else {
            System.out.println("Firebase is not available. Message remains in DLX.");
        }
    }

    // Firebase 상태를 확인하는 가상의 메서드
    private boolean checkFirebaseStatus() {
        // 실제 Firebase 상태 확인 로직을 여기에 추가 (예: HTTP 핑 테스트)
        return true; // 이 부분을 실제 Firebase 연결 상태 확인 로직으로 교체하세요.
    }
}
