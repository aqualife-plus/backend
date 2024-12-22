package com.aqualifeplus.aqualifeplus.firebase.repository;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.firebase.entity.FishbowlData;
import com.aqualifeplus.aqualifeplus.websocket.DlxHandler;
import com.aqualifeplus.aqualifeplus.websocket.DlxMessageProcessor;
import com.aqualifeplus.aqualifeplus.websocket.MessageQueueService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class FirebaseRealTimeRepository {
    private final DlxHandler dlxHandler;
    private final FirebaseDatabase databaseReference;

    FirebaseRealTimeRepository(DlxHandler dlxHandler) {
        this.dlxHandler = dlxHandler;
        databaseReference = FirebaseDatabase.getInstance();
        databaseReference.setPersistenceEnabled(false);
    }

    public void updateOnOff(String userId, String fishbowlId, String type, boolean onOff) {
        if (!(type.equals("co2State") || type.equals("lightState"))) {
            log.error("error check");
            throw new CustomException(ErrorCode.NOT_MATCH_UPDATE_COLUMN);
        }

        DatabaseReference userRef =
                databaseReference
                        .getReference(userId)
                        .child(fishbowlId)
                        .child("now")
                        .child(type);

        userRef.setValue(onOff, (error, databaseReference) -> {
            if (error != null) {
                log.error("{} update 중 에러", type);

                String path = userId + "/" + fishbowlId;
                String formattedMessage = "Type: " + type + ", Message: " + onOff;

                dlxHandler.sendMessageToDlx("retry" + "<>" + path + "<>" + formattedMessage);
            }
        });
    }

    public void updateFilter(String userId, String fishbowlId) {
        DatabaseReference userRef =
                databaseReference
                        .getReference(userId)
                        .child(fishbowlId)
                        .child("filterData")
                        .child("filterOnOff");

        userRef.setValue(true, (error, databaseReference) -> {
            if (error != null) {
                log.error("filter update 중 에러");

                String path = userId + "/" + fishbowlId;
                String formattedMessage = "Type: " + "filter" + ", Message: " + "on";

                dlxHandler.sendMessageToDlx("retry" + "<>" + path + "<>" + formattedMessage);
            }
        });
    }
}