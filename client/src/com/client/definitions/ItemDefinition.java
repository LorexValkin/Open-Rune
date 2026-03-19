package com.client.definitions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import com.client.DrawingArea;
import com.client.MRUNodes;
import com.client.Model;
import com.client.Rasterizer;
import com.client.Sprite;
import com.client.Stream;
import com.client.StreamLoader;
import com.client.sign.Signlink;
import com.client.utilities.FileOperations;

public final class ItemDefinition {
	public byte[] customSpriteLocation;
	public static void unpackConfig(final StreamLoader streamLoader) {
		 stream = new Stream(streamLoader.getDataForName("obj.dat"));
		 Stream stream = new Stream(streamLoader.getDataForName("obj.idx"));
		//stream = new Stream(FileOperations.readFile(Signlink.getCacheDirectory() + "/data/obj.dat"));
		//final Stream stream = new Stream(FileOperations.readFile(Signlink.getCacheDirectory() + "/data/obj.idx"));

		totalItems = stream.readUnsignedWord();
		streamIndices = new int[totalItems + 10000];
		int i = 2;
		for (int j = 0; j < totalItems; j++) {
			streamIndices[j] = i;
			i += stream.readUnsignedWord();
		}

		cache = new ItemDefinition[10];
		for (int index = 0; index < 10; index++) {
			cache[index] = new ItemDefinition();
		}
		//itemDump();
	}

	public static ItemDefinition forID(int itemId) {
		for (int j = 0; j < 10; j++) {
			if (cache[j].id == itemId) {
				return cache[j];
			}
		}

		if (itemId == -1)
			itemId = 0;
		if (itemId > streamIndices.length)
			itemId = 0;

		cacheIndex = (cacheIndex + 1) % 10;
		final ItemDefinition itemDef = cache[cacheIndex];
		stream.currentOffset = streamIndices[itemId];
		itemDef.id = itemId;
		itemDef.setDefaults();
		itemDef.readValues(stream);

		if (itemDef.certTemplateID != -1) {
			itemDef.toNote();
		}

		if (itemDef.opcode140 != -1) {
			itemDef.method2789(forID(itemDef.opcode140), forID(itemDef.opcode139));
		}

		if (itemDef.opcode149 != -1) {
			itemDef.method2790(forID(itemDef.opcode149), forID(itemDef.opcode148));
		}

		customItems(itemDef.id);

		switch (itemId) {

		}
		return itemDef;
	}

