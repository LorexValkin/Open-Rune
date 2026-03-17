// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 


final class SpawnObjectNode extends Node {

	SpawnObjectNode()
	{
		delay = -1;
	}

	public int objectId;
	public int objectType;
	public int objectOrientation;
	public int delay;
	public int objectPlane;
	public int group;
	public int objectX;
	public int objectY;
	public int previousId;
	public int previousOrientation;
	public int previousType;
	public int longestDelay;
}
