package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;

/**
 * Created by usbpc on 30.04.2017.
 */
class ConfigFile extends ConfigObject {
    private File configFile;
    private static Logger logger = LoggerFactory.getLogger(ConfigObject.class);
    ConfigFile(File configFile) {
        if (configFile.exists()) {
            try {
                this.objects = gson.fromJson(new FileReader(configFile), mapType);
            } catch (FileNotFoundException e) {
                logger.error("File {} does not exist. I got {}", configFile.getAbsolutePath(), e.getMessage());
            }
        } else {
            this.objects = new HashMap<>();
        }
        this.configFile = configFile;
    }

    void save() {
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write(gson.toJson(collectSubObjects()));
            fileWriter.flush();
        } catch (IOException e) {
            logger.error("Something went wrong trying to write to {}", configFile.getName(), e);
        }
    }
}
