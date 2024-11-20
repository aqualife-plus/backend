package com.aqualifeplus.aqualifeplus.websocket;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class FirebaseRealTimeService {

    private DatabaseReference database;
    private final ChatHandler webSocketHandler;

    @Autowired
    public FirebaseRealTimeService(@Lazy ChatHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
        database = FirebaseDatabase.getInstance().getReference();

        // Firebase에 데이터 변경 시 리스너 추가
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                System.out.println("add");
//                traverseDataSnapshot(snapshot, new StringBuilder());
                traverseData(snapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                System.out.println("change");
//                traverseDataSnapshot(snapshot, new StringBuilder());
                traverseData(snapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                // 필요한 경우 삭제 처리 로직 추가
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                // 필요한 경우 이동 처리 로직 추가
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // 오류 처리 로직 추가
            }
        });
    }

    private void traverseData(DataSnapshot snapshot) {
        Map<String, Object> allData = traverseDataSnapshot(snapshot); // 모든 데이터를 수집
        System.out.println("Collected data: " + allData);

        System.out.println(allData);
        System.out.println(allData.keySet().stream().toList().get(0));

        try {
            webSocketHandler.broadcastMessageToClients(allData,
                    allData.keySet().stream().toList().get(0).split(" ")[0]);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private Map<String, Object> traverseDataSnapshot(DataSnapshot snapshot) {
        Map<String, Object> allData = new HashMap<>();
        allData.put("keys", snapshot.getKey().trim());
        collectAllData(snapshot, "", allData); // 루트 노드부터 탐색 시작
        return allData;
    }

    private void collectAllData(DataSnapshot snapshot, String currentPath, Map<String, Object> allData) {
        String path = currentPath.isEmpty() ? snapshot.getKey() : currentPath + "/" + snapshot.getKey();

        if (snapshot.hasChildren()) {
            for (DataSnapshot child : snapshot.getChildren()) {
                collectAllData(child, path, allData); // 자식 노드 재귀 탐색
            }
        } else {
            allData.put(snapshot.getKey(), snapshot.getValue()); // 최종 데이터를 경로와 함께 저장
        }
    }
}

