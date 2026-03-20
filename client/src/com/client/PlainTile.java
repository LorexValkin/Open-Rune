package com.client;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class PlainTile {

	public PlainTile(int i, int j, int k, int l, int i1, int j1, boolean flag) {
		isFlat = true;
		northEastColor = i;
		southEastColor = j;
		centerColor = k;
		southWestColor = l;
		northWestColor = i1;
		textureId = j1;
		isFlat = flag;
	}

	final int northEastColor;
	final int southEastColor;
	final int centerColor;
	final int southWestColor;
	final int northWestColor;
	boolean isFlat;
	final int textureId;
}
