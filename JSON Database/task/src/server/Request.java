package server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Request {
    private final String type;
    private final JsonElement key;
    private final JsonElement value;

    public Request(String type, JsonArray key, JsonElement message) {
        this.type = type;
        this.key = key;
        this.value = message;
    }

    public String getType() {
        return type != null ? type : "";
    }

    public List<String> getKeys() {
        if (key.toString().startsWith("[") && key.toString().endsWith("]")) {
            return new ArrayList<>(Arrays.asList(key.toString()
                    .substring(2, key.toString().length() - 2)
                    .replace("\"", "")
                    .split(",")));
        } else {
            return List.of(key.toString().substring(1, key.toString().length() - 1));
        }
    }

    public JsonElement getValue() {
        return value;
    }
}
