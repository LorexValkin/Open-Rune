// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SoundEnvelope
{

	public void decodeSE(Stream stream)
	{
		formEnd = stream.readUnsignedByte();
			formDuration = stream.readDWord();
			formStart = stream.readDWord();
			decodeShape(stream);
	}

	public void decodeShape(Stream stream)
	{
		segmentCount = stream.readUnsignedByte();
		segmentDuration = new int[segmentCount];
		segmentPeak = new int[segmentCount];
		for(int i = 0; i < segmentCount; i++)
		{
			segmentDuration[i] = stream.readUnsignedWord();
			segmentPeak[i] = stream.readUnsignedWord();
		}

	}

	void resetValues()
	{
		currentSegment = 0;
		segmentPosition = 0;
		currentPeak = 0;
		nextPeak = 0;
		interpolationStep = 0;
	}

	int evaluateSE(int i)
	{
		if(interpolationStep >= currentSegment)
		{
			nextPeak = segmentPeak[segmentPosition++] << 15;
			if(segmentPosition >= segmentCount)
				segmentPosition = segmentCount - 1;
			currentSegment = (int)(((double)segmentDuration[segmentPosition] / 65536D) * (double)i);
			if(currentSegment > interpolationStep)
				currentPeak = ((segmentPeak[segmentPosition] << 15) - nextPeak) / (currentSegment - interpolationStep);
		}
		nextPeak += currentPeak;
		interpolationStep++;
		return nextPeak - currentPeak >> 15;
	}

	public SoundEnvelope()
	{
	}

	private int segmentCount;
	private int[] segmentDuration;
	private int[] segmentPeak;
	int formDuration;
	int formStart;
	int formEnd;
	private int currentSegment;
	private int segmentPosition;
	private int currentPeak;
	private int nextPeak;
	private int interpolationStep;
	public static int sampleRate;
}
