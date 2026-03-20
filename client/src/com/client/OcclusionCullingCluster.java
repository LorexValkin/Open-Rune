package com.client;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class OcclusionCullingCluster {

	OcclusionCullingCluster() {
	}

	int tileMinX;
	int tileMinZ;
	int tileMaxX;
	int tileMaxZ;
	int cullMode;
	int worldMinX;
	int worldMinZ;
	int worldMaxX;
	int worldMaxZ;
	int worldMinY;
	int worldMaxY;
	int cullDirection;
	int searchTop;
	int searchBottom;
	int nearXSlope;
	int farXSlope;
	int nearYSlope;
	int farYSlope;
}
