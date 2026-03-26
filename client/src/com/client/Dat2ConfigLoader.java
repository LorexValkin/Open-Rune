package com.client;



/**
 * Loads definition data from a dat2 cache config archive.
 *
 * In dat2, definitions are stored in index 2 (configs):
 *   - Group 9 = NPCs, Group 10 = Items, Group 6 = Objects
 *   - Group 12 = Sequences (animations), Group 13 = SpotAnims
 *   - Group 1 = Underlays, Group 3 = Overlays, Group 2 = IdentKits
 *   - Group 14 = VarPlayers, Group 16 = VarBits
 *
 * Each group is a multi-file container where each file is one definition.
 * The reference table (from idx255) tells us how many files exist.
 */
public class Dat2ConfigLoader {

    // Config group IDs in OSRS dat2 format
    public static final int GROUP_UNDERLAYS = 1;
    public static final int GROUP_IDENTKITS = 3;
    public static final int GROUP_OVERLAYS = 4;
    public static final int GROUP_INVENTORIES = 5;
    public static final int GROUP_OBJECTS = 6;
    public static final int GROUP_ENUMS = 8;
    public static final int GROUP_NPCS = 9;
    public static final int GROUP_ITEMS = 10;
    public static final int GROUP_SEQUENCES = 12;
    public static final int GROUP_SPOTANIMS = 13;
    public static final int GROUP_VARPLAYERS = 14;
    public static final int GROUP_VARBITS = 16;

    private final Decompressor configIndex;   // index 2 decompressor
    private final Decompressor metaIndex;     // index 255 decompressor
    private int[] refTableGroupIds;
    private int[][] refTableFileIds;
    private int[] refTableFileCounts;
    private int refTableGroupCount;

    public Dat2ConfigLoader(Decompressor configIndex, Decompressor metaIndex) {
        this.configIndex = configIndex;
        this.metaIndex = metaIndex;
        loadReferenceTable();
    }

    /**
     * Load definitions from a specific config group.
     * Returns an array of byte arrays, indexed by file position (0..fileCount-1).
     * Each byte[] is one definition's opcode data.
     */
    public byte[][] loadGroup(int groupId) {
        // Find this group in the reference table
        int groupIndex = -1;
        for (int i = 0; i < refTableGroupCount; i++) {
            if (refTableGroupIds[i] == groupId) {
                groupIndex = i;
                break;
            }
        }
        if (groupIndex < 0) {
            System.out.println("[Dat2Config] Group " + groupId + " not found in reference table");
            return null;
        }

        int fileCount = refTableFileCounts[groupIndex];
        int[] fileIds = refTableFileIds[groupIndex];

        // Read the group container from index 2
        byte[] raw = configIndex.decompress(groupId);
        if (raw == null) {
            System.out.println("[Dat2Config] Failed to read group " + groupId + " from config index");
            return null;
        }

        // Decompress the container
        byte[] data = ContainerDecompressor.decompress(raw);
        if (data == null) {
            System.out.println("[Dat2Config] Failed to decompress group " + groupId);
            return null;
        }

        // Single file — return as-is
        if (fileCount == 1) {
            return new byte[][] { data };
        }

        // Multi-file — split using the accumulated size table at the end
        return splitGroupFiles(data, fileCount);
    }

    /**
     * Get the number of files in a group.
     */
    public int getFileCount(int groupId) {
        for (int i = 0; i < refTableGroupCount; i++) {
            if (refTableGroupIds[i] == groupId) {
                return refTableFileCounts[i];
            }
        }
        return 0;
    }

    /**
     * Get the file IDs for a group.
     */
    public int[] getFileIds(int groupId) {
        for (int i = 0; i < refTableGroupCount; i++) {
            if (refTableGroupIds[i] == groupId) {
                return refTableFileIds[i];
            }
        }
        return new int[0];
    }

    // ─── Reference table parsing ────────────────────────────────────

