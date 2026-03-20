package com.client;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SpawnedObject extends CacheableNode {

	SpawnedObject() {
		spawnDelay = -1;
	}

	public int previousId;
	public int previousOrientation;
	public int previousType;
	public int spawnDelay;
	public int heightLevel;
	public int objectGroup;
	public int localX;
	public int localY;
	public int objectId;
	public int objectOrientation;
	public int objectType;
	public int retryCount;
}
