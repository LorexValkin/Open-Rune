// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class NPC extends Entity
{

	private Model getNPCModel()
	{
		if(super.anim >= 0 && super.animDelay == 0)
		{
			int k = Animation.anims[super.anim].frameIds[super.animFrame];
			int i1 = -1;
			if(super.movementAnimId >= 0 && super.movementAnimId != super.standAnimId)
				i1 = Animation.anims[super.movementAnimId].frameIds[super.movementAnimFrame];
			return desc.getAnimatedModel(i1, k, Animation.anims[super.anim].interleaveOrder);
		}
		int l = -1;
		if(super.movementAnimId >= 0)
			l = Animation.anims[super.movementAnimId].frameIds[super.movementAnimFrame];
		return desc.getAnimatedModel(-1, l, null);
	}

	public Model getRotatedModel()
	{
		if(desc == null)
			return null;
		Model model = getNPCModel();
		if(model == null)
			return null;
		super.height = model.modelHeight;
		if(super.spotAnimId != -1 && super.spotAnimFrame != -1)
		{
			SpotAnim spotAnim = SpotAnim.cache[super.spotAnimId];
			Model model_1 = spotAnim.getModel();
			if(model_1 != null)
			{
				int j = spotAnim.animation.frameIds[super.spotAnimFrame];
				Model model_2 = new Model(true, AnimFrame.isFrameLoaded(j), false, model_1);
				model_2.translate(0, -super.spotAnimHeight, 0);
				model_2.buildLabelGroups();
				model_2.applyTransform(j);
				model_2.labelGroupsUnused = null;
				model_2.labelGroups = null;
				if(spotAnim.scaleXY != 128 || spotAnim.scaleZ != 128)
					model_2.scale(spotAnim.scaleXY, spotAnim.scaleXY, spotAnim.scaleZ);
				model_2.calculateLighting(64 + spotAnim.ambient, 850 + spotAnim.contrast, -30, -50, -30, true);
				Model aModel[] = {
						model, model_2
				};
				model = new Model(aModel);
			}
		}
		if(desc.tileSpan == 1)
			model.singleTile = true;
		return model;
	}

	public boolean isVisible()
	{
		return desc != null;
	}

	NPC()
	{
	}

	public EntityDef desc;
}
