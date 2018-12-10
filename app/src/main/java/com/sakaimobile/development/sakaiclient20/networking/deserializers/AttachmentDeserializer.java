package com.sakaimobile.development.sakaiclient20.networking.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Attachment;

import java.lang.reflect.Type;

public class AttachmentDeserializer implements JsonDeserializer<Attachment> {
    @Override
    public Attachment deserialize(JsonElement raw,
                                  Type typeOfT,
                                  JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject json = raw.getAsJsonObject();

        String url = json.get("url").getAsString();
        Attachment attachment = new Attachment(url);
        attachment.name = json.get("name").getAsString();

        return attachment;
    }
}