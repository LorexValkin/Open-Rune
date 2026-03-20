package com.client;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class VarPlayer {

	public static int cacheSize;
	
	public static void unpackConfig(JagArchive streamLoader) {
		Buffer stream = new Buffer(streamLoader.getDataForName("varp.dat"));
		highAlchCount = 0;
		cacheSize = stream.readUnsignedWord();
		if (cache == null)
			cache = new VarPlayer[cacheSize + 1000];
		if (alchIndices == null)
			alchIndices = new int[cacheSize];
		for (int j = 0; j < cacheSize; j++) {
			if (cache[j] == null)
				cache[j] = new VarPlayer();
			cache[j].readValues(stream, j);
		}
		if (stream.currentOffset != stream.buffer.length)
			System.out.println("varptype load mismatch");
	}

	private void readValues(Buffer stream, int i) {
		do {
			int j = stream.readUnsignedByte();
			if (j == 0)
				return;
			if (j == 1)
				stream.readUnsignedByte();
			else if (j == 2)
				stream.readUnsignedByte();
			else if (j == 3)
				alchIndices[highAlchCount++] = i;
			else if (j == 4) {
			} else if (j == 5)
				configType = stream.readUnsignedWord();
			else if (j == 6) {
			} else if (j == 7)
				stream.readDWord();
			else if (j == 8)
				isMembersOnly = true;
			else if (j == 10)
				stream.readString();
			else if (j == 11)
				isMembersOnly = true;
			else if (j == 12)
				stream.readDWord();
			else if (j == 13) {
			} else
				System.out.println("Error unrecognised config code: " + j);
		} while (true);
	}

	private VarPlayer() {
		isMembersOnly = false;
	}

	public static VarPlayer cache[];
	private static int highAlchCount;
	private static int[] alchIndices;
	public int configType;
	public boolean isMembersOnly;

}
