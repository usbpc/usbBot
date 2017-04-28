package config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigObject {
    File configFile;
    FileReader reader;
    FileWriter writer;
    Map<String, JsonObject> boundObjects = new HashMap<>();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public ConfigObject(File configFile) {
        this.configFile = configFile;

        try {
            reader = new FileReader(configFile);
            writer = new FileWriter(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigObject bindToProperty(String name) {
        JsonObject file = gson.fromJson(reader, JsonObject.class);
        JsonArray array = file.getAsJsonArray(name);

        array.forEach(x -> boundObjects.put(x.getAsJsonObject().get("name").getAsString(), x.getAsJsonObject()));

        return this;
    }

    public <T> T getObjectbyName(String name, Class<T> typeOfT) {
        if (!boundObjects.containsKey(name)) return null;
        return gson.fromJson(boundObjects.get(name), typeOfT);
    }

    public void closeConnections() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
