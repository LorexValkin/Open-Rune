package com.client;

import com.client.definitions.GraphicsDefinition;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Projectile extends Renderable {

	public void method455(int i, int j, int k, int l) {
		if (!projectileMoving) {
			double d = l - startX;
			double d2 = j - startZ;
			double d3 = Math.sqrt(d * d + d2 * d2);
			currentX = startX + (d * startSpeed) / d3;
			currentZ = startZ + (d2 * startSpeed) / d3;
			currentY = startY;
		}
		double d1 = (endCycle + 1) - i;
		velocityX = (l - currentX) / d1;
		velocityZ = (j - currentZ) / d1;
		horizontalSpeed = Math.sqrt(velocityX * velocityX + velocityZ
				* velocityZ);
		if (!projectileMoving)
			verticalSpeed = -horizontalSpeed * Math.tan(slopeAngle * 0.02454369D);
		gravity = (2D * (k - currentY - verticalSpeed * d1)) / (d1 * d1);
	}

	@Override
	public Model getRotatedModel() {
		Model model = projectileGfx.getModel();
		if (model == null)
			return null;
		int j = -1;
		if (projectileGfx.aAnimation_407 != null)
			j = projectileGfx.aAnimation_407.primaryFrames[projFrameIndex];
		Model model_1 = new Model(true, AnimationFrame.method532(j), false, model);
		if (j != -1) {
			model_1.buildVertexGroups();
			model_1.applyFrame(j);
			model_1.faceGroups = null;
			model_1.vertexGroups = null;
		}
		if (projectileGfx.anInt410 != 128 || projectileGfx.anInt411 != 128)
			model_1.scale(projectileGfx.anInt410, projectileGfx.anInt410,
					projectileGfx.anInt411);
		model_1.rotatePitch(pitchAngle);
		model_1.applyLighting(64 + projectileGfx.anInt413,
				850 + projectileGfx.anInt414, -30, -50, -30, true);
		return model_1;
	}

	public Projectile(int i, int j, int l, int i1, int j1, int k1, int l1,
			int i2, int j2, int k2, int l2) {
		projectileMoving = false;
		projectileGfx = GraphicsDefinition.cache[l2];
		sourceIndex = k1;
		startX = j2;
		startZ = i2;
		startY = l1;
		startCycle = l;
		endCycle = i1;
		slopeAngle = i;
		startSpeed = j1;
		startHeight = k2;
		targetIndex = j;
		projectileMoving = false;
	}

	public void method456(int i) {
		projectileMoving = true;
		currentX += velocityX * i;
		currentZ += velocityZ * i;
		currentY += verticalSpeed * i + 0.5D * gravity * i * i;
		verticalSpeed += gravity * i;
		yawAngle = (int) (Math.atan2(velocityX, velocityZ) * 325.94900000000001D) + 1024 & 0x7ff;
		pitchAngle = (int) (Math.atan2(verticalSpeed, horizontalSpeed) * 325.94900000000001D) & 0x7ff;
		if (projectileGfx.aAnimation_407 != null)
			for (projFrameClock += i; projFrameClock > projectileGfx.aAnimation_407
					.getFrameDuration(projFrameIndex);) {
				projFrameClock -= projectileGfx.aAnimation_407.getFrameDuration(projFrameIndex) + 1;
				projFrameIndex++;
				if (projFrameIndex >= projectileGfx.aAnimation_407.anInt352)
					projFrameIndex = 0;
			}

	}

	public final int startCycle;
	public final int endCycle;
	private double velocityX;
	private double velocityZ;
	private double horizontalSpeed;
	private double verticalSpeed;
	private double gravity;
	private boolean projectileMoving;
	private final int startX;
	private final int startZ;
	private final int startY;
	public final int targetIndex;
	public double currentX;
	public double currentZ;
	public double currentY;
	private final int slopeAngle;
	private final int startSpeed;
	public final int startHeight;
	private final GraphicsDefinition projectileGfx;
	private int projFrameIndex;
	private int projFrameClock;
	public int yawAngle;
	private int pitchAngle;
	public final int sourceIndex;
}
