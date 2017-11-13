package com.dbkoda.drill.utils;

import java.util.logging.Logger;

public interface DrillLogger {

    default void info(String message) {
        Logger.getLogger(this.getClass().getName()).info(message);
    }

    default void fine(String msg) {
        Logger.getLogger(this.getClass().getName()).fine(msg);
    }

    default void warning(String msg) {
        Logger.getLogger(this.getClass().getName()).warning(msg);
    }

    default void severe(String msg) {
        Logger.getLogger(this.getClass().getName()).severe(msg);
    }

    default void finer(String msg) {
        Logger.getLogger(this.getClass().getName()).finer(msg);
    }

    default void finest(String msg) {
        Logger.getLogger(this.getClass().getName()).finest(msg);
    }
}
