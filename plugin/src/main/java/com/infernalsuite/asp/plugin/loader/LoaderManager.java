package com.infernalsuite.asp.plugin.loader;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.loaders.UpdatableLoader;
import com.infernalsuite.asp.loaders.api.APILoader;
import com.infernalsuite.asp.loaders.file.FileLoader;
import com.infernalsuite.asp.loaders.mongo.MongoLoader;
import com.infernalsuite.asp.loaders.mysql.MysqlLoader;
import com.infernalsuite.asp.loaders.redis.RedisLoader;
import com.infernalsuite.asp.plugin.config.DatasourcesConfig;
import com.mongodb.MongoException;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoaderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoaderManager.class);

    private final Map<String, SlimeLoader> loaders = new HashMap<>();

    public LoaderManager() {
        this(com.infernalsuite.asp.plugin.config.ConfigManager.getDatasourcesConfig());
    }

    LoaderManager(final DatasourcesConfig config) {
        // File loader
        final DatasourcesConfig.FileConfig fileConfig = config.getFileConfig();
        registerLoader("file", createFileLoader(fileConfig));

        // Mysql loader
        final DatasourcesConfig.MysqlConfig mysqlConfig = config.getMysqlConfig();
        if (mysqlConfig.isEnabled()) {
            try {
                registerLoader("mysql", createMysqlLoader(mysqlConfig));
            } catch (final SQLException ex) {
                LOGGER.error("Failed to establish connection to the MySQL server:", ex);
            }
        }

        // MongoDB loader
        final DatasourcesConfig.MongoDBConfig mongoConfig = config.getMongoDbConfig();

        if (mongoConfig.isEnabled()) {
            try {
                registerLoader("mongodb", createMongoLoader(mongoConfig));
            } catch (final MongoException ex) {
                LOGGER.error("Failed to establish connection to the MongoDB server:", ex);
            }
        }

        final DatasourcesConfig.RedisConfig redisConfig = config.getRedisConfig();
        if (redisConfig.isEnabled()){
            try {
                registerLoader("redis", createRedisLoader(redisConfig));
            } catch (final RedisException ex) {
                LOGGER.error("Failed to establish connection to the Redis server:", ex);
            }
        }

        final DatasourcesConfig.APIConfig apiConfig = config.getApiConfig();
        if(apiConfig.isEnabled()){
            registerLoader("api", createApiLoader(apiConfig));
        }
    }

    protected SlimeLoader createFileLoader(final DatasourcesConfig.FileConfig fileConfig) {
        return new FileLoader(new File(fileConfig.getPath()));
    }

    protected SlimeLoader createMysqlLoader(final DatasourcesConfig.MysqlConfig mysqlConfig) throws SQLException {
        return new MysqlLoader(
                mysqlConfig.getSqlUrl(),
                mysqlConfig.getHost(), mysqlConfig.getPort(),
                mysqlConfig.getDatabase(), mysqlConfig.isUsessl(),
                mysqlConfig.getUsername(), mysqlConfig.getPassword()
        );
    }

    protected SlimeLoader createMongoLoader(final DatasourcesConfig.MongoDBConfig mongoConfig) throws MongoException {
        return new MongoLoader(
                mongoConfig.getDatabase(),
                mongoConfig.getCollection(),
                mongoConfig.getUsername(),
                mongoConfig.getPassword(),
                mongoConfig.getAuthSource(),
                mongoConfig.getHost(),
                mongoConfig.getPort(),
                mongoConfig.getUri()
        );
    }

    protected SlimeLoader createRedisLoader(final DatasourcesConfig.RedisConfig redisConfig) throws RedisException {
        return new RedisLoader(redisConfig.getUri());
    }

    protected SlimeLoader createApiLoader(final DatasourcesConfig.APIConfig apiConfig) {
        return new APILoader(
                apiConfig.getUrl(),
                apiConfig.getUsername(),
                apiConfig.getToken(),
                apiConfig.isIgnoreSslCertificate()
        );
    }

    public void registerLoader(String dataSource, SlimeLoader loader) {
        if (loaders.containsKey(dataSource)) {
            throw new IllegalArgumentException("Data source " + dataSource + " already has a declared loader!");
        }

        if (loader instanceof UpdatableLoader) {
            try {
                ((UpdatableLoader) loader).update();
            } catch (final UpdatableLoader.NewerStorageException e) {
                LOGGER.error("Data source {} version is {}, while this loader version only supports up to version {}.",
                        dataSource, e.getStorageVersion(), e.getImplementationVersion(), e);
                return;
            } catch (final IOException ex) {
                LOGGER.error("Failed to update data source {}", dataSource, ex);
                return;
            }
        }

        loaders.put(dataSource, loader);
    }

    public SlimeLoader getLoader(String dataSource) {
        return loaders.get(dataSource);
    }

    public Map<String, SlimeLoader> getLoaders() {
        return loaders;
    }
}
