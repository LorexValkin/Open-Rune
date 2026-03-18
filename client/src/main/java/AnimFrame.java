// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
import sign.signlink;

public final class AnimFrame
{

    public static void loadCustomAnimations(int i) {
		i = 50000;
		frameCache = new AnimFrame[i + 1];
		frameMissing = new boolean[i + 1];
		for(int j = 0; j < i + 1; j++)
			frameMissing[j] = true;
    }

	public static int offset = 33600;
	public static void methodCustomAnimations(boolean flag, int file) {
		byte abyte0[];
		abyte0 = FileOperations.ReadFile(signlink.findcachedir() + "data/" + file + ".dat");
		Animation.FrameStart[file] = offset;
		Stream stream = new Stream(abyte0);
		stream.currentOffset = abyte0.length - 8;
		int i = stream.readUnsignedWord();
		int j = stream.readUnsignedWord();
		int k = stream.readUnsignedWord();
		int l = stream.readUnsignedWord();
		int i1 = 0;
		Stream stream_1 = new Stream(abyte0);
		stream_1.currentOffset = i1;
		i1 += i + 2;
		Stream stream_2 = new Stream(abyte0);
		stream_2.currentOffset = i1;
		i1 += j;
		Stream stream_3 = new Stream(abyte0);
		stream_3.currentOffset = i1;
		i1 += k;
		Stream stream_4 = new Stream(abyte0);
		stream_4.currentOffset = i1;
		i1 += l;
		Stream stream_5 = new Stream(abyte0);
		stream_5.currentOffset = i1;
		if(flag) {
			for(int j1 = 1; j1 > 0; j1++);
		}
		AnimBase animBase = new AnimBase(stream_5);
		int k1 = stream_1.readUnsignedWord();
		int ai[] = new int[500];
		int ai1[] = new int[500];
		int ai2[] = new int[500];
		int ai3[] = new int[500];
		for(int l1 = 0; l1 < k1; l1++) {
			int i2 = stream_1.readUnsignedWord();
			i2 = offset;
			offset++;
			AnimFrame animFrame = frameCache[i2] = new AnimFrame();
			animFrame.displayLength = stream_4.readUnsignedByte();
			animFrame.base = animBase;
			int j2 = stream_1.readUnsignedByte();
			int k2 = -1;
			int l2 = 0;
			for(int i3 = 0; i3 < j2; i3++) {
				int j3 = stream_2.readUnsignedByte();
				if(j3 > 0) {
					if(animBase.badCombinations[i3] != 0) {
						for(int l3 = i3 - 1; l3 > k2; l3--) {
							if(animBase.badCombinations[l3] != 0)
								continue;
							ai[l2] = l3;
							ai1[l2] = 0;
							ai2[l2] = 0;
							ai3[l2] = 0;
							l2++;
							break;
						}
					}
					ai[l2] = i3;
					char c = '\0';
					if(animBase.badCombinations[i3] == 3)
						c = '\200';
					if((j3 & 1) != 0)
						ai1[l2] = stream_3.readSmartSigned();
					else
						ai1[l2] = c;
					if((j3 & 2) != 0)
						ai2[l2] = stream_3.readSmartSigned();
					else
						ai2[l2] = c;
					if((j3 & 4) != 0)
						ai3[l2] = stream_3.readSmartSigned();
					else
						ai3[l2] = c;
					k2 = i3;
					l2++;
					if(animBase.badCombinations[i3] == 5)
						frameMissing[i2] = false;
				}
			}
			animFrame.transformCount = l2;
			animFrame.transformTypes = new int[l2];
			animFrame.transformX = new int[l2];
			animFrame.transformY = new int[l2];
			animFrame.transformZ = new int[l2];
			for(int k3 = 0; k3 < l2; k3++) {
				animFrame.transformTypes[k3] = ai[k3];
				animFrame.transformX[k3] = ai1[k3];
				animFrame.transformY[k3] = ai2[k3];
				animFrame.transformZ[k3] = ai3[k3];
			}
		}
	}

