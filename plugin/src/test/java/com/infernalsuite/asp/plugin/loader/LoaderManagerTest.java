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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoaderManager")
class LoaderManagerTest {

    private LoaderManager loaderManager;

    @Mock
    private SlimeLoader mockLoader;

    @Mock
    private UpdatableLoader mockUpdatableLoader;

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with file loader")
        void shouldInitializeWithFileLoader() {
            try (MockedStatic<com.infernalsuite.asp.plugin.config.ConfigManager> configManager = 
                    mockStatic(com.infernalsuite.asp.plugin.config.ConfigManager.class)) {
                
                com.infernalsuite.asp.plugin.config.DatasourcesConfig mockConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig fileConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig mysqlConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig mongoConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig redisConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig apiConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig.class);
                
                when(fileConfig.getPath()).thenReturn(".");
                when(mysqlConfig.isEnabled()).thenReturn(false);
                when(mongoConfig.isEnabled()).thenReturn(false);
                when(redisConfig.isEnabled()).thenReturn(false);
                when(apiConfig.isEnabled()).thenReturn(false);
                
                when(mockConfig.getFileConfig()).thenReturn(fileConfig);
                when(mockConfig.getMysqlConfig()).thenReturn(mysqlConfig);
                when(mockConfig.getMongoDbConfig()).thenReturn(mongoConfig);
                when(mockConfig.getRedisConfig()).thenReturn(redisConfig);
                when(mockConfig.getApiConfig()).thenReturn(apiConfig);
                
                configManager.when(com.infernalsuite.asp.plugin.config.ConfigManager::getDatasourcesConfig)
                    .thenReturn(mockConfig);
                
                loaderManager = new LoaderManager();
                
                assertNotNull(loaderManager.getLoader("file"));
                assertInstanceOf(FileLoader.class, loaderManager.getLoader("file"));
            }
        }

        @Test
        @DisplayName("should handle disabled loaders")
        void shouldHandleDisabledLoaders() {
            try (MockedStatic<com.infernalsuite.asp.plugin.config.ConfigManager> configManager = 
                    mockStatic(com.infernalsuite.asp.plugin.config.ConfigManager.class)) {
                
                com.infernalsuite.asp.plugin.config.DatasourcesConfig mockConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig fileConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig mysqlConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig mongoConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig redisConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig apiConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig.class);
                
                when(fileConfig.getPath()).thenReturn(".");
                when(mysqlConfig.isEnabled()).thenReturn(false);
                when(mongoConfig.isEnabled()).thenReturn(false);
                when(redisConfig.isEnabled()).thenReturn(false);
                when(apiConfig.isEnabled()).thenReturn(false);
                
                when(mockConfig.getFileConfig()).thenReturn(fileConfig);
                when(mockConfig.getMysqlConfig()).thenReturn(mysqlConfig);
                when(mockConfig.getMongoDbConfig()).thenReturn(mongoConfig);
                when(mockConfig.getRedisConfig()).thenReturn(redisConfig);
                when(mockConfig.getApiConfig()).thenReturn(apiConfig);
                
                configManager.when(com.infernalsuite.asp.plugin.config.ConfigManager::getDatasourcesConfig)
                    .thenReturn(mockConfig);
                
                loaderManager = new LoaderManager();
                
                assertNull(loaderManager.getLoader("mysql"));
                assertNull(loaderManager.getLoader("mongodb"));
                assertNull(loaderManager.getLoader("redis"));
                assertNull(loaderManager.getLoader("api"));
            }
        }

        @Test
        @DisplayName("should initialize all enabled optional loaders")
        void shouldInitializeAllEnabledOptionalLoaders() {
            try (MockedStatic<com.infernalsuite.asp.plugin.config.ConfigManager> configManager =
                         mockStatic(com.infernalsuite.asp.plugin.config.ConfigManager.class);
                 MockedConstruction<FileLoader> fileLoaderConstruction = mockConstruction(FileLoader.class);
                 MockedConstruction<MysqlLoader> mysqlLoaderConstruction = mockConstruction(MysqlLoader.class);
                 MockedConstruction<MongoLoader> mongoLoaderConstruction = mockConstruction(MongoLoader.class);
                 MockedConstruction<RedisLoader> redisLoaderConstruction = mockConstruction(RedisLoader.class);
                 MockedConstruction<APILoader> apiLoaderConstruction = mockConstruction(APILoader.class)) {

                com.infernalsuite.asp.plugin.config.DatasourcesConfig mockConfig =
                        mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig fileConfig =
                        mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig mysqlConfig =
                        mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig mongoConfig =
                        mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig redisConfig =
                        mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig apiConfig =
                        mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig.class);

                when(fileConfig.getPath()).thenReturn(".");

                when(mysqlConfig.isEnabled()).thenReturn(true);
                when(mysqlConfig.getSqlUrl()).thenReturn("jdbc:mysql://localhost:3306/asp");
                when(mysqlConfig.getHost()).thenReturn("localhost");
                when(mysqlConfig.getPort()).thenReturn(3306);
                when(mysqlConfig.getDatabase()).thenReturn("asp");
                when(mysqlConfig.isUsessl()).thenReturn(false);
                when(mysqlConfig.getUsername()).thenReturn("root");
                when(mysqlConfig.getPassword()).thenReturn("secret");

                when(mongoConfig.isEnabled()).thenReturn(true);
                when(mongoConfig.getDatabase()).thenReturn("asp");
                when(mongoConfig.getCollection()).thenReturn("worlds");
                when(mongoConfig.getUsername()).thenReturn("mongo");
                when(mongoConfig.getPassword()).thenReturn("secret");
                when(mongoConfig.getAuthSource()).thenReturn("admin");
                when(mongoConfig.getHost()).thenReturn("localhost");
                when(mongoConfig.getPort()).thenReturn(27017);
                when(mongoConfig.getUri()).thenReturn("mongodb://localhost:27017");

                when(redisConfig.isEnabled()).thenReturn(true);
                when(redisConfig.getUri()).thenReturn("redis://localhost:6379");

                when(apiConfig.isEnabled()).thenReturn(true);
                when(apiConfig.getUrl()).thenReturn("https://example.test/api");
                when(apiConfig.getUsername()).thenReturn("api-user");
                when(apiConfig.getToken()).thenReturn("token");
                when(apiConfig.isIgnoreSslCertificate()).thenReturn(true);

                when(mockConfig.getFileConfig()).thenReturn(fileConfig);
                when(mockConfig.getMysqlConfig()).thenReturn(mysqlConfig);
                when(mockConfig.getMongoDbConfig()).thenReturn(mongoConfig);
                when(mockConfig.getRedisConfig()).thenReturn(redisConfig);
                when(mockConfig.getApiConfig()).thenReturn(apiConfig);

                configManager.when(com.infernalsuite.asp.plugin.config.ConfigManager::getDatasourcesConfig)
                        .thenReturn(mockConfig);

                loaderManager = new LoaderManager();

                assertEquals(1, fileLoaderConstruction.constructed().size());
                assertEquals(1, mysqlLoaderConstruction.constructed().size());
                assertEquals(1, mongoLoaderConstruction.constructed().size());
                assertEquals(1, redisLoaderConstruction.constructed().size());
                assertEquals(1, apiLoaderConstruction.constructed().size());

                assertSame(fileLoaderConstruction.constructed().getFirst(), loaderManager.getLoader("file"));
                assertSame(mysqlLoaderConstruction.constructed().getFirst(), loaderManager.getLoader("mysql"));
                assertSame(mongoLoaderConstruction.constructed().getFirst(), loaderManager.getLoader("mongodb"));
                assertSame(redisLoaderConstruction.constructed().getFirst(), loaderManager.getLoader("redis"));
                assertSame(apiLoaderConstruction.constructed().getFirst(), loaderManager.getLoader("api"));
            }
        }

        @Test
        @DisplayName("should skip mysql loader when its construction fails")
        void shouldSkipMysqlLoaderWhenItsConstructionFails() {
            SlimeLoader fileLoader = mock(SlimeLoader.class);

            loaderManager = new LoaderManager(configForOptionalLoaders(true, false, false, false)) {
                @Override
                protected SlimeLoader createFileLoader(final DatasourcesConfig.FileConfig fileConfig) {
                    return fileLoader;
                }

                @Override
                protected SlimeLoader createMysqlLoader(final DatasourcesConfig.MysqlConfig mysqlConfig) throws java.sql.SQLException {
                    throw new java.sql.SQLException("mysql down");
                }
            };

            assertSame(fileLoader, loaderManager.getLoader("file"));
            assertNull(loaderManager.getLoader("mysql"));
        }

        @Test
        @DisplayName("should skip mongo loader when its construction fails")
        void shouldSkipMongoLoaderWhenItsConstructionFails() {
            SlimeLoader fileLoader = mock(SlimeLoader.class);

            loaderManager = new LoaderManager(configForOptionalLoaders(false, true, false, false)) {
                @Override
                protected SlimeLoader createFileLoader(final DatasourcesConfig.FileConfig fileConfig) {
                    return fileLoader;
                }

                @Override
                protected SlimeLoader createMongoLoader(final DatasourcesConfig.MongoDBConfig mongoConfig) throws MongoException {
                    throw new MongoException("mongo down");
                }
            };

            assertSame(fileLoader, loaderManager.getLoader("file"));
            assertNull(loaderManager.getLoader("mongodb"));
        }

        @Test
        @DisplayName("should skip redis loader when its construction fails")
        void shouldSkipRedisLoaderWhenItsConstructionFails() {
            SlimeLoader fileLoader = mock(SlimeLoader.class);

            loaderManager = new LoaderManager(configForOptionalLoaders(false, false, true, false)) {
                @Override
                protected SlimeLoader createFileLoader(final DatasourcesConfig.FileConfig fileConfig) {
                    return fileLoader;
                }

                @Override
                protected SlimeLoader createRedisLoader(final DatasourcesConfig.RedisConfig redisConfig) throws RedisException {
                    throw new RedisException("redis down");
                }
            };

            assertSame(fileLoader, loaderManager.getLoader("file"));
            assertNull(loaderManager.getLoader("redis"));
        }
    }

    @Nested
    @DisplayName("registerLoader")
    class RegisterLoaderTests {

        @BeforeEach
        void setUp() {
            try (MockedStatic<com.infernalsuite.asp.plugin.config.ConfigManager> configManager = 
                    mockStatic(com.infernalsuite.asp.plugin.config.ConfigManager.class)) {
                
                com.infernalsuite.asp.plugin.config.DatasourcesConfig mockConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig fileConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig mysqlConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig mongoConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig redisConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig apiConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig.class);
                
                when(fileConfig.getPath()).thenReturn(".");
                when(mysqlConfig.isEnabled()).thenReturn(false);
                when(mongoConfig.isEnabled()).thenReturn(false);
                when(redisConfig.isEnabled()).thenReturn(false);
                when(apiConfig.isEnabled()).thenReturn(false);
                
                when(mockConfig.getFileConfig()).thenReturn(fileConfig);
                when(mockConfig.getMysqlConfig()).thenReturn(mysqlConfig);
                when(mockConfig.getMongoDbConfig()).thenReturn(mongoConfig);
                when(mockConfig.getRedisConfig()).thenReturn(redisConfig);
                when(mockConfig.getApiConfig()).thenReturn(apiConfig);
                
                configManager.when(com.infernalsuite.asp.plugin.config.ConfigManager::getDatasourcesConfig)
                    .thenReturn(mockConfig);
                
                loaderManager = new LoaderManager();
            }
        }

        @Test
        @DisplayName("should register new loader")
        void shouldRegisterNewLoader() {
            loaderManager.registerLoader("custom", mockLoader);
            
            assertEquals(mockLoader, loaderManager.getLoader("custom"));
        }

        @Test
        @DisplayName("should throw exception for duplicate loader registration")
        void shouldThrowExceptionForDuplicateLoaderRegistration() {
            loaderManager.registerLoader("custom", mockLoader);
            
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                loaderManager.registerLoader("custom", mock(SlimeLoader.class))
            );
            
            assertTrue(exception.getMessage().contains("already has a declared loader"));
        }

        @Test
        @DisplayName("should call update on UpdatableLoader during registration")
        void shouldCallUpdateOnUpdatableLoaderDuringRegistration() throws IOException, UpdatableLoader.NewerStorageException {
            loaderManager.registerLoader("updatable", mockUpdatableLoader);
            
            verify(mockUpdatableLoader, times(1)).update();
            assertEquals(mockUpdatableLoader, loaderManager.getLoader("updatable"));
        }

        @Test
        @DisplayName("should handle NewerStorageException during loader registration")
        void shouldHandleNewerStorageExceptionDuringLoaderRegistration() throws IOException, UpdatableLoader.NewerStorageException {
            doThrow(new UpdatableLoader.NewerStorageException(2, 1))
                .when(mockUpdatableLoader).update();
            
            loaderManager.registerLoader("updatable", mockUpdatableLoader);
            
            assertNull(loaderManager.getLoader("updatable"));
        }

        @Test
        @DisplayName("should handle IOException during loader registration")
        void shouldHandleIOExceptionDuringLoaderRegistration() throws IOException, UpdatableLoader.NewerStorageException {
            doThrow(new IOException("Connection failed"))
                .when(mockUpdatableLoader).update();
            
            loaderManager.registerLoader("updatable", mockUpdatableLoader);
            
            assertNull(loaderManager.getLoader("updatable"));
        }
    }

    @Nested
    @DisplayName("getLoader")
    class GetLoaderTests {

        @BeforeEach
        void setUp() {
            try (MockedStatic<com.infernalsuite.asp.plugin.config.ConfigManager> configManager = 
                    mockStatic(com.infernalsuite.asp.plugin.config.ConfigManager.class)) {
                
                com.infernalsuite.asp.plugin.config.DatasourcesConfig mockConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig fileConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig mysqlConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig mongoConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig redisConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig apiConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig.class);
                
                when(fileConfig.getPath()).thenReturn(".");
                when(mysqlConfig.isEnabled()).thenReturn(false);
                when(mongoConfig.isEnabled()).thenReturn(false);
                when(redisConfig.isEnabled()).thenReturn(false);
                when(apiConfig.isEnabled()).thenReturn(false);
                
                when(mockConfig.getFileConfig()).thenReturn(fileConfig);
                when(mockConfig.getMysqlConfig()).thenReturn(mysqlConfig);
                when(mockConfig.getMongoDbConfig()).thenReturn(mongoConfig);
                when(mockConfig.getRedisConfig()).thenReturn(redisConfig);
                when(mockConfig.getApiConfig()).thenReturn(apiConfig);
                
                configManager.when(com.infernalsuite.asp.plugin.config.ConfigManager::getDatasourcesConfig)
                    .thenReturn(mockConfig);
                
                loaderManager = new LoaderManager();
            }
        }

        @Test
        @DisplayName("should return loader for existing data source")
        void shouldReturnLoaderForExistingDataSource() {
            loaderManager.registerLoader("custom", mockLoader);
            
            SlimeLoader retrievedLoader = loaderManager.getLoader("custom");
            
            assertEquals(mockLoader, retrievedLoader);
        }

        @Test
        @DisplayName("should return null for non-existent data source")
        void shouldReturnNullForNonExistentDataSource() {
            SlimeLoader retrievedLoader = loaderManager.getLoader("nonexistent");
            
            assertNull(retrievedLoader);
        }

        @Test
        @DisplayName("should return file loader by default")
        void shouldReturnFileLoaderByDefault() {
            SlimeLoader fileLoader = loaderManager.getLoader("file");
            
            assertNotNull(fileLoader);
            assertInstanceOf(FileLoader.class, fileLoader);
        }
    }

    @Nested
    @DisplayName("getLoaders")
    class GetLoadersTests {

        @BeforeEach
        void setUp() {
            try (MockedStatic<com.infernalsuite.asp.plugin.config.ConfigManager> configManager = 
                    mockStatic(com.infernalsuite.asp.plugin.config.ConfigManager.class)) {
                
                com.infernalsuite.asp.plugin.config.DatasourcesConfig mockConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig fileConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.FileConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig mysqlConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MysqlConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig mongoConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.MongoDBConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig redisConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.RedisConfig.class);
                com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig apiConfig = 
                    mock(com.infernalsuite.asp.plugin.config.DatasourcesConfig.APIConfig.class);
                
                when(fileConfig.getPath()).thenReturn(".");
                when(mysqlConfig.isEnabled()).thenReturn(false);
                when(mongoConfig.isEnabled()).thenReturn(false);
                when(redisConfig.isEnabled()).thenReturn(false);
                when(apiConfig.isEnabled()).thenReturn(false);
                
                when(mockConfig.getFileConfig()).thenReturn(fileConfig);
                when(mockConfig.getMysqlConfig()).thenReturn(mysqlConfig);
                when(mockConfig.getMongoDbConfig()).thenReturn(mongoConfig);
                when(mockConfig.getRedisConfig()).thenReturn(redisConfig);
                when(mockConfig.getApiConfig()).thenReturn(apiConfig);
                
                configManager.when(com.infernalsuite.asp.plugin.config.ConfigManager::getDatasourcesConfig)
                    .thenReturn(mockConfig);
                
                loaderManager = new LoaderManager();
            }
        }

        @Test
        @DisplayName("should return map containing all registered loaders")
        void shouldReturnMapContainingAllRegisteredLoaders() {
            loaderManager.registerLoader("custom1", mockLoader);
            loaderManager.registerLoader("custom2", mock(SlimeLoader.class));
            
            Map<String, SlimeLoader> loadersMap = loaderManager.getLoaders();
            
            assertNotNull(loadersMap);
            assertTrue(loadersMap.containsKey("file"));
            assertTrue(loadersMap.containsKey("custom1"));
            assertTrue(loadersMap.containsKey("custom2"));
            assertEquals(3, loadersMap.size());
        }

        @Test
        @DisplayName("should return map with only file loader initially")
        void shouldReturnMapWithOnlyFileLoaderInitially() {
            Map<String, SlimeLoader> loadersMap = loaderManager.getLoaders();
            
            assertNotNull(loadersMap);
            assertTrue(loadersMap.containsKey("file"));
            assertEquals(1, loadersMap.size());
        }
    }

    private static DatasourcesConfig configForOptionalLoaders(
            final boolean mysqlEnabled,
            final boolean mongoEnabled,
            final boolean redisEnabled,
            final boolean apiEnabled
    ) {
        DatasourcesConfig mockConfig = mock(DatasourcesConfig.class);
        DatasourcesConfig.FileConfig fileConfig = mock(DatasourcesConfig.FileConfig.class);
        DatasourcesConfig.MysqlConfig mysqlConfig = mock(DatasourcesConfig.MysqlConfig.class);
        DatasourcesConfig.MongoDBConfig mongoConfig = mock(DatasourcesConfig.MongoDBConfig.class);
        DatasourcesConfig.RedisConfig redisConfig = mock(DatasourcesConfig.RedisConfig.class);
        DatasourcesConfig.APIConfig apiConfig = mock(DatasourcesConfig.APIConfig.class);

        lenient().when(fileConfig.getPath()).thenReturn(".");

        lenient().when(mysqlConfig.isEnabled()).thenReturn(mysqlEnabled);
        lenient().when(mysqlConfig.getSqlUrl()).thenReturn("jdbc:mysql://localhost:3306/asp");
        lenient().when(mysqlConfig.getHost()).thenReturn("localhost");
        lenient().when(mysqlConfig.getPort()).thenReturn(3306);
        lenient().when(mysqlConfig.getDatabase()).thenReturn("asp");
        lenient().when(mysqlConfig.isUsessl()).thenReturn(false);
        lenient().when(mysqlConfig.getUsername()).thenReturn("root");
        lenient().when(mysqlConfig.getPassword()).thenReturn("secret");

        lenient().when(mongoConfig.isEnabled()).thenReturn(mongoEnabled);
        lenient().when(mongoConfig.getDatabase()).thenReturn("asp");
        lenient().when(mongoConfig.getCollection()).thenReturn("worlds");
        lenient().when(mongoConfig.getUsername()).thenReturn("mongo");
        lenient().when(mongoConfig.getPassword()).thenReturn("secret");
        lenient().when(mongoConfig.getAuthSource()).thenReturn("admin");
        lenient().when(mongoConfig.getHost()).thenReturn("localhost");
        lenient().when(mongoConfig.getPort()).thenReturn(27017);
        lenient().when(mongoConfig.getUri()).thenReturn("mongodb://localhost:27017");

        lenient().when(redisConfig.isEnabled()).thenReturn(redisEnabled);
        lenient().when(redisConfig.getUri()).thenReturn("redis://localhost:6379");

        lenient().when(apiConfig.isEnabled()).thenReturn(apiEnabled);
        lenient().when(apiConfig.getUrl()).thenReturn("https://example.test/api");
        lenient().when(apiConfig.getUsername()).thenReturn("api-user");
        lenient().when(apiConfig.getToken()).thenReturn("token");
        lenient().when(apiConfig.isIgnoreSslCertificate()).thenReturn(false);

        lenient().when(mockConfig.getFileConfig()).thenReturn(fileConfig);
        lenient().when(mockConfig.getMysqlConfig()).thenReturn(mysqlConfig);
        lenient().when(mockConfig.getMongoDbConfig()).thenReturn(mongoConfig);
        lenient().when(mockConfig.getRedisConfig()).thenReturn(redisConfig);
        lenient().when(mockConfig.getApiConfig()).thenReturn(apiConfig);

        return mockConfig;
    }
}
