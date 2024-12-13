package com.aqualifeplus.aqualifeplus.common.deserializer;

import com.aqualifeplus.aqualifeplus.common.exception.CustomDeserializeException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class CustomPhDeserializer extends JsonDeserializer<Double> {

    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String key = p.currentName();
        String value = p.getText(); // 입력값을 문자열로 가져옴

        if (value == null || value.trim().isEmpty()) {
            throw new CustomDeserializeException(key, "ph 설정값이 필요합니다.");
        }

        // 문자열이 숫자로만 구성되어 있는지 검증
        if (!value.matches("^[0-9.]+$")) {
            throw new CustomDeserializeException(key, "숫자값이 아닙니다.");
        }

        double doubleValue = Double.parseDouble(value);

        if (0 > doubleValue || doubleValue > 10) {
            throw new CustomDeserializeException(key, "0 ~ 10 사이값이여야 합니다.");
        }


        return doubleValue;
    }
}