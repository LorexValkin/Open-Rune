package com.client;

import com.client.definitions.AnimationDefinition;

public class Entity extends Renderable {

	public final void setPos(int i, int j, boolean flag) {
		if (anim != -1 && AnimationDefinition.anims[anim].anInt364 == 1)
			anim = -1;
		if (!flag) {
			int k = i - smallX[0];
			int l = j - smallY[0];
			if (k >= -8 && k <= 8 && l >= -8 && l <= 8) {
				if (smallXYIndex < 9)
					smallXYIndex++;
				for (int i1 = smallXYIndex; i1 > 0; i1--) {
					smallX[i1] = smallX[i1 - 1];
					smallY[i1] = smallY[i1 - 1];
					pathRun[i1] = pathRun[i1 - 1];
				}

				smallX[0] = i;
				smallY[0] = j;
				pathRun[0] = false;
				return;
			}
		}
		smallXYIndex = 0;
		pathEndIndex = 0;
		moveDelay = 0;
		smallX[0] = i;
		smallY[0] = j;
		x = smallX[0] * 128 + tileSize * 64;
		y = smallY[0] * 128 + tileSize * 64;
	}

	public final void method446() {
		smallXYIndex = 0;
		pathEndIndex = 0;
	}

	public final void updateHitData(int j, int k, int l) {
		for (int i1 = 0; i1 < 4; i1++)
			if (hitsLoopCycle[i1] <= l) {
				hitArray[i1] = k;
				hitMarkTypes[i1] = j;
				hitsLoopCycle[i1] = l + 70;
				return;
			}
	}

	public final void moveInDir(boolean flag, int i) {
		int j = smallX[0];
		int k = smallY[0];
		if (i == 0) {
			j--;
			k++;
		}
		if (i == 1)
			k++;
		if (i == 2) {
			j++;
			k++;
		}
		if (i == 3)
			j--;
		if (i == 4)
			j++;
		if (i == 5) {
			j--;
			k--;
		}
		if (i == 6)
			k--;
		if (i == 7) {
			j++;
			k--;
		}
		if (anim != -1 && AnimationDefinition.anims[anim].anInt364 == 1)
			anim = -1;
		if (smallXYIndex < 9)
			smallXYIndex++;
		for (int l = smallXYIndex; l > 0; l--) {
			smallX[l] = smallX[l - 1];
			smallY[l] = smallY[l - 1];
			pathRun[l] = pathRun[l - 1];
		}
		smallX[0] = j;
		smallY[0] = k;
		pathRun[0] = flag;
	}

	public int entScreenX;
	public int entScreenY;
	public final int index = -1;

	public boolean isVisible() {
		return false;
	}

	Entity() {
		smallX = new int[10];
		smallY = new int[10];
		interactingEntity = -1;
		degreesToTurn = 32;
		forcedAnimId = -1;
		height = 200;
		standAnimId = -1;
		standTurnAnimId = -1;
		hitArray = new int[4];
		hitMarkTypes = new int[4];
		hitsLoopCycle = new int[4];
		currentAnimId = -1;
		spotAnimId = -1;
		anim = -1;
		loopCycleStatus = -1000;
		textCycle = 100;
		tileSize = 1;
		isVisible = false;
		pathRun = new boolean[10];
		walkAnimId = -1;
		turnAroundAnimId = -1;
		turnRightAnimId = -1;
		turnLeftAnimId = -1;
	}

	public final int[] smallX;
	public final int[] smallY;
	public int interactingEntity;
	int moveDelay;
	int degreesToTurn;
	int forcedAnimId;
	public String textSpoken;
	public int height;
	public int turnDirection;
	int standAnimId;
	int standTurnAnimId;
	int chatAnimId;
	final int[] hitArray;
	final int[] hitMarkTypes;
	final int[] hitsLoopCycle;
	int currentAnimId;
	int animFrameIndex;
	int animFrameDelay;
	int spotAnimId;
	int spotAnimFrame;
	int spotAnimDelay;
	int spotAnimEndCycle;
	int spotAnimHeight;
	int smallXYIndex;
	public int anim;
	int animDelayCycle;
	int animResetCycle;
	int animEndCycle;
	int animCycleDelay;
	int graphicId;
	public int loopCycleStatus;
	public int currentHealth;
	public int maxHealth;
	int textCycle;
	int lastUpdateCycle;
	int faceX;
	int faceY;
	int tileSize;
	boolean isVisible;
	int pathEndIndex;
	int moveStartX;
	int moveStartY;
	int moveEndX;
	int moveEndY;
	int moveStartCycle;
	int moveEndCycle;
	int moveDirection;
	public int x;
	public int y;
	int currentFaceDirection;
	final boolean[] pathRun;
	int walkAnimId;
	int turnAroundAnimId;
	int turnRightAnimId;
	int turnLeftAnimId;
}
