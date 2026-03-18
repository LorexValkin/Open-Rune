// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class IDK {

	public static void unpackConfig(StreamLoader streamLoader)
	{
		Stream stream = new Stream(streamLoader.getDataForName("idk.dat"));
		length = stream.readUnsignedWord();
		if(cache == null)
			cache = new IDK[length];
		for(int j = 0; j < length; j++)
		{
			if(cache[j] == null)
				cache[j] = new IDK();
			cache[j].readValues(stream);
		}
	}

	private void readValues(Stream stream)
	{
		do
		{
			int i = stream.readUnsignedByte();
			if(i == 0)
				return;
			if(i == 1)
				bodyPartId = stream.readUnsignedByte();
			else
			if(i == 2)
			{
				int j = stream.readUnsignedByte();
				bodyModelIds = new int[j];
				for(int k = 0; k < j; k++)
					bodyModelIds[k] = stream.readUnsignedWord();

			} else
			if(i == 3)
				aBoolean662 = true;
			else
			if(i >= 40 && i < 50)
				originalColors[i - 40] = stream.readUnsignedWord();
			else
			if(i >= 50 && i < 60)
				modifiedColors[i - 50] = stream.readUnsignedWord();
			else
			if(i >= 60 && i < 70)
				headModelIds[i - 60] = stream.readUnsignedWord();
			else
				System.out.println("Error unrecognised config code: " + i);
		} while(true);
	}

	public boolean isIDKHeadModelReady()
	{
		if(bodyModelIds == null)
			return true;
		boolean flag = true;
		for(int j = 0; j < bodyModelIds.length; j++)
			if(!Model.isModelLoaded(bodyModelIds[j]))
				flag = false;

		return flag;
	}

	public Model getIDKHeadModel()
	{
		if(bodyModelIds == null)
			return null;
		Model aclass30_sub2_sub4_sub6s[] = new Model[bodyModelIds.length];
		for(int i = 0; i < bodyModelIds.length; i++)
			aclass30_sub2_sub4_sub6s[i] = Model.getModel(bodyModelIds[i]);

		Model model;
		if(aclass30_sub2_sub4_sub6s.length == 1)
			model = aclass30_sub2_sub4_sub6s[0];
		else
			model = new Model(aclass30_sub2_sub4_sub6s.length, aclass30_sub2_sub4_sub6s);
		for(int j = 0; j < 6; j++)
		{
			if(originalColors[j] == 0)
				break;
			model.replaceColor(originalColors[j], modifiedColors[j]);
		}

		return model;
	}

	public boolean isBodyModelReady()
	{
		boolean flag1 = true;
		for(int i = 0; i < 5; i++)
			if(headModelIds[i] != -1 && !Model.isModelLoaded(headModelIds[i]))
				flag1 = false;

		return flag1;
	}

	public Model getBodyModel()
	{
		Model aclass30_sub2_sub4_sub6s[] = new Model[5];
		int j = 0;
		for(int k = 0; k < 5; k++)
			if(headModelIds[k] != -1)
				aclass30_sub2_sub4_sub6s[j++] = Model.getModel(headModelIds[k]);

		Model model = new Model(j, aclass30_sub2_sub4_sub6s);
		for(int l = 0; l < 6; l++)
		{
			if(originalColors[l] == 0)
				break;
			model.replaceColor(originalColors[l], modifiedColors[l]);
		}

		return model;
	}

	private IDK()
	{
		bodyPartId = -1;
		originalColors = new int[6];
		modifiedColors = new int[6];
		aBoolean662 = false;
	}

	public static int length;
	public static IDK cache[];
	public int bodyPartId;
	private int[] bodyModelIds;
	private final int[] originalColors;
	private final int[] modifiedColors;
	private final int[] headModelIds = {
		-1, -1, -1, -1, -1
	};
	public boolean aBoolean662;
}
