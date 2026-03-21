package com.infernalsuite.asp.plugin.commands;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.plugin.SWPlugin;
import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import com.infernalsuite.asp.plugin.commands.sub.GotoCmd;
import com.infernalsuite.asp.plugin.commands.sub.ReloadConfigCmd;
import com.infernalsuite.asp.plugin.commands.sub.SaveWorldCmd;
import com.infernalsuite.asp.plugin.commands.sub.SetSpawnCmd;
import com.infernalsuite.asp.plugin.commands.sub.VersionCmd;
import com.infernalsuite.asp.plugin.config.ConfigManager;
import com.infernalsuite.asp.plugin.config.WorldData;
import com.infernalsuite.asp.plugin.config.WorldsConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Basic plugin command behavior")
class BasicCommandBehaviorTest extends AbstractCommandTest {

    @Nested
    @DisplayName("SlimeCommand")
    class SlimeCommandTests {

        @Test
        @DisplayName("should delegate world cloning lookup to the API")
        void shouldDelegateWorldCloningLookupToTheApi() throws Exception {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                SlimeLoader loader = mock(SlimeLoader.class);
                SlimePropertyMap properties = new SlimePropertyMap();
                SlimeWorld world = mock(SlimeWorld.class);
                when(asp.readWorld(loader, "arena", false, properties)).thenReturn(world);

                TestSlimeCommand command = new TestSlimeCommand(commandManager);

                SlimeWorld result = command.callGetWorldReadyForCloning("arena", loader, properties);

                assertEquals(world, result);
                verify(asp).readWorld(loader, "arena", false, properties);
            }
        }

        private static final class TestSlimeCommand extends SlimeCommand {

            private TestSlimeCommand(final CommandManager commandManager) {
                super(commandManager);
            }

