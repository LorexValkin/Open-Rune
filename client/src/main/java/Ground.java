// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 


public final class Ground extends Node {

	public Ground(int i, int j, int k)
	{
		obj5Array = new InteractiveObject[5];
		obj5UIDs = new int[5];
		originalPlane = plane = i;
		tileX = j;
		tileY = k;
	}

	int plane;
	final int tileX;
	final int tileY;
	final int originalPlane;
	public PlainTile plainTile;
	public ShapedTile shapedTile;
	public WallObject obj1;
	public WallDecoration obj2;
	public GroundDecoration obj3;
	public GroundItemPile obj4;
	int obj5Count;
	public final InteractiveObject[] obj5Array;
	final int[] obj5UIDs;
	int wallCullDirection;
	int logicHeight;
	boolean visible;
	boolean rendered;
	boolean hasObjects;
	int wallCullPlane0;
	int wallCullPlane1;
	int wallCullPlane2;
	int wallCullPlane3;
	public Ground bridge;
}
