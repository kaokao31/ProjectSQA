package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

public class ZipArchiveEntryTest {

    @Test
    public void testConstructorSetsName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
    }

    @Test
    public void testPlatformAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("platform");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());

        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());

        entry.setPlatform(7);
        assertEquals(7, entry.getPlatform());
    }

    @Test
    public void testInternalAndExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("attrs");
        assertEquals(0, entry.getInternalAttributes());
        assertEquals(0L, entry.getExternalAttributes());

        entry.setInternalAttributes(0x12345678);
        assertEquals(0x12345678, entry.getInternalAttributes());

        entry.setExternalAttributes(0x0102030405060708L);
        assertEquals(0x0102030405060708L, entry.getExternalAttributes());
    }

    @Test
    public void testCopyConstructorFromZipEntry() throws Exception {
        java.util.zip.ZipEntry source = new java.util.zip.ZipEntry("copy.txt");
        source.setComment("a comment");
        source.setMethod(java.util.zip.ZipEntry.DEFLATED);
        source.setSize(42L);
        source.setCompressedSize(30L);
        source.setCrc(0x12345678L);
        source.setTime(123456789L);
        source.setExtra(buildExtra(new ZipShort(0x3333), new byte[] { 9, 8, 7 }));

        ZipArchiveEntry entry = new ZipArchiveEntry(source);
        assertEquals("copy.txt", entry.getName());
        assertEquals("a comment", entry.getComment());
        assertEquals(java.util.zip.ZipEntry.DEFLATED, entry.getMethod());
        assertEquals(42L, entry.getSize());
        assertEquals(30L, entry.getCompressedSize());
        assertEquals(0x12345678L, entry.getCrc());
        assertEquals(123456789L, entry.getTime());
        assertNotNull(entry.getExtraField(new ZipShort(0x3333)));
    }

    @Test
    public void testExtraFieldsInitiallyEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("empty");
        assertEquals(0, entry.getExtraFields().length);
        assertNull(entry.getExtraField(new ZipShort(0x0001)));
    }

    @Test
    public void testSetExtraFieldsAndGetExtraFieldsReturnsCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("setfields");
        TestExtraField first = new TestExtraField(new ZipShort(0x1001), new byte[] { 1 }, new byte[] { 11 });
        TestExtraField second = new TestExtraField(new ZipShort(0x1002), new byte[] { 2 }, new byte[] { 22 });

        entry.setExtraFields(new ZipExtraField[] { first, second });
        assertEquals(2, entry.getExtraFields().length);
        assertSame(first, entry.getExtraField(new ZipShort(0x1001)));
        assertSame(second, entry.getExtraField(new ZipShort(0x1002)));

        ZipExtraField[] returned = entry.getExtraFields();
        returned[0] = null;
        assertEquals(2, entry.getExtraFields().length);
        assertSame(first, entry.getExtraFields()[0]);
    }

    @Test
    public void testAddExtraFieldAndGetExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("add");
        TestExtraField field = new TestExtraField(new ZipShort(0x2001), new byte[] { 3 }, new byte[] { 33 });

        entry.addExtraField(field);
        assertSame(field, entry.getExtraField(new ZipShort(0x2001)));
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testAddExtraFieldReplacesSameHeaderId() {
        ZipArchiveEntry entry = new ZipArchiveEntry("replace");
        TestExtraField first = new TestExtraField(new ZipShort(0x2002), new byte[] { 4 }, new byte[] { 44 });
        TestExtraField replacement = new TestExtraField(new ZipShort(0x2002), new byte[] { 5 }, new byte[] { 55 });

        entry.addExtraField(first);
        entry.addExtraField(replacement);

        assertEquals(1, entry.getExtraFields().length);
        assertSame(replacement, entry.getExtraField(new ZipShort(0x2002)));
    }

    @Test
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("asfirst");
        TestExtraField first = new TestExtraField(new ZipShort(0x3001), new byte[] { 6 }, new byte[] { 66 });
        TestExtraField second = new TestExtraField(new ZipShort(0x3002), new byte[] { 7 }, new byte[] { 77 });

        entry.addExtraField(first);
        entry.addAsFirstExtraField(second);

        assertEquals(2, entry.getExtraFields().length);
        assertSame(second, entry.getExtraFields()[0]);
        assertSame(first, entry.getExtraFields()[1]);
    }

    @Test
    public void testAddAsFirstExtraFieldReplacesSameHeaderId() {
        ZipArchiveEntry entry = new ZipArchiveEntry("asfirstreplace");
        TestExtraField first = new TestExtraField(new ZipShort(0x3003), new byte[] { 8 }, new byte[] { 88 });
        TestExtraField replacement = new TestExtraField(new ZipShort(0x3003), new byte[] { 9 }, new byte[] { 99 });

        entry.addExtraField(first);
        entry.addAsFirstExtraField(replacement);

        assertEquals(1, entry.getExtraFields().length);
        assertSame(replacement, entry.getExtraFields()[0]);
    }

    @Test
    public void testRemoveExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("remove");
        TestExtraField field = new TestExtraField(new ZipShort(0x4001), new byte[] { 10 }, new byte[] { 1010 });

        entry.addExtraField(field);
        entry.removeExtraField(new ZipShort(0x4001));

        assertEquals(0, entry.getExtraFields().length);
        assertNull(entry.getExtraField(new ZipShort(0x4001)));
    }

    @Test
    public void testRemoveExtraFieldThrowsWhenNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("removemissing");
        try {
            entry.removeExtraField(new ZipShort(0x9999));
            fail("Expected IllegalArgumentException for missing extra field");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testSetExtraParsesFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("parse");
        ZipShort id = new ZipShort(0x2222);
        byte[] data = new byte[] { 5, 6, 7, 8 };
        byte[] extra = buildExtra(id, data);

        entry.setExtra(extra);

        ZipExtraField field = entry.getExtraField(id);
        assertNotNull(field);
        assertArrayEquals(data, field.getLocalFileDataData());
        assertArrayEquals(extra, entry.getExtra());
    }

    @Test
    public void testSetExtraNullClearsFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("nullclear");
        entry.addExtraField(new TestExtraField(new ZipShort(0x5001), new byte[] { 1 }, new byte[] { 2 }));

        entry.setExtra(null);

        assertEquals(0, entry.getExtraFields().length);
        assertNull(entry.getExtraField(new ZipShort(0x5001)));
        assertArrayEquals(new byte[0], entry.getLocalFileDataExtra());
    }

    @Test
    public void testLocalFileDataExtraReturnsSetExtraBytes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("local");
        byte[] extra = buildExtra(new ZipShort(0x6001), new byte[] { 0x41, 0x42 });

        entry.setExtra(extra);
        assertArrayEquals(extra, entry.getLocalFileDataExtra());
    }

    @Test
    public void testCentralDirectoryExtraUsesCentralDataNotLocalData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("bug15");
        byte[] localData = new byte[] { 0x01, 0x02, 0x03 };
        byte[] centralData = new byte[] { 0x31, 0x32, 0x33 };
        TestExtraField field = new TestExtraField(new ZipShort(0x1357), localData, centralData);

        entry.addExtraField(field);

        byte[] centralExtra = entry.getCentralDirectoryExtra();
        assertTrue("Central directory extra data should contain central field data",
                contains(centralExtra, centralData));
        assertFalse("Central directory extra data must not contain local field data",
                contains(centralExtra, localData));
    }

    @Test
    public void testSetCentralDirectoryExtraParsesCentralData() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("centralparse");
        ZipShort id = new ZipShort(0x1234);
        byte[] data = new byte[] { 0x11, 0x22, 0x33 };
        byte[] centralExtra = buildExtra(id, data);

        entry.setCentralDirectoryExtra(centralExtra);

        ZipExtraField field = entry.getExtraField(id);
        assertNotNull(field);
        assertArrayEquals(data, field.getCentralDirectoryData());
    }

    @Test
    public void testSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("old");
        entry.setName("new");
        assertEquals("new", entry.getName());
    }

    private static byte[] buildExtra(ZipShort headerId, byte[] data) {
        byte[] header = headerId.getBytes();
        byte[] length = new ZipShort(data.length).getBytes();
        byte[] extra = new byte[header.length + length.length + data.length];
        System.arraycopy(header, 0, extra, 0, header.length);
        System.arraycopy(length, 0, extra, header.length, length.length);
        System.arraycopy(data, 0, extra, header.length + length.length, data.length);
        return extra;
    }

    private static boolean contains(byte[] data, byte[] target) {
        if (target == null || target.length == 0) {
            return true;
        }
        if (data == null || data.length < target.length) {
            return false;
        }
        outer:
        for (int i = 0; i <= data.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (data[i + j] != target[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    private static class TestExtraField implements ZipExtraField {
        private final ZipShort headerId;
        private byte[] localData;
        private byte[] centralData;

        TestExtraField(ZipShort headerId, byte[] localData, byte[] centralData) {
            this.headerId = headerId;
            this.localData = localData == null ? new byte[0] : localData.clone();
            this.centralData = centralData == null ? new byte[0] : centralData.clone();
        }

        @Override
        public ZipShort getHeaderId() {
            return headerId;
        }

        @Override
        public ZipShort getLocalFileDataLength() {
            return new ZipShort(localData.length);
        }

        @Override
        public ZipShort getCentralDirectoryLength() {
            return new ZipShort(centralData.length);
        }

        @Override
        public byte[] getLocalFileDataData() {
            return localData.clone();
        }

        @Override
        public byte[] getCentralDirectoryData() {
            return centralData.clone();
        }

        @Override
        public void parseFromLocalFileData(byte[] data, int offset, int length) {
            localData = Arrays.copyOfRange(data, offset, offset + length);
        }

        @Override
        public void parseFromCentralDirectoryData(byte[] data, int offset, int length) {
            centralData = Arrays.copyOfRange(data, offset, offset + length);
        }
    }
}