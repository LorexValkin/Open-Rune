// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Animable_Sub3 extends Animable {

	public Animable_Sub3(int i, int j, int l, int i1, int j1, int k1,
						 int l1)
	{
		finished = false;
		spotAnim = SpotAnim.cache[i1];
		plane = i;
		startX = l1;
		startY = k1;
		startZ = j1;
		endCycle = j + l;
			finished = false;
	}

	public Model getRotatedModel()
	{
		Model model = spotAnim.getModel();
		if(model == null)
			return null;
		int j = spotAnim.animation.frameIds[animFrame];
		Model model_1 = new Model(true, AnimFrame.isFrameLoaded(j), false, model);
		if(!finished)
		{
			model_1.buildLabelGroups();
			model_1.applyTransform(j);
			model_1.labelGroupsUnused = null;
			model_1.labelGroups = null;
		}
		if(spotAnim.scaleXY != 128 || spotAnim.scaleZ != 128)
			model_1.scale(spotAnim.scaleXY, spotAnim.scaleXY, spotAnim.scaleZ);
		if(spotAnim.rotation != 0)
		{
			if(spotAnim.rotation == 90)
				model_1.rotateY90();
			if(spotAnim.rotation == 180)
			{
				model_1.rotateY90();
				model_1.rotateY90();
			}
			if(spotAnim.rotation == 270)
			{
				model_1.rotateY90();
				model_1.rotateY90();
				model_1.rotateY90();
			}
		}
		model_1.calculateLighting(64 + spotAnim.ambient, 850 + spotAnim.contrast, -30, -50, -30, true);
		return model_1;
	}

	public void advanceSpotAnimFrame(int i)
	{
		for(animCycle += i; animCycle > spotAnim.animation.getFrameDuration(animFrame);)
		{
			animCycle -= spotAnim.animation.getFrameDuration(animFrame) + 1;
			animFrame++;
			if(animFrame >= spotAnim.animation.frameCount && (animFrame < 0 || animFrame >= spotAnim.animation.frameCount))
			{
				animFrame = 0;
				finished = true;
			}
		}

	}

	public final int plane;
	public final int startX;
	public final int startY;
	public final int startZ;
	public final int endCycle;
	public boolean finished;
	private final SpotAnim spotAnim;
	private int animFrame;
	private int animCycle;
}
