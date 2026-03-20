package com.client;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class SceneTile extends CacheableNode {

	public SceneTile(int i, int j, int k) {
		obj5Array = new SceneObject[5];
		objectTypeFlags = new int[5];
		originLevel = renderLevel = i;
		tileX = j;
		tileY = k;
	}

	int renderLevel;
	final int tileX;
	final int tileY;
	final int originLevel;
	public PlainTile plainTile;
	public ShapedTile shapedTile;
	public WallObject obj1;
	public WallDecoration obj2;
	public GroundDecoration obj3;
	public InteractiveObject obj4;
	int objectCount;
	public final SceneObject[] obj5Array;
	final int[] objectTypeFlags;
	int combinedFlags;
	int logicHeight;
	boolean visible;
	boolean rendered;
	boolean hasObjects;
	int wallDrawFlags;
	int wallCullDirection;
	int wallCullOpposite;
	int wallUncullDirection;
	public SceneTile linkedTile;
}
