package com.aqualifeplus.aqualifeplus.firebase.repository;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.firebase.entity.FishbowlData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FirebaseRealTimeRepository {
    public void createFishbowl(long userId, String fishbowlId, FishbowlData fishbowlData) {
        DatabaseReference userRef =
                FirebaseDatabase.getInstance().getReference(String.valueOf(userId));
        userRef.child(fishbowlId)
                .updateChildrenAsync(FishbowlData.convertDTOToMap(fishbowlData));
    }

    //이건 옳기는 게 좋을 듯?
    public void updateName(long userId, String fishbowlId, String name) {
        DatabaseReference userRef =
                FirebaseDatabase.getInstance()
                        .getReference(String.valueOf(userId))
                        .child(fishbowlId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        userRef.updateChildren(updates, (error, databaseReference) -> {
            if (error != null) {
                throw new CustomException(ErrorCode.FAIL_UPDATE_NAME);
            }
        });
    }

    public void updateOnOff(String userId, String fishbowlId, String type, boolean onOff) {
        if (!(type.equals("co2State") || type.equals("lightState"))) {
            throw new CustomException(ErrorCode.NOT_MATCH_UPDATE_COLUMN);
        }

        DatabaseReference userRef =
                FirebaseDatabase.getInstance()
                        .getReference(userId)
                        .child(fishbowlId)
                        .child("now")
                        .child(type);

        userRef.setValue(onOff, (error, databaseReference) -> {
            if (error != null && type.equals("co2State")) {
                throw new CustomException(ErrorCode.FAIL_UPDATE_NOW_CO2);
            } else if (error != null){
                throw new CustomException(ErrorCode.FAIL_UPDATE_NOW_LIGHT);
            }
        });
    }
}