    private void loadReferenceTable() {
        // Read config index reference table from idx255, archive 2
        byte[] raw = metaIndex.decompress(2);
        if (raw == null) {
            System.out.println("[Dat2Config] Failed to read config reference table from idx255");
            return;
        }

        byte[] data = ContainerDecompressor.decompress(raw);
        if (data == null) {
            System.out.println("[Dat2Config] Failed to decompress config reference table");
            return;
        }

        Buffer buf = new Buffer(data);
        int protocol = buf.readUnsignedByte();

        if (protocol >= 6) {
            buf.readDWord(); // revision
        }

        int flags = buf.readUnsignedByte();
        boolean named = (flags & 0x01) != 0;
        boolean hasSizes = (flags & 0x04) != 0;

        // Group count
        refTableGroupCount = (protocol >= 7) ? readBigSmart(buf) : buf.readUnsignedWord();

        // Group ID deltas
        refTableGroupIds = new int[refTableGroupCount];
        int accum = 0;
        for (int i = 0; i < refTableGroupCount; i++) {
            accum += (protocol >= 7) ? readBigSmart(buf) : buf.readUnsignedWord();
            refTableGroupIds[i] = accum;
        }

        // Name hashes (skip)
        if (named) {
            for (int i = 0; i < refTableGroupCount; i++) {
                buf.readDWord();
            }
        }

        // CRCs (skip)
        for (int i = 0; i < refTableGroupCount; i++) buf.readDWord();

        // Whirlpool (skip)
        if ((flags & 0x02) != 0) {
            for (int i = 0; i < refTableGroupCount; i++) {
                buf.currentOffset += 64;
            }
        }

        // Sizes (skip)
        if (hasSizes) {
            for (int i = 0; i < refTableGroupCount; i++) {
                buf.readDWord();
                buf.readDWord();
            }
        }

        // Versions (skip)
        for (int i = 0; i < refTableGroupCount; i++) buf.readDWord();

        // File counts
        refTableFileCounts = new int[refTableGroupCount];
        for (int i = 0; i < refTableGroupCount; i++) {
            refTableFileCounts[i] = (protocol >= 7) ? readBigSmart(buf) : buf.readUnsignedWord();
        }

        // File ID deltas
        refTableFileIds = new int[refTableGroupCount][];
        for (int i = 0; i < refTableGroupCount; i++) {
            int count = refTableFileCounts[i];
            int[] ids = new int[count];
            int fileAccum = 0;
            for (int j = 0; j < count; j++) {
                fileAccum += (protocol >= 7) ? readBigSmart(buf) : buf.readUnsignedWord();
                ids[j] = fileAccum;
            }
            refTableFileIds[i] = ids;
        }

        System.out.println("[Dat2Config] Reference table loaded: " + refTableGroupCount + " groups");
    }

    private int readBigSmart(Buffer buf) {
        int peek = buf.buffer[buf.currentOffset] & 0xFF;
        if (peek >= 128) {
            return buf.readDWord() & 0x7FFFFFFF;
        } else {
            return buf.readUnsignedWord();
        }
    }

    // ─── Multi-file group splitting ─────────────────────────────────

    /**
     * Split a multi-file group using accumulated size table at the end.
     */
    private byte[][] splitGroupFiles(byte[] data, int fileCount) {
        if (data.length == 0 || fileCount == 0) return null;

        int chunks = data[data.length - 1] & 0xFF;
        if (chunks == 0) return null;

        int sizeTableLength = fileCount * chunks * 4 + 1;
        if (sizeTableLength > data.length) return null;

        int sizeTableStart = data.length - sizeTableLength;

        // First pass: compute total sizes per file
        int[] totalSizes = new int[fileCount];
        int pos = sizeTableStart;
        for (int chunk = 0; chunk < chunks; chunk++) {
            int chunkSize = 0;
            for (int file = 0; file < fileCount; file++) {
                chunkSize += readInt(data, pos);
                pos += 4;
                totalSizes[file] += chunkSize;
            }
        }

        // Second pass: extract file data
        byte[][] files = new byte[fileCount][];
        int[] fileWriteOffsets = new int[fileCount];
        for (int i = 0; i < fileCount; i++) {
            files[i] = new byte[totalSizes[i]];
        }

        pos = sizeTableStart;
        int dataOffset = 0;
        for (int chunk = 0; chunk < chunks; chunk++) {
            int chunkSize = 0;
            for (int file = 0; file < fileCount; file++) {
                chunkSize += readInt(data, pos);
                pos += 4;
                if (chunkSize > 0 && dataOffset + chunkSize <= sizeTableStart) {
                    System.arraycopy(data, dataOffset, files[file], fileWriteOffsets[file], chunkSize);
                    fileWriteOffsets[file] += chunkSize;
                }
                dataOffset += chunkSize;
            }
        }

        return files;
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
    }
}
