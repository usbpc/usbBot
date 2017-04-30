package config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ConfigObject {
    protected static Type mapType = new TypeToken<Map<String, JsonElement>>(){}.getType();
    protected final Map<String, ConfigObject> subObjects = new HashMap<>();
    protected Map<String, JsonElement> objects;
    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected ConfigObject() {
    }

    public ConfigObject(Map<String, JsonElement> objects) {
        this.objects = objects;
    }

    public ConfigObject getPropertyAsSubConfigObject(String name) {
        if (subObjects.containsKey(name)) return subObjects.get(name);
        ConfigObject subObject;
        if (!objects.containsKey(name)) {
            subObject = new ConfigObject(new HashMap<>());
        } else {
            subObject = new ConfigObject(gson.fromJson(objects.get(name), mapType));
        }
        subObjects.put(name, subObject);
        return subObject;
    }

    public void putConfigElement(ConfigElement object) {
        objects.put(object.getUUID(), gson.toJsonTree(object));
    }

    public void removeConfigElement(String name) {
        if (subObjects.containsKey(name)) throw new IllegalArgumentException("The Object you tried to remove is a SubConfigObject");
        objects.remove(name);
    }

    public <T extends ConfigElement> T getObjectByName(String name, Type typeOfT) {
        if (subObjects.containsKey(name)) throw new IllegalArgumentException("The Object you tried to get is already a SubConfigObject!");

        if (!objects.containsKey(name)) return null;
        return gson.fromJson(objects.get(name), typeOfT);
    }

    public <T extends ConfigElement> Collection<T> getAllObjectsAs(Type typeOfT) {
        Set<T> tmp = new HashSet<>();
        objects.values().forEach(x -> tmp.add(gson.fromJson(x, typeOfT)));
        return tmp;
    }

    protected Map<String, JsonElement> collectSubObjects() {
        if (subObjects.isEmpty()) return objects;
        subObjects.forEach((key, value) -> objects.put(key, gson.toJsonTree(value.collectSubObjects())));
        return objects;
    }
}
