package config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConfigObject {
    String boundTo = null;
    File configFile;
    FileReader reader;
    FileWriter writer;
    JsonObject file;
    Map<String, JsonObject> boundObjects = new HashMap<>();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //TODO make config file writable!
    public ConfigObject(File configFile) {
        this.configFile = configFile;

        try {
            reader = new FileReader(configFile);
            //TODO this produces NullPointerException if the file to read is empty or the property dosen't exist
            file = new Gson().fromJson(reader, JsonObject.class);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigObject bindToProperty(String name) {
        boundTo = name;
        JsonArray array = file.getAsJsonArray(name);

        array.forEach(x -> boundObjects.put(x.getAsJsonObject().get("name").getAsString(), x.getAsJsonObject()));

        return this;
    }
    public void putObject(ConfigElement object) {
        boundObjects.put(object.getUUID(), gson.fromJson(gson.toJson(object), JsonObject.class));
    }

    public <T extends ConfigElement> T getObjectbyName(String name, Type typeOfT) {
        if (!boundObjects.containsKey(name)) return null;
        return gson.fromJson(boundObjects.get(name), typeOfT);
    }

    public void closeConnections() {
        try {
            file.remove(boundTo);
            JsonArray array = new JsonArray();
            boundObjects.values().forEach(array::add);
            file.add(boundTo, array);
            writer = new FileWriter(configFile);
            writer.write(gson.toJson(file));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
