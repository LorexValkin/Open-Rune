package com.client;

import com.client.definitions.GraphicsDefinition;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class StationaryGraphic extends Renderable {

	public StationaryGraphic(int i, int j, int l, int i1, int j1, int k1, int l1) {
		animFinished = false;
		graphicsDef = GraphicsDefinition.cache[i1];
		gfxLevel = i;
		gfxWorldY = l1;
		gfxWorldX = k1;
		gfxWorldZ = j1;
		gfxEndCycle = j + l;
		animFinished = false;
	}

	@Override
	public Model getRotatedModel() {
		Model model = graphicsDef.getModel();
		if (model == null)
			return null;
		int j = graphicsDef.aAnimation_407.primaryFrames[gfxFrameIndex];
		Model model_1 = new Model(true, AnimationFrame.method532(j), false, model);
		if (!animFinished) {
			model_1.buildVertexGroups();
			model_1.applyFrame(j);
			model_1.faceGroups = null;
			model_1.vertexGroups = null;
		}
		if (graphicsDef.anInt410 != 128 || graphicsDef.anInt411 != 128)
			model_1.scale(graphicsDef.anInt410, graphicsDef.anInt410,
					graphicsDef.anInt411);
		if (graphicsDef.anInt412 != 0) {
			if (graphicsDef.anInt412 == 90)
				model_1.rotateY90();
			if (graphicsDef.anInt412 == 180) {
				model_1.rotateY90();
				model_1.rotateY90();
			}
			if (graphicsDef.anInt412 == 270) {
				model_1.rotateY90();
				model_1.rotateY90();
				model_1.rotateY90();
			}
		}
		model_1.applyLighting(64 + graphicsDef.anInt413,
				850 + graphicsDef.anInt414, -30, -50, -30, true);
		return model_1;
	}

	public void method454(int i) {
		for (gfxFrameClock += i; gfxFrameClock > graphicsDef.aAnimation_407.getFrameDuration(gfxFrameIndex); ) {
			gfxFrameClock -= graphicsDef.aAnimation_407.getFrameDuration(gfxFrameIndex) + 1;
			gfxFrameIndex++;
			if (gfxFrameIndex >= graphicsDef.aAnimation_407.anInt352
					&& (gfxFrameIndex < 0 || gfxFrameIndex >= graphicsDef.aAnimation_407.anInt352)) {
				gfxFrameIndex = 0;
				animFinished = true;
			}
		}

	}

	public final int gfxLevel;
	public final int gfxWorldY;
	public final int gfxWorldX;
	public final int gfxWorldZ;
	public final int gfxEndCycle;
	public boolean animFinished;
	private final GraphicsDefinition graphicsDef;
	private int gfxFrameIndex;
	private int gfxFrameClock;
}
