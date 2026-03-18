// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class AnimBase
{

	public AnimBase(Stream stream)
	{
		int anInt341 = stream.readUnsignedByte();
		badCombinations = new int[anInt341];
		badWordFragments = new int[anInt341][];
		for(int j = 0; j < anInt341; j++)
			badCombinations[j] = stream.readUnsignedByte();

		for(int k = 0; k < anInt341; k++)
		{
			int l = stream.readUnsignedByte();
			badWordFragments[k] = new int[l];
			for(int i1 = 0; i1 < l; i1++)
				badWordFragments[k][i1] = stream.readUnsignedByte();

		}

	}

	public final int[] badCombinations;
	public final int[][] badWordFragments;
}
