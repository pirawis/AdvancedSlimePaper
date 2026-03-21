package com.infernalsuite.asp.plugin.commands;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.plugin.SWPlugin;
import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import com.infernalsuite.asp.plugin.testutil.TestAdvancedSlimePaperAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.verification.VerificationMode;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbstractCommandTest {

    @Mock
    protected AdvancedSlimePaperAPI asp;

    @Mock
    protected CommandManager commandManager;

    @Mock
    protected SWPlugin plugin;

    @Mock
    protected CommandSender sender;

    protected final Set<String> worldsInUse = new HashSet<>();

    protected MockedStatic<AdvancedSlimePaperAPI> mockApiInstance() {
        TestAdvancedSlimePaperAPI.setDelegate(asp);
        lenient().when(commandManager.getPlugin()).thenReturn(plugin);
        lenient().when(commandManager.getWorldsInUse()).thenReturn(worldsInUse);
        return new NoOpAdvancedSlimePaperMock();
    }

    protected Source source(final CommandSender commandSender) {
        Source source = mock(Source.class);
        lenient().when(source.source()).thenReturn(commandSender);
        return source;
    }

    protected PlayerSource playerSource(final Player player) {
        PlayerSource playerSource = mock(PlayerSource.class);
        lenient().when(playerSource.source()).thenReturn(player);
        return playerSource;
    }

    protected static String plainText(final Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    protected static MessageCommandException assertMessageException(final Throwable throwable) {
        Throwable current = throwable;
        while ((current instanceof CompletionException) && current.getCause() != null) {
            current = current.getCause();
        }
        return assertInstanceOf(MessageCommandException.class, current);
    }

    private static final class NoOpAdvancedSlimePaperMock implements MockedStatic<AdvancedSlimePaperAPI> {

        private boolean closed;

        @Override
        public <S> OngoingStubbing<S> when(final Verification verification) {
            throw new UnsupportedOperationException("No-op static mock does not support stubbing");
        }

        @Override
        public void verify(final Verification verification, final VerificationMode mode) {
            throw new UnsupportedOperationException("No-op static mock does not support verification");
        }

        @Override
        public void reset() {
        }

        @Override
        public void clearInvocations() {
        }

        @Override
        public void verifyNoMoreInteractions() {
        }

        @Override
        public void verifyNoInteractions() {
        }

        @Override
        public void close() {
            closeOnDemand();
        }

        @Override
        public void closeOnDemand() {
            if (!closed) {
                closed = true;
                TestAdvancedSlimePaperAPI.setDelegate(null);
            }
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }
}
