package com.aqualifeplus.aqualifeplus.common.deserializer;

import com.aqualifeplus.aqualifeplus.common.exception.CustomDeserializeException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String key = p.currentName();
        String value = p.getText(); // 입력값을 문자열로 가져옴

        if (value == null || value.trim().isEmpty()) {
            throw new CustomDeserializeException(key, "설정할 시간를 포함해야 합니다.");
        }

        // 문자열이 숫자로만 구성되어 있는지 검증
        if (!value.matches("^\\d{2}:\\d{2}$")) {
            throw new CustomDeserializeException(key, "HH:mm 형식이여야 합니다.");
        }

        String[] strArr = value.split(":");
        int hour = Integer.parseInt(strArr[0]);
        int minute = Integer.parseInt(strArr[1]);
        if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60) {
            throw new CustomDeserializeException(key, "시간이 올바르지 않습니다.");
        }


        return LocalTime.parse(value);
    }
}