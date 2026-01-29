package com.infernalsuite.asp.plugin.loader;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.loaders.UpdatableLoader;
import com.infernalsuite.asp.loaders.file.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
}
