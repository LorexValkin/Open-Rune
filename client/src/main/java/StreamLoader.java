// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

final class StreamLoader {

	public StreamLoader(byte abyte0[])
	{
		Stream stream = new Stream(abyte0);
		int i = stream.read3Bytes();
		int j = stream.read3Bytes();
		if(j != i)
		{
			byte abyte1[] = new byte[i];
			BZip2Decoder.decompress(abyte1, i, abyte0, j, 6);
			aByteArray726 = abyte1;
			stream = new Stream(aByteArray726);
			isCompressed = true;
		} else
		{
			aByteArray726 = abyte0;
			isCompressed = false;
		}
		dataSize = stream.readUnsignedWord();
		nameHashes = new int[dataSize];
		fileSizes = new int[dataSize];
		decompressedSizes = new int[dataSize];
		fileOffsets = new int[dataSize];
		int k = stream.currentOffset + dataSize * 10;
		for(int l = 0; l < dataSize; l++)
		{
			nameHashes[l] = stream.readDWord();
			fileSizes[l] = stream.read3Bytes();
			decompressedSizes[l] = stream.read3Bytes();
			fileOffsets[l] = k;
			k += decompressedSizes[l];
		}
	}

	public byte[] getDataForName(String s)
	{
		byte abyte0[] = null; //was a parameter
		int i = 0;
		s = s.toUpperCase();
		for(int j = 0; j < s.length(); j++)
			i = (i * 61 + s.charAt(j)) - 32;
		if (s.equalsIgnoreCase("NPC.DAT") || s.equalsIgnoreCase("NPC.IDX"))
			System.out.println("");

		for(int k = 0; k < dataSize; k++)
			if(nameHashes[k] == i)
			{
				if(abyte0 == null)
					abyte0 = new byte[fileSizes[k]];
				if(!isCompressed)
				{
					BZip2Decoder.decompress(abyte0, fileSizes[k], aByteArray726, decompressedSizes[k], fileOffsets[k]);
					if (s.equalsIgnoreCase("NPC.DAT") || s.equalsIgnoreCase("NPC.IDX"))
						System.out.println("");
				} else
				{
					System.arraycopy(aByteArray726, fileOffsets[k], abyte0, 0, fileSizes[k]);
				}
				return abyte0;
			}

		return null;
	}
	
	public byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	
	private final byte[] aByteArray726;
	private final int dataSize;
	private final int[] nameHashes;
	private final int[] fileSizes;
	private final int[] decompressedSizes;
	private final int[] fileOffsets;
	private final boolean isCompressed;
}
