package com.infernalsuite.asp.serialization.slime.reader.impl;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.SlimeWorldReader;
import com.infernalsuite.asp.serialization.slime.reader.VersionedByteSlimeWorldReader;
import com.infernalsuite.asp.serialization.slime.reader.impl.v10.v10WorldFormat;
import com.infernalsuite.asp.serialization.slime.reader.impl.v11.v11WorldFormat;
import com.infernalsuite.asp.serialization.slime.reader.impl.v12.v12WorldFormat;
import com.infernalsuite.asp.serialization.slime.reader.impl.v13.v13WorldFormat;
import com.infernalsuite.asp.serialization.slime.reader.impl.v1_9.v1_9WorldFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("WorldFormat smoke tests")
@ExtendWith(MockitoExtension.class)
class WorldFormatSmokeTest {

    @Mock
    private SlimeWorldReader<String> dataReader;

    @Mock
    private VersionedByteSlimeWorldReader<String> versionedReader;

    @Mock
    private SlimeLoader loader;

    @Mock
    private SlimePropertyMap propertyMap;

    @Mock
    private SlimeWorld slimeWorld;

    @Test
    @DisplayName("SimpleWorldFormat should delegate to byte reader and world adapter")
    void simpleWorldFormatShouldDelegateToByteReaderAndWorldAdapter() throws Exception {
        SimpleWorldFormat<String> format = new SimpleWorldFormat<>(dataReader, versionedReader);
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(new byte[0]));

        when(versionedReader.deserializeWorld((byte) 10, loader, "world", dataStream, propertyMap, true)).thenReturn("decoded");
        when(dataReader.readFromData("decoded")).thenReturn(slimeWorld);

        SlimeWorld world = format.deserializeWorld((byte) 10, loader, "world", dataStream, propertyMap, true);

        assertSame(slimeWorld, world);
        verify(versionedReader).deserializeWorld((byte) 10, loader, "world", dataStream, propertyMap, true);
        verify(dataReader).readFromData("decoded");
    }

    @Test
    @DisplayName("Versioned format constants should be initialized")
    void versionedFormatConstantsShouldBeInitialized() {
        assertNotNull(v1_9WorldFormat.FORMAT);
        assertNotNull(v10WorldFormat.FORMAT);
        assertNotNull(v11WorldFormat.FORMAT);
        assertNotNull(v12WorldFormat.FORMAT);
        assertNotNull(v13WorldFormat.FORMAT);
    }
}