	private static void customItems(int itemId) {
		ItemDefinition itemDef = forID(itemId);

		switch (itemId) {
		// Blood money
		case 30230:
			ItemDefinition coins = forID(995);
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.stackAmounts = coins.stackAmounts;
			 itemDef.stackAmounts = new int[] { 2, 3, 50, 100, 500000, 1000000, 2500000,
			 10000000, 100000000, 0 };//amount the model will change at
			 itemDef.stackIDs = new int[] { 30231, 30232, 30233, 30234, 30235, 30236, 30237,
			 30238, 30239, 0 };//new item id to grab the model from
			itemDef.modelId = coins.modelId;
			itemDef.modelOffset1 = coins.modelOffset1;
			itemDef.modelOffset2 = coins.modelOffset2;
			itemDef.modelRotation1 = coins.modelRotation1;
			itemDef.modelRotation2 = coins.modelRotation2;
			itemDef.modelZoom = coins.modelZoom;
		break;
		case 30231:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2485;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		break;
		case 30232:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2486;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		break;
		case 30233:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2487;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		break;
		case 30234:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2488;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		break;
		case 30235:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2667;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		break;
		case 30236:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2825;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		case 30237:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2423;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		break;
		case 30238:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2710;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
		break;
		case 30239:
			itemDef.name = "Donator Coins";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 8128; //original color
			itemDef.originalModelColors[0] = 48930; 
			itemDef.modelId = 2775;
			itemDef.modelOffset1 = 3;
			itemDef.modelOffset2 = 0;
			itemDef.modelRotation1 = 184;
			itemDef.modelRotation2 = 2012;
			itemDef.modelZoom = 710;
			break;
		case 30307:
			itemDef.name = "@cya@Blue Santa hat";
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = "Wear";
			itemDef.originalModelColors = new int[] { 43848 };
			itemDef.modifiedModelColors  = new int[] { 933 };
			itemDef.modelId = 2537;//Item Look
			itemDef.modelZoom = 540;
			itemDef.modelRotation1 = 72;
			itemDef.modelRotation2 = 136;
			itemDef.modelOffset2 = 0;
			itemDef.modelOffset1 = 1;
			itemDef.maleModel = 189;
			itemDef.femaleModel = 366;
			itemDef.description = "A Blue Santa hat, the most expensive item in the game";
			break;	
		case 30308:
			itemDef.name = "@mag@Purple Santa hat";
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = "Wear";
			itemDef.modifiedModelColors = new int[1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 933;
			itemDef.originalModelColors[0] = 2934671; 
			itemDef.modelId = 2537;//Item Look
			itemDef.modelZoom = 540;
			itemDef.modelRotation1 = 72;
			itemDef.modelRotation2 = 136;
			itemDef.modelOffset2 = 0;
			itemDef.modelOffset1 = 1;
			itemDef.maleModel = 189;
			itemDef.femaleModel = 366;
			itemDef.description = "A Purple Santa hat, the most expensive item in the game";
			break;
		case 30290:
			itemDef.name = "Purple h'ween mask";
			itemDef.inventoryOptions = new String[5]; 
			itemDef.inventoryOptions[1] = "Wear"; 
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 926; //original color
			itemDef.originalModelColors[0] = 2934671; 
			itemDef.modelId = 2438;
			itemDef.modelZoom = 730;
			itemDef.modelRotation1 = 516;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = -10;
			itemDef.maleModel = 3188;
			itemDef.femaleModel = 3192;
			itemDef.description = "An Purple H'ween Mask";
			break;
		case 30291:
			itemDef.name = "Pink h'ween mask";
			itemDef.inventoryOptions = new String[5]; 
			itemDef.inventoryOptions[1] = "Wear"; 
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 926; //original color
			itemDef.originalModelColors[0] = 55626; 
			itemDef.modelId = 2438;
			itemDef.modelZoom = 730;
			itemDef.modelRotation1 = 516;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = -10;
			itemDef.maleModel = 3188;
			itemDef.femaleModel = 3192;
			itemDef.description = "A Pink H'ween Mask";
			break;
		case 30292:
			itemDef.name = "Lime h'ween mask";
			itemDef.inventoryOptions = new String[5]; 
			itemDef.inventoryOptions[1] = "Wear"; 
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 926; //original color
			itemDef.originalModelColors[0] = 23345; 
			itemDef.modelId = 2438;
			itemDef.modelZoom = 730;
			itemDef.modelRotation1 = 516;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = -10;
			itemDef.maleModel = 3188;
			itemDef.femaleModel = 3192;
			itemDef.description = "A Lime Green H'ween Mask";
			break;
		case 30293:
			itemDef.name = "Orange h'ween mask";
			itemDef.inventoryOptions = new String[5]; 
			itemDef.inventoryOptions[1] = "Wear"; 
			itemDef.modifiedModelColors = new int [1];
			itemDef.originalModelColors = new int[1];
			itemDef.modifiedModelColors[0] = 926; //original color
			itemDef.originalModelColors[0] = 4911; 
			itemDef.modelId = 2438;
			itemDef.modelZoom = 730;
			itemDef.modelRotation1 = 516;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = -10;
			itemDef.maleModel = 3188;
			itemDef.femaleModel = 3192;
			itemDef.description = "An Orange H'Ween Mask";
			break;
		case 30196:
			itemDef.name = "Pet night beast";
			itemDef.description = "none.";
			itemDef.modelId = 32933;
			itemDef.modelZoom = 7000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 270;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;
		case 30197:
			itemDef.name = "Pet greater abyssal demon";
			itemDef.description = "none.";
			itemDef.modelId = 32921;
			itemDef.modelZoom = 5000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 270;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30198:
			itemDef.name = "Pet crushing hand";
			itemDef.description = "none.";
			itemDef.modelId = 32922;
			itemDef.modelZoom = 4500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30199:
			itemDef.name = "Pet chasm crawler";
			itemDef.description = "none.";
			itemDef.modelId = 32918;
			itemDef.modelZoom = 2500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30200:
			itemDef.name = "Pet screaming banshee";
			itemDef.description = "none.";
			itemDef.modelId = 32823;
			itemDef.modelZoom = 5500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30201:
			itemDef.name = "Pet twisted banshee";
			itemDef.description = "none.";
			itemDef.modelId = 32847;
			itemDef.modelZoom = 5500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30202:
			itemDef.name = "Pet giant rockslug";
			itemDef.description = "none.";
			itemDef.modelId = 32919;
			itemDef.modelZoom = 4500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30203:
			itemDef.name = "Pet cockathrice";
			itemDef.description = "none.";
			itemDef.modelId = 32920;
			itemDef.modelZoom = 4500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30204:
			itemDef.name = "Pet flaming pyrelord";
			itemDef.description = "none.";
			itemDef.modelId = 32923;
			itemDef.modelZoom = 4500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30205:
			itemDef.name = "Pet monstrous basilisk";
			itemDef.description = "none.";
			itemDef.modelId = 32924;
			itemDef.modelZoom = 4500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30206:
			itemDef.name = "Pet malevolent mage";
			itemDef.description = "none.";
			itemDef.modelId = 32929;
			itemDef.modelZoom = 2500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30207:
			itemDef.name = "Pet insatiable bloodveld";
			itemDef.description = "none.";
			itemDef.modelId = 32926;
			itemDef.modelZoom = 5000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30208:
			itemDef.name = "Pet insatiable mutated bloodveld";
			itemDef.description = "none.";
			itemDef.modelId = 32925;
			itemDef.modelZoom = 5000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30209:
			itemDef.name = "Pet vitreous jelly";
			itemDef.description = "none.";
			itemDef.modelId = 32852;
			itemDef.modelZoom = 4500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30210:
			itemDef.name = "Pet vitreous warped jelly";
			itemDef.description = "none.";
			itemDef.modelId = 32917;
			itemDef.modelZoom = 6000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30211:
			itemDef.name = "Pet cave abomination";
			itemDef.description = "none.";
			itemDef.modelId = 32935;
			itemDef.modelZoom = 5500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30212:
			itemDef.name = "Pet abhorrent spectre";
			itemDef.description = "none.";
			itemDef.modelId = 32930;
			itemDef.modelZoom = 6500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30213:
			itemDef.name = "pet repugnant spectre";
			itemDef.description = "none.";
			itemDef.modelId = 32931;
			itemDef.modelZoom = 6500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30214:
			itemDef.name = "Pet choke devil";
			itemDef.description = "none.";
			itemDef.modelId = 32927;
			itemDef.modelZoom = 6000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30215:
			itemDef.name = "Pet king kurask";
			itemDef.description = "none.";
			itemDef.modelId = 32934;
			itemDef.modelZoom = 8000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30217:
			itemDef.name = "Pet nuclear smoke devil";
			itemDef.description = "none.";
			itemDef.modelId = 32928;
			itemDef.modelZoom = 5500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30218:
			itemDef.name = "Pet marble gargoyle";
			itemDef.description = "none.";
			itemDef.modelId = 34251;
			itemDef.modelZoom = 8000;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;

		case 30219:
			itemDef.name = "Pet nechryarch";
			itemDef.description = "none.";
			itemDef.modelId = 32932;
			itemDef.modelZoom = 6500;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = null;
			itemDef.inventoryOptions[2] = null;
			// itemDef.aByte205 = 3;
			break;
		/**
		 * Start of Max Capes
		 */
			case 13282:
			case 13329:
			case 13331:
			case 13333:
			case 13335:
			case 13337:
			case 13342:
			case 20760:
			//case 21284:
			case 21285:
			case 21776:
			case 21780:
			case 21784:
			case 21898:
				itemDef.inventoryOptions[2] = "Perks";
				itemDef.inventoryOptions[3] = "Features";
				break;
				/**
				 * End of Max capes
				 */
			case 12020:
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[0] = "Fill";
				itemDef.inventoryOptions[2] = "Check";
				itemDef.inventoryOptions[3] = "Empty";
				itemDef.inventoryOptions[4] = "Drop";
				//itemDef.inventoryOptions = new String[] { "Fill", null, "Check", null, "Empty", null, "Destroy", null };
				itemDef.modifiedModelColors = new int [4];
				itemDef.originalModelColors = new int[4];
				itemDef.modifiedModelColors[0] = 6430; //original color
				itemDef.originalModelColors[0] = -32700;
				itemDef.modifiedModelColors[1] = 7467; //original color
				itemDef.originalModelColors[1] = -32591;
				itemDef.modifiedModelColors[2] = 6798; //original color
				itemDef.originalModelColors[2] = -32597;
				itemDef.modifiedModelColors[3] = 7223; //original color
				itemDef.originalModelColors[3] = -14637;
				itemDef.modelZoom = 1095;
				itemDef.modelRotation1 = 189;
				itemDef.modelRotation2 = 337;
				itemDef.modelOffset1 = 3;
				itemDef.modelOffset2 = 0;
				break;
			case 12019:
				itemDef.modifiedModelColors = new int [4];
				itemDef.originalModelColors = new int[4];
				itemDef.modifiedModelColors[0] = 6430; //original color
				itemDef.originalModelColors[0] = 37;
				itemDef.modifiedModelColors[1] = 7467; //original color
				itemDef.originalModelColors[1] = 28;
				itemDef.modifiedModelColors[2] = 6798; //original color
				itemDef.originalModelColors[2] = 16;
				itemDef.modifiedModelColors[3] = 7223; //original color
				itemDef.originalModelColors[3] = 6445;
				itemDef.modelZoom = 779;
				itemDef.modelRotation1 = 54;
				itemDef.modelRotation2 = 269;
				itemDef.modelOffset1 = 3;
				itemDef.modelOffset2 = 0;
				break;
			case 5509:
				itemDef.modelZoom = 520;
				itemDef.modelRotation1 = 216;
				itemDef.modelRotation2 = 108;
				//itemDef.modelOffset1 = 0;
				//itemDef.modelOffset2 = 0;
				itemDef.modelId = 7510;
				itemDef.name = "Small Pouch";
				break;
			case 5510:
				itemDef.modelZoom = 650;
				itemDef.modelRotation1 = 188;
				itemDef.modelRotation2 = 64;
				itemDef.modelOffset1 = 0;
				itemDef.modelOffset2 = 0;
				itemDef.modelId = 7507;
				itemDef.name = "Medium Pouch";
				break;
			case 5511:
				itemDef.modelZoom = 650;
				itemDef.modelRotation1 = 188;
				itemDef.modelRotation2 = 64;
				itemDef.modelOffset1 = 0;
				itemDef.modelOffset2 = 0;
				itemDef.modelId = 7507;
				itemDef.name = "Medium Pouch";
				break;
			case 5512:
				itemDef.modelZoom = 650;
				itemDef.modelRotation1 = 132;
				itemDef.modelRotation2 = 96;
				itemDef.modelOffset1 = 0;
				itemDef.modelOffset2 = 2;
				itemDef.modelId = 7509;
				itemDef.name = "Large Pouch";
				break;
			case 5514:
				itemDef.modelZoom = 760;
				itemDef.modelRotation1 = 128;
				itemDef.modelRotation2 = 1872;
			//	itemDef.modelOffset1 = 0;
				//itemDef.modelOffset2 = 2;
				itemDef.modelId = 7508;
				itemDef.name = "Giant Pouch";
				break;
			case 5515:
				itemDef.modelZoom = 760;
				itemDef.modelRotation1 = 128;
				itemDef.modelRotation2 = 1872;
			//	itemDef.modelOffset1 = 0;
				//itemDef.modelOffset2 = 2;
				itemDef.modelId = 7508;
				itemDef.name = "Giant Pouch";
				break;
		case 6802:
			itemDef.name = "Donator Scroll";
			itemDef.description = "Used as currency in the donator shop.";
			itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
			break;
		case 1815:
			itemDef.modelZoom = 3030;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 84;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.modelId = 6668;
			itemDef.name = "Ahrim Jr";
			itemDef.description = "A Barrows pet!";
			break;
		case 1816:
			itemDef.modelZoom = 3030;
			itemDef.modelRotation1 = 0;
			itemDef.modelRotation2 = 84;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.modelId = 6675;
			itemDef.name = "Karil Jr";
			itemDef.description = "A Barrows pet!";
			break;
		case 1820:
			itemDef.modelZoom = 1030;
			itemDef.modelRotation1 = 96;
			itemDef.modelRotation2 = 128;
			itemDef.modelOffset1 = 5;
			itemDef.modelOffset2 = 95;
			itemDef.modelId = 6652;
			itemDef.name = "Dharok Jr";
			itemDef.description = "A Barrows pet!";
			break;
		case 1817:
			itemDef.modelZoom = 1530;
			itemDef.modelRotation1 = 96;
			itemDef.modelRotation2 = 128;
			itemDef.modelOffset1 = 5;
			itemDef.modelOffset2 = 75;
			itemDef.modelId = 6678;
			itemDef.name = "Verac Jr";
			itemDef.description = "A Barrows pet!";
			break;
		case 1819:
			itemDef.modelZoom = 860;
			itemDef.modelRotation1 = 96;
			itemDef.modelRotation2 = 128;
			itemDef.modelOffset1 = 17;
			itemDef.modelOffset2 = 125;
			itemDef.modelId = 6654;
			itemDef.name = "Guthan Jr";
			itemDef.description = "A Barrows pet!";
			break;
		case 1818:
			itemDef.modelZoom = 685;
			itemDef.modelRotation1 = 96;
			itemDef.modelRotation2 = 118;
			itemDef.modelOffset1 = 8;
			itemDef.modelOffset2 = 125;
			itemDef.modelId = 6657;
			itemDef.name = "Torag Jr";
			itemDef.description = "A Barrows pet!";
			break;
		case 3255://Steve pet
			itemDef.name = "Steve";
			break;
		case 13513:
			itemDef.name = "Bonus XP book (35%) (30Min)";
			itemDef.description = "@red@Provides 35% more experience to whoever claims for 30 minutes!";
			itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
			break;
		case 13279:
			itemDef.name = "Double Drop book (15Min)";
			itemDef.description = "@red@Provides Double Drops to whoever claims for 15 minutes!";
			itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
			break;	
		case 14945:
itemDef.name = "Bonus XP book (35%) (15Min)";
itemDef.modelId = 50503;
itemDef.modelRotation1 = 219;
itemDef.modelRotation2 = 1979;
itemDef.modelOffset2 = 1;
itemDef.modelOffset1 = 4;
itemDef.modelZoom = 905;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;
		case 14947:
			itemDef.name = "Bonus XP book (35%) (60Min)";
itemDef.modelId = 50504;
itemDef.modelRotation1 = 219;
itemDef.modelRotation2 = 1979;
itemDef.modelOffset2 = 1;
itemDef.modelOffset1 = 4;
itemDef.modelZoom = 905;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;
		case 14949:
			itemDef.name = "Bonus XP book (35%) (3Hour)";
itemDef.modelId = 50505;
itemDef.modelRotation1 = 219;
itemDef.modelRotation2 = 1979;
itemDef.modelOffset2 = 1;
itemDef.modelOffset1 = 4;
itemDef.modelZoom = 905;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;
		case 14951:
			itemDef.name = "Bonus XP book (35%) (90 min)";
itemDef.modelId = 50506;
itemDef.modelRotation1 = 219;
itemDef.modelRotation2 = 1979;
itemDef.modelOffset2 = 1;
itemDef.modelOffset1 = 4;
itemDef.modelZoom = 905;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;
		case 14953:
			itemDef.name = "Double Drop book (90Min)";
itemDef.modelId = 50507;
itemDef.modelRotation1 = 276;
itemDef.modelRotation2 = 124;
itemDef.modelOffset2 = 3;
itemDef.modelOffset1 = 5;
itemDef.modelZoom = 1789;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;
		case 14955:
itemDef.name = "Double Drop book (30Min)";
itemDef.modelId = 50508;
itemDef.modelRotation1 = 276;
itemDef.modelRotation2 = 124;
itemDef.modelOffset2 = 3;
itemDef.modelOffset1 = 5;
itemDef.modelZoom = 1789;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;
		case 14957:
itemDef.name = "Double Drop book (60Min)";
itemDef.modelId = 50509;
itemDef.modelRotation1 = 276;
itemDef.modelRotation2 = 124;
itemDef.modelOffset2 = 3;
itemDef.modelOffset1 = 5;
itemDef.modelZoom = 1789;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;
		case 14959:
itemDef.name = "Double Drop book (3Hour)";
itemDef.modelId = 50510;
itemDef.modelRotation1 = 276;
itemDef.modelRotation2 = 124;
itemDef.modelOffset2 = 3;
itemDef.modelOffset1 = 5;
itemDef.modelZoom = 1789;
itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
break;

		case 11179:
			itemDef.name = "Raid Points";
			itemDef.description = "Gather these from doing raids.";
			break;
			
		case 20791:
			itemDef.name = "Skilling Supply Crate";
			itemDef.description = "A reward from completeing Daily tasks.";
			break;
		
		case 22560:
			itemDef.modelId = 8275;
			itemDef.name = "Divine Spirit Shield";
			itemDef.description = "It's a Divine Spirit Shield.";
			itemDef.maleModel = 8275;
			itemDef.femaleModel = 8275;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = "Wield";
			itemDef.inventoryOptions[4] = "Drop";
			byte[] ph = FileOperations.readFile(Signlink.getCacheDirectory() + "/Customs/Divine_spirit_shield.png");
			itemDef.customSpriteLocation = ph;
			break;
		case 22562:
			itemDef.name = "Dice (up to 100)";
			itemDef.description = "A 100-sided dice.";
			itemDef.modelId = 31223;
			itemDef.modelZoom = 1050;
			itemDef.modelRotation2 = 215;
			itemDef.modelRotation1 = 94;
			itemDef.modelOffset2 = -5;
			itemDef.modelOffset1 = -10;
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = "Public-roll";
			itemDef.inventoryOptions[2] = null;
			itemDef.name = "Dice (up to 100)";
			itemDef.anInt196 = 15;
			itemDef.anInt184 = 25;
			break;
			
		case 11864:
		case 11865:
		case 19639:
		case 19641:
		case 19643:
		case 19645:
		case 19647:
		case 19649:
		case 21264:
		case 21266:
		case 21888:
		case 21890:
		case 23073:
		case 23075:
			itemDef.equipActions[2] = "Log";
			itemDef.equipActions[1] = "Check";
			break;
		case 21347:
			itemDef.inventoryOptions = new String[5];
			itemDef.inventoryOptions[1] = "Set Bolt Tips";
			itemDef.inventoryOptions[2] = "Set Arrowtips";
			itemDef.inventoryOptions[3] = "Set Javelin Heads";
			break;
		case 8152:
            itemDef.name = "@mag@Bank Chest";
            itemDef.inventoryOptions = new String[] { "Open", null, "Check Charges", null, "Drop" };
            itemDef.description = "Bank your items here, for a price!";
            break;
        case 12792:
			itemDef.name = "Graceful Recolor";
			itemDef.description = "Use on your Graceful to recolor it!";
			break;			
		case 3495:
			itemDef.name = "Bank Charge";
			itemDef.inventoryOptions = new String[] { "Extract", "Use", null, null, "Drop" };
			itemDef.description = "Gives you one charge for your bank.";
			break;
		case 13136:
			itemDef.equipActions[2] = "Elidinis";
			itemDef.equipActions[1] = "Kalphite Hive";
			break;
		case 2550:
			itemDef.equipActions[2] = "Check";
			break;

		case 1712:
		case 1710:
		case 1708:
		case 1706:
			itemDef.equipActions[1] = "Edgeville";
			itemDef.equipActions[2] = "Karamja";
			itemDef.equipActions[3] = "Draynor";
			itemDef.equipActions[4] = "Al-Kharid";
			break;

		case 2552:
		case 2554:
		case 2556:
		case 2558:
		case 2560:
		case 2562:
		case 2564:
		case 2566: // Ring of duelling
			itemDef.equipActions[2] = "Shantay Pass";
			itemDef.equipActions[1] = "Clan wars";
			break;

		case 19668:
			itemDef.name = "Portable Bank";
			break;
		
		case 13226:
			itemDef.name = "Herb sack";
			break;
			
		case 11739:
			itemDef.name = "Hourly Box";
		break;
			 //DeadlyStuff
        case 13302:
            itemDef.name = "@red@Deadly Key";
            break;
           
      /*  case 964:
            itemDef.name = "@red@Deadly Key Piece";
            break;*/
           
        case 6104:
            itemDef.name = "@red@Deadly Key Piece";
            break;
        //EndDeadlyStuff
        case 6949:
        	itemDef.name = "Imbued scroll";
        	break;
            
        case 20238:
        	itemDef.name = "Imbue Scroll";
        	itemDef.inventoryOptions = new String[] { null, null, null, "Drop" };
        	break;
			case 21307:
				itemDef.name = "Pursuit crate";
				break;
			case 2996:
				itemDef.name = "PKP Ticket";
				itemDef.inventoryOptions = new String[5];
				itemDef.description = "Exchange this for a PK Point.";
				break;
			case 13346:
				itemDef.name = "Ultra Mystery Box";
				itemDef.inventoryOptions = new String[] { "Open", null, null, null, "Drop" };
				itemDef.description = "Mystery box that contains goodies.";
				break;
			case 15098:
				itemDef.name = "Dice (up to 100)";
				itemDef.description = "A 100-sided dice.";
				itemDef.modelId = 31223;
				itemDef.modelZoom = 1050;
				itemDef.modelRotation2 = 215;
				itemDef.modelRotation1 = 94;
				itemDef.modelOffset2 = -5;
				itemDef.modelOffset1 = 0;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Public-roll";
				itemDef.inventoryOptions[2] = null;
				itemDef.name = "Dice (up to 100)";
				itemDef.anInt196 = 15;
				itemDef.anInt184 = 25;
				break;
		case 11773:
		case 11771:
		case 11770:
		case 11772:
			itemDef.anInt196 += 45;
			break;
		case 2697:
			itemDef.name = "$5 Scroll";
			itemDef.description = "Read this scroll to be rewarded with $5 credit.";
			break;
		case 2698:
			itemDef.name = "$15 Scroll";
			itemDef.description = "Read this scroll to be rewarded with $15 credit.";
			break;
		case 2699:
			itemDef.name = "$35 Scroll";
			itemDef.description = "Read this scroll to be rewarded with $35 credit.";
			break;
		case 2700:
			itemDef.name = "$50 Scroll";
			itemDef.description = "Read this scroll to be rewarded with $50 credit.";
			break;
		case 1464:
			itemDef.name = "Vote ticket";
			itemDef.description = "This ticket can be exchanged for a voting point.";
			break;
		}
	}

	void method2789(ItemDefinition var1, ItemDefinition var2) {
		modelId = var1.modelId * 1;
		modelZoom = var1.modelZoom * 1;
		modelRotation1 = 1 * var1.modelRotation1;
		modelRotation2 = 1 * var1.modelRotation2;
		anInt204 = 1 * var1.anInt204;
		modelOffset1 = 1 * var1.modelOffset1;
		modelOffset2 = var1.modelOffset2 * 1;
		originalModelColors = var2.originalModelColors;
		modifiedModelColors = var2.modifiedModelColors;
		// originalTextureColors = var2.originalTextureColors;
		// modifiedTextureColors = var2.modifiedTextureColors;
		name = var2.name;
		membersObject = var2.membersObject;
		stackable = var2.stackable;
		maleModel = 1 * var2.maleModel;
		anInt188 = 1 * var2.anInt188;
		anInt185 = 1 * var2.anInt185;
		femaleModel = var2.femaleModel * 1;
		anInt164 = var2.anInt164 * 1;
		anInt162 = 1 * var2.anInt162;
		anInt175 = 1 * var2.anInt175;
		anInt166 = var2.anInt166 * 1;
		anInt197 = var2.anInt197 * 1;
		anInt173 = var2.anInt173 * 1;
		team = var2.team * 1;
		groundOptions = var2.groundOptions;
		inventoryOptions = new String[5];
		equipActions = new String[5];
		if (null != var2.inventoryOptions) {
			for (int var4 = 0; var4 < 4; ++var4) {
				inventoryOptions[var4] = var2.inventoryOptions[var4];
			}
		}

		inventoryOptions[4] = "Discard";
		value = 0;
	}

	void method2790(ItemDefinition var1, ItemDefinition var2) {
		modelId = var1.modelId * 1;
		modelZoom = 1 * var1.modelZoom;
		modelRotation1 = var1.modelRotation1 * 1;
		modelRotation2 = var1.modelRotation2 * 1;
		anInt204 = var1.anInt204 * 1;
		modelOffset1 = 1 * var1.modelOffset1;
		modelOffset2 = var1.modelOffset2 * 1;
		originalModelColors = var1.originalModelColors;
		modifiedModelColors = var1.modifiedModelColors;
		originalTextureColors = var1.originalTextureColors;
		modifiedTextureColors = var1.modifiedTextureColors;
		stackable = var1.stackable;
		name = var2.name;
		value = 0;
	}


	private void readValues(Stream stream) {
		while (true) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				return;
			if (opcode == 1)
				modelId = stream.readUnsignedWord();
			else if (opcode == 2)
				name = stream.readString();
			else if (opcode == 3)
				description = stream.readString();
			else if (opcode == 4)
				modelZoom = stream.readUnsignedWord();
			else if (opcode == 5)
				modelRotation1 = stream.readUnsignedWord();
			else if (opcode == 6)
				modelRotation2 = stream.readUnsignedWord();
			else if (opcode == 7) {
				modelOffset1 = stream.readUnsignedWord();
				if (modelOffset1 > 32767)
					modelOffset1 -= 0x10000;
			} else if (opcode == 8) {
				modelOffset2 = stream.readUnsignedWord();
				if (modelOffset2 > 32767)
					modelOffset2 -= 0x10000;
			} else if (opcode == 11)
				stackable = true;
			else if (opcode == 12)
				value = stream.readDWord();
			else if (opcode == 16)
				membersObject = true;
			else if (opcode == 23) {
				maleModel = stream.readUnsignedWord();
				aByte205 = stream.readSignedByte();
			} else if (opcode == 24)
				anInt188 = stream.readUnsignedWord();
			else if (opcode == 25) {
				femaleModel = stream.readUnsignedWord();
				aByte154 = stream.readSignedByte();
			} else if (opcode == 26)
				anInt164 = stream.readUnsignedWord();
			else if (opcode >= 30 && opcode < 35) {
				if (groundOptions == null)
					groundOptions = new String[5];
				groundOptions[opcode - 30] = stream.readString();
				if (groundOptions[opcode - 30].equalsIgnoreCase("hidden"))
					groundOptions[opcode - 30] = null;
			} else if (opcode >= 35 && opcode < 40) {
				if (inventoryOptions == null)
					inventoryOptions = new String[5];
				inventoryOptions[opcode - 35] = stream.readString();
			} else if (opcode == 40) {
				int size = stream.readUnsignedByte();
				originalModelColors = new int[size];
				modifiedModelColors = new int[size];
				for (int index = 0; index < size; index++) {
					originalModelColors[index] = stream.readUnsignedWord();
					modifiedModelColors[index] = stream.readUnsignedWord();
				}
			} else if (opcode == 41) {
				int size = stream.readUnsignedByte();
				originalTextureColors = new short[size];
				modifiedTextureColors = new short[size];
				for (int index = 0; index < size; index++) {
					originalTextureColors[index] = (short) stream.readUnsignedWord();
					modifiedTextureColors[index] = (short) stream.readUnsignedWord();
				}
			} else if (opcode == 65) {
				searchableItem = true;
			} else if (opcode == 78)
				anInt185 = stream.readUnsignedWord();
			else if (opcode == 79)
				anInt162 = stream.readUnsignedWord();
			else if (opcode == 90)
				anInt175 = stream.readUnsignedWord();
			else if (opcode == 91)
				anInt197 = stream.readUnsignedWord();
			else if (opcode == 92)
				anInt166 = stream.readUnsignedWord();
			else if (opcode == 93)
				anInt173 = stream.readUnsignedWord();
			else if (opcode == 95)
				anInt204 = stream.readUnsignedWord();
			else if (opcode == 97)
				certID = stream.readUnsignedWord();
			else if (opcode == 98)
				certTemplateID = stream.readUnsignedWord();
			else if (opcode >= 100 && opcode < 110) {
				if (stackIDs == null) {
					stackIDs = new int[10];
					stackAmounts = new int[10];
				}
				stackIDs[opcode - 100] = stream.readUnsignedWord();
				stackAmounts[opcode - 100] = stream.readUnsignedWord();
			} else if (opcode == 110)
				anInt167 = stream.readUnsignedWord();
			else if (opcode == 111)
				anInt192 = stream.readUnsignedWord();
			else if (opcode == 112)
				anInt191 = stream.readUnsignedWord();
			else if (opcode == 113)
				anInt196 = stream.readSignedByte();
			else if (opcode == 114)
				anInt184 = stream.readSignedByte() * 5;
			else if (opcode == 115)
				team = stream.readUnsignedByte();
			else if (opcode == 139)
				opcode139 = stream.readUnsignedWord();
			else if (opcode == 140)
				opcode140 = stream.readUnsignedWord();
			else if (opcode == 148)
				opcode148 = stream.readUnsignedWord();
			else if (opcode == 149)
				opcode149 = stream.readUnsignedWord();
			else {
				// System.out.println("Error loading item " + id + ", opcode " + opcode);
			}
		}
	}

