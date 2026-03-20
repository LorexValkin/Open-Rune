package com.client.definitions;

import com.client.Buffer;
import com.client.JagArchive;

public final class VarBit {

	public static void unpackConfig(JagArchive streamLoader) {
		Buffer stream = new Buffer(streamLoader.getDataForName("varbit.dat"));
		int cacheSize = stream.readUnsignedWord();
		if (cache == null)
			cache = new VarBit[cacheSize];
		for (int j = 0; j < cacheSize; j++) {
			if (cache[j] == null)
				cache[j] = new VarBit();
			cache[j].readValues(stream);
		}

		if (stream.currentOffset != stream.buffer.length)
			System.out.println("varbit load mismatch");
	}

	private void readValues(Buffer stream) {
		settingIndex = stream.readUnsignedWord();
		lowBit = stream.readUnsignedByte();
		highBit = stream.readUnsignedByte();
	}

	private VarBit() {
		
	}

	public static VarBit cache[];
	public int settingIndex;
	public int lowBit;
	public int highBit;
	
}
