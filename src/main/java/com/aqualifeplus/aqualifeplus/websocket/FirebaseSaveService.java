package com.aqualifeplus.aqualifeplus.websocket;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FirebaseSaveService {
    private DatabaseReference database;

    public FirebaseSaveService() {
        database = FirebaseDatabase.getInstance().getReference();
    }


    public void saveData(String key1, String key2, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key2, value);
        database.child(key1).updateChildrenAsync(data);
    }
}