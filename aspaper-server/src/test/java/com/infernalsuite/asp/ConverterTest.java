package com.infernalsuite.asp;

import com.infernalsuite.asp.api.utils.NibbleArray;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
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
}
