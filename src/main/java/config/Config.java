package config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private final static Map<String, ConfigFile> objectMap = new HashMap<>();
    private final static File configFolder = new File("configs");

    public synchronized static ConfigObject getConfigByName(String name) {
        if (objectMap.containsKey(name)) return objectMap.get(name);

        ConfigFile tmp = new ConfigFile(new File(configFolder, name + ".json"));
        objectMap.put(name, tmp);
        return tmp;
    }

    public static void close() {
        objectMap.values().forEach(ConfigFile::save);
    }


}
