package com.ehrengrenlund.cozyhomes.configuration;

import com.ehrengrenlund.cozyhomes.utils.CozyLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class Configuration {
    private CozyLogger logger;
    private CozyConfig configClass;
    private static Configuration instance;
    private static final String CONFIG_FILE = "CozyHomes.json";

    private Configuration() { }

    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }

        return instance;
    }

    public int loadConfiguration(@NotNull CozyLogger logger) {
        this.logger = logger;

        File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile();

        try {
            Gson gson = new Gson();
            CozyConfig configObject = gson.fromJson(new FileReader(configFile), CozyConfig.class);

            if (configObject == null)
                throw new Exception("Failed to parse config");

            this.configClass = configObject;
        } catch (FileNotFoundException e) {
            this.logger.info("File not found, creating new config file.");
            this.configClass = new CozyConfig();

            if (this.saveConfiguration() == Status.CONFIG_ERROR) {
                this.logger.error("Failed to save config");
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage());
            return Status.CONFIG_ERROR;
        }

        return Status.CONFIG_SUCCESS;
    }

    public int saveConfiguration() {
        try {
            File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile();
            FileWriter writer = new FileWriter(configFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this.configClass, writer);
            writer.close();

            this.logger.info("Saved configuration");
        } catch (IOException e) {
            logger.log(CozyLogger.Verbosity.ERROR, "Failed to save config.");
            return Status.CONFIG_ERROR;
        }

        return Status.CONFIG_SUCCESS;
    }

    public int getMaxHomes() {
        return this.configClass.maxHomes;
    }

    public int getStandStillTime() {
        return this.configClass.standStillTime;
    }

    public int getCoolDownTime() {
        return this.configClass.coolDownTime;
    }

    public boolean getShowBossBar() {
        return this.configClass.bossbar;
    }

    public static class Status {
        public static final int CONFIG_SUCCESS = 0;
        public static final int CONFIG_ERROR = -1;
    }
}
