// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 


final class PlainTile
{

	public PlainTile(int i, int j, int k, int l, int i1, int j1, boolean flag)
	{
		isFlat = true;
		colorNE = i;
		colorNW = j;
		colorSE = k;
		colorSW = l;
		textureId = i1;
		rgbColor = j1;
		isFlat = flag;
	}

	final int colorNE;
	final int colorNW;
	final int colorSE;
	final int colorSW;
	final int textureId;
	boolean isFlat;
	final int rgbColor;
}
