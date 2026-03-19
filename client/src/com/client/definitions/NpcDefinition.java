package com.client.definitions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Arrays;
//
//import org.apache.commons.io.FileUtils;

import com.client.Class36;
import com.client.Client;
import com.client.Configuration;
import com.client.MRUNodes;
import com.client.Model;
import com.client.Stream;
import com.client.StreamLoader;

public final class NpcDefinition {

	public static NpcDefinition forID(int i) {
		for (int j = 0; j < 20; j++)
			if (cache[j].interfaceType == i)
				return cache[j];

		anInt56 = (anInt56 + 1) % 20;
		NpcDefinition entityDef = cache[anInt56] = new NpcDefinition();
		stream.currentOffset = streamIndices[i];
		entityDef.interfaceType = i;
		entityDef.readValues(stream);
		if (i == 5693) {
			entityDef.name = "Pet night beast";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32933};
			entityDef.standAnim = 2730;
			entityDef.walkAnim = 2729;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5671) {
			entityDef.name = "Pet greater abyssal demon";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32921};
			entityDef.standAnim = 1536;
			entityDef.walkAnim = 1534;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5672) {
			entityDef.name = "Pet crushing hand";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32922};
			entityDef.standAnim = 1588;
			entityDef.walkAnim = 1589;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 50;
			entityDef.anInt86 = 50;

		}


		if (i == 5673) {
			entityDef.name = "Pet chasm crawler";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32918};
			entityDef.standAnim = 226;
			entityDef.walkAnim = 225;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 50;
			entityDef.anInt86 = 50;

		}

		if (i == 5674) {
			entityDef.name = "Pet screaming banshee";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32823};
			entityDef.standAnim = 7545;
			entityDef.walkAnim = 1104;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5675) {
			entityDef.name = "Pet twisted banshee";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32847};
			entityDef.standAnim = 7545;
			entityDef.walkAnim = 1104;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5676) {
			entityDef.name = "Pet giant rockslug";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32919};
			entityDef.standAnim = 1566;
			entityDef.walkAnim = 1564;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5677) {
			entityDef.name = "Pet cockathrice";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32920};
			entityDef.standAnim = 1561;
			entityDef.walkAnim = 1559;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 60;
			entityDef.anInt86 = 60;

		}

		if (i == 5678) {
			entityDef.name = "Pet flaming pyrelord";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32923};
			entityDef.standAnim = 1578;
			entityDef.walkAnim = 1579;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 50;
			entityDef.anInt86 = 50;

		}


		if (i == 5679) {
			entityDef.name = "Pet monstrous basilisk";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32924};
			entityDef.standAnim = 1545;
			entityDef.walkAnim = 1544;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5680) {
			entityDef.name = "Pet malevolent mage";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32929};
			entityDef.standAnim = 813;
			entityDef.walkAnim = 1205;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 70;
			entityDef.anInt86 = 70;

		}

		if (i == 5681) {
			entityDef.name = "Pet insatiable bloodveld";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32926};
			entityDef.standAnim = 1551;
			entityDef.walkAnim = 1549;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 50;
			entityDef.anInt86 = 50;

		}

		if (i == 5682) {
			entityDef.name = "Pet insatiable mutated bloodveld";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32925};
			entityDef.standAnim = 1551;
			entityDef.walkAnim = 1549;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 50;
			entityDef.anInt86 = 50;

		}

		if (i == 5683) {
			entityDef.name = "Pet vitreous jelly";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32852};
			entityDef.standAnim = 1583;
			entityDef.walkAnim = 1584;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5684) {
			entityDef.name = "Pet vitreous warped jelly";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32917};
			entityDef.standAnim = 1583;
			entityDef.walkAnim = 1584;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 30;
			entityDef.anInt86 = 30;

		}


		if (i == 5685) {
			entityDef.name = "Pet cave abomination";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32935};
			entityDef.standAnim = 4231;
			entityDef.walkAnim = 4236;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 50;
			entityDef.anInt86 = 50;

		}


		if (i == 5686) {
			entityDef.name = "Pet abhorrent spectre";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32930};
			entityDef.standAnim = 6371;
			entityDef.walkAnim = 6372;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}

		if (i == 5687) {
			entityDef.name = "Pet repugnant spectre";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32931};
			entityDef.standAnim = 6371;
			entityDef.walkAnim = 6372;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 40;
			entityDef.anInt86 = 40;

		}


		if (i == 5688) {
			entityDef.name = "Pet choke devil";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32927};
			entityDef.standAnim = 1556;
			entityDef.walkAnim = 1554;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 50;
			entityDef.anInt86 = 50;

		}


		if (i == 5689) {
			entityDef.name = "Pet king kurask";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32934};
			entityDef.standAnim = 4231;
			entityDef.walkAnim = 4236;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 30;
			entityDef.anInt86 = 30;

		}


		if (i == 5690) {
			entityDef.name = "Pet nuclear smoke devil";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32928};
			entityDef.standAnim = 1829;
			entityDef.walkAnim = 1828;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 30;
			entityDef.anInt86 = 30;

		}

		if (i == 5691) {
			entityDef.name = "Pet marble gargoyle";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 34251};
			entityDef.standAnim = 7810;
			entityDef.walkAnim = 7810;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 30;
			entityDef.anInt86 = 30;

		}

		if (i == 5692) {
			entityDef.name = "Pet nechryarch";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 32932};
			entityDef.standAnim = 6371;
			entityDef.walkAnim = 6372;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 30;
			entityDef.anInt86 = 30;

		}
		if (i == 6005) {
			entityDef.name = "Peter Rabbit";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] {28602};
			entityDef.standAnim = 6089;
			entityDef.walkAnim = 6088;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 120;
			entityDef.anInt86 = 120;
			entityDef.originalColors = new int []  { 2378 };
			entityDef.newColors = new int [] { 9152 };
		}
		if (i == 3397) {
			entityDef.name = "Baby Dragon (Onyx)";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 17413, 17427, 17421, 17424 };
			entityDef.standAnim = 27;
			entityDef.walkAnim = 21;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };;
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
            entityDef.originalColors = new int []  { 910, 912, 1938, 1814, 2588, 2469 };
		    entityDef.newColors = new int [] {-22008, -22013, -22011, -22016, -27459, -27450};
		    entityDef.anInt91 = 45;
            entityDef.anInt86 = 45;
		}
		
		if (i == 3398) {
			entityDef.name = "Baby Dragon (Sapphire)";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 17413, 17427, 17421, 17424 };
			entityDef.standAnim = 27;
			entityDef.walkAnim = 21;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };;
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
            entityDef.originalColors = new int []  { 910, 912, 1938, 1814, 2588, 2469 };
		    entityDef.newColors = new int [] {-21585, -21590, -21595, -21570, -27459, -27450};
		    entityDef.anInt91 = 45;
            entityDef.anInt86 = 45;
		}
		
		if (i == 3405) {
			entityDef.name = "Baby Dragon (Emerald)";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 17413, 17427, 17421, 17424 };
			entityDef.standAnim = 27;
			entityDef.walkAnim = 21;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };;
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
            entityDef.originalColors = new int []  { 910, 912, 1938, 1814, 2588, 2469 };
		    entityDef.newColors = new int [] {22443, 22447, 22449, 22451, -27459, -27450};
		    entityDef.anInt91 = 45;
            entityDef.anInt86 = 45;
		}
		
		if (i == 3400) {
			entityDef.name = "Baby Dragon (Ruby)";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 17413, 17427, 17421, 17424 };
			entityDef.standAnim = 27;
			entityDef.walkAnim = 21;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };;
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
            entityDef.originalColors = new int []  { 910, 912, 1938, 1814, 2588, 2469 };
		    entityDef.newColors = new int [] {931, 935, 940, 943, -27459, -27450};
		    entityDef.anInt91 = 45;
            entityDef.anInt86 = 45;
		}
		
		if (i == 3401) {
			entityDef.name = "Baby Dragon (Diamond)";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 17413, 17427, 17421, 17424 };
			entityDef.standAnim = 27;
			entityDef.walkAnim = 21;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };;
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
            entityDef.originalColors = new int []  { 910, 912, 1938, 1814, 2588, 2469 };
		    entityDef.newColors = new int [] {127, 125, 123, 120, -27459, -27450};
		    entityDef.anInt91 = 45;
            entityDef.anInt86 = 45;
		}
		
		if (i == 3402) {
			entityDef.name = "Baby Dragon (Dragonstone)";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 17413, 17427, 17421, 17424 };
			entityDef.standAnim = 27;
			entityDef.walkAnim = 21;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };;
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
            entityDef.originalColors = new int []  { 910, 912, 1938, 1814, 2588, 2469 };
		    entityDef.newColors = new int [] {-14425, -14430, -14435, -14440, -27459, -27450};
		    entityDef.anInt91 = 45;
            entityDef.anInt86 = 45;
            
		}
		
		if (i == 3404) {
			entityDef.name = "Baby Dragon (Zenyte)";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] { 17413, 17427, 17421, 17424 };
			entityDef.standAnim = 27;
			entityDef.walkAnim = 21;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };;
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
			//entityDef.anInt91 = 105;
          //  entityDef.anInt86 = 105;
            entityDef.originalColors = new int []  { 910, 912, 1938, 1814, 2588, 2469 };
		    entityDef.newColors = new int [] {5056, 5060, 5065, 5070, -27459, -27450};
		    entityDef.anInt91 = 45;
            entityDef.anInt86 = 45;
            
		}
		if (i == 5458) {
			entityDef.name = "Pet wyrm";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] {36166};
			entityDef.standAnim = 8266;
			entityDef.walkAnim = 8266;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", "Transform", null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 35;
			entityDef.anInt86 = 35;

		}
		if (i == 5459) {
			entityDef.name = "Pet drake";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] {36160};
			entityDef.standAnim = 8274;
			entityDef.walkAnim = 8273;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", null, null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 35;
			entityDef.anInt86 = 35;

		}
		if (i == 5460) {
			entityDef.name = "Pet wyrm";
			entityDef.combatLevel = 0;
			entityDef.models = new int[] {36167};
			entityDef.standAnim = 8267;
			entityDef.walkAnim = 8267;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick-up", "Transform", null, null, null };
			entityDef.getDegreesToTurn = 64;
			entityDef.anInt91 = 35;
			entityDef.anInt86 = 35;

		}
	if(i==7303) {
		entityDef.name = "Clue Expert";
		entityDef.description = "Trade a set of clue scrolls for a better one";
		entityDef.actions = new String[] { "Talk-to", null, null, null, null };
	}
		if (i == 506) {
			entityDef.name = "Iron Man Store";
			entityDef.description = "A shop specifically for iron men.";
		}if(i==3218 || i ==3217) {
			entityDef.actions = new String[] { "Talk-to", null, null, null, null };
		}
	if(i==2989) {
		entityDef.name = "Prestige Manager";
		entityDef.actions = new String[] { "Talk-to", null, "Quick-Manage", "Quick-Shop", null };
	}
	if(i==1603) {
		entityDef.actions = new String[] { "Talk-to", null, "Trade", null, null };
	}
	//start of quest npcs
	if (i == 778) {
		entityDef.name = "Ethereal Elder";
		entityDef.description = "Wise & worried.. I wonder if she has a quest for me!";
	}
	if (i == 777) {
		entityDef.name = "Distraught Ethereal Elder";
		entityDef.description = "Something doesn't seem right here..";
	}
	if (i == 3291) {
		entityDef.name = "Petie";
		entityDef.onMinimap = false;
		entityDef.actions = new String[5];
		entityDef.actions = new String[] { "Pick-up", null, null, null, null };
		entityDef.anInt91 = 90;
		entityDef.anInt86 = 90;
	}
	if (i == 786) {
		entityDef.actions = new String[] { null, null, "Pick up", null, null };
		entityDef.models = new int[] { 16480, 16484, 16487, 16471, 16475, 16482, 16474, 16489, 16483, 11049 };
		entityDef.originalColors = new int[7];
		entityDef.newColors = new int[7];
		entityDef.originalColors[0] = 33676;
		entityDef.newColors[0] = 49346;
		entityDef.originalColors[1] = 410;
		entityDef.newColors[1] = 29902;
		entityDef.originalColors[2] = 33300;
		entityDef.newColors[2] = 50244;
		entityDef.originalColors[3] = 36007;
		entityDef.newColors[3] = 231;
		entityDef.originalColors[4] = 6573;
		entityDef.newColors[4] = 54371;
		entityDef.originalColors[5] = 33036;
		entityDef.newColors[5] = 35404;
		entityDef.originalColors[6] = 82;
		entityDef.newColors[6] = 35404;
		entityDef.anInt86 = 80;
		entityDef.anInt91 = 80;
		entityDef.combatLevel = 0;
		entityDef.onMinimap = false;
		entityDef.name = "Ethereal Scavenger";
		entityDef.standAnim = 813;
		entityDef.walkAnim = 1205;
		entityDef.description = "Very good at finding rare loot.";
	}
	
	if (i == 785) {
		entityDef.models = new int[] { 16480, 16484, 16487, 16471, 16475, 16482, 16474, 35371, 16483 };
		entityDef.originalColors = new int[20];
		entityDef.newColors = new int[20];
		entityDef.originalColors[0] = 33676;
		entityDef.newColors[0] = 49346;
		entityDef.originalColors[1] = 410;
		entityDef.newColors[1] = 29902;
		entityDef.originalColors[2] = 33300;
		entityDef.newColors[2] = 50244;
		entityDef.originalColors[3] = 36007;
		entityDef.newColors[3] = 231;
		entityDef.originalColors[4] = 6573;
		entityDef.newColors[4] = 54371;
		entityDef.originalColors[5] = 33;//handle
		entityDef.newColors[5] = 54371;
		//entityDef.newColors[5] = 35404;
		entityDef.originalColors[5] = 16;
		entityDef.originalColors[6] = 24;
		entityDef.newColors[6] = 54371;
		entityDef.originalColors[7] = 20;
		entityDef.newColors[7] = 54371;
		entityDef.originalColors[8] = 37;
		entityDef.newColors[8] = 54371;
		entityDef.originalColors[9] = 28;
		entityDef.newColors[9] = 54371;
		entityDef.originalColors[10] = 78;//blade
		entityDef.newColors[10] = 29902;
	//	entityDef.originalColors[11] = 78;//blade
	//	entityDef.newColors[11] = 29902;
		entityDef.originalColors[11] = 61;//blade
		entityDef.newColors[11] = 29902;
		entityDef.originalColors[12] = 49;//blade
		entityDef.newColors[12] = 29902;
		//entityDef.newColors[13] = 35404;
		entityDef.originalColors[13] = 536;//Red spikes
		entityDef.newColors[13] = 49346;
		entityDef.originalColors[14] = 784;//bottom section of vial
		entityDef.newColors[14] = 49346;
		entityDef.originalColors[15] = 796;//top section of vial
		entityDef.newColors[15] = 49346;
		entityDef.originalColors[16] = 790;//middle section of vial
		entityDef.newColors[16] = 49346;
		entityDef.originalColors[17] = 41;//bottom of handle
		entityDef.newColors[17] = 54371;
		entityDef.originalColors[18] = 33;//middle of handle
		entityDef.newColors[18] = 54371;
		entityDef.originalColors[19] = 33036;//middle of handle
		entityDef.newColors[19] = 31058;
		entityDef.name = "Ethereal Reaper";
		entityDef.description = "Something doesn't seem right..";
	}
	if (i == 657) {
		entityDef.models = new int[] { 35349, 35359, 35356, 36928, 33103, 36325, 35371, 31766, 11048 };
		entityDef.name = "Taavi";
		entityDef.description = "Something doesn't seem right..";
		entityDef.standAnim = 813;
		entityDef.walkAnim = 1205;
	}
	if (i == 658) {
		entityDef.models = new int[] { 35943, 35953, 35950, 36928, 33103, 36325, 35969, 31766, 35376 };
		entityDef.name = "Aegeus";
		entityDef.description = "Something doesn't seem right..";
		entityDef.standAnim = 813;
		entityDef.walkAnim = 1205;
	}
	if (i == 659) {
		entityDef.models = new int[] { 35944, 35952, 35949, 29249, 34148, 31231, 35772, 31767, 11050 };
		entityDef.name = "Kagan";
		entityDef.description = "Something doesn't seem right..";
		entityDef.standAnim = 813;
		entityDef.walkAnim = 1205;
	}
	if (i == 660) {
		entityDef.models = new int[] { 35945, 35954, 35948, 34628, 32667, 31781, 29252, 32668, 12778 };
		entityDef.name = "Obasi";
		entityDef.description = "Something doesn't seem right..";
		entityDef.standAnim = 808;
		entityDef.walkAnim = 819;
	}
	if (i == 661) {
		entityDef.models = new int[] { 36960, 36965, 36964, 36963, 36962, 36968, 36966, 34649, 31652, 36326 };
		entityDef.name = "Ornate Guard";
		entityDef.description = "A guard wearing some of the fanciest gear";
		entityDef.standAnim = 813;
		entityDef.walkAnim = 1205;
	}
	if (i == 3078 || i == 3106) { //Man
		entityDef.actions = new String[5];
		entityDef.actions = new String[] { "Pickpocket", null, null, null, null };
	}
		if (i == 6035) { //Nieve
			entityDef.name = "Nieve";
			entityDef.models = new int[] { 392, 27644, 27640, 29255, 3661, 28827, 9644, 27654, 9640, 11048 };
			entityDef.standAnim = 813;
			entityDef.walkAnim = 1205;
			entityDef.onMinimap = false;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 80;
			entityDef.anInt91 = 80;
		}
		
		if (i == 6798 || i == 6801) { //Steve and Pieve
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 80;
			entityDef.anInt91 = 80;
			entityDef.onMinimap = false;
		}
		if (i == 1693) { //Pest control pets
			entityDef.name = "Splatter";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 80;
			entityDef.anInt91 = 80;
			entityDef.onMinimap = false;
		}
		if (i == 1712) { //Pest control pets
			entityDef.name = "Spinner";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 80;
			entityDef.anInt91 = 80;
			entityDef.onMinimap = false;
		}
		if (i == 1703) { //Pest control pets
			entityDef.name = "Shifter";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 80;
			entityDef.anInt91 = 80;
			entityDef.onMinimap = false;
		}
		if (i == 3822) {
			entityDef.name = "Lava strykewyrm";
			entityDef.combatLevel = 562;
			entityDef.models = new int[] {60835};
			entityDef.standAnim = 5070;
			entityDef.walkAnim = 5070;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { null, "Attack", null, null, null };
			entityDef.getDegreesToTurn = 64;
			//entityDef.size = 2;
		}
		/**
		 * Start of Slayer pets
		 */
		if (i == 449) { //Crawling Hand
			entityDef.name = "Crawling hand";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 482) { //Cave Bug
			entityDef.name = "Cave bug larva";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			//entityDef.anInt86 = 60;
			//entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 407) { //Cave Crawler
			entityDef.name = "Cave crawler";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}	
		if (i == 3984) {//Banshee
		entityDef.actions = new String[5];
		entityDef.actions = new String[] { "Pick Up", null, null, null, null };
		NpcDefinition banshee = forID(414);
		entityDef.models = new int[2];
		entityDef.models[0] = banshee.models[0];
		entityDef.models[1] = banshee.models[1];
		entityDef.name = "Banshee";
		entityDef.combatLevel = 0;
		entityDef.standAnim = banshee.standAnim;
		entityDef.walkAnim = banshee.walkAnim;
		entityDef.anInt86 = 60;
		entityDef.anInt91 = 60;
		entityDef.onMinimap = false;
		entityDef.standAnim = banshee.standAnim;
		entityDef.walkAnim = banshee.walkAnim;
		}
		if (i == 3985) {//Cave Slime
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
		NpcDefinition caveSlime = forID(480);
		entityDef.models = new int[1];
		entityDef.models[0] = caveSlime.models[0];
		entityDef.name = "Cave slime";
		entityDef.combatLevel = 0;
		entityDef.standAnim = caveSlime.standAnim;
		entityDef.walkAnim = caveSlime.walkAnim;
		entityDef.anInt86 = 60;
		entityDef.anInt91 = 60;
		entityDef.onMinimap = false;
		entityDef.standAnim = caveSlime.standAnim;
		entityDef.walkAnim = caveSlime.walkAnim;
		}
		if (i == 422) { //Rockslug
			entityDef.name = "Rockslug";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 420) { //Cockatrice
			entityDef.name = "Cockatrice";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 434) { //Pyrefiend
			entityDef.name = "Pyrefiend";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 418) { //Basilisk
			entityDef.name = "Basilisk";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 443) { //Infernal Mage
			entityDef.name = "Infernal mage";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 486) { //BloodVeld
			entityDef.name = "Bloodveld";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 440) { //Jelly
			entityDef.name = "Jelly";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 426) { //Turoth
			entityDef.name = "Turoth";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 504) { //Cave Horror
				entityDef.actions = new String[5];
				entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			NpcDefinition caveHorror = forID(3213);
			entityDef.models = new int[2];
			entityDef.models[0] = caveHorror.models[0];
			entityDef.models[1] = caveHorror.models[1];
			entityDef.name = "Cave horror";
			entityDef.combatLevel = 0;
			entityDef.standAnim = caveHorror.standAnim;
			entityDef.walkAnim = caveHorror.walkAnim;
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
			entityDef.standAnim = caveHorror.standAnim;
			entityDef.walkAnim = caveHorror.walkAnim;
			}
		if (i == 3) { //Abberrant Spectre
			entityDef.name = "Aberrant sepctre";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 30;
			entityDef.anInt91 = 30;
			entityDef.onMinimap = false;
		}
		if (i == 7249) { //Dust Devil
			entityDef.name = "Dust devil";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 410) { //Kurask
			entityDef.name = "Kurask";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 60;
			entityDef.anInt91 = 60;
			entityDef.onMinimap = false;
		}
		if (i == 466) { //Skeletal Wyvern
			entityDef.name = "Skeletal wyvern";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 50;
			entityDef.anInt91 = 50;
			entityDef.onMinimap = false;
		}
		if (i == 412) { //Gargoyle
			entityDef.name = "gargoyle";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 50;
			entityDef.anInt91 = 50;
			entityDef.onMinimap = false;
		}
		if (i == 8) { //Nechryael
			entityDef.name = "nechryael";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 50;
			entityDef.anInt91 = 50;
			entityDef.onMinimap = false;
		}
		if (i == 416) { //Abyssal Demon
			entityDef.name = "abyssal Demon";
			entityDef.combatLevel = 0;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
			entityDef.anInt86 = 30;
			entityDef.anInt91 = 30;
			entityDef.onMinimap = false;
		}
		if (i == 603) { //Dark Beast
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Pick Up", null, null, null, null };
		NpcDefinition darkBeast = forID(4005);
		entityDef.models = new int[1];
		entityDef.models[0] = darkBeast.models[0];
		entityDef.name = "Dark beast";
		entityDef.combatLevel = 0;
		entityDef.standAnim = darkBeast.standAnim;
		entityDef.walkAnim = darkBeast.walkAnim;
		entityDef.anInt86 = 60;
		entityDef.anInt91 = 60;
		entityDef.onMinimap = false;
		entityDef.standAnim = darkBeast.standAnim;
		entityDef.walkAnim = darkBeast.walkAnim;
		}
		/**
		 * Barrows pets
		 */
		if (i == 5845) {
		entityDef.actions[0] = "Pick-up";
		entityDef.actions[2] = "Talk-to";
		NpcDefinition dharok = forID(1673);
		//entityDef.readValues(stream);
		entityDef.models = new int[6];
		entityDef.models[0] = dharok.models[0];
		entityDef.models[1] = dharok.models[1];
		entityDef.models[2] = dharok.models[2];
		entityDef.models[3] = dharok.models[3];
		entityDef.models[4] = dharok.models[4];
		entityDef.models[5] = dharok.models[5];
		entityDef.name = "Dharok Jr";
		entityDef.description = "A smaller version of Dharok himself.";
		entityDef.standAnim = dharok.standAnim;
		entityDef.walkAnim = dharok.walkAnim;
		entityDef.anInt86 = 90;
		entityDef.anInt91 = 90;
		entityDef.onMinimap = false;
		}
		if (i == 5846) {
		//stream.currentOffset = streamIndices[1674];
		entityDef.actions[0] = "Pick-up";
		entityDef.actions[2] = "Talk-to";
		NpcDefinition guthan = forID(1674);
		//entityDef.readValues(stream);
		entityDef.models = new int[6];
		entityDef.models[0] = guthan.models[0];
		entityDef.models[1] = guthan.models[1];
		entityDef.models[2] = guthan.models[2];
		entityDef.models[3] = guthan.models[3];
		entityDef.models[4] = guthan.models[4];
		entityDef.models[5] = guthan.models[5];
		entityDef.name = "Guthan Jr";
		entityDef.description = "A smaller version of Guthan himself.";
		entityDef.standAnim = guthan.standAnim;
		entityDef.walkAnim = guthan.walkAnim;
		entityDef.anInt86 = 90;
		entityDef.anInt91 = 90;
		entityDef.onMinimap = false;
		}
		if (i == 2391) {
		//stream.currentOffset = streamIndices[1674];
		entityDef.actions[0] = "Pick-up";
		entityDef.actions[2] = "Talk-to";
		NpcDefinition torag = forID(1676);
		//entityDef.readValues(stream);
		entityDef.models = new int[6];
		entityDef.models[0] = torag.models[0];
		entityDef.models[1] = torag.models[1];
		entityDef.models[2] = torag.models[2];
		entityDef.models[3] = torag.models[3];
		entityDef.models[4] = torag.models[4];
		entityDef.models[5] = torag.models[5];
		entityDef.name = "Torag Jr";
		entityDef.description = "A smaller version of Torag himself.";
		entityDef.standAnim = torag.standAnim;
		entityDef.walkAnim = torag.walkAnim;
		entityDef.anInt86 = 90;
		entityDef.anInt91 = 90;
		}
		if (i == 2392) {
		//stream.currentOffset = streamIndices[1674];
		entityDef.actions[0] = "Pick-up";
		entityDef.actions[2] = "Talk-to";
		NpcDefinition karil = forID(1675);
		//entityDef.readValues(stream);
		entityDef.models = new int[1];
		entityDef.models[0] = karil.models[0];
		entityDef.name = "Karil Jr";
		entityDef.description = "A smaller version of Karil himself.";
		entityDef.standAnim = karil.standAnim;
		entityDef.walkAnim = karil.walkAnim;
		entityDef.anInt86 = 90;
		entityDef.anInt91 = 90;
		}
		if (i == 2393) {
		//stream.currentOffset = streamIndices[1674];
		entityDef.actions[0] = "Pick-up";
		entityDef.actions[2] = "Talk-to";
		NpcDefinition verac = forID(1677);
		//entityDef.readValues(stream);
		entityDef.models = new int[2];
		entityDef.models[0] = verac.models[0];
		entityDef.models[1] = verac.models[1];
		entityDef.name = "Verac Jr";
		entityDef.description = "A smaller version of Verac himself.";
		entityDef.standAnim = verac.standAnim;
		entityDef.walkAnim = verac.walkAnim;
		entityDef.anInt86 = 90;
		entityDef.anInt91 = 90;
		}
		if (i == 2394) {
		//stream.currentOffset = streamIndices[1674];
		entityDef.actions[0] = "Pick-up";
		entityDef.actions[2] = "Talk-to";
		NpcDefinition ahrim = forID(1672);
		//entityDef.readValues(stream);
		entityDef.models = new int[1];
		entityDef.models[0] = ahrim.models[0];
		entityDef.name = "Ahrim Jr";
		entityDef.description = "A smaller version of Ahrim himself.";
		entityDef.standAnim = ahrim.standAnim;
		entityDef.walkAnim = ahrim.walkAnim;
		entityDef.anInt86 = 90;
		entityDef.anInt91 = 90;
		}
		/**
		 * End of Barrows pets
		 */
		if (i == 771) {
            entityDef.name = "";
            entityDef.models = new int[1];
            entityDef.models[0] = 1822;
            entityDef.actions = new String[5];
            entityDef.actions[0] = "";
            entityDef.actions[1] = null;
            entityDef.actions[2] = null;
            entityDef.actions[3] = null;
            entityDef.actions[4] = null;
            entityDef.description = "";
            entityDef.onMinimap = false;
        }
		if (i == 2580) {
		entityDef.actions = new String[] { "Talk-to", null, "Teleport Abyss", null, "Teleport Mine" };
		}
		if (i == 7371) {
		entityDef.actions = new String[] { "Get Daily Task", null, "Open Shop", null, null };
		entityDef.name = "Daily Manager";
		entityDef.description = "Handles Daily Tasks & the Daily Point Shop";
		}
		if (i == 6295) {
            entityDef.name = "@red@Demon @whi@God";
            entityDef.description = "A mighty foe, only the best can defeat!";
            entityDef.combatLevel = 5268;
            entityDef.onMinimap = true;
        }
		
		if (i == 8026) {
			entityDef.name = "Vorkath";
			// entityDef.combatLevel = 732;
			entityDef.models = new int[] { 35023 };
			entityDef.standAnim = 7946;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { "Poke", null, null, null, null };
			entityDef.anInt86 = 100;
			entityDef.anInt91 = 100;
		}
		if (i == 8027) {
			entityDef.name = "Vorkath";
			entityDef.combatLevel = 732;
			entityDef.models = new int[] { 35023 };
			entityDef.standAnim = 7950;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { null, null, null, null, null };
			entityDef.anInt86 = 100;
			entityDef.anInt91 = 100;
		}
		if (i == 8028) {
			entityDef.name = "Vorkath";
			entityDef.combatLevel = 732;
			entityDef.models = new int[] { 35023 };
			entityDef.standAnim = 7948;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { null, "Attack", null, null, null };
			entityDef.anInt86 = 100;
			entityDef.anInt91 = 100;
		}
		
		if (i == 8059) {
			entityDef.name = "Vorkath";
			entityDef.combatLevel = 732;
			entityDef.models = new int[] { 35023 };
			entityDef.standAnim = 7950;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { null, null, null, null, null };
			entityDef.anInt86 = 100;
			entityDef.anInt91 = 100;
		}
		if (i == 8061) {
			entityDef.name = "Vorkath";
			entityDef.combatLevel = 732;
			entityDef.models = new int[] { 35023 };
			entityDef.standAnim = 7948;
			entityDef.onMinimap = true;
			entityDef.actions = new String[5];
			entityDef.actions = new String[] { null, "Attack", null, null, null };
			entityDef.anInt86 = 100;
			entityDef.anInt91 = 100;
		}
		
		if (i==3257){
			entityDef.actions = new String[] { "Trade", null, null, null, null };
		}
		if (i==17){
			entityDef.actions = new String[] { "Pollnivneach", null, null, null, null };
		}
		if (i==3936){
			entityDef.actions = new String[] { "Neitiznot", "Jatizso", null, null, null };
		}
		if (i==1056){
			entityDef.name = "Herblore Shop";
			entityDef.actions = new String[] { "Trade", null, null, null, null };
		}
		if (i==1815){
			entityDef.name = "Fishing Supplies";
			entityDef.actions = new String[] { "Trade", null, null, null, null };
		}
		if (i==4625){
			entityDef.name = "Donator shop";
			entityDef.actions = new String[] { "Talk-to", null, "Trade", null, null };
		}
	
		if (i == 5314) {
			entityDef.name = "Mystical Wizard";
			entityDef.actions = new String[] { "Teleport", "Previous Location", null, null, null };
			entityDef.description = "This wizard has the power to teleport you to many locations.";
		}
	

		if(i==7144){
	entityDef.anInt75 = 0;
		}
		if(i==963){
			entityDef.anInt75 = 6;
		}
		if(i==7145){
	entityDef.anInt75 = 1;
		}
		if(i==7146){
	entityDef.anInt75 = 2;
		}
		
		
		switch (i) {//this turns into cases/breaks
		
		case 3257:
			entityDef.name = "Master Farmer";
			entityDef.actions = new String[] { "Talk-to", "Trade", "Pickpocket", null, null };
			break;
			
		case 822:
			//TODO
				entityDef.anInt58 = 1;
				entityDef.anInt83 = 1;
				entityDef.anInt55 = 1;
				entityDef.anInt57 = 1;
				entityDef.anInt59 = 1;
				//entityDef.anInt55 = 1;	
			break;
		
		
		}
		return entityDef;
	}

	public static int totalAmount;

	public static void unpackConfig(StreamLoader streamLoader) {
		stream = new Stream(streamLoader.getDataForName("npc.dat"));
		Stream stream = new Stream(streamLoader.getDataForName("npc.idx"));
		totalAmount = stream.readUnsignedWord();
		streamIndices = new int[totalAmount];
		int i = 2;
		for (int j = 0; j < totalAmount; j++) {
			streamIndices[j] = i;
			i += stream.readUnsignedWord();
		}

		cache = new NpcDefinition[20];
		for (int k = 0; k < 20; k++)
			cache[k] = new NpcDefinition();
		/*for (int index = 0; index < totalAmount; index++) {
			NpcDefinition ed = forID(index);
			if (ed == null)
				continue;
			if (ed.name == null)
				continue;
		}*/
	}

	
	private void readValues(Stream stream) {
		int last = -1;
		while (true) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				return;
			if (opcode == 1) {
				int j = stream.readUnsignedByte();
				models = new int[j];
				for (int j1 = 0; j1 < j; j1++)
					models[j1] = stream.readUnsignedWord();

			} else if (opcode == 2)
				name = stream.readString();
			else if (opcode == 3)
				description = stream.readString();
			else if (opcode == 12)
				boundDim = stream.readSignedByte();
			else if (opcode == 13)
				standAnim = stream.readUnsignedWord();
			else if (opcode == 14)
				walkAnim = stream.readUnsignedWord();
			else if(opcode == 15 || opcode == 16)
				stream.readUnsignedWord();
			else if (opcode == 17) {
				walkAnim = stream.readUnsignedWord();
				anInt58 = stream.readUnsignedWord();
				anInt83 = stream.readUnsignedWord();
				anInt55 = stream.readUnsignedWord();
				if (anInt58 == 65535) {
					anInt58 = -1;
				}
				if (anInt83 == 65535) {
					anInt83 = -1;
				}
				if (anInt55 == 65535) {
					anInt55 = -1;
				}
			} else if (opcode >= 30 && opcode < 40) {
				if (actions == null)
					actions = new String[10];
				actions[opcode - 30] = stream.readString();
				if (actions[opcode - 30].equalsIgnoreCase("hidden"))
					actions[opcode - 30] = null;
			} else if (opcode == 40) {
				int k = stream.readUnsignedByte();
				originalColors = new int[k];
				newColors = new int[k];
				for (int k1 = 0; k1 < k; k1++) {
					originalColors[k1] = stream.readUnsignedWord();
					newColors[k1] = stream.readUnsignedWord();
				}
			} else if (opcode == 41) {
				int k = stream.readUnsignedByte();
				for (int k1 = 0; k1 < k; k1++) {
					stream.readUnsignedWord();
					stream.readUnsignedWord();
				}

			} else if (opcode == 60) {
				int l = stream.readUnsignedByte();
				dialogueModels = new int[l];
				for (int l1 = 0; l1 < l; l1++)
					dialogueModels[l1] = stream.readUnsignedWord();

			} else if (opcode == 93)
				onMinimap = false;
			else if (opcode == 95)
				combatLevel = stream.readUnsignedWord();
			else if (opcode == 97)
				anInt91 = stream.readUnsignedWord();
			else if (opcode == 98)
				anInt86 = stream.readUnsignedWord();
			else if (opcode == 99)
				aBoolean93 = true;
			else if (opcode == 100)
				anInt85 = stream.readSignedByte();
			else if (opcode == 101)
				anInt92 = stream.readSignedByte();
			else if (opcode == 102)
				anInt75 = stream.readUnsignedWord();
			else if (opcode == 103)
				getDegreesToTurn = stream.readUnsignedWord();
			else if (opcode == 106 || opcode == 118) {
				anInt57 = stream.readUnsignedWord();
				if (anInt57 == 65535)
					anInt57 = -1;
				anInt59 = stream.readUnsignedWord();
				if (anInt59 == 65535)
					anInt59 = -1;
				
				int var3 = -1;
				if(opcode == 118) {
					var3 = stream.readUnsignedWord();
				}
				int i1 = stream.readUnsignedByte();
				childrenIDs = new int[i1 + 2];
				for (int i2 = 0; i2 <= i1; i2++) {
					childrenIDs[i2] = stream.readUnsignedWord();
					if (childrenIDs[i2] == 65535)
						childrenIDs[i2] = -1;
				}
				childrenIDs[i1 + 1] = var3;
				
			} else if (opcode == 107)
				aBoolean84 = false;
			else if(opcode == 111 || opcode == 107 || opcode == 109) {
				
			} else {
				System.out.println("Missing NPC opcode " + opcode + "last: " + last);
				continue;
			}
			last = opcode;
		}
	}

	public Model method160() {
		if (childrenIDs != null) {
			NpcDefinition entityDef = method161();
			if (entityDef == null)
				return null;
			else
				return entityDef.method160();
		}
		if (dialogueModels == null) {
			return null;
		}
		boolean flag1 = false;
		for (int i = 0; i < dialogueModels.length; i++)
			if (!Model.method463(dialogueModels[i]))
				flag1 = true;

		if (flag1)
			return null;
		Model aclass30_sub2_sub4_sub6s[] = new Model[dialogueModels.length];
		for (int j = 0; j < dialogueModels.length; j++)
			aclass30_sub2_sub4_sub6s[j] = Model.method462(dialogueModels[j]);

		Model model;
		if (aclass30_sub2_sub4_sub6s.length == 1)
			model = aclass30_sub2_sub4_sub6s[0];
		else
			model = new Model(aclass30_sub2_sub4_sub6s.length, aclass30_sub2_sub4_sub6s);

		if (originalColors != null)
			for (int k = 0; k < originalColors.length; k++)
				model.replaceColor(originalColors[k], newColors[k]);

		return model;
	}

	public NpcDefinition method161() {
		int j = -1;
		if (anInt57 != -1) {
			VarBit varBit = VarBit.cache[anInt57];
			int k = varBit.anInt648;
			int l = varBit.anInt649;
			int i1 = varBit.anInt650;
			int j1 = Client.anIntArray1232[i1 - l];
			j = clientInstance.variousSettings[k] >> l & j1;
		} else if (anInt59 != -1)
			j = clientInstance.variousSettings[anInt59];
		int var2;
		if(j >= 0 && j < childrenIDs.length)
			var2 = childrenIDs[j];
		else
			var2 = childrenIDs[childrenIDs.length - 1];
		
			return var2 == -1 ? null : forID(var2);
	}

	public Model method164(int j, int k, int ai[]) {
		if (childrenIDs != null) {
			NpcDefinition entityDef = method161();
			if (entityDef == null)
				return null;
			else
				return entityDef.method164(j, k, ai);
		}
		Model model = (Model) mruNodes.insertFromCache(interfaceType);
		if (model == null) {
			boolean flag = false;
			for (int i1 = 0; i1 < models.length; i1++)
				if (!Model.method463(models[i1]))
					flag = true;

			if (flag)
				return null;
			Model aclass30_sub2_sub4_sub6s[] = new Model[models.length];
			for (int j1 = 0; j1 < models.length; j1++)
				aclass30_sub2_sub4_sub6s[j1] = Model.method462(models[j1]);

			if (aclass30_sub2_sub4_sub6s.length == 1)
				model = aclass30_sub2_sub4_sub6s[0];
			else
				model = new Model(aclass30_sub2_sub4_sub6s.length, aclass30_sub2_sub4_sub6s);
			if (originalColors != null) {
				for (int k1 = 0; k1 < originalColors.length; k1++)
					model.replaceColor(originalColors[k1], newColors[k1]);

			}
			model.method469();
			model.method479(64 + anInt85, 850 + anInt92, -30, -50, -30, true);
			// model.method479(84 + anInt85, 1000 + anInt92, -90, -580, -90, true);
			mruNodes.removeFromCache(model, interfaceType);
		}
		Model model_1 = Model.EMPTY_MODEL;
		model_1.method464(model, Class36.method532(k) & Class36.method532(j));
		if (k != -1 && j != -1)
			model_1.method471(ai, j, k);
		else if (k != -1)
			model_1.method470(k);
		if (anInt91 != 128 || anInt86 != 128)
			model_1.method478(anInt91, anInt91, anInt86);
		model_1.method466();
		model_1.faceGroups = null;
		model_1.vertexGroups = null;
		if (boundDim == 1)
			model_1.aBoolean1659 = true;
		return model_1;
	}

	private NpcDefinition() {
		anInt55 = -1;
		anInt57 = walkAnim;
		anInt58 = walkAnim;
		anInt59 = walkAnim;
		combatLevel = -1;
		anInt64 = 1834;
		walkAnim = -1;
		boundDim = 1;
		anInt75 = -1;
		standAnim = -1;
		interfaceType = -1L;
		getDegreesToTurn = 32;
		anInt83 = -1;
		aBoolean84 = true;
		anInt86 = 128;
		onMinimap = true;
		anInt91 = 128;
		aBoolean93 = false;
	}

	public static void nullLoader() {
		mruNodes = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public static void dumpList() {
		try {
			File file = new File(System.getProperty("user.home") + "/Desktop/npcList 178" + Configuration.dumpID + ".txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (int i = 0; i < totalAmount; i++) {
					NpcDefinition definition = forID(i);
					if (definition != null) {
						writer.write("npc = " + i + "\t" + definition.name + "\t" + definition.combatLevel + "\t"
								+ definition.standAnim + "\t" + definition.walkAnim + "\t");
						writer.newLine();
					}
				}
			}

			System.out.println("Finished dumping npc definitions.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dumpSizes() {
		try {
			File file = new File(System.getProperty("user.home") + "/Desktop/npcSizes 143.txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (int i = 0; i < totalAmount; i++) {
					NpcDefinition definition = forID(i);
					if (definition != null) {
						writer.write(i + "	" + definition.boundDim);
						writer.newLine();
					}
				}
			}

			System.out.println("Finished dumping npc definitions.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int anInt55;
	public static int anInt56;
	public int anInt57;
	public int anInt58;
	public int anInt59;
	public static Stream stream;
	public int combatLevel;
	public final int anInt64;
	public String name;
	public String actions[];
	public int walkAnim;
	public byte boundDim;
	public int[] newColors;
	public static int[] streamIndices;
	public int[] dialogueModels;
	public int anInt75;
	public int[] originalColors;
	public int standAnim;
	public long interfaceType;
	public int getDegreesToTurn;
	public static NpcDefinition[] cache;
	public static Client clientInstance;
	public int anInt83;
	public boolean aBoolean84;
	public int anInt85;
	public int anInt86;
	public boolean onMinimap;
	public int childrenIDs[];
	public String description;
	public int anInt91;
	public int anInt92;
	public boolean aBoolean93;
	public int[] models;
	public static MRUNodes mruNodes = new MRUNodes(30);

}