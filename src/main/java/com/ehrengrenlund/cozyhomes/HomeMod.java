package com.ehrengrenlund.cozyhomes;

import com.ehrengrenlund.cozyhomes.commands.HomeCommand;
import com.ehrengrenlund.cozyhomes.configuration.Configuration;

import com.ehrengrenlund.cozyhomes.utils.CozyLogger;
import net.fabricmc.api.ModInitializer;

// TODO Will be adding support for permissions libraries

public class HomeMod implements ModInitializer {
	public static final CozyLogger cozyLogger = CozyLogger.InitializeLogger("cozyhomes");
	public static Configuration config = Configuration.getInstance();

	@Override
	public void onInitialize() {
		cozyLogger.log(CozyLogger.Verbosity.INFO, "Initializing mod...");
		cozyLogger.log(CozyLogger.Verbosity.INFO,"Created by SGEhren-dev");

		if (config.loadConfiguration(cozyLogger) == Configuration.Status.CONFIG_ERROR) {
			cozyLogger.log(CozyLogger.Verbosity.ERROR, "Failed to load the configuration.");
		}

		new HomeCommand(config, cozyLogger)
				.RegisterCommands();
	}
}