package com.infernalsuite.asp.api.testutil;

import com.infernalsuite.asp.api.SlimeDataConverter;
import com.infernalsuite.asp.api.SlimeNMSBridge;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;

import java.io.IOException;
import java.util.List;

public final class DummySlimeNMSBridge implements SlimeNMSBridge {

    private static final SlimeDataConverter CONVERTER = new DummySlimeDataConverter();

    @Override
    public boolean loadOverworldOverride() {
        return false;
    }

    @Override
    public boolean loadNetherOverride() {
        return false;
    }

    @Override
    public boolean loadEndOverride() {
        return false;
    }

    @Override
    public void setDefaultWorlds(final SlimeWorld normalWorld, final SlimeWorld netherWorld, final SlimeWorld endWorld) throws IOException {
    }

    @Override
    public SlimeWorldInstance loadInstance(final SlimeWorld slimeWorld) {
        return null;
    }

    @Override
    public SlimeWorldInstance getInstance(final World world) {
        return null;
    }

    @Override
    public int getCurrentVersion() {
        return 0;
    }

    @Override
    public void extractCraftPDC(final PersistentDataContainer source, final CompoundBinaryTag.Builder builder) {
    }

    @Override
    public SlimeDataConverter getSlimeDataConverter() {
        return CONVERTER;
    }

    private static final class DummySlimeDataConverter implements SlimeDataConverter {

        @Override
        public SlimeWorld applyDataFixers(final SlimeWorld world) {
            return world;
        }

        @Override
        public CompoundBinaryTag convertChunkTo1_13(final CompoundBinaryTag globalTag) {
            return globalTag;
        }

        @Override
        public List<CompoundBinaryTag> convertEntities(final List<CompoundBinaryTag> input, final int from, final int to) {
            return input;
        }

        @Override
        public List<CompoundBinaryTag> convertTileEntities(final List<CompoundBinaryTag> input, final int from, final int to) {
            return input;
        }

        @Override
        public ListBinaryTag convertBlockPalette(final ListBinaryTag input, final int from, final int to) {
            return input;
        }
    }
}
