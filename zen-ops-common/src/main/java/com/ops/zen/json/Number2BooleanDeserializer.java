package com.ops.zen.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @Author xiaoyingnan
 * @Date 2020/07/01 13:41
 * @Description
 */
public class Number2BooleanDeserializer extends JsonDeserializer<Number> {

    @Override
    public Number deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        boolean b = jsonParser.getBooleanValue();
        return b ? Byte.parseByte("1"): Byte.parseByte("0");
    }

}
