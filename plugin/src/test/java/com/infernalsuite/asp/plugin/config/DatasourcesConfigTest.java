package com.infernalsuite.asp.plugin.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatasourcesConfig")
class DatasourcesConfigTest {

    private DatasourcesConfig config;

    @BeforeEach
    void setUp() {
        config = new DatasourcesConfig();
    }

    @Nested
    @DisplayName("FileConfig")
    class FileConfigTests {

        @Test
        @DisplayName("should have default path")
        void shouldHaveDefaultPath() {
            assertEquals("slime_worlds", config.getFileConfig().getPath());
        }

        @Test
        @DisplayName("should allow setting file config")
        void shouldAllowSettingFileConfig() {
            DatasourcesConfig.FileConfig fileConfig = new DatasourcesConfig.FileConfig();
            config.setFileConfig(fileConfig);
            assertSame(fileConfig, config.getFileConfig());
        }
    }

    @Nested
    @DisplayName("MysqlConfig")
    class MysqlConfigTests {

        @Test
        @DisplayName("should be disabled by default")
        void shouldBeDisabledByDefault() {
            assertFalse(config.getMysqlConfig().isEnabled());
        }

        @Test
        @DisplayName("should have default host")
        void shouldHaveDefaultHost() {
            assertEquals("127.0.0.1", config.getMysqlConfig().getHost());
        }

        @Test
        @DisplayName("should have default port")
        void shouldHaveDefaultPort() {
            assertEquals(3306, config.getMysqlConfig().getPort());
        }

        @Test
        @DisplayName("should have default database")
        void shouldHaveDefaultDatabase() {
            assertEquals("slimeworldmanager", config.getMysqlConfig().getDatabase());
        }

        @Test
        @DisplayName("should allow setting all properties")
        void shouldAllowSettingAllProperties() {
            DatasourcesConfig.MysqlConfig mysql = config.getMysqlConfig();
            
            mysql.setEnabled(true);
            mysql.setHost("localhost");
            mysql.setPort(3307);
            mysql.setUsername("testuser");
            mysql.setPassword("testpass");
            mysql.setDatabase("testdb");
            mysql.setUsessl(true);
            mysql.setSqlUrl("jdbc:mysql://custom");

            assertTrue(mysql.isEnabled());
            assertEquals("localhost", mysql.getHost());
            assertEquals(3307, mysql.getPort());
            assertEquals("testuser", mysql.getUsername());
            assertEquals("testpass", mysql.getPassword());
            assertEquals("testdb", mysql.getDatabase());
            assertTrue(mysql.isUsessl());
            assertEquals("jdbc:mysql://custom", mysql.getSqlUrl());
        }
    }

    @Nested
    @DisplayName("MongoDBConfig")
    class MongoDBConfigTests {

        @Test
        @DisplayName("should be disabled by default")
        void shouldBeDisabledByDefault() {
            assertFalse(config.getMongoDbConfig().isEnabled());
        }

        @Test
        @DisplayName("should have default port")
        void shouldHaveDefaultPort() {
            assertEquals(27017, config.getMongoDbConfig().getPort());
        }

        @Test
        @DisplayName("should have default auth source")
        void shouldHaveDefaultAuthSource() {
            assertEquals("admin", config.getMongoDbConfig().getAuthSource());
        }

        @Test
        @DisplayName("should have default collection")
        void shouldHaveDefaultCollection() {
            assertEquals("worlds", config.getMongoDbConfig().getCollection());
        }

        @Test
        @DisplayName("should allow setting all properties")
        void shouldAllowSettingAllProperties() {
            DatasourcesConfig.MongoDBConfig mongo = config.getMongoDbConfig();
            
            mongo.setEnabled(true);
            mongo.setHost("mongohost");
            mongo.setPort(27018);
            mongo.setAuthSource("authdb");
            mongo.setUsername("mongouser");
            mongo.setPassword("mongopass");
            mongo.setDatabase("mongodb");
            mongo.setCollection("worlds_collection");
            mongo.setUri("mongodb://custom");

            assertTrue(mongo.isEnabled());
            assertEquals("mongohost", mongo.getHost());
            assertEquals(27018, mongo.getPort());
            assertEquals("authdb", mongo.getAuthSource());
            assertEquals("mongouser", mongo.getUsername());
            assertEquals("mongopass", mongo.getPassword());
            assertEquals("mongodb", mongo.getDatabase());
            assertEquals("worlds_collection", mongo.getCollection());
            assertEquals("mongodb://custom", mongo.getUri());
        }
    }

    @Nested
    @DisplayName("RedisConfig")
    class RedisConfigTests {

        @Test
        @DisplayName("should be disabled by default")
        void shouldBeDisabledByDefault() {
            assertFalse(config.getRedisConfig().isEnabled());
        }

        @Test
        @DisplayName("should have default uri")
        void shouldHaveDefaultUri() {
            assertEquals("redis://127.0.0.1/", config.getRedisConfig().getUri());
        }

        @Test
        @DisplayName("should allow setting redis config")
        void shouldAllowSettingRedisConfig() {
            DatasourcesConfig.RedisConfig redisConfig = new DatasourcesConfig.RedisConfig();
            config.setRedisConfig(redisConfig);
            assertSame(redisConfig, config.getRedisConfig());
        }
    }

    @Nested
    @DisplayName("APIConfig")
    class APIConfigTests {

        @Test
        @DisplayName("should be disabled by default")
        void shouldBeDisabledByDefault() {
            assertFalse(config.getApiConfig().isEnabled());
        }

        @Test
        @DisplayName("should not ignore SSL by default")
        void shouldNotIgnoreSslByDefault() {
            assertFalse(config.getApiConfig().isIgnoreSslCertificate());
        }

        @Test
        @DisplayName("should have empty credentials by default")
        void shouldHaveEmptyCredentialsByDefault() {
            assertEquals("", config.getApiConfig().getUsername());
            assertEquals("", config.getApiConfig().getToken());
            assertEquals("", config.getApiConfig().getUrl());
        }

        @Test
        @DisplayName("should allow setting api config")
        void shouldAllowSettingApiConfig() {
            DatasourcesConfig.APIConfig apiConfig = new DatasourcesConfig.APIConfig();
            config.setApiConfig(apiConfig);
            assertSame(apiConfig, config.getApiConfig());
        }
    }

    @Nested
    @DisplayName("Config setters")
    class ConfigSettersTests {

        @Test
        @DisplayName("should allow setting mysql config")
        void shouldAllowSettingMysqlConfig() {
            DatasourcesConfig.MysqlConfig mysqlConfig = new DatasourcesConfig.MysqlConfig();
            config.setMysqlConfig(mysqlConfig);
            assertSame(mysqlConfig, config.getMysqlConfig());
        }

        @Test
        @DisplayName("should allow setting mongodb config")
        void shouldAllowSettingMongodbConfig() {
            DatasourcesConfig.MongoDBConfig mongoConfig = new DatasourcesConfig.MongoDBConfig();
            config.setMongoDbConfig(mongoConfig);
            assertSame(mongoConfig, config.getMongoDbConfig());
        }
    }
}
