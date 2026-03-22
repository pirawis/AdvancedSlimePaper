package com.infernalsuite.asp.api.testutil;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.InvalidWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.asp.api.exceptions.WorldLoadedException;
import com.infernalsuite.asp.api.exceptions.WorldTooBigException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.loaders.SlimeSerializationAdapter;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class DummyAdvancedSlimePaperAPI implements AdvancedSlimePaperAPI {

    @Override
    public SlimeWorld readWorld(final SlimeLoader loader, final String worldName, final boolean readOnly,
                                final SlimePropertyMap propertyMap)
            throws UnknownWorldException, IOException, CorruptedWorldException, NewerFormatException {
        return null;
    }

    @Override
    public SlimeWorldInstance getLoadedWorld(final String worldName) {
        return null;
    }

    @Override
    public List<SlimeWorldInstance> getLoadedWorlds() {
        return List.of();
    }

    @Override
    public SlimeWorldInstance loadWorld(final SlimeWorld world, final boolean callWorldLoadEvent) throws IllegalArgumentException {
        return null;
    }

    @Override
    public boolean worldLoaded(final SlimeWorld world) {
        return false;
    }

    @Override
    public void saveWorld(final SlimeWorld world) throws IOException {
    }

    @Override
    public void migrateWorld(final String worldName, final SlimeLoader currentLoader, final SlimeLoader newLoader)
            throws IOException, WorldAlreadyExistsException, UnknownWorldException {
    }

    @Override
    public SlimeWorld createEmptyWorld(final String worldName, final boolean readOnly, final SlimePropertyMap propertyMap,
                                       @Nullable final SlimeLoader loader) {
        return null;
    }

    @Override
    public SlimeWorld readVanillaWorld(final File worldDir, final String worldName, @Nullable final SlimeLoader loader)
            throws InvalidWorldException, WorldLoadedException, WorldTooBigException, IOException, WorldAlreadyExistsException {
        return null;
    }

    @Override
    public SlimeSerializationAdapter getSerializer() {
        return null;
    }
}
