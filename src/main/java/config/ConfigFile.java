package config;

import java.io.*;
import java.util.HashMap;

/**
 * Created by usbpc on 30.04.2017.
 */
class ConfigFile extends ConfigObject {
    private File configFile;

    ConfigFile(File configFile) {
        try {
            this.objects = gson.fromJson(new FileReader(configFile), mapType);
        } catch (FileNotFoundException e) {
            this.objects = new HashMap<>();
        }
        this.configFile = configFile;
    }

    void save() {
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write(gson.toJson(collectSubObjects()));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