            private SlimeWorld callGetWorldReadyForCloning(final String name, final SlimeLoader loader,
                                                           final SlimePropertyMap properties) throws Exception {
                return getWorldReadyForCloning(name, loader, properties);
            }
        }
    }

    @Nested
    @DisplayName("VersionCmd")
    class VersionCmdTests {

        @Test
        @DisplayName("should send the current plugin and format version")
        void shouldSendTheCurrentPluginAndFormatVersion() {
            PluginDescriptionFile description = mock(PluginDescriptionFile.class);
            when(plugin.getDescription()).thenReturn(description);
            when(description.getVersion()).thenReturn("4.2.0-test");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<SWPlugin> pluginStatic = mockStatic(SWPlugin.class)) {
                pluginStatic.when(SWPlugin::getInstance).thenReturn(plugin);
                VersionCmd command = new VersionCmd(commandManager);

                command.showVersion(source(sender));

                ArgumentCaptor<Component> messageCaptor = ArgumentCaptor.forClass(Component.class);
                verify(sender).sendMessage(messageCaptor.capture());
                String text = plainText(messageCaptor.getValue());
                assertTrue(text.contains("v4.2.0-test"));
                assertTrue(text.contains("supports up to Slime Format"));
            }
        }
    }

    @Nested
    @DisplayName("GotoCmd")
    class GotoCmdTests {

        @Test
        @DisplayName("should reject console sender without target player")
        void shouldRejectConsoleSenderWithoutTargetPlayer() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                GotoCmd command = new GotoCmd(commandManager);
                World world = mock(World.class);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.onCommand(source(sender), world, null)
                );

                assertTrue(plainText(exception.getComponent()).contains("console cannot be teleported"));
            }
        }

        @Test
        @DisplayName("should teleport the sender to configured spawn when target is omitted")
        void shouldTeleportTheSenderToConfiguredSpawnWhenTargetIsOmitted() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                GotoCmd command = new GotoCmd(commandManager);
                Player player = mock(Player.class);
                PlayerSource playerSource = playerSource(player);
                World world = mock(World.class);
                when(world.getName()).thenReturn("arena");
                when(player.teleportAsync(org.mockito.ArgumentMatchers.any(Location.class)))
                        .thenReturn(CompletableFuture.completedFuture(true));

                Map<String, WorldData> worlds = new HashMap<>();
                WorldData worldData = new WorldData();
                worldData.setSpawn("10.5, 64.0, -3.25");
                worlds.put("arena", worldData);
                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(worlds);

                try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                command.onCommand(playerSource, world, null);

                ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
                verify(player).teleportAsync(locationCaptor.capture());
                Location location = locationCaptor.getValue();
                assertEquals(10.5, location.getX());
                assertEquals(64.0, location.getY());
                assertEquals(-3.25, location.getZ());

                ArgumentCaptor<Component> messageCaptor = ArgumentCaptor.forClass(Component.class);
                verify(player).sendMessage(messageCaptor.capture());
                assertTrue(plainText(messageCaptor.getValue()).contains("Teleporting yourself to arena"));
                }
            }
        }

        @Test
        @DisplayName("should teleport the explicit target to world spawn when no config entry exists")
        void shouldTeleportTheExplicitTargetToWorldSpawnWhenNoConfigEntryExists() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                GotoCmd command = new GotoCmd(commandManager);
                World world = mock(World.class);
                when(world.getName()).thenReturn("lobby");
                Location spawn = new Location(world, 5, 70, 9);
                when(world.getSpawnLocation()).thenReturn(spawn);

                Player target = mock(Player.class);
                when(target.getName()).thenReturn("target-player");
                when(target.teleportAsync(spawn)).thenReturn(CompletableFuture.completedFuture(true));

                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());

                try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                command.onCommand(source(sender), world, target);

                verify(target).teleportAsync(spawn);
                ArgumentCaptor<Component> messageCaptor = ArgumentCaptor.forClass(Component.class);
                verify(sender).sendMessage(messageCaptor.capture());
                assertTrue(plainText(messageCaptor.getValue()).contains("Teleporting target-player to lobby"));
                }
            }
        }
    }

    @Nested
    @DisplayName("SaveWorldCmd")
    class SaveWorldCmdTests {

        @Test
        @DisplayName("should save the world and acknowledge success")
        void shouldSaveTheWorldAndAcknowledgeSuccess() throws Exception {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                SaveWorldCmd command = new SaveWorldCmd(commandManager);
                SlimeWorld world = mock(SlimeWorld.class);
                when(world.getName()).thenReturn("arena");

                command.saveWorld(source(sender), world);

                verify(asp).saveWorld(world);
                ArgumentCaptor<Component> messageCaptor = ArgumentCaptor.forClass(Component.class);
                verify(sender).sendMessage(messageCaptor.capture());
                assertTrue(plainText(messageCaptor.getValue()).contains("World arena saved."));
            }
        }

        @Test
        @DisplayName("should wrap IO failures in a message command exception")
        void shouldWrapIoFailuresInAMessageCommandException() throws Exception {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                SaveWorldCmd command = new SaveWorldCmd(commandManager);
                SlimeWorld world = mock(SlimeWorld.class);
                when(world.getName()).thenReturn("arena");
                doThrow(new IOException("disk full")).when(asp).saveWorld(world);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.saveWorld(source(sender), world)
                );

                assertTrue(plainText(exception.getComponent()).contains("Failed to save world arena."));
            }
        }
    }

    @Nested
    @DisplayName("SetSpawnCmd")
    class SetSpawnCmdTests {

        @Test
        @DisplayName("should reject non-player senders")
        void shouldRejectNonPlayerSenders() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                SetSpawnCmd command = new SetSpawnCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.setSpawn(source(sender))
                );

                assertTrue(plainText(exception.getComponent()).contains("This command is for players"));
            }
        }

        @Test
        @DisplayName("should fail when the current world is not registered")
        void shouldFailWhenTheCurrentWorldIsNotRegistered() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                SetSpawnCmd command = new SetSpawnCmd(commandManager);
                Player player = mock(Player.class);
                PlayerSource playerSource = playerSource(player);
                World world = mock(World.class);
                when(world.getName()).thenReturn("unregistered");
                when(player.getLocation()).thenReturn(new Location(world, 1, 65, 2));

                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());

                try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.setSpawn(playerSource)
                );

                assertTrue(plainText(exception.getComponent()).contains("is not a registered slime world"));
                }
            }
        }

        @Test
        @DisplayName("should persist the player location as world spawn")
        void shouldPersistThePlayerLocationAsWorldSpawn() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                SetSpawnCmd command = new SetSpawnCmd(commandManager);
                Player player = mock(Player.class);
                PlayerSource playerSource = playerSource(player);
                World world = mock(World.class);
                when(world.getName()).thenReturn("arena");
                Location location = new Location(world, 12.5, 70, -4);
                when(player.getLocation()).thenReturn(location);

                WorldData worldData = new WorldData();
                Map<String, WorldData> worlds = new HashMap<>();
                worlds.put("arena", worldData);
                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(worlds);

                try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                command.setSpawn(playerSource);

                verify(world).setSpawnLocation(location);
                verify(worldsConfig).save();
                assertEquals("12.5, 70.0, -4.0", worldData.getSpawn());

                ArgumentCaptor<Component> messageCaptor = ArgumentCaptor.forClass(Component.class);
                verify(player).sendMessage(messageCaptor.capture());
                assertTrue(plainText(messageCaptor.getValue()).contains("Set spawn for arena."));
                }
            }
        }
    }

}
