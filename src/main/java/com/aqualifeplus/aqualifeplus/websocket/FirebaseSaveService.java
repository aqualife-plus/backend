package com.aqualifeplus.aqualifeplus.websocket;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.FishbowlsDTO;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseSaveService {
    private static final int ADAY = 60 * 60 * 24;
    private final RedisTemplate<String, String> redisTemplate;
    private final DatabaseReference database
            = FirebaseDatabase.getInstance().getReference();

    public void createFishbowl(long userId, String fishbowlId, FishbowlsDTO fishbowlsDTO) {
        DatabaseReference userRef
                = FirebaseDatabase.getInstance().getReference(String.valueOf(userId));
        userRef.child(fishbowlId)
                .updateChildrenAsync(FishbowlsDTO.convertDTOToMap(fishbowlsDTO));
        redisTemplate.opsForValue().set(
                "fishbowl id : " + userId,
                fishbowlId,
                ADAY,
                TimeUnit.MILLISECONDS);
    }

    public void updateName(long userId, String fishbowlId, String name) {
        DatabaseReference userRef
                = FirebaseDatabase.getInstance().getReference(String.valueOf(userId)).child(fishbowlId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        userRef.updateChildren(updates, (error, databaseReference) -> {
            if (error != null) {
                throw new CustomException(ErrorCode.FAIL_UPDATE_NAME);
            }
        });
    }
}