package com.yellowpineapple.wakup.sdk.communications.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.yellowpineapple.wakup.sdk.models.Category;

import java.lang.reflect.Type;

public class CategorySerializer implements JsonSerializer<Category>, JsonDeserializer<Category> {

    @Override
    public JsonElement serialize(Category category, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(category.getIdentifier());
    }

    @Override
    public Category deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Category.fromIdentifier(jsonElement.getAsString());
    }
}
