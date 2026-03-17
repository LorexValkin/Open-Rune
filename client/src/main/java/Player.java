// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Player extends Entity {

	public Model getRotatedModel() {
		if(!visible)
			return null;
		Model model = getPlayerModel();
		if(model == null)
			return null;
		super.height = model.modelHeight;
		model.singleTile = true;
		if(lowDetail)
			return model;
		if(super.spotAnimId != -1 && super.spotAnimFrame != -1)
		{
			SpotAnim spotAnim = SpotAnim.cache[super.spotAnimId];
			Model model_2 = spotAnim.getModel();
			if(model_2 != null)
			{
				Model model_3 = new Model(true, AnimFrame.isFrameLoaded(super.spotAnimFrame), false, model_2);
				model_3.translate(0, -super.spotAnimHeight, 0);
				model_3.buildLabelGroups();
				model_3.applyTransform(spotAnim.aAnimation_407.anIntArray353[super.spotAnimFrame]);
				model_3.labelGroupsUnused = null;
				model_3.labelGroups = null;
				if(spotAnim.anInt410 != 128 || spotAnim.anInt411 != 128)
					model_3.scale(spotAnim.anInt410, spotAnim.anInt410, spotAnim.anInt411);
				model_3.calculateLighting(64 + spotAnim.anInt413, 850 + spotAnim.anInt414, -30, -50, -30, true);
				Model aclass30_sub2_sub4_sub6_1s[] = {
						model, model_3
				};
				model = new Model(aclass30_sub2_sub4_sub6_1s);
			}
		}
		if(attachedModel != null)
		{
			if(client.loopCycle >= attachedModelEndCycle)
				attachedModel = null;
			if(client.loopCycle >= attachedModelStartCycle && client.loopCycle < attachedModelEndCycle)
			{
				Model model_1 = attachedModel;
				model_1.translate(attachedModelX - super.x, attachedModelOffsetY - attachedModelHeight, attachedModelY - super.y);
				if(super.turnDirection == 512)
				{
					model_1.rotateY90();
					model_1.rotateY90();
					model_1.rotateY90();
				} else
				if(super.turnDirection == 1024)
				{
					model_1.rotateY90();
					model_1.rotateY90();
				} else
				if(super.turnDirection == 1536)
					model_1.rotateY90();
				Model aclass30_sub2_sub4_sub6s[] = {
						model, model_1
				};
				model = new Model(aclass30_sub2_sub4_sub6s);
				if(super.turnDirection == 512)
					model_1.rotateY90();
				else
				if(super.turnDirection == 1024)
				{
					model_1.rotateY90();
					model_1.rotateY90();
				} else
				if(super.turnDirection == 1536)
				{
					model_1.rotateY90();
					model_1.rotateY90();
					model_1.rotateY90();
				}
				model_1.translate(super.x - attachedModelX, attachedModelHeight - attachedModelOffsetY, super.y - attachedModelY);
			}
		}
		model.singleTile = true;
		return model;
	}

	public void updatePlayer(Stream stream)
	{
		stream.currentOffset = 0;
		gender = stream.readUnsignedByte();
		headIcon = stream.readUnsignedByte();
		skullIcon = stream.readUnsignedByte();
		//hintIcon = stream.readUnsignedByte();
		desc = null;
		team = 0;
		for(int j = 0; j < 12; j++)
		{
			int k = stream.readUnsignedByte();
			if(k == 0)
			{
				equipment[j] = 0;
				continue;
			}
			int i1 = stream.readUnsignedByte();
			equipment[j] = (k << 8) + i1;
			if(j == 0 && equipment[0] == 65535)
			{
				desc = EntityDef.forID(stream.readUnsignedWord());
				break;
			}
			if(equipment[j] >= 512 && equipment[j] - 512 < ItemDef.totalItems)
			{
				int l1 = ItemDef.forID(equipment[j] - 512).team;
				if(l1 != 0)
					team = l1;
			}
		}

		for(int l = 0; l < 5; l++)
		{
			int j1 = stream.readUnsignedByte();
			if(j1 < 0 || j1 >= client.anIntArrayArray1003[l].length)
				j1 = 0;
			bodyColors[l] = j1;
		}

		super.standAnimId = stream.readUnsignedWord();
		if(super.standAnimId == 65535)
			super.standAnimId = -1;
		super.turnAnimId = stream.readUnsignedWord();
		if(super.turnAnimId == 65535)
			super.turnAnimId = -1;
		super.walkBackAnimId = stream.readUnsignedWord();
		if(super.walkBackAnimId == 65535)
			super.walkBackAnimId = -1;
		super.walkLeftAnimId = stream.readUnsignedWord();
		if(super.walkLeftAnimId == 65535)
			super.walkLeftAnimId = -1;
		super.walkRightAnimId = stream.readUnsignedWord();
		if(super.walkRightAnimId == 65535)
			super.walkRightAnimId = -1;
		super.runAnimId = stream.readUnsignedWord();
		if(super.runAnimId == 65535)
			super.runAnimId = -1;
		super.walkAnimId = stream.readUnsignedWord();
		if(super.walkAnimId == 65535)
			super.walkAnimId = -1;
		name = TextClass.fixName(TextClass.nameForLong(stream.readQWord()));
		combatLevel = stream.readUnsignedByte();
		skill = stream.readUnsignedWord();
		visible = true;
		appearanceHash = 0L;
		for(int k1 = 0; k1 < 12; k1++)
		{
			appearanceHash <<= 4;
			if(equipment[k1] >= 256)
				appearanceHash += equipment[k1] - 256;
		}

		if(equipment[0] >= 256)
			appearanceHash += equipment[0] - 256 >> 4;
		if(equipment[1] >= 256)
			appearanceHash += equipment[1] - 256 >> 8;
		for(int i2 = 0; i2 < 5; i2++)
		{
			appearanceHash <<= 3;
			appearanceHash += bodyColors[i2];
		}

		appearanceHash <<= 1;
		appearanceHash += gender;
	}

	public Model getPlayerModel()
	{
		if(desc != null)
		{
			int j = -1;
			if(super.anim >= 0 && super.animDelay == 0)
				j = Animation.anims[super.anim].anIntArray353[super.animFrame];
			else
			if(super.movementAnimId >= 0)
				j = Animation.anims[super.movementAnimId].anIntArray353[super.movementAnimFrame];
			Model model = desc.getAnimatedModel(-1, j, null);
			return model;
		}
		long l = appearanceHash;
		int k = -1;
		int i1 = -1;
		int j1 = -1;
		int k1 = -1;
		if(super.anim >= 0 && super.animDelay == 0)
		{
			Animation animation = Animation.anims[super.anim];
			k = animation.anIntArray353[super.animFrame];
			if(super.movementAnimId >= 0 && super.movementAnimId != super.standAnimId)
				i1 = Animation.anims[super.movementAnimId].anIntArray353[super.movementAnimFrame];
			if(animation.anInt360 >= 0)
			{
				j1 = animation.anInt360;
				l += j1 - equipment[5] << 40;
			}
			if(animation.anInt361 >= 0)
			{
				k1 = animation.anInt361;
				l += k1 - equipment[3] << 48;
			}
		} else
		if(super.movementAnimId >= 0)
			k = Animation.anims[super.movementAnimId].anIntArray353[super.movementAnimFrame];
		Model model_1 = (Model) mruNodes.insertFromCache(l);
		if(model_1 == null)
		{
			boolean flag = false;
			for(int i2 = 0; i2 < 12; i2++)
			{
				int k2 = equipment[i2];
				if(k1 >= 0 && i2 == 3)
					k2 = k1;
				if(j1 >= 0 && i2 == 5)
					k2 = j1;
				if(k2 >= 256 && k2 < 512 && !IDK.cache[k2 - 256].isIDKHeadModelReady())
					flag = true;
				if(k2 >= 512 && !ItemDef.forID(k2 - 512).isHeadModelReady(gender))
					flag = true;
			}

			if(flag)
			{
				if(cachedModelKey != -1L)
					model_1 = (Model) mruNodes.insertFromCache(cachedModelKey);
				if(model_1 == null)
					return null;
			}
		}
		if(model_1 == null)
		{
			Model aclass30_sub2_sub4_sub6s[] = new Model[12];
			int j2 = 0;
			for(int l2 = 0; l2 < 12; l2++)
			{
				int i3 = equipment[l2];
				if(k1 >= 0 && l2 == 3)
					i3 = k1;
				if(j1 >= 0 && l2 == 5)
					i3 = j1;
				if(i3 >= 256 && i3 < 512)
				{
					Model model_3 = IDK.cache[i3 - 256].getIDKHeadModel();
					if(model_3 != null)
						aclass30_sub2_sub4_sub6s[j2++] = model_3;
				}
				if(i3 >= 512)
				{
					Model model_4 = ItemDef.forID(i3 - 512).getHeadModelItem(gender);
					if(model_4 != null)
						aclass30_sub2_sub4_sub6s[j2++] = model_4;
				}
			}

			model_1 = new Model(j2, aclass30_sub2_sub4_sub6s);
			for(int j3 = 0; j3 < 5; j3++)
				if(bodyColors[j3] != 0)
				{
					model_1.replaceColor(client.anIntArrayArray1003[j3][0], client.anIntArrayArray1003[j3][bodyColors[j3]]);
					if(j3 == 1)
						model_1.replaceColor(client.anIntArray1204[0], client.anIntArray1204[bodyColors[j3]]);
				}

			model_1.buildLabelGroups();
			model_1.calculateLighting(64, 850, -30, -50, -30, true);
			mruNodes.removeFromCache(model_1, l);
			cachedModelKey = l;
		}
		if(lowDetail)
			return model_1;
		Model model_2 = Model.sharedModel;
		model_2.copyAnimated(model_1, AnimFrame.isFrameLoaded(k) & AnimFrame.isFrameLoaded(i1));
		if(k != -1 && i1 != -1)
			model_2.recolorAll(Animation.anims[super.anim].anIntArray357, i1, k);
		else
		if(k != -1)
			model_2.applyTransform(k);
		model_2.calculateBounds();
		model_2.labelGroupsUnused = null;
		model_2.labelGroups = null;
		return model_2;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public int privelage;
	public Model getChatHeadModel()
	{
		if(!visible)
			return null;
		if(desc != null)
			return desc.getHeadModel();
		boolean flag = false;
		for(int i = 0; i < 12; i++)
		{
			int j = equipment[i];
			if(j >= 256 && j < 512 && !IDK.cache[j - 256].isBodyModelReady())
				flag = true;
			if(j >= 512 && !ItemDef.forID(j - 512).isDialogueModelReady(gender))
				flag = true;
		}

		if(flag)
			return null;
		Model aclass30_sub2_sub4_sub6s[] = new Model[12];
		int k = 0;
		for(int l = 0; l < 12; l++)
		{
			int i1 = equipment[l];
			if(i1 >= 256 && i1 < 512)
			{
				Model model_1 = IDK.cache[i1 - 256].getBodyModel();
				if(model_1 != null)
					aclass30_sub2_sub4_sub6s[k++] = model_1;
			}
			if(i1 >= 512)
			{
				Model model_2 = ItemDef.forID(i1 - 512).getDialogueModel(gender);
				if(model_2 != null)
					aclass30_sub2_sub4_sub6s[k++] = model_2;
			}
		}

		Model model = new Model(k, aclass30_sub2_sub4_sub6s);
		for(int j1 = 0; j1 < 5; j1++)
			if(bodyColors[j1] != 0)
			{
				model.replaceColor(client.anIntArrayArray1003[j1][0], client.anIntArrayArray1003[j1][bodyColors[j1]]);
				if(j1 == 1)
					model.replaceColor(client.anIntArray1204[0], client.anIntArray1204[bodyColors[j1]]);
			}

		return model;
	}

	Player()
	{
		cachedModelKey = -1L;
		lowDetail = false;
		bodyColors = new int[5];
		visible = false;
		objectAppearanceStartIndex = 9;
		equipment = new int[12];
	}

	private long cachedModelKey;
	public EntityDef desc;
	boolean lowDetail;
	final int[] bodyColors;
	public int team;
	private int gender;
	public String name;
	static MRUNodes mruNodes = new MRUNodes(260);
	public int combatLevel;
	public int headIcon;
	public int skullIcon;
	public int hintIcon;
	public int attachedModelStartCycle;
	int attachedModelEndCycle;
	int attachedModelHeight;
	boolean visible;
	int attachedModelX;
	int attachedModelOffsetY;
	int attachedModelY;
	Model attachedModel;
	private int objectAppearanceStartIndex;
	public final int[] equipment;
	private long appearanceHash;
	int anInt1719;
	int anInt1720;
	int anInt1721;
	int anInt1722;
	int skill;

}
