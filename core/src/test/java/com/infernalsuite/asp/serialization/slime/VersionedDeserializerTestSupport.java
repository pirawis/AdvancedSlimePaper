package com.infernalsuite.asp.serialization.slime;

import com.github.luben.zstd.Zstd;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public final class VersionedDeserializerTestSupport {

    private VersionedDeserializerTestSupport() {
    }

    public static SlimePropertyMap propertyMap(final int minSection, final int maxSection) {
        SlimePropertyMap propertyMap = new SlimePropertyMap();
        propertyMap.setValue(SlimeProperties.CHUNK_SECTION_MIN, minSection);
        propertyMap.setValue(SlimeProperties.CHUNK_SECTION_MAX, maxSection);
        return propertyMap;
    }

    public static DataInputStream input(final byte[] bytes) {
        return new DataInputStream(new ByteArrayInputStream(bytes));
    }

    public static void writeCompressedBlock(final DataOutputStream output, final byte[] bytes) throws IOException {
        byte[] compressed = Zstd.compress(bytes);
        output.writeInt(compressed.length);
        output.writeInt(bytes.length);
        output.write(compressed);
    }

    public static void writeRawBlock(final DataOutputStream output, final byte[] bytes) throws IOException {
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    public static byte[] nbt(final CompoundBinaryTag tag) throws IOException {
        return SlimeSerializer.serializeCompoundTag(tag);
    }

    public static byte[] lightBytes(final byte value) {
        byte[] bytes = new byte[2048];
        bytes[0] = value;
        return bytes;
    }

    public static CompoundBinaryTag blockStatesTag(final String name) {
        return CompoundBinaryTag.builder()
                .put("palette", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                        CompoundBinaryTag.builder().putString("Name", name).build()
                )))
                .build();
    }

    public static CompoundBinaryTag biomeTag(final String biome) {
        return CompoundBinaryTag.builder()
                .put("palette", ListBinaryTag.listBinaryTag(BinaryTagTypes.STRING, List.of(
                        StringBinaryTag.stringBinaryTag(biome)
                )))
                .build();
    }

    public static CompoundBinaryTag entity(final String id, final double x, final double y, final double z) {
        return CompoundBinaryTag.builder()
                .putString("id", id)
                .put("Pos", ListBinaryTag.listBinaryTag(BinaryTagTypes.DOUBLE, List.of(
                        DoubleBinaryTag.doubleBinaryTag(x),
                        DoubleBinaryTag.doubleBinaryTag(y),
                        DoubleBinaryTag.doubleBinaryTag(z)
                )))
                .build();
    }

    public static CompoundBinaryTag tileEntity(final String id, final int x, final int z) {
        return CompoundBinaryTag.builder()
                .putString("id", id)
                .putInt("x", x)
                .putInt("z", z)
                .build();
    }

    public static CompoundBinaryTag worldExtra(final SlimePropertyMap properties, final String key, final BinaryTag value) {
        return CompoundBinaryTag.builder()
                .put("properties", properties.toCompound())
                .put(key, value)
                .build();
    }
}
