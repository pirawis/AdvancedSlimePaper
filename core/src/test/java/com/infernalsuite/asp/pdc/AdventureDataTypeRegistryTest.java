package com.infernalsuite.asp.pdc;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdventureDataTypeRegistry")
class AdventureDataTypeRegistryTest {

    private AdventureDataTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AdventureDataTypeRegistry();
    }

    @Nested
    @DisplayName("wrap primitive types")
    class WrapPrimitiveTests {

        @Test
        @DisplayName("should wrap byte value")
        void shouldWrapByteValue() {
            BinaryTag tag = registry.wrap(PersistentDataType.BYTE, (byte) 42);

            assertInstanceOf(ByteBinaryTag.class, tag);
            assertEquals((byte) 42, ((ByteBinaryTag) tag).value());
        }

        @Test
        @DisplayName("should wrap short value")
        void shouldWrapShortValue() {
            BinaryTag tag = registry.wrap(PersistentDataType.SHORT, (short) 1000);

            assertInstanceOf(ShortBinaryTag.class, tag);
            assertEquals((short) 1000, ((ShortBinaryTag) tag).value());
        }

        @Test
        @DisplayName("should wrap integer value")
        void shouldWrapIntegerValue() {
            BinaryTag tag = registry.wrap(PersistentDataType.INTEGER, 123456);

            assertInstanceOf(IntBinaryTag.class, tag);
            assertEquals(123456, ((IntBinaryTag) tag).value());
        }

        @Test
        @DisplayName("should wrap long value")
        void shouldWrapLongValue() {
            BinaryTag tag = registry.wrap(PersistentDataType.LONG, 9876543210L);

            assertInstanceOf(LongBinaryTag.class, tag);
            assertEquals(9876543210L, ((LongBinaryTag) tag).value());
        }

        @Test
        @DisplayName("should wrap float value")
        void shouldWrapFloatValue() {
            BinaryTag tag = registry.wrap(PersistentDataType.FLOAT, 3.14f);

            assertInstanceOf(FloatBinaryTag.class, tag);
            assertEquals(3.14f, ((FloatBinaryTag) tag).value(), 0.001f);
        }

        @Test
        @DisplayName("should wrap double value")
        void shouldWrapDoubleValue() {
            BinaryTag tag = registry.wrap(PersistentDataType.DOUBLE, 2.71828);

            assertInstanceOf(DoubleBinaryTag.class, tag);
            assertEquals(2.71828, ((DoubleBinaryTag) tag).value(), 0.00001);
        }

        @Test
        @DisplayName("should wrap string value")
        void shouldWrapStringValue() {
            BinaryTag tag = registry.wrap(PersistentDataType.STRING, "hello");

            assertInstanceOf(StringBinaryTag.class, tag);
            assertEquals("hello", ((StringBinaryTag) tag).value());
        }

        @Test
        @DisplayName("should wrap byte array value")
        void shouldWrapByteArrayValue() {
            byte[] bytes = {1, 2, 3, 4, 5};
            BinaryTag tag = registry.wrap(PersistentDataType.BYTE_ARRAY, bytes);

            assertInstanceOf(ByteArrayBinaryTag.class, tag);
            assertArrayEquals(bytes, ((ByteArrayBinaryTag) tag).value());
        }

        @Test
        @DisplayName("should wrap int array value")
        void shouldWrapIntArrayValue() {
            int[] ints = {10, 20, 30};
            BinaryTag tag = registry.wrap(PersistentDataType.INTEGER_ARRAY, ints);

            assertInstanceOf(IntArrayBinaryTag.class, tag);
            assertArrayEquals(ints, ((IntArrayBinaryTag) tag).value());
        }

        @Test
        @DisplayName("should wrap long array value")
        void shouldWrapLongArrayValue() {
            long[] longs = {100L, 200L, 300L};
            BinaryTag tag = registry.wrap(PersistentDataType.LONG_ARRAY, longs);

            assertInstanceOf(LongArrayBinaryTag.class, tag);
            assertArrayEquals(longs, ((LongArrayBinaryTag) tag).value());
        }
    }

    @Nested
    @DisplayName("extract primitive types")
    class ExtractPrimitiveTests {

        @Test
        @DisplayName("should extract byte value")
        void shouldExtractByteValue() {
            ByteBinaryTag tag = ByteBinaryTag.byteBinaryTag((byte) 42);

            Byte result = registry.extract(PersistentDataType.BYTE, tag);

            assertEquals((byte) 42, result);
        }

        @Test
        @DisplayName("should extract short value")
        void shouldExtractShortValue() {
            ShortBinaryTag tag = ShortBinaryTag.shortBinaryTag((short) 1000);

            Short result = registry.extract(PersistentDataType.SHORT, tag);

            assertEquals((short) 1000, result);
        }

        @Test
        @DisplayName("should extract integer value")
        void shouldExtractIntegerValue() {
            IntBinaryTag tag = IntBinaryTag.intBinaryTag(123456);

            Integer result = registry.extract(PersistentDataType.INTEGER, tag);

            assertEquals(123456, result);
        }

        @Test
        @DisplayName("should extract long value")
        void shouldExtractLongValue() {
            LongBinaryTag tag = LongBinaryTag.longBinaryTag(9876543210L);

            Long result = registry.extract(PersistentDataType.LONG, tag);

            assertEquals(9876543210L, result);
        }

        @Test
        @DisplayName("should extract float value")
        void shouldExtractFloatValue() {
            FloatBinaryTag tag = FloatBinaryTag.floatBinaryTag(3.14f);

            Float result = registry.extract(PersistentDataType.FLOAT, tag);

            assertEquals(3.14f, result, 0.001f);
        }

        @Test
        @DisplayName("should extract double value")
        void shouldExtractDoubleValue() {
            DoubleBinaryTag tag = DoubleBinaryTag.doubleBinaryTag(2.71828);

            Double result = registry.extract(PersistentDataType.DOUBLE, tag);

            assertEquals(2.71828, result, 0.00001);
        }

        @Test
        @DisplayName("should extract string value")
        void shouldExtractStringValue() {
            StringBinaryTag tag = StringBinaryTag.stringBinaryTag("hello");

            String result = registry.extract(PersistentDataType.STRING, tag);

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("should extract byte array value")
        void shouldExtractByteArrayValue() {
            byte[] bytes = {1, 2, 3, 4, 5};
            ByteArrayBinaryTag tag = ByteArrayBinaryTag.byteArrayBinaryTag(bytes);

            byte[] result = registry.extract(PersistentDataType.BYTE_ARRAY, tag);

            assertArrayEquals(bytes, result);
        }

        @Test
        @DisplayName("should extract int array value")
        void shouldExtractIntArrayValue() {
            int[] ints = {10, 20, 30};
            IntArrayBinaryTag tag = IntArrayBinaryTag.intArrayBinaryTag(ints);

            int[] result = registry.extract(PersistentDataType.INTEGER_ARRAY, tag);

            assertArrayEquals(ints, result);
        }

        @Test
        @DisplayName("should extract long array value")
        void shouldExtractLongArrayValue() {
            long[] longs = {100L, 200L, 300L};
            LongArrayBinaryTag tag = LongArrayBinaryTag.longArrayBinaryTag(longs);

            long[] result = registry.extract(PersistentDataType.LONG_ARRAY, tag);

            assertArrayEquals(longs, result);
        }
    }

    @Nested
    @DisplayName("isInstanceOf")
    class IsInstanceOfTests {

        @Test
        @DisplayName("should return true for matching byte tag")
        void shouldReturnTrueForMatchingByteTag() {
            ByteBinaryTag tag = ByteBinaryTag.byteBinaryTag((byte) 1);

            assertTrue(registry.isInstanceOf(PersistentDataType.BYTE, tag));
        }

        @Test
        @DisplayName("should return false for non-matching tag type")
        void shouldReturnFalseForNonMatchingTagType() {
            StringBinaryTag tag = StringBinaryTag.stringBinaryTag("test");

            assertFalse(registry.isInstanceOf(PersistentDataType.INTEGER, tag));
        }

        @Test
        @DisplayName("should return true for matching string tag")
        void shouldReturnTrueForMatchingStringTag() {
            StringBinaryTag tag = StringBinaryTag.stringBinaryTag("test");

            assertTrue(registry.isInstanceOf(PersistentDataType.STRING, tag));
        }

        @Test
        @DisplayName("should return true for matching int array tag")
        void shouldReturnTrueForMatchingIntArrayTag() {
            IntArrayBinaryTag tag = IntArrayBinaryTag.intArrayBinaryTag(1, 2, 3);

            assertTrue(registry.isInstanceOf(PersistentDataType.INTEGER_ARRAY, tag));
        }
    }

    @Nested
    @DisplayName("DEFAULT instance")
    class DefaultInstanceTests {

        @Test
        @DisplayName("should have a default instance")
        void shouldHaveDefaultInstance() {
            assertNotNull(AdventureDataTypeRegistry.DEFAULT);
        }

        @Test
        @DisplayName("default instance should work for wrapping")
        void defaultInstanceShouldWorkForWrapping() {
            BinaryTag tag = AdventureDataTypeRegistry.DEFAULT.wrap(PersistentDataType.INTEGER, 42);

            assertInstanceOf(IntBinaryTag.class, tag);
        }
    }

    @Nested
    @DisplayName("PersistentDataContainer adapter")
    class PersistentDataContainerTests {

        @Test
        @DisplayName("should wrap AdventurePersistentDataContainer")
        void shouldWrapAdventurePersistentDataContainer() {
            AdventurePersistentDataContainer container = new AdventurePersistentDataContainer(registry);

            BinaryTag tag = registry.wrap(PersistentDataType.TAG_CONTAINER, container);

            assertInstanceOf(CompoundBinaryTag.class, tag);
        }

        @Test
        @DisplayName("should extract PersistentDataContainer")
        void shouldExtractPersistentDataContainer() {
            CompoundBinaryTag tag = CompoundBinaryTag.empty();

            var result = registry.extract(PersistentDataType.TAG_CONTAINER, tag);

            assertInstanceOf(AdventurePersistentDataContainer.class, result);
        }
    }

    @Nested
    @DisplayName("error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should throw when extracting with wrong tag type")
        void shouldThrowWhenExtractingWithWrongTagType() {
            StringBinaryTag tag = StringBinaryTag.stringBinaryTag("test");

            assertThrows(IllegalArgumentException.class, () ->
                    registry.extract(PersistentDataType.INTEGER, tag));
        }
    }
}
