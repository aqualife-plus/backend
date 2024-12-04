package com.aqualifeplus.aqualifeplus.websocket;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.FishbowlDTO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseSaveService {
    public void createFishbowl(long userId, String fishbowlId, FishbowlDTO fishbowlDTO) {
        DatabaseReference userRef =
                FirebaseDatabase.getInstance().getReference(String.valueOf(userId));
        userRef.child(fishbowlId)
                .updateChildrenAsync(FishbowlDTO.convertDTOToMap(fishbowlDTO));
    }

    public void deleteFishbowlWithName(long userId, String targetName) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(String.valueOf(userId));

        // 로그 추가: userId 확인
        log.info("Starting delete process for userId: " + userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 로그 추가: 최상위 데이터 스냅샷 확인
                log.info("DataSnapshot for userId: " + userId + " -> " + dataSnapshot.getValue());

                for (DataSnapshot uuidSnapshot : dataSnapshot.getChildren()) {
                    log.info("Inspecting UUID: " + uuidSnapshot.getKey());

                    boolean shouldDelete = true;

                    for (DataSnapshot fishbowlIdSnapshot : uuidSnapshot.getChildren()) {
                        log.info("Inspecting fishbowl: " + fishbowlIdSnapshot.getKey());

                        if (!fishbowlIdSnapshot.getKey().equals("name")) {
                            continue;
                        }

                        String name = fishbowlIdSnapshot.getValue(String.class);
                        log.info("Fishbowl name: " + name);

                        // 조건에 맞지 않는 이름이 존재하면 삭제 중단
                        if (!targetName.equals(name)) {
                            log.info("Name does not match targetName. Skipping UUID: " + uuidSnapshot.getKey());
                            shouldDelete = false;
                            break;
                        }
                        break;
                    }

                    if (shouldDelete) {
                        // 조건에 맞는 UUID 노드 전체 삭제
                        log.info("Deleting UUID: " + uuidSnapshot.getKey());
                        uuidSnapshot.getRef().removeValue((error, ref) -> {
                            if (error != null) {
                                System.err.println("Failed to delete UUID node: " + error.getMessage());
                            } else {
                                log.info("Deleted UUID node successfully: " + ref.getKey());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Query cancelled: " + databaseError.getMessage());
            }
        });
    }


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
}