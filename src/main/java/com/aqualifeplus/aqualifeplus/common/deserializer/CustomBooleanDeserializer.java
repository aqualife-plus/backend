package com.aqualifeplus.aqualifeplus.common.deserializer;

import com.aqualifeplus.aqualifeplus.common.exception.CustomDeserializeException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class CustomBooleanDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String key = p.currentName();
        String value = p.getText(); // 입력값을 문자열로 가져옴

        // 문자열이 숫자로만 구성되어 있는지 검증
        if (!value.equals("true") && !value.equals("false") ) {
            throw new CustomDeserializeException(key, "boolean 값이 아닙니다.");
        }

        return Boolean.parseBoolean(value);
    }
}