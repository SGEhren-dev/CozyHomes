package com.ehrengrenlund.cozyhomes.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CozyLogger {
    private final Logger logger;
    private static final String loggerPrefix = "[Cozy Homes]: ";

    private CozyLogger(Logger logInstance) {
        this.logger = logInstance;
    }

    public static CozyLogger InitializeLogger(String modId) {
        return new CozyLogger(LoggerFactory.getLogger(modId));
    }

    private String getPrefixedMessage(String message) {
        return loggerPrefix + message;
    }

    public void info(String message) {
        logger.info(getPrefixedMessage(message));
    }

    public void warn(String message) {
        logger.warn(getPrefixedMessage(message));
    }

    public void error(String message) {
        logger.error(getPrefixedMessage(message));
    }

    public void log(Verbosity verbosity, String message) {
        switch (verbosity) {
            case INFO -> {
                logger.info(getPrefixedMessage(message));
                break;
            }
            case DEBUG -> {
                logger.debug(getPrefixedMessage(message));
                break;
            }
            case WARN -> {
                logger.warn(getPrefixedMessage(message));
                break;
            }
            case ERROR -> {
                logger.error(getPrefixedMessage(message));
                break;
            }
        }
    }

    public enum Verbosity {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
}
