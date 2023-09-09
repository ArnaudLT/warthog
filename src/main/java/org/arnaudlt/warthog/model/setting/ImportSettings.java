package org.arnaudlt.warthog.model.setting;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

public interface ImportSettings {


    @Slf4j
    class ImportSettingsDeserializerAdapter implements JsonSerializer<ImportSettings>, JsonDeserializer<ImportSettings> {

        private static final String TYPE = "type";
        private static final String DATA = "data";

        private static final String LOCAL_DIRECTORY = "local_directory";

        private static final String DATABASE_TABLE = "database_table";


        @Override
        public ImportSettings deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

            ImportSettings importSettings;

            try {
                final JsonObject wrapper = (JsonObject) jsonElement;
                final JsonElement typeName = get(wrapper, TYPE);
                final JsonElement data = get(wrapper, DATA);
                final Type actualType = typeForName(typeName);
                importSettings = jsonDeserializationContext.deserialize(data, actualType);
            } catch (Exception e) {
                log.error("Unable to deserialize import settings {}", jsonElement);
                importSettings = null;
            }
            return importSettings;
        }

        private JsonElement get(JsonObject wrapper, String memberName) {

            return wrapper.get(memberName);
        }

        private Type typeForName(JsonElement typeElem) {

            String typeStringRepresentation = typeElem.getAsString();
            return switch (typeStringRepresentation) {
                case LOCAL_DIRECTORY -> ImportDirectorySettings.class;
                case DATABASE_TABLE -> ImportDatabaseTableSettings.class;
                default -> {
                    log.error("Unable to determine type for {}", typeStringRepresentation);
                    yield null;
                }
            };
        }

        private String nameForType(Type type) {

            final String name;
            if (type ==  ImportDirectorySettings.class) {
                name = LOCAL_DIRECTORY;
            } else if (type == ImportDatabaseTableSettings.class) {
                name = DATABASE_TABLE;
            } else {
                log.error("Unable to determine string for {}", type);
                name = "unknown";
            }
            return name;
        }

        @Override
        public JsonElement serialize(ImportSettings importSettings, Type type, JsonSerializationContext jsonSerializationContext) {

            final JsonObject wrapper = new JsonObject();
            wrapper.addProperty(TYPE, nameForType(importSettings.getClass()));
            wrapper.add(DATA, jsonSerializationContext.serialize(importSettings));
            return wrapper;
        }
    }
}