	public int opcode139, opcode140, opcode148, opcode149;

	public static void nullLoader() {
		mruNodes2 = null;
		mruNodes1 = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public boolean method192(int j) {
		int k = anInt175;
		int l = anInt166;
		if (j == 1) {
			k = anInt197;
			l = anInt173;
		}
		if (k == -1)
			return true;
		boolean flag = true;
		if (!Model.method463(k))
			flag = false;
		if (l != -1 && !Model.method463(l))
			flag = false;
		return flag;
	}

	public Model method194(int j) {
		int k = anInt175;
		int l = anInt166;
		if (j == 1) {
			k = anInt197;
			l = anInt173;
		}
		if (k == -1)
			return null;
		Model model = Model.method462(k);
		if (l != -1) {
			Model model_1 = Model.method462(l);
			Model aclass30_sub2_sub4_sub6s[] = { model, model_1 };
			model = new Model(2, aclass30_sub2_sub4_sub6s);
		}
		if (modifiedModelColors != null) {
			for (int i1 = 0; i1 < modifiedModelColors.length; i1++)
				model.method476(modifiedModelColors[i1], originalModelColors[i1]);

		}
		return model;
	}

	public boolean method195(int j) {
		int k = maleModel;
		int l = anInt188;
		int i1 = anInt185;
		if (j == 1) {
			k = femaleModel;
			l = anInt164;
			i1 = anInt162;
		}
		if (k == -1)
			return true;
		boolean flag = true;
		if (!Model.method463(k))
			flag = false;
		if (l != -1 && !Model.method463(l))
			flag = false;
		if (i1 != -1 && !Model.method463(i1))
			flag = false;
		return flag;
	}

	public Model method196(int i) {
		int j = maleModel;
		int k = anInt188;
		int l = anInt185;
		if (i == 1) {
			j = femaleModel;
			k = anInt164;
			l = anInt162;
		}
		if (j == -1)
			return null;
		Model model = Model.method462(j);
		if (k != -1)
			if (l != -1) {
				Model model_1 = Model.method462(k);
				Model model_3 = Model.method462(l);
				Model aclass30_sub2_sub4_sub6_1s[] = { model, model_1, model_3 };
				model = new Model(3, aclass30_sub2_sub4_sub6_1s);
			} else {
				Model model_2 = Model.method462(k);
				Model aclass30_sub2_sub4_sub6s[] = { model, model_2 };
				model = new Model(2, aclass30_sub2_sub4_sub6s);
			}
		if (i == 0 && aByte205 != 0)
			model.method475(0, aByte205, 0);
		if (i == 1 && aByte154 != 0)
			model.method475(0, aByte154, 0);
		if (modifiedModelColors != null) {
			for (int i1 = 0; i1 < modifiedModelColors.length; i1++)
				model.method476(modifiedModelColors[i1], originalModelColors[i1]);

		}
		return model;
	}

	private void setDefaults() {
		// equipActions = new String[6];
		customSpriteLocation = null;
		equipActions = new String[] { "Remove", null, "Operate", null, null };
		modelId = 0;
		name = null;
		description = null;
		modifiedModelColors = null;
		originalModelColors = null;
		modifiedTextureColors = null;
		originalTextureColors = null;
		modelZoom = 2000;
		modelRotation1 = 0;
		modelRotation2 = 0;
		anInt204 = 0;
		modelOffset1 = 0;
		modelOffset2 = 0;
		stackable = false;
		value = 1;
		membersObject = false;
		groundOptions = null;
		inventoryOptions = null;
		maleModel = -1;
		anInt188 = -1;
		aByte205 = 0;
		femaleModel = -1;
		anInt164 = -1;
		aByte154 = 0;
		anInt185 = -1;
		anInt162 = -1;
		anInt175 = -1;
		anInt166 = -1;
		anInt197 = -1;
		anInt173 = -1;
		stackIDs = null;
		stackAmounts = null;
		certID = -1;
		certTemplateID = -1;
		anInt167 = 128;
		anInt192 = 128;
		anInt191 = 128;
		anInt196 = 0;
		anInt184 = 0;
		team = 0;

		opcode140 = -1;
		opcode139 = -1;
		opcode148 = -1;
		opcode149 = -1;

		searchableItem = false;
	}

	public static void dumpBonuses() {
		int[] bonuses = new int[14];
		int bonus = 0;
		int amount = 0;
		for (int i = 21304; i < totalItems; i++) {
			ItemDefinition item = ItemDefinition.forID(i);
			URL url;
			try {
				try {
					try {
						url = new URL("http://2007.runescape.wikia.com/wiki/" + item.name.replaceAll(" ", "_"));
						URLConnection con = url.openConnection();
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String line;
						BufferedWriter writer = new BufferedWriter(new FileWriter("item.cfg", true));
						while ((line = in.readLine()) != null) {
							try {
								if (line.contains("<td style=\"text-align: center; width: 35px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "")
											.replace("\"\"", "")
											.replace("<td style=\"text-align: center; width: 35px;\">", "");
									bonuses[bonus] = Integer.parseInt(line);
									bonus++;
								} else if (line.contains("<td style=\"text-align: center; width: 30px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<td style=\"text-align: center; width: 30px;\">", "");
									bonuses[bonus] = Integer.parseInt(line);
									bonus++;
								}
							} catch (NumberFormatException e) {

							}
							if (bonus >= 13)
								bonus = 0;
							// in.close();
						}
						in.close();
						writer.write("item	=	" + i + "	" + item.name.replace(" ", "_") + "	"
								+ item.description.replace(" ", "_") + "	" + item.value + "	" + item.value + "	"
								+ item.value + "	" + bonuses[0] + "	" + bonuses[1] + "	" + bonuses[2] + "	"
								+ bonuses[3] + "	" + bonuses[4] + "	" + bonuses[5] + "	" + bonuses[6] + "	"
								+ bonuses[7] + "	" + bonuses[8] + "	" + bonuses[9] + "	" + bonuses[10] + "	"
								+ bonuses[13]);
						bonuses[0] = bonuses[1] = bonuses[2] = bonuses[3] = bonuses[4] = bonuses[5] = bonuses[6] = bonuses[7] = bonuses[8] = bonuses[9] = bonuses[10] = bonuses[13] = 0;
						writer.newLine();
						amount++;
						writer.close();
					} catch (NullPointerException e) {

					}
				} catch (FileNotFoundException e) {

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done dumping " + amount + " item bonuses!");
	}

	public static void dumpBonus() {
		final int[] wikiBonuses = new int[18];
		int bonus = 0;
		int amount = 0;
		System.out.println("Starting to dump item bonuses...");
		for (int i = 20000; i < totalItems; i++) {
			ItemDefinition item = ItemDefinition.forID(i);
			try {
				try {
					try {
						final URL url = new URL(
								"http://2007.runescape.wikia.com/wiki/" + item.name.replaceAll(" ", "_"));
						URLConnection con = url.openConnection();
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String line;
						writer = new BufferedWriter(new FileWriter("item.cfg", true));
						while ((line = in.readLine()) != null) {
							try {
								if (line.contains("<td style=\"text-align: center; width: 35px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "")
											.replace("\"\"", "")
											.replace("<td style=\"text-align: center; width: 35px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								} else if (line.contains("<td style=\"text-align: center; width: 30px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<td style=\"text-align: center; width: 30px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								}
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
							in.close();
							writer.write("item = " + i + "	" + item.name.replace(" ", "_") + "	"
									+ item.description.replace(" ", "_") + "	" + item.value + "	" + item.value
									+ "	" + item.value + "	" + wikiBonuses[0] + "	" + wikiBonuses[1] + "	"
									+ wikiBonuses[2] + "	" + wikiBonuses[3] + "	" + wikiBonuses[4] + "	"
									+ wikiBonuses[5] + "	" + wikiBonuses[6] + "	" + wikiBonuses[7] + "	"
									+ wikiBonuses[8] + "	" + wikiBonuses[9] + "	" + wikiBonuses[10] + "	"
									+ wikiBonuses[13]);
							amount++;
							wikiBonuses[0] = wikiBonuses[1] = wikiBonuses[2] = wikiBonuses[3] = wikiBonuses[4] = wikiBonuses[5] = wikiBonuses[6] = wikiBonuses[7] = wikiBonuses[8] = wikiBonuses[9] = wikiBonuses[10] = wikiBonuses[11] = wikiBonuses[13] = 0;
							writer.newLine();
							writer.close();
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Done dumping " + amount + " item bonuses!");
		}
	}

	public static void dumpItemDefs() {
		final int[] wikiBonuses = new int[18];
		int bonus = 0;
		int amount = 0;
		int value = 0;
		int slot = -1;
		// Testing Variables just so i know format is correct
		String fullmask = "false";
		// boolean stackable1 = false;
		String stackable = "false";
		// boolean noteable1 = false;
		String noteable = "true";
		// boolean tradeable1 = false;
		String tradeable = "true";
		// boolean wearable1 = false;
		String wearable = "true";
		String showBeard = "true";
		String members = "true";
		boolean twoHanded = false;
		System.out.println("Starting to dump item definitions...");
		for (int i = 21298; i < totalItems; i++) {
			ItemDefinition item = ItemDefinition.forID(i);
			try {
				try {
					try {
						final URL url = new URL(
								"http://2007.runescape.wikia.com/wiki/" + item.name.replaceAll(" ", "_"));
						URLConnection con = url.openConnection();
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String line;
						writer = new BufferedWriter(new FileWriter("itemDefs.json", true));
						while ((line = in.readLine()) != null) {
							try {
								if (line.contains("<td style=\"text-align: center; width: 35px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "")
											.replace("\"\"", "")
											.replace("<td style=\"text-align: center; width: 35px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								} else if (line.contains("<td style=\"text-align: center; width: 30px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<td style=\"text-align: center; width: 30px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								}
								if (line.contains("<div id=\"GEPCalcResult\" style=\"display:inline;\">")) {
									line = line.replace("</div>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<div id=\"GEPCalcResult\" style=\"display:inline;\">", "");
									value = Integer.parseInt(line);
								}

							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
							in.close();
							// fw.write("ItemID: "+itemDefinition.id+" - "+itemDefinition.name);
							// fw.write(System.getProperty("line.separator"));
							// writer.write("[\n");
							writer.write("  {\n\t\"id\": " + item.id + ",\n\t\"name\": \"" + item.name
									+ "\",\n\t\"desc\": \"" + item.description.replace("_", " ") + "\",\n\t\"value\": "
									+ value + ",\n\t\"dropValue\": " + value + ",\n\t\"bonus\": [\n\t  "
									+ wikiBonuses[0] + ",\n\t  " + wikiBonuses[1] + ",\n\t  " + wikiBonuses[2]
									+ ",\n\t  " + wikiBonuses[3] + ",\n\t  " + wikiBonuses[4] + ",\n\t  "
									+ wikiBonuses[5] + ",\n\t  " + wikiBonuses[6] + ",\n\t  " + wikiBonuses[7]
									+ ",\n\t  " + wikiBonuses[8] + ",\n\t  " + wikiBonuses[9] + ",\n\t  "
									+ wikiBonuses[10] + ",\n\t  " + wikiBonuses[13] + ",\n\t],\n\t\"slot\": " + slot
									+ ",\n\t\"fullmask\": " + fullmask + ",\n\t\"stackable\": " + stackable
									+ ",\n\t\"noteable\": " + noteable + ",\n\t\"tradeable\": " + tradeable
									+ ",\n\t\"wearable\": " + wearable + ",\n\t\"showBeard\": " + showBeard
									+ ",\n\t\"members\": " + members + ",\n\t\"slot\": " + twoHanded
									+ ",\n\t\"requirements\": [\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t]\n  },\n");
							/*
							 * writer.write("item = " + i + "	" + item.name.replace(" ", "_") + "	" +
							 * item.description.replace(" ", "_") + "	" + item.value + "	" + item.value +
							 * "	" + item.value + "	" + wikiBonuses[0] + "	" + wikiBonuses[1] + "	" +
							 * wikiBonuses[2] + "	" + wikiBonuses[3] + "	" + wikiBonuses[4] + "	" +
							 * wikiBonuses[5] + "	" + wikiBonuses[6] + "	" + wikiBonuses[7] + "	" +
							 * wikiBonuses[8] + "	" + wikiBonuses[9] + "	" + wikiBonuses[10] + "	" +
							 * wikiBonuses[13]);
							 */
							amount++;
							wikiBonuses[0] = wikiBonuses[1] = wikiBonuses[2] = wikiBonuses[3] = wikiBonuses[4] = wikiBonuses[5] = wikiBonuses[6] = wikiBonuses[7] = wikiBonuses[8] = wikiBonuses[9] = wikiBonuses[10] = wikiBonuses[11] = wikiBonuses[13] = 0;
							writer.newLine();
							writer.close();
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Done dumping " + amount + " item definitions!");
		}
	}

	public static void itemDump() {
		try {
			FileWriter fw = new FileWriter(System.getProperty("user.home") + "/Desktop/Item Dump.txt");
			for (int i = totalItems - 9000; i < totalItems; i++) {
				ItemDefinition item = ItemDefinition.forID(i);
				fw.write("case " + i + ":");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.name = \"" + item.name + "\";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelID= " + item.modelId + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.maleModel= " + item.maleModel + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.femaleModel= " + item.femaleModel + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelZoom = " + item.modelZoom + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelRotationX = " + item.modelRotation1 + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelRotationY = " + item.modelRotation2 + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelOffset1 = " + item.modelOffset1 + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelOffset2 = " + item.modelOffset2 + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.description = \"" + item.description + "\";");
				fw.write(System.getProperty("line.separator"));

				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.value = " + item.value + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.team = " + item.team + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("break;");
				fw.write(System.getProperty("line.separator"));
				fw.write(System.getProperty("line.separator"));
			}
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	   public static void applyTexturing(Model model, int id) {
		      switch(id) {
		      case 1843:
		         model.setTexture(25);
		         break;
		      }

		   }
	public static void dumpList() {
		try {
			FileWriter fw = new FileWriter(System.getProperty("user.home") + "/Desktop/item_data_178.json");
			for (int i = 0; i < totalItems; i++) {
				if(i== 20548) {
					i+=60;
				}
				ItemDefinition itemDefinition = ItemDefinition.forID(i);
				fw.write("id: " + itemDefinition.id + " - " + itemDefinition.name + "\n");
			}
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void dumpStackableList() {
		try {
			File file = new File("stackables.dat");

			if (!file.exists()) {
				file.createNewFile();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (int i = 0; i < totalItems; i++) {
					ItemDefinition definition = forID(i);
					if (definition != null) {
						writer.write(definition.id + "\t" + definition.stackable);
						writer.newLine();
					} else {
						writer.write(i + "\tfalse");
						writer.newLine();
					}
				}
			}

			System.out.println("Finished dumping noted items definitions.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int[] unNoteable = {};

	public static void dumpNotes() {
		try {
			FileOutputStream out = new FileOutputStream(new File("notes.dat"));
			for (int j = 0; j < totalItems; j++) {
				ItemDefinition item = ItemDefinition.forID(j);
				for (int i = 0; i < totalItems; i++)
					if (j == unNoteable[i] + 1)
						out.write(0);
					else
						out.write(item.certTemplateID != -1 ? 0 : 1);
			}
			out.write(-1);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	public static void dumpColors() {
		try {
			FileWriter fw = new FileWriter(System.getProperty("user.home") + "/Desktop/item_data.json");
			for (int i = 19000; i < totalItems; i++) {
				ItemDefinition itemDefinition = ItemDefinition.forID(i);
				fw.write("id: " + itemDefinition.id + " - " + itemDefinition.name + "\n");
				fw.write("model id: " + itemDefinition.modelId + "\n");
				fw.write("male model id: " + itemDefinition.anInt188 + "\n");
				fw.write("female model id: " + itemDefinition.anInt164 + "\n");
				fw.write("modified color: " + Arrays.toString(itemDefinition.modifiedModelColors) + "\n");
				fw.write("original color: " + Arrays.toString(itemDefinition.originalModelColors) + "\n\n");
			}
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("Done dumping item colors!");
	}
	public static void dumpStackable() {
		try {
			FileOutputStream out = new FileOutputStream(new File("stackable.dat"));
			for (int j = 0; j < totalItems; j++) {
				ItemDefinition item = ItemDefinition.forID(j);
				out.write(item.stackable ? 1 : 0);
			}
			out.write(-1);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void dumpNotableList() {
		try {
			File file = new File("note_id.dat");

			if (!file.exists()) {
				file.createNewFile();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (int i = 0; i < totalItems; i++) {
					ItemDefinition definition = ItemDefinition.forID(i);
					if (definition != null) {
						if (definition.certTemplateID == -1 && definition.certID != -1) {
							writer.write(definition.id + "\t" + definition.certID);
							writer.newLine();
						}
					} else {
						writer.write(i + "\t-1");
						writer.newLine();
					}
				}
			}

			System.out.println("Finished dumping noted items definitions.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void toNote() {
		ItemDefinition itemDef = forID(certTemplateID);
		modelId = itemDef.modelId;
		modelZoom = itemDef.modelZoom;
		modelRotation1 = itemDef.modelRotation1;
		modelRotation2 = itemDef.modelRotation2;

		anInt204 = itemDef.anInt204;
		modelOffset1 = itemDef.modelOffset1;
		modelOffset2 = itemDef.modelOffset2;
		modifiedModelColors = itemDef.modifiedModelColors;
		originalModelColors = itemDef.originalModelColors;
		ItemDefinition itemDef_1 = forID(certID);
		name = itemDef_1.name;
		membersObject = itemDef_1.membersObject;
		value = itemDef_1.value;
		String s = "a";
		char c = itemDef_1.name.charAt(0);
		if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U')
			s = "an";
		description = ("Swap this note at any bank for " + s + " " + itemDef_1.name + ".");
		stackable = true;
	}

	public static Sprite getSmallSprite(int itemId) {
		ItemDefinition itemDef = forID(itemId);
		Model model = itemDef.method201(1);
		if (model == null) {
			return null;
		}
		Sprite sprite1 = null;
		if (itemDef.certTemplateID != -1) {
			sprite1 = getSprite(itemDef.certID, 10, -1);
			if (sprite1 == null) {
				return null;
			}
		}
		Sprite enabledSprite = new Sprite(18, 18);
		int k1 = Rasterizer.textureInt1;
		int l1 = Rasterizer.textureInt2;
		int ai[] = Rasterizer.anIntArray1472;
		int ai1[] = DrawingArea.pixels;
		int i2 = DrawingArea.width;
		int j2 = DrawingArea.height;
		int k2 = DrawingArea.topX;
		int l2 = DrawingArea.bottomX;
		int i3 = DrawingArea.topY;
		int j3 = DrawingArea.bottomY;
		Rasterizer.aBoolean1464 = false;
		DrawingArea.initDrawingArea(18, 18, enabledSprite.myPixels, new float[1024]);
		DrawingArea.method336(18, 0, 0, 0, 18);
		Rasterizer.method364();
		int k3 = (int) (itemDef.modelZoom * 1.6D);
		int l3 = Rasterizer.anIntArray1470[itemDef.modelRotation1] * k3 >> 16;
		int i4 = Rasterizer.anIntArray1471[itemDef.modelRotation1] * k3 >> 16;
		model.method482(itemDef.modelRotation2, itemDef.anInt204, itemDef.modelRotation1, itemDef.modelOffset1,
				l3 + model.modelHeight / 2 + itemDef.modelOffset2, i4 + itemDef.modelOffset2);
		if (itemDef.certTemplateID != -1) {
			int l5 = sprite1.maxWidth;
			int j6 = sprite1.maxHeight;
			sprite1.maxWidth = 18;
			sprite1.maxHeight = 18;
			sprite1.drawSprite(0, 0);
			sprite1.maxWidth = l5;
			sprite1.maxHeight = j6;
		}
		DrawingArea.initDrawingArea(j2, i2, ai1, new float[1024]);
		DrawingArea.setDrawingArea(j3, k2, l2, i3);
		Rasterizer.textureInt1 = k1;
		Rasterizer.textureInt2 = l1;
		Rasterizer.anIntArray1472 = ai;
		Rasterizer.aBoolean1464 = true;

		enabledSprite.maxWidth = 18;
		enabledSprite.maxHeight = 18;

		return enabledSprite;
	}

	public static Sprite getSprite(int itemId, int itemAmount, int highlightColor) {
		if (highlightColor == 0) {
			Sprite sprite = (Sprite) mruNodes1.insertFromCache(itemId);
			if (sprite != null && sprite.maxHeight != itemAmount && sprite.maxHeight != -1) {
				sprite.unlink();
				sprite = null;
			}
			if (sprite != null)
				return sprite;
		}
		ItemDefinition itemDef = forID(itemId);
		if (itemDef.stackIDs == null)
			itemAmount = -1;
		if (itemAmount > 1) {
			int i1 = -1;
			for (int j1 = 0; j1 < 10; j1++)
				if (itemAmount >= itemDef.stackAmounts[j1] && itemDef.stackAmounts[j1] != 0)
					i1 = itemDef.stackIDs[j1];

			if (i1 != -1)
				itemDef = forID(i1);
		}
		Model model = itemDef.method201(1);
		if (model == null)
			return null;
		Sprite sprite = null;
		if (itemDef.certTemplateID != -1) {
			sprite = getSprite(itemDef.certID, 10, -1);
			if (sprite == null)
				return null;
		} else if (itemDef.opcode140 != -1) {
			sprite = getSprite(itemDef.opcode139, itemAmount, -1);
			if (sprite == null)
				return null;
		} else if (itemDef.opcode149 != -1) {
			sprite = getSprite(itemDef.opcode148, itemAmount, -1);
			if (sprite == null)
				return null;
		}
		Sprite sprite2 = new Sprite(32, 32);
		int k1 = Rasterizer.textureInt1;
		int l1 = Rasterizer.textureInt2;
		int ai[] = Rasterizer.anIntArray1472;
		int ai1[] = DrawingArea.pixels;
		int i2 = DrawingArea.width;
		int j2 = DrawingArea.height;
		int k2 = DrawingArea.topX;
		int l2 = DrawingArea.bottomX;
		int i3 = DrawingArea.topY;
		int j3 = DrawingArea.bottomY;
		Rasterizer.aBoolean1464 = false;
		DrawingArea.initDrawingArea(32, 32, sprite2.myPixels, new float[1024]);
		DrawingArea.method336(32, 0, 0, 0, 32);
		Rasterizer.method364();
		if (itemDef.opcode149 != -1) {
			int l5 = sprite.maxWidth;
			int j6 = sprite.maxHeight;
			sprite.maxWidth = 32;
			sprite.maxHeight = 32;
			sprite.drawSprite(0, 0);
			sprite.maxWidth = l5;
			sprite.maxHeight = j6;
		}
		int k3 = itemDef.modelZoom;
		if (highlightColor == -1)
			k3 = (int) ((double) k3 * 1.5D);
		if (highlightColor > 0)
			k3 = (int) ((double) k3 * 1.04D);
		int l3 = Rasterizer.anIntArray1470[itemDef.modelRotation1] * k3 >> 16;
		int i4 = Rasterizer.anIntArray1471[itemDef.modelRotation1] * k3 >> 16;
		model.method482(itemDef.modelRotation2, itemDef.anInt204, itemDef.modelRotation1, itemDef.modelOffset1,
				l3 + model.modelHeight / 2 + itemDef.modelOffset2, i4 + itemDef.modelOffset2);
		if (itemDef.opcode140 != -1) {
			int l5 = sprite.maxWidth;
			int j6 = sprite.maxHeight;
			sprite.maxWidth = 32;
			sprite.maxHeight = 32;
			sprite.drawSprite(0, 0);
			sprite.maxWidth = l5;
			sprite.maxHeight = j6;
		}
		for (int i5 = 31; i5 >= 0; i5--) {
			for (int j4 = 31; j4 >= 0; j4--)
				if (sprite2.myPixels[i5 + j4 * 32] == 0)
					if (i5 > 0 && sprite2.myPixels[(i5 - 1) + j4 * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if (j4 > 0 && sprite2.myPixels[i5 + (j4 - 1) * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if (i5 < 31 && sprite2.myPixels[i5 + 1 + j4 * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if (j4 < 31 && sprite2.myPixels[i5 + (j4 + 1) * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;

		}

		if (highlightColor > 0) {
			for (int j5 = 31; j5 >= 0; j5--) {
				for (int k4 = 31; k4 >= 0; k4--)
					if (sprite2.myPixels[j5 + k4 * 32] == 0)
						if (j5 > 0 && sprite2.myPixels[(j5 - 1) + k4 * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;
						else if (k4 > 0 && sprite2.myPixels[j5 + (k4 - 1) * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;
						else if (j5 < 31 && sprite2.myPixels[j5 + 1 + k4 * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;
						else if (k4 < 31 && sprite2.myPixels[j5 + (k4 + 1) * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;

			}

		} else if (highlightColor == 0) {
			for (int k5 = 31; k5 >= 0; k5--) {
				for (int l4 = 31; l4 >= 0; l4--)
					if (sprite2.myPixels[k5 + l4 * 32] == 0 && k5 > 0 && l4 > 0
							&& sprite2.myPixels[(k5 - 1) + (l4 - 1) * 32] > 0)
						sprite2.myPixels[k5 + l4 * 32] = 0x302020;

			}

		}
		if (itemDef.certTemplateID != -1) {
			int l5 = sprite.maxWidth;
			int j6 = sprite.maxHeight;
			sprite.maxWidth = 32;
			sprite.maxHeight = 32;
			sprite.drawSprite(0, 0);
			sprite.maxWidth = l5;
			sprite.maxHeight = j6;
		}
		if (highlightColor == 0)
			mruNodes1.removeFromCache(sprite2, itemId);
		DrawingArea.initDrawingArea(j2, i2, ai1, new float[1024]);
		DrawingArea.setDrawingArea(j3, k2, l2, i3);
		Rasterizer.textureInt1 = k1;
		Rasterizer.textureInt2 = l1;
		Rasterizer.anIntArray1472 = ai;
		Rasterizer.aBoolean1464 = true;
		if (itemDef.stackable)
			sprite2.maxWidth = 33;
		else
			sprite2.maxWidth = 32;
		sprite2.maxHeight = itemAmount;
		return sprite2;
	}

	public Model method201(int i) {
		if (stackIDs != null && i > 1) {
			int j = -1;
			for (int k = 0; k < 10; k++)
				if (i >= stackAmounts[k] && stackAmounts[k] != 0)
					j = stackIDs[k];

			if (j != -1)
				return forID(j).method201(1);
		}
		Model model = (Model) mruNodes2.insertFromCache(id);
		if (model != null)
			return model;
		model = Model.method462(modelId);
		if (model == null)
			return null;
		if (anInt167 != 128 || anInt192 != 128 || anInt191 != 128)
			model.method478(anInt167, anInt191, anInt192);
		if (modifiedModelColors != null) {
			for (int l = 0; l < modifiedModelColors.length; l++)
				model.method476(modifiedModelColors[l], originalModelColors[l]);

		}
		model.method479(64 + anInt196, 768 + anInt184, -50, -10, -50, true);
		model.aBoolean1659 = true;
		mruNodes2.removeFromCache(model, id);
		return model;
	}

	public Model method202(int i) {
		if (stackIDs != null && i > 1) {
			int j = -1;
			for (int k = 0; k < 10; k++)
				if (i >= stackAmounts[k] && stackAmounts[k] != 0)
					j = stackIDs[k];

			if (j != -1)
				return forID(j).method202(1);
		}
		Model model = Model.method462(modelId);
		if (model == null)
			return null;
		if (modifiedModelColors != null) {
			for (int l = 0; l < modifiedModelColors.length; l++)
				model.method476(modifiedModelColors[l], originalModelColors[l]);

		}
		return model;
	}

	private ItemDefinition() {
		id = -1;
	}

	private byte aByte154;
	public int value;
	public int[] originalModelColors;
	public int[] modifiedModelColors;

	private short[] originalTextureColors;
	private short[] modifiedTextureColors;

	public int id;
	public static MRUNodes mruNodes1 = new MRUNodes(100);
	public static MRUNodes mruNodes2 = new MRUNodes(50);

	public boolean membersObject;
	private int anInt162;
	public int certTemplateID;
	private int anInt164;
	public int maleModel;
	private int anInt166;
	private int anInt167;
	public String groundOptions[];
	public int modelOffset1;
	public String name;
	private static ItemDefinition[] cache;
	private int anInt173;
	public int modelId;
	private int anInt175;
	public boolean stackable;
	public String description;
	public int certID;
	private static int cacheIndex;
	public int modelZoom;
	private static Stream stream;
	private int anInt184;
	private int anInt185;
	private int anInt188;
	public String inventoryOptions[];
	public String equipActions[];
	public int modelRotation1;
	private int anInt191;
	private int anInt192;
	private int[] stackIDs;
	public int modelOffset2;
	private static int[] streamIndices;
	private int anInt196;
	private int anInt197;
	public int modelRotation2;
	public int femaleModel;
	private int[] stackAmounts;
	public int team;
	public static int totalItems;
	private int anInt204;
	private byte aByte205;
	public boolean searchableItem;
	private static BufferedWriter writer;

}
