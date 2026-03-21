package com.infernalsuite.asp.plugin.testutil;

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

public final class TestAdvancedSlimePaperAPI implements AdvancedSlimePaperAPI {

    private static volatile AdvancedSlimePaperAPI delegate;

    public static void setDelegate(final AdvancedSlimePaperAPI delegate) {
        TestAdvancedSlimePaperAPI.delegate = delegate;
    }

    private static AdvancedSlimePaperAPI delegate() {
        if (delegate == null) {
            throw new IllegalStateException("No AdvancedSlimePaperAPI test delegate configured");
        }
        return delegate;
    }

    @Override
    public SlimeWorld readWorld(final SlimeLoader loader, final String worldName, final boolean readOnly,
                                final SlimePropertyMap propertyMap)
            throws UnknownWorldException, IOException, CorruptedWorldException, NewerFormatException {
        return delegate().readWorld(loader, worldName, readOnly, propertyMap);
    }

    @Override
    public SlimeWorldInstance getLoadedWorld(final String worldName) {
        return delegate().getLoadedWorld(worldName);
    }

    @Override
    public List<SlimeWorldInstance> getLoadedWorlds() {
        return delegate().getLoadedWorlds();
    }

    @Override
    public SlimeWorldInstance loadWorld(final SlimeWorld world, final boolean callWorldLoadEvent) throws IllegalArgumentException {
        return delegate().loadWorld(world, callWorldLoadEvent);
    }

    @Override
    public boolean worldLoaded(final SlimeWorld world) {
        return delegate().worldLoaded(world);
    }

    @Override
    public void saveWorld(final SlimeWorld world) throws IOException {
        delegate().saveWorld(world);
    }

    @Override
    public void migrateWorld(final String worldName, final SlimeLoader currentLoader, final SlimeLoader newLoader)
            throws IOException, WorldAlreadyExistsException, UnknownWorldException {
        delegate().migrateWorld(worldName, currentLoader, newLoader);
    }

    @Override
    public SlimeWorld createEmptyWorld(final String worldName, final boolean readOnly, final SlimePropertyMap propertyMap,
                                       @Nullable final SlimeLoader loader) {
        return delegate().createEmptyWorld(worldName, readOnly, propertyMap, loader);
    }

    @Override
    public SlimeWorld readVanillaWorld(final File worldDir, final String worldName, @Nullable final SlimeLoader loader)
            throws InvalidWorldException, WorldLoadedException, WorldTooBigException, IOException, WorldAlreadyExistsException {
        return delegate().readVanillaWorld(worldDir, worldName, loader);
    }

    @Override
    public SlimeSerializationAdapter getSerializer() {
        return delegate().getSerializer();
    }
}
