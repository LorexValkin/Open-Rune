// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Animable_Sub5 extends Animable {

	public Model getRotatedModel()
	{
		int j = -1;
		if(animation != null)
		{
			int k = client.loopCycle - animStartCycle;
			if(k > 100 && animation.loopOffset > 0)
				k = 100;
			while(k > animation.getFrameDuration(animFrame))
			{
				k -= animation.getFrameDuration(animFrame);
				animFrame++;
				if(animFrame < animation.frameCount)
					continue;
				animFrame -= animation.loopOffset;
				if(animFrame >= 0 && animFrame < animation.frameCount)
					continue;
				animation = null;
				break;
			}
			animStartCycle = client.loopCycle - k;
			if(animation != null)
				j = animation.frameIds[animFrame];
		}
		ObjectDef class46;
		if(childrenIDs != null)
			class46 = getChildObjectDef();
		else
			class46 = ObjectDef.forID(objectId);
		if(class46 == null)
		{
			return null;
		} else
		{
			return class46.getObjectModel(objectType, objectFace, southWestX, southWestY, northEastX, northEastY, j);
		}
	}

	private ObjectDef getChildObjectDef()
	{
		int i = -1;
		if(varbitId != -1)
		{
			VarBit varBit = VarBit.cache[varbitId];
			int k = varBit.settingIndex;
			int l = varBit.lowBit;
			int i1 = varBit.highBit;
			int j1 = client.anIntArray1232[i1 - l];
			i = clientInstance.variousSettings[k] >> l & j1;
		} else
		if(settingId != -1)
			i = clientInstance.variousSettings[settingId];
		if(i < 0 || i >= childrenIDs.length || childrenIDs[i] == -1)
			return null;
		else
			return ObjectDef.forID(childrenIDs[i]);
	}

	public Animable_Sub5(int i, int j, int k, int l, int i1, int j1,
						 int k1, int l1, boolean flag)
	{
		objectId = i;
		objectType = k;
		objectFace = j;
		southWestX = j1;
		southWestY = l;
		northEastX = i1;
		northEastY = k1;
		if(l1 != -1)
		{
			animation = Animation.anims[l1];
			animFrame = 0;
			animStartCycle = client.loopCycle;
			if(flag && animation.loopOffset != -1)
			{
				animFrame = (int)(Math.random() * (double) animation.frameCount);
				animStartCycle -= (int)(Math.random() * (double) animation.getFrameDuration(animFrame));
			}
		}
		ObjectDef class46 = ObjectDef.forID(objectId);
		varbitId = class46.varbitId;
		settingId = class46.settingId;
		childrenIDs = class46.childrenIDs;
	}

	private int animFrame;
	private final int[] childrenIDs;
	private final int varbitId;
	private final int settingId;
	private final int southWestX;
	private final int southWestY;
	private final int northEastX;
	private final int northEastY;
	private Animation animation;
	private int animStartCycle;
	public static client clientInstance;
	private final int objectId;
	private final int objectType;
	private final int objectFace;
}
