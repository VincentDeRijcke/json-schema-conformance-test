/*
 * Copyright 2019 the original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.leadpony.jsonschema.test.base;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

/**
 * @author leadpony
 */
public class Fixture {

    private final String name;
    private final String schema;
    private final String data;
    private final String description;
    private final boolean valid;

    private Fixture(String name, String schema, String data, String description, boolean valid) {
        this.name = name;
        this.schema = schema;
        this.data = data;
        this.description = description;
        this.valid = valid;
    }

    String getSchema() {
        return schema;
    }

    String getData() {
        return data;
    }

    boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return name + ": " + description;
    }

    static Stream<Fixture> load(Path path) {
        String name = getName(path);
        return readArray(path).stream()
            .map(JsonValue::asJsonObject)
            .flatMap(object->{
                String schema = object.get("schema").toString();
                return object.getJsonArray("tests").stream()
                        .map(JsonValue::asJsonObject)
                        .map(test->{
                            return new Fixture(
                                    name,
                                    schema,
                                    test.get("data").toString(),
                                    test.getString("description"),
                                    test.getBoolean("valid")
                                    );
                        });
            });
    }

    private static String getName(Path path) {
        String name = path.toString().contains("/optional/") ? "Optional " : "";
        name = path.toString().contains("/optional/format/") ? "Format " : name;

        String fileName = path.getFileName().toString();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        return name + fileName;
    }

    private static JsonArray readArray(Path path) {
        try (JsonReader reader = Json.createReader(Files.newInputStream(path))) {
            return reader.readArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
