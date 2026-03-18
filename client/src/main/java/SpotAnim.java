// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.Scanner;
public final class SpotAnim {
	public static int length;
	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = null;
		stream = new Stream(streamLoader.getDataForName("spotanim.dat"));
		try {
		//stream = new Stream(streamLoader.getBytesFromFile(new File("test.dat")));
		} catch(Exception ioe){}
		//Stream stream = new Stream
		length = stream.readUnsignedWord();
		length = 1500;
		if(cache == null)
			cache = new SpotAnim[length];
		for(int j = 0; j < length; j++) {
			if(cache[j] == null)
				cache[j] = new SpotAnim();
			cache[j].id = j;
			if(j < 666)
				cache[j].readValues(stream);
			else
				setGFXBase(j);
			if(j == 672){ // Darkbow spec
				cache[j].modelId = 26391;
				cache[j].animationId = 6585;
				cache[j].animation = Animation.anims[cache[j].animationId];
			}		
			if(j == 756){ // Dragon bolt  spec
				cache[j].modelId = 16935;// Model
				cache[j].animationId = 4450; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}	
			if(j == 758){ // diamond bolt spec
				cache[j].modelId = 16943;// Model
				cache[j].animationId = 4452; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}	
			if(j == 752){ // Emerald bolt spec
				cache[j].modelId = 16941;// Model
				cache[j].animationId = 4446; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}			
			if(j == 754){ // ruby bolt spec
				cache[j].modelId = 16932;// Model
				cache[j].animationId = 4448; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}			
			if(j == 753){ // onyx bolt spec
				cache[j].modelId = 16939;// Model
				cache[j].animationId = 4447; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if(j == 1100){ // dbow impact
				cache[j].modelId = 26390;// Model
				cache[j].animationId = 6586; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 726){
				cache[j].modelId = 16828;// Model
				cache[j].animationId = 4417; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update		
			}
			if(j == 1220){
				cache[j].modelId = 28195;// Model
				cache[j].animationId = 7068; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1221){
				cache[j].modelId = 28223;// Model
				cache[j].animationId = 7069; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1222){
				cache[j].modelId = 28249;// Model
				cache[j].animationId = 7075; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1223){
				cache[j].modelId = 28211;// Model
				cache[j].animationId = 7077; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1224){
				cache[j].modelId = 28175;// Model
				cache[j].animationId = 7076; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1211){
				cache[j].modelId = 28221;// Model
				cache[j].animationId = 7036; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1213){
				cache[j].modelId = 28172;// Model
				cache[j].animationId = 7038; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1212){
				cache[j].modelId = 28172;// Model
				cache[j].animationId = 7037; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1190){
				cache[j].modelId = 28240;// Model
				cache[j].animationId = 6957; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1198){
				cache[j].modelId = 28176;// Model
				cache[j].animationId = 6970; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if(j == 1197){
				cache[j].modelId = 28177;// Model
				cache[j].animationId = 6970; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1203){
				cache[j].modelId = 28202;// Model
				cache[j].animationId = 7025; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if(j == 1206){
				cache[j].modelId = 28218;// Model
				cache[j].animationId = 7030; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
			if (j == 1207) {
				cache[j].modelId = 28219;// Model
				cache[j].animationId = 7031; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if (j == 1165) {
				cache[j].modelId = 26597;// Model
				cache[j].animationId = 6697; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if (j == 1166) {
				cache[j].modelId = 26595;// Model
				cache[j].animationId = 6698; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if (j == 678) {
				cache[j].modelId = 12734;// Model
				cache[j].animationId = 4072; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if (j == 1103) {
				cache[j].modelId = 26393;// Model
				cache[j].animationId = 6588; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if (j == 1104) {
				cache[j].modelId = 26393;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
				cache[j].modifiedColors = new int[2];
				cache[j].modifiedColors[0] = 57;
				cache[j].modifiedColors[1] = 61;
			}
			if (j == 1104) {
				cache[j].modelId = 26393;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
				cache[j].modifiedColors = new int[2];
				cache[j].modifiedColors[0] = 57;
				cache[j].modifiedColors[1] = 61;
			}
			if (j == 1105) {
				cache[j].modelId = 26393;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
				cache[j].modifiedColors = new int[2];
				cache[j].modifiedColors[0] = 57;
				cache[j].modifiedColors[1] = 61;
			}
			if (j == 1106) {
				cache[j].modelId = 26293;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if (j == 1107) {
				cache[j].modelId = 26293;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
				cache[j].modifiedColors = new int[2];
				cache[j].modifiedColors[0] = 57;
				cache[j].modifiedColors[1] = 61;
			}
			if (j == 1108) {
				cache[j].modelId = 26293;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
				cache[j].modifiedColors = new int[2];
				cache[j].modifiedColors[0] = 57;
				cache[j].modifiedColors[1] = 61;
			}
			if (j == 1109) {
				cache[j].modelId = 26293;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
				cache[j].modifiedColors = new int[2];
				cache[j].modifiedColors[0] = 57;
				cache[j].modifiedColors[1] = 61;
			}
			if (j == 1110) {
				cache[j].modelId = 26293;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
				cache[j].modifiedColors = new int[5];
				cache[j].modifiedColors[0] = 57;
				cache[j].modifiedColors[1] = 61;
				cache[j].modifiedColors[2] = 5012;
				cache[j].modifiedColors[3] = 926;
				cache[j].modifiedColors[4] = 5012;
			}
			if (j == 1111) {
				cache[j].modelId = 26294;// Model
				cache[j].animationId = 366; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if (j == 1124) {
				cache[j].modelId = 26598;// Model
				cache[j].animationId = 6694; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update
			}
			if(j == 657){
				cache[j].modelId = 12411;// Model
				cache[j].animationId = 4417; // animation
				cache[j].animation = Animation.anims[cache[j].animationId];// Update			
			}
		}
		//loadAllFromTexts();
		//dumpGfx();
	}
	
	/*public static void loadAllFromTexts() {
		File[] dir = new File("./gfx/").listFiles();
		for (int j = 0; j < dir.length; j++) {
			try {
				File f = dir[j];
				Scanner s = new Scanner(f);
				System.out.println("File name: " + f.getName());
				while (s.hasNextLine()) {
					String line = s.nextLine();
					if (line.startsWith("spotAnim.model")) {
						cache[j].modelId = Integer.parseInt(line.substring(line.indexOf("=") + 2, line.length() - 2));
						System.out.println("model: " + line.substring(line.indexOf("=") + 2, line.length() - 1));					
					} else if (line.startsWith("spotAnim.animation")) {
						cache[j].modelId = Integer.parseInt(line.substring(line.indexOf("=") + 2, line.length() - 2));
						if (cache[j].modelId != -1)
							cache[j].animation = Animation.anims[cache[j].animationId];
						System.out.println("animation: " + line.substring(line.indexOf("=") + 2, line.length() - 1));					
						break;
					}
				}
			} catch (Exception e){}	
		
		}
	
	}
	public static void dumpGfx() {
		try {
			File f = new File("test.dat");
			f.delete();
			DataOutputStream out = new DataOutputStream(new FileOutputStream("test.dat"));
			//Stream out = new Stream(streamLoader.getDataForName("spotanim.dat"));
			out.writeShort(length);
			for (int j = 0; j < cache.length; j++) {
				if (cache[j].modelId == 0 || cache[j].animationId == 0 || cache[j].animation == null) {
					out.writeByte(0);
				} else {
					out.writeByte(1);
					out.writeShort(cache[j].modelId);
					out.writeByte(2);
					out.writeShort(cache[j].animationId);
					out.writeByte(0);
				}
			}
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}	
	}*/

	public static void setGFXBase(int j) {
		cache[j].modelId = cache[369].modelId;
		cache[j].animation = cache[369].animation;
		cache[j].animationId = cache[369].animationId;
		cache[j].scaleXY = cache[369].scaleXY;
		cache[j].scaleZ = cache[369].scaleZ;
		cache[j].rotation = cache[369].rotation;
		cache[j].ambient = cache[369].ambient;
		cache[j].contrast = cache[369].contrast;
		cache[j].originalColors = cache[369].originalColors;
		cache[j].modifiedColors = cache[369].modifiedColors;
	}
	

	private void readValues(Stream stream) {
		do {
			int i = stream.readUnsignedByte();
			if(i == 0)
				return;
			if(i == 1)
				modelId = stream.readUnsignedWord();
			else if(i == 2) {
				animationId = stream.readUnsignedWord();
				if(Animation.anims != null)
					animation = Animation.anims[animationId];
			} else if(i == 4)
				scaleXY = stream.readUnsignedWord();
			else if(i == 5)
				scaleZ = stream.readUnsignedWord();
			else if(i == 6)
				rotation = stream.readUnsignedWord();
			else if(i == 7)
				ambient = stream.readUnsignedByte();
			else if(i == 8)
				contrast = stream.readUnsignedByte();
			else if(i >= 40 && i < 50)
				originalColors[i - 40] = stream.readUnsignedWord();
			else if(i >= 50 && i < 60)
				modifiedColors[i - 50] = stream.readUnsignedWord();
			else
				System.out.println("Error unrecognised spotanim config code: " + i);
		} while(true);
	}

	public Model getModel() {
		Model model = (Model) modelCache.insertFromCache(id);
		if(model != null)
			return model;
		model = Model.getModel(modelId);
		if(model == null)
			return null;
		for(int i = 0; i < 6; i++)
			if(originalColors[0] != 0)
				model.replaceColor(originalColors[i], modifiedColors[i]);

		modelCache.removeFromCache(model, id);
		return model;
	}

	private SpotAnim() {
		unk400 = 9;
		animationId = -1;
		originalColors = new int[6];
		modifiedColors = new int[6];
		scaleXY = 128;
		scaleZ = 128;
	}

	private final int unk400;
	public static SpotAnim cache[];
	private int id;
	private int modelId;
	private int animationId;
	public Animation animation = null;
	private int[] originalColors;
	private int[] modifiedColors;
	public int scaleXY;
	public int scaleZ;
	public int rotation;
	public int ambient;
	public int contrast;
	public static MRUNodes modelCache = new MRUNodes(30);

}
