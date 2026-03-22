package com.infernalsuite.asp.plugin.commands;

import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import com.infernalsuite.asp.plugin.commands.parser.suggestion.KnownSlimeWorldSuggestionProvider;
import com.infernalsuite.asp.plugin.commands.sub.HelpCmd;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommandManager behavior")
class CommandManagerBehaviorTest extends AbstractCommandTest {

    @Mock
    private ParserRegistry<Source> parserRegistry;

    @Mock
    private PaperCommandManager<Source> cloudCommandManager;

    @Mock
    private PaperSimpleSenderMapper senderMapper;

    @Mock
    private PaperCommandManager.Builder<Source> builder;

    @Mock
    private PaperCommandManager.CoordinatedBuilder<Source> coordinatedBuilder;

    @Test
    @DisplayName("should register parsers, commands and expose plugin state")
    void shouldRegisterParsersCommandsAndExposePluginState() {
        BuiltCommandManager built = buildCommandManager();
        CommandManager manager = built.manager();

        assertSame(plugin, manager.getPlugin());
        assertTrue(manager.getWorldsInUse().isEmpty());
        manager.getWorldsInUse().add("arena");
        assertTrue(manager.getWorldsInUse().contains("arena"));

        verify(builder).executionCoordinator(any());
        verify(coordinatedBuilder).buildOnEnable(plugin);
        verify(parserRegistry).registerSuggestionProvider(eq("known-slime-worlds"), any(KnownSlimeWorldSuggestionProvider.class));
        verify(parserRegistry, times(4)).registerParserSupplier(any(TypeToken.class), any());
        assertEquals(1, built.annotationParserCount());
        assertEquals(1, built.helpCommandCount());
    }

    @Test
    @DisplayName("should route parser and permission failures through registered handlers")
    void shouldRouteParserAndPermissionFailuresThroughRegisteredHandlers() throws Throwable {
        BuiltCommandManager built = buildCommandManager();
        CommandContext<Source> context = new CommandContext<>(source(sender), cloudCommandManager);

        built.exceptionController().handleException(
                context,
                new ArgumentParseException(new MessageCommandException(Component.text("custom parser failure")), sender, List.of())
        );
        assertLastMessageContains("custom parser failure");

        clearInvocations(sender);
        built.exceptionController().handleException(
                context,
                new ArgumentParseException(new IllegalStateException("plain parser failure"), sender, List.of())
        );
        assertLastMessageContains("plain parser failure");

        clearInvocations(sender);
        built.exceptionController().handleException(
                context,
                new ArgumentParseException(new IllegalStateException(), sender, List.of())
        );
        assertLastMessageContains("An error occurred while parsing the command!");

        clearInvocations(sender);
        built.exceptionController().handleException(
                context,
                new InvalidSyntaxException("swm help", sender, List.of(mock(org.incendo.cloud.component.CommandComponent.class)))
        );
        assertLastMessageContains("Unknown subcommand");

        clearInvocations(sender);
        built.exceptionController().handleException(
                context,
                new InvalidSyntaxException("swm clone-world <template-world> <world-name>", sender, List.of(
                        mock(org.incendo.cloud.component.CommandComponent.class),
                        mock(org.incendo.cloud.component.CommandComponent.class)
                ))
        );
        assertLastMessageContains("Command usage: /swm clone-world <template-world> <world-name>.");

        clearInvocations(sender);
        built.exceptionController().handleException(
                context,
                new NoPermissionException(PermissionResult.denied(Permission.of("swm.use")), sender, List.of())
        );
        assertLastMessageContains("You do not have permission to perform this command.");

        clearInvocations(sender);
        built.exceptionController().handleException(
                context,
                new MessageCommandException(Component.text("direct message failure"))
        );
        assertLastMessageContains("direct message failure");

        clearInvocations(sender);
        built.exceptionController().handleException(context, new CommandExecutionException(
                new MessageCommandException(Component.text("wrapped failure")),
                context
        ));
        assertLastMessageContains("wrapped failure");
    }

    @Test
    @DisplayName("should send the base help message when the root command is used")
    void shouldSendTheBaseHelpMessageWhenTheRootCommandIsUsed() {
        BuiltCommandManager built = buildCommandManager();

        built.manager().onCommand(source(sender));

        assertLastMessageContains("This is the main command for the Slime World Plugin.");
        assertLastMessageContains("/swp help");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BuiltCommandManager buildCommandManager() {
        ExceptionController<Source> exceptionController = new ExceptionController<>();

        when(cloudCommandManager.parserRegistry()).thenReturn(parserRegistry);
        when(cloudCommandManager.exceptionController()).thenReturn(exceptionController);
        when(builder.executionCoordinator(any())).thenReturn(coordinatedBuilder);
        when(coordinatedBuilder.buildOnEnable(plugin)).thenReturn(cloudCommandManager);

        try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
             MockedStatic<PaperSimpleSenderMapper> senderMapperStatic = org.mockito.Mockito.mockStatic(PaperSimpleSenderMapper.class);
             MockedStatic<PaperCommandManager> paperCommandManagerStatic = org.mockito.Mockito.mockStatic(PaperCommandManager.class);
             MockedConstruction<AnnotationParser> annotationParsers = org.mockito.Mockito.mockConstruction(
                     AnnotationParser.class,
                     (mock, context) -> when(mock.parse(any(Object[].class))).thenReturn(List.of())
             );
             MockedConstruction<HelpCmd> helpCommands = org.mockito.Mockito.mockConstruction(HelpCmd.class)) {
            senderMapperStatic.when(PaperSimpleSenderMapper::simpleSenderMapper).thenReturn(senderMapper);
            paperCommandManagerStatic.when(() -> PaperCommandManager.builder(senderMapper)).thenReturn(builder);

            CommandManager manager = new CommandManager(plugin);
            return new BuiltCommandManager(
                    manager,
                    exceptionController,
                    annotationParsers.constructed().size(),
                    helpCommands.constructed().size()
            );
        }
    }

    private void assertLastMessageContains(final String expectedText) {
        ArgumentCaptor<Component> messageCaptor = ArgumentCaptor.forClass(Component.class);
        verify((CommandSender) sender).sendMessage(messageCaptor.capture());
        assertTrue(plainText(messageCaptor.getValue()).contains(expectedText));
    }

    private record BuiltCommandManager(
            CommandManager manager,
            ExceptionController<Source> exceptionController,
            int annotationParserCount,
            int helpCommandCount
    ) {
    }
}