	public static void decodeFrames(byte abyte0[])
	{
		Stream stream = new Stream(abyte0);
		stream.currentOffset = abyte0.length - 8;
		int i = stream.readUnsignedWord();
		int j = stream.readUnsignedWord();
		int k = stream.readUnsignedWord();
		int l = stream.readUnsignedWord();
		int i1 = 0;
		Stream stream_1 = new Stream(abyte0);
		stream_1.currentOffset = i1;
		i1 += i + 2;
		Stream stream_2 = new Stream(abyte0);
		stream_2.currentOffset = i1;
		i1 += j;
		Stream stream_3 = new Stream(abyte0);
		stream_3.currentOffset = i1;
		i1 += k;
		Stream stream_4 = new Stream(abyte0);
		stream_4.currentOffset = i1;
		i1 += l;
		Stream stream_5 = new Stream(abyte0);
		stream_5.currentOffset = i1;
		AnimBase animBase = new AnimBase(stream_5);
		int k1 = stream_1.readUnsignedWord();
		int ai[] = new int[500];
		int ai1[] = new int[500];
		int ai2[] = new int[500];
		int ai3[] = new int[500];
		for(int l1 = 0; l1 < k1; l1++)
		{
			int i2 = stream_1.readUnsignedWord();
			AnimFrame animFrame = frameCache[i2] = new AnimFrame();
			animFrame.displayLength = stream_4.readUnsignedByte();
			animFrame.base = animBase;
			int j2 = stream_1.readUnsignedByte();
			int k2 = -1;
			int l2 = 0;
			for(int i3 = 0; i3 < j2; i3++)
			{
				int j3 = stream_2.readUnsignedByte();
				if(j3 > 0)
				{
					if(animBase.badCombinations[i3] != 0)
					{
						for(int l3 = i3 - 1; l3 > k2; l3--)
						{
							if(animBase.badCombinations[l3] != 0)
								continue;
							ai[l2] = l3;
							ai1[l2] = 0;
							ai2[l2] = 0;
							ai3[l2] = 0;
							l2++;
							break;
						}

					}
					ai[l2] = i3;
					char c = '\0';
					if(animBase.badCombinations[i3] == 3)
						c = '\200';
					if((j3 & 1) != 0)
						ai1[l2] = stream_3.readSmartSigned();
					else
						ai1[l2] = c;
					if((j3 & 2) != 0)
						ai2[l2] = stream_3.readSmartSigned();
					else
						ai2[l2] = c;
					if((j3 & 4) != 0)
						ai3[l2] = stream_3.readSmartSigned();
					else
						ai3[l2] = c;
					k2 = i3;
					l2++;
					if(animBase.badCombinations[i3] == 5)
						frameMissing[i2] = false;
				}
			}

			animFrame.transformCount = l2;
			animFrame.transformTypes = new int[l2];
			animFrame.transformX = new int[l2];
			animFrame.transformY = new int[l2];
			animFrame.transformZ = new int[l2];
			for(int k3 = 0; k3 < l2; k3++)
			{
				animFrame.transformTypes[k3] = ai[k3];
				animFrame.transformX[k3] = ai1[k3];
				animFrame.transformY[k3] = ai2[k3];
				animFrame.transformZ[k3] = ai3[k3];
			}

		}

	}

	public static void nullLoader()
	{
		frameCache = null;
	}

	public static AnimFrame getFrame(int j)
	{
		if(frameCache == null)
			return null;
		else
			return frameCache[j];
	}

	public static boolean isFrameLoaded(int i)
	{
		return i == -1;
	}

	private AnimFrame()
	{
	}

	private static AnimFrame[] frameCache;
	public int displayLength;
	public AnimBase base;
	public int transformCount;
	public int transformTypes[];
	public int transformX[];
	public int transformY[];
	public int transformZ[];
	private static boolean[] frameMissing;

}
