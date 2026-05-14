package com.ops.zen.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @Author xiaoyingnan
 * @Date 2020/07/01 13:41
 * @Description
 */
public class Number2BooleanSerializer extends JsonSerializer<Number> {
    @Override
    public void serialize(Number n, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeBoolean(n.longValue() == 1);
    }
}
