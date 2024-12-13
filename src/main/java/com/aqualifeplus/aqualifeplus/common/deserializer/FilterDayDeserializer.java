package com.aqualifeplus.aqualifeplus.common.deserializer;

import com.aqualifeplus.aqualifeplus.common.exception.CustomDeserializeException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class FilterDayDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String key = p.currentName();
        String value = p.getText(); // 입력값을 문자열로 가져옴

        // 문자열이 숫자로만 구성되어 있는지 검증
        if (!value.matches("^[01]+$")) {
            throw new CustomDeserializeException(key, "0과 1로만 구성되어야 합니다.");
        }

        if (value.length() != 7) {
            throw new CustomDeserializeException(key, "7자리여야 합니다.");
        }


        return value;
    }
}