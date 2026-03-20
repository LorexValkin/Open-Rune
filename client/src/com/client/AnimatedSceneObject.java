package com.client;

import com.client.definitions.AnimationDefinition;
import com.client.definitions.ObjectDefinition;
import com.client.definitions.VarBit;

final class AnimatedSceneObject extends Renderable {
	private int animFrameIndex;
	private final int[] childObjectIds;
	private final int varbitId;
	private final int varpId;
	private final int heightNE;
	private final int heightNW;
	private final int heightSE;
	private final int heightSW;
	private AnimationDefinition objectAnimation;
	private int animStartCycle;
	public static Client clientInstance;
	private final int objectDefId;
	private final int objectType;
	private final int objectRotation;

	/*
	 * private ObjectDef method457() { int i = -1; if(varbitId != -1) { try {
	 * VarBit varBit = VarBit.overlays[varbitId]; int k = varBit.settingIndex; int l =
	 * varBit.lowBit; int i1 = varBit.highBit; int j1 =
	 * client.anIntArray1232[i1 - l]; i = clientInstance.variousSettings[k] >> l
	 * & j1; } catch(Exception ex){} } else if(varpId != -1) i =
	 * clientInstance.variousSettings[varpId]; if(i < 0 || i >=
	 * childObjectIds.length || childObjectIds[i] == -1) return null; else
	 * return ObjectDef.forID(childObjectIds[i]); }
	 */
	private ObjectDefinition method457() {
		int i = -1;
		if (varbitId != -1 && varbitId < VarBit.cache.length) {
			VarBit varBit = VarBit.cache[varbitId];
			int k = varBit.settingIndex;
			int l = varBit.lowBit;
			int i1 = varBit.highBit;
			int j1 = Client.anIntArray1232[i1 - l];
			i = clientInstance.variousSettings[k] >> l & j1;
		} else if (varpId != -1
				&& varpId < clientInstance.variousSettings.length)
			i = clientInstance.variousSettings[varpId];
		if (i < 0 || i >= childObjectIds.length || childObjectIds[i] == -1)
			return null;
		else
			return ObjectDefinition.forID(childObjectIds[i]);
	}

	@Override
	public Model getRotatedModel() {
		int j = -1;
		if (objectAnimation != null) {
			int k = Client.loopCycle - animStartCycle;
			if (k > 100 && objectAnimation.anInt356 > 0)
				k = 100;
			while (k > objectAnimation.getFrameDuration(animFrameIndex)) {
				k -= objectAnimation.getFrameDuration(animFrameIndex);
				animFrameIndex++;
				if (animFrameIndex < objectAnimation.anInt352)
					continue;
				animFrameIndex -= objectAnimation.anInt356;
				if (animFrameIndex >= 0 && animFrameIndex < objectAnimation.anInt352)
					continue;
				objectAnimation = null;
				break;
			}
			animStartCycle = Client.loopCycle - k;
			if (objectAnimation != null)
				j = objectAnimation.primaryFrames[animFrameIndex];
		}
		ObjectDefinition class46;
		if (childObjectIds != null)
			class46 = method457();
		else
			class46 = ObjectDefinition.forID(objectDefId);
		if (class46 == null) {
			return null;
		} else {
			return class46.modelAt(objectType, objectRotation, heightNE,
					heightNW, heightSE, heightSW, j);
		}
	}

	public AnimatedSceneObject(int i, int j, int k, int l, int i1, int j1, int k1,
			int l1, boolean flag) {
		objectDefId = i;
		objectType = k;
		objectRotation = j;
		heightNE = j1;
		heightNW = l;
		heightSE = i1;
		heightSW = k1;
		if (l1 != -1) {
			objectAnimation = AnimationDefinition.anims[l1];
			animFrameIndex = 0;
			animStartCycle = Client.loopCycle;
			if (flag && objectAnimation.anInt356 != -1) {
				animFrameIndex = (int) (Math.random() * objectAnimation.anInt352);
				animStartCycle -= (int) (Math.random() * objectAnimation
						.getFrameDuration(animFrameIndex));
			}
		}
		ObjectDefinition class46 = ObjectDefinition.forID(objectDefId);
		varbitId = class46.anInt774;
		varpId = class46.anInt749;
		childObjectIds = class46.childrenIDs;
	}
}