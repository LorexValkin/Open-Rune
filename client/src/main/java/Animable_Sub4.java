// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Animable_Sub4 extends Animable {

	public void trackTarget(int i, int j, int k, int l)
	{
		if(!moving)
		{
			double d = l - startX;
			double d2 = j - startY;
			double d3 = Math.sqrt(d * d + d2 * d2);
			currentX = (double)startX + (d * (double)startSpeed) / d3;
			currentY = (double)startY + (d2 * (double)startSpeed) / d3;
			currentZ = startZ;
		}
		double d1 = (endCycle + 1) - i;
		velocityX = ((double)l - currentX) / d1;
		velocityY = ((double)j - currentY) / d1;
		velocityXY = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
		if(!moving)
			velocityZ = -velocityXY * Math.tan((double)slopeAngle * 0.02454369D);
		accelerationZ = (2D * ((double)k - currentZ - velocityZ * d1)) / (d1 * d1);
	}

	public Model getRotatedModel()
	{
		Model model = spotAnim.getModel();
		if(model == null)
			return null;
		int j = -1;
		if(spotAnim.animation != null)
			j = spotAnim.animation.frameIds[animFrame];
		Model model_1 = new Model(true, AnimFrame.isFrameLoaded(j), false, model);
		if(j != -1)
		{
			model_1.buildLabelGroups();
			model_1.applyTransform(j);
			model_1.labelGroupsUnused = null;
			model_1.labelGroups = null;
		}
		if(spotAnim.scaleXY != 128 || spotAnim.scaleZ != 128)
			model_1.scale(spotAnim.scaleXY, spotAnim.scaleXY, spotAnim.scaleZ);
		model_1.rotateX(pitchAngle);
		model_1.calculateLighting(64 + spotAnim.ambient, 850 + spotAnim.contrast, -30, -50, -30, true);
			return model_1;
	}

	public Animable_Sub4(int i, int j, int l, int i1, int j1, int k1,
						 int l1, int i2, int j2, int k2, int l2)
	{
		moving = false;
		spotAnim = SpotAnim.cache[l2];
		sourceEntityIndex = k1;
		startX = j2;
		startY = i2;
		startZ = l1;
		startCycle = l;
		endCycle = i1;
		slopeAngle = i;
		startSpeed = j1;
		targetLocSize = k2;
		targetEntityIndex = j;
		moving = false;
	}

	public void advanceProjectile(int i)
	{
		moving = true;
		currentX += velocityX * (double)i;
		currentY += velocityY * (double)i;
		currentZ += velocityZ * (double)i + 0.5D * accelerationZ * (double)i * (double)i;
		velocityZ += accelerationZ * (double)i;
		yawAngle = (int)(Math.atan2(velocityX, velocityY) * 325.94900000000001D) + 1024 & 0x7ff;
		pitchAngle = (int)(Math.atan2(velocityZ, velocityXY) * 325.94900000000001D) & 0x7ff;
		if(spotAnim.animation != null)
			for(animCycle += i; animCycle > spotAnim.animation.getFrameDuration(animFrame);)
			{
				animCycle -= spotAnim.animation.getFrameDuration(animFrame) + 1;
				animFrame++;
				if(animFrame >= spotAnim.animation.frameCount)
					animFrame = 0;
			}

	}

	public final int startCycle;
	public final int endCycle;
	private double velocityX;
	private double velocityY;
	private double velocityXY;
	private double velocityZ;
	private double accelerationZ;
	private boolean moving;
	private final int startX;
	private final int startY;
	private final int startZ;
	public final int targetEntityIndex;
	public double currentX;
	public double currentY;
	public double currentZ;
	private final int slopeAngle;
	private final int startSpeed;
	public final int targetLocSize;
	private final SpotAnim spotAnim;
	private int animFrame;
	private int animCycle;
	public int yawAngle;
	private int pitchAngle;
	public final int sourceEntityIndex;
}
