package com.infernalsuite.asp;

import com.infernalsuite.asp.api.utils.NibbleArray;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.DataLayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Converter")
class ConverterTest {

    @Test
    @DisplayName("should round-trip NibbleArray")
    void shouldRoundTripNibbleArray() {
        byte[] backing = new byte[] {0x12, 0x34, 0x56};
        NibbleArray original = new NibbleArray(backing);

        DataLayer dataLayer = Converter.convertArray(original);
        NibbleArray roundTrip = Converter.convertArray(dataLayer);

        assertArrayEquals(backing, roundTrip.getBacking());
    }

    @Test
    @DisplayName("should return null for null DataLayer")
    void shouldReturnNullForNullDataLayer() {
        assertNull(Converter.convertArray((DataLayer) null));
    }

    @Test
    @DisplayName("should round-trip compound tags")
    void shouldRoundTripCompoundTags() {
        CompoundBinaryTag binary = CompoundBinaryTag.builder()
            .put("level", IntBinaryTag.intBinaryTag(42))
            .put("name", StringBinaryTag.stringBinaryTag("test"))
            .put("list", ListBinaryTag.listBinaryTag(IntBinaryTag.intBinaryTag(1).type(), List.of(
                IntBinaryTag.intBinaryTag(1),
                IntBinaryTag.intBinaryTag(2)
            )))
            .build();

        Tag converted = Converter.convertTag(binary);
        assertTrue(converted instanceof CompoundTag);

        CompoundBinaryTag roundTrip = Converter.convertTag((CompoundTag) converted);
        assertEquals(binary, roundTrip);
    }

    @Test
    @DisplayName("should preserve empty list tag")
    void shouldPreserveEmptyListTag() {
        ListBinaryTag empty = ListBinaryTag.listBinaryTag(BinaryTagTypes.END, List.of());
        Tag converted = Converter.convertTag(empty);
        ListBinaryTag roundTrip = Converter.convertTag(converted);
        assertEquals(empty, roundTrip);
    }

    @Test
    @DisplayName("should round-trip primitive tags")
    void shouldRoundTripPrimitiveTags() {
        List<Object> tags = List.of(
            ByteBinaryTag.byteBinaryTag((byte) 7),
            ShortBinaryTag.shortBinaryTag((short) 32000),
            IntBinaryTag.intBinaryTag(123456),
            LongBinaryTag.longBinaryTag(1234567890123L),
            FloatBinaryTag.floatBinaryTag(1.5f),
            DoubleBinaryTag.doubleBinaryTag(2.5d),
            StringBinaryTag.stringBinaryTag("hello")
        );

        for (Object tag : tags) {
            Tag converted = Converter.convertTag((net.kyori.adventure.nbt.BinaryTag) tag);
            net.kyori.adventure.nbt.BinaryTag roundTrip = Converter.convertTag(converted);
            assertEquals(tag, roundTrip);
        }
    }
}
