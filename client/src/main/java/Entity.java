// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
import java.util.Random;

public class Entity extends Animable {

	public final void setPos(int i, int j, boolean flag)
	{
		if(anim != -1 && Animation.anims[anim].anInt364 == 1)
			anim = -1;
		if(!flag)
		{
			int k = i - smallX[0];
			int l = j - smallY[0];
			if(k >= -8 && k <= 8 && l >= -8 && l <= 8)
			{
				if(smallXYIndex < 9)
					smallXYIndex++;
				for(int i1 = smallXYIndex; i1 > 0; i1--)
				{
					smallX[i1] = smallX[i1 - 1];
					smallY[i1] = smallY[i1 - 1];
					pathRunning[i1] = pathRunning[i1 - 1];
				}

				smallX[0] = i;
				smallY[0] = j;
				pathRunning[0] = false;
				return;
			}
		}
		smallXYIndex = 0;
		pathRemainder = 0;
		stepDelayCounter = 0;
		smallX[0] = i;
		smallY[0] = j;
		x = smallX[0] * 128 + tileSize * 64;
		y = smallY[0] * 128 + tileSize * 64;
	}

	public final void resetPath()
	{
		smallXYIndex = 0;
		pathRemainder = 0;
	}

	public final void updateHitData(int j, int k, int l)
	{
		for(int i1 = 0; i1 < 4; i1++)
			if(hitsLoopCycle[i1] <= l)
			{
				hitArray[i1] = k * ((client.newDamage == true && k > 0) ? 10 : 1);
				if (client.newDamage && k > 0) {
					hitArray[i1] += new java.util.Random().nextInt(9);
				}
				hitMarkTypes[i1] = j;
				hitsLoopCycle[i1] = l + 70;
				return;
			}
	}

	public final void moveInDir(boolean flag, int i)
	{
		int j = smallX[0];
		int k = smallY[0];
		if(i == 0)
		{
			j--;
			k++;
		}
		if(i == 1)
			k++;
		if(i == 2)
		{
			j++;
			k++;
		}
		if(i == 3)
			j--;
		if(i == 4)
			j++;
		if(i == 5)
		{
			j--;
			k--;
		}
		if(i == 6)
			k--;
		if(i == 7)
		{
			j++;
			k--;
		}
		if(anim != -1 && Animation.anims[anim].anInt364 == 1)
			anim = -1;
		if(smallXYIndex < 9)
			smallXYIndex++;
		for(int l = smallXYIndex; l > 0; l--)
		{
			smallX[l] = smallX[l - 1];
			smallY[l] = smallY[l - 1];
			pathRunning[l] = pathRunning[l - 1];
		}
			smallX[0] = j;
			smallY[0] = k;
			pathRunning[0] = flag;
	}

	public int entScreenX;
	public int entScreenY;
	public final int index = -1;
	public boolean isVisible()
	{
		return false;
	}

	Entity()
	{
		smallX = new int[10];
		smallY = new int[10];
		interactingEntity = -1;
		turnSpeed = 32;
		walkAnimId = -1;
		height = 200;
		standAnimId = -1;
		turnAnimId = -1;
		hitArray = new int[4];
		hitMarkTypes = new int[4];
		hitsLoopCycle = new int[4];
		movementAnimId = -1;
		spotAnimId = -1;
		anim = -1;
		loopCycleStatus = -1000;
		textCycle = 100;
		tileSize = 1;
		animStretches = false;
		pathRunning = new boolean[10];
		walkBackAnimId = -1;
		walkLeftAnimId = -1;
		walkRightAnimId = -1;
		runAnimId = -1;
	}

	public final int[] smallX;
	public final int[] smallY;
	public int interactingEntity;
	int stepDelayCounter;
	int turnSpeed;
	int walkAnimId;
	public String textSpoken;
	public int height;
	public int turnDirection;
	int standAnimId;
	int turnAnimId;
	int turnAroundAnimId;
	final int[] hitArray;
	final int[] hitMarkTypes;
	final int[] hitsLoopCycle;
	int movementAnimId;
	int movementAnimFrame;
	int movementAnimCycle;
	int spotAnimId;
	int spotAnimFrame;
	int spotAnimCycle;
	int spotAnimDelay;
	int spotAnimHeight;
	int smallXYIndex;
	public int anim;
	int animFrame;
	int animCycle;
	int animDelay;
	int animFrameCount;
	int animResetCycle;
	public int loopCycleStatus;
	public int currentHealth;
	public int maxHealth;
	int textCycle;
	int textColor;
	int textEffect;
	int textAlpha;
	int tileSize;
	boolean animStretches;
	int pathRemainder;
	int forceMoveStartX;
	int forceMoveEndX;
	int forceMoveStartY;
	int forceMoveEndY;
	int forceMoveEndCycle;
	int forceMoveStartCycle;
	int forceMoveDirection;
	public int x;
	public int y;
	int faceAngle;
	final boolean[] pathRunning;
	int walkBackAnimId;
	int walkLeftAnimId;
	int walkRightAnimId;
	int runAnimId;
}
