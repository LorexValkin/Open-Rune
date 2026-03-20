package com.client;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class IdentityKit {

	public static void unpackConfig(JagArchive streamLoader) {
		Buffer stream = new Buffer(streamLoader.getDataForName("idk.dat"));
		length = stream.readUnsignedWord();
		if (cache == null)
			cache = new IdentityKit[length];
		for (int j = 0; j < length; j++) {
			if (cache[j] == null)
				cache[j] = new IdentityKit();
			cache[j].readValues(stream);
			cache[j].originalColors[0] = 55232;
			cache[j].replacementColors[0] = 6798;
		}
	}

	private void readValues(Buffer stream) {
		do {
			int i = stream.readUnsignedByte();
			if (i == 0)
				return;
			if (i == 1)
				bodyPartId = stream.readUnsignedByte();
			else if (i == 2) {
				int j = stream.readUnsignedByte();
				modelIds = new int[j];
				for (int k = 0; k < j; k++)
					modelIds[k] = stream.readUnsignedWord();

			} else if (i == 3)
				nonSelectable = true;
			else if (i >= 40 && i < 50)
				originalColors[i - 40] = stream.readUnsignedWord();
			else if (i >= 50 && i < 60)
				replacementColors[i - 50] = stream.readUnsignedWord();
			else if (i >= 60 && i < 70)
				headModelIds[i - 60] = stream.readUnsignedWord();
			else
				System.out.println("Error unrecognised config code: " + i);
		} while (true);
	}

	public boolean method537() {
		if (modelIds == null)
			return true;
		boolean flag = true;
		for (int j = 0; j < modelIds.length; j++)
			if (!Model.isModelLoaded(modelIds[j]))
				flag = false;

		return flag;
	}

	public Model method538() {
		if (modelIds == null)
			return null;
		Model aclass30_sub2_sub4_sub6s[] = new Model[modelIds.length];
		for (int i = 0; i < modelIds.length; i++)
			aclass30_sub2_sub4_sub6s[i] = Model.getModel(modelIds[i]);

		Model model;
		if (aclass30_sub2_sub4_sub6s.length == 1)
			model = aclass30_sub2_sub4_sub6s[0];
		else
			model = new Model(aclass30_sub2_sub4_sub6s.length,
					aclass30_sub2_sub4_sub6s);
		for (int j = 0; j < 6; j++) {
			if (originalColors[j] == 0)
				break;
			model.replaceColor(originalColors[j], replacementColors[j]);
		}

		return model;
	}

	public boolean method539() {
		boolean flag1 = true;
		for (int i = 0; i < 5; i++)
			if (headModelIds[i] != -1 && !Model.isModelLoaded(headModelIds[i]))
				flag1 = false;

		return flag1;
	}

	public Model method540() {
		Model aclass30_sub2_sub4_sub6s[] = new Model[5];
		int j = 0;
		for (int k = 0; k < 5; k++)
			if (headModelIds[k] != -1)
				aclass30_sub2_sub4_sub6s[j++] = Model
						.getModel(headModelIds[k]);

		Model model = new Model(j, aclass30_sub2_sub4_sub6s);
		for (int l = 0; l < 6; l++) {
			if (originalColors[l] == 0)
				break;
			model.replaceColor(originalColors[l], replacementColors[l]);
		}

		return model;
	}

	private IdentityKit() {
		bodyPartId = -1;
		originalColors = new int[6];
		replacementColors = new int[6];
		nonSelectable = false;
	}

	public static int length;
	public static IdentityKit cache[];
	public int bodyPartId;
	private int[] modelIds;
	private final int[] originalColors;
	private final int[] replacementColors;
	private final int[] headModelIds = { -1, -1, -1, -1, -1 };
	public boolean nonSelectable;
}
