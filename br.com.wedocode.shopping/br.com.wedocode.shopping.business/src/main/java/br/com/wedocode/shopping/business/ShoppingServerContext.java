package br.com.wedocode.shopping.business;

import java.nio.file.Path;

import javax.sql.DataSource;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;

public class ShoppingServerContext {

    private static Path baseDir;

    private static Path configDir;

    private static Path dataDir;

    private static Path logDir;

    private static Path tempDir;

    private static ScheduledExecutor scheduledExecutor;

    private static DataSource defaultDataSource;

    public static final Path getBaseDir() {
        return baseDir;
    }

    public static final Path getConfigDir() {
        return configDir;
    }

    public static final Path getDataDir() {
        return dataDir;
    }

    public static final Path getLogDir() {
        return logDir;
    }

    public static final Path getTempDir() {
        return tempDir;
    }

    public static ScheduledExecutor getScheduledExecutor() {
        return scheduledExecutor;
    }

    public static final DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    public static class Internals {

        public static void setBaseDir(Path path) {
            ShoppingServerContext.baseDir = path;
        }

        public static void setConfigDir(Path path) {
            ShoppingServerContext.configDir = path;
        }

        public static void setDataDir(Path path) {
            ShoppingServerContext.dataDir = path;
        }

        public static void setLogDir(Path path) {
            ShoppingServerContext.logDir = path;
        }

        public static void setTempDir(Path path) {
            ShoppingServerContext.tempDir = path;
        }

        public static void setDefaultDataSource(DataSource datasource) {
            ShoppingServerContext.defaultDataSource = datasource;
        }

        public static void setScheduledExecutor(ScheduledExecutor executor) {
            ShoppingServerContext.scheduledExecutor = executor;
        }

    }

}
