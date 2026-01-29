package com.infernalsuite.asp.level;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.saveddata.SavedData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("ReadOnlyDimensionDataStorage")
class ReadOnlyDimensionDataStorageTest {

    @Test
    @DisplayName("should return null for unknown entries")
    void shouldReturnNullForUnknownEntries() {
        ReadOnlyDimensionDataStorage storage = new ReadOnlyDimensionDataStorage(
            Path.of("build", "tmp", "read-only-storage"),
            Mockito.mock(DataFixer.class),
            Mockito.mock(HolderLookup.Provider.class)
        );

        SavedData.Factory<SavedData> factory = Mockito.mock(SavedData.Factory.class);
        assertNull(storage.get(factory, "missing"));
    }

    @Test
    @DisplayName("should return completed future on scheduleSave")
    void shouldReturnCompletedFutureOnScheduleSave() {
        ReadOnlyDimensionDataStorage storage = new ReadOnlyDimensionDataStorage(
            Path.of("build", "tmp", "read-only-storage"),
            Mockito.mock(DataFixer.class),
            Mockito.mock(HolderLookup.Provider.class)
        );

        CompletableFuture<?> future = storage.scheduleSave();
        assertSame(null, future.join());
    }

    @Test
    @DisplayName("should allow saveAndJoin and close")
    void shouldAllowSaveAndJoinAndClose() {
        ReadOnlyDimensionDataStorage storage = new ReadOnlyDimensionDataStorage(
            Path.of("build", "tmp", "read-only-storage"),
            Mockito.mock(DataFixer.class),
            Mockito.mock(HolderLookup.Provider.class)
        );

        assertDoesNotThrow(storage::saveAndJoin);
        assertDoesNotThrow(storage::close);
    }
}
