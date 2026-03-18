// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 


import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.zip.CRC32;
import java.lang.reflect.Method;
import sign.signlink;
import javax.swing.*;

public class client extends RSApplet {
	

	public int MapX, MapY;
	public static int spellID = 0;
	public static boolean newDamage = false;
	public int followPlayer = 0;
public int followNPC = 0;
public int followDistance = 1;
	
	private static String intToKOrMilLongName(int i) {
		String s = String.valueOf(i);
        for(int k = s.length() - 3; k > 0; k -= 3)
            s = s.substring(0, k) + "," + s.substring(k);

        //if(j != 0)
           // aBoolean1224 = !aBoolean1224;
        if(s.length() > 8)
            s = "@gre@" + s.substring(0, s.length() - 8) + " million @whi@(" + s + ")";
        else
        if(s.length() > 4)
            s = "@cya@" + s.substring(0, s.length() - 4) + "K @whi@(" + s + ")";
        return " " + s;
	}
	 public final String methodR(/*int i,*/ int j)
    {
        //if(i <= 0)
           // pktType = inStream.readUnsignedByte();
        if(j >= 0 && j < 10000)
            return String.valueOf(j);
        if(j >= 10000 && j < 10000000)
            return j / 1000 + "K";
        if(j >= 10000000 && j  < 999999999)
            return j / 1000000 + "M";
        if(j >= 999999999)
            return "*";
		else
		return "?";
    }
	
	public static final byte[] ReadFile(String s) {
		try {
			byte abyte0[];
			File file = new File(s);
			int i = (int)file.length();
			abyte0 = new byte[i];
			DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(s)));
			datainputstream.readFully(abyte0, 0, i);
			datainputstream.close();
			return abyte0;
		} catch(Exception e) {
			System.out.println((new StringBuilder()).append("Read Error: ").append(s).toString());
			return null;
		}
	}

	public void models() {
		for(int ModelIndex = 0; ModelIndex < 29191; ModelIndex++) {
			byte[] abyte0 = getModel(ModelIndex);
			if(abyte0 != null && abyte0.length > 0) {
				decompressors[1].readCacheData(abyte0.length, abyte0, ModelIndex);
				pushMessage("Model added successfully!", 0, "");
			}
		}
	}
	public byte[] getModel(int Index) {
		try {
			File Model = new File(signlink.findcachedir()+"model/"+Index+".gz");
			byte[] aByte = new byte[(int)Model.length()];
			FileInputStream fis = new FileInputStream(Model);
			fis.read(aByte);
			pushMessage("aByte = ["+aByte+"]!", 0, "");
			fis.close();
			return aByte;
		}
		catch(Exception e)
		{return null;}
	}
	
	private void stopMidi() {
		signlink.midifade = 0;
		signlink.midi = "stop";
	}
	
	private boolean menuHasAddFriend(int j) {
		if(j < 0)
			return false;
		int k = menuActionID[j];
		if(k >= 2000)
			k -= 2000;
		return k == 337;
	}
	
	public void drawChannelButtons() {
		String text[] = { "On", "Friends", "Off", "Hide" };
		int textColor[] = { 65280, 0xffff00, 0xff0000, 65535 };
		switch(cButtonCPos) {
			case 0:
				chatButtons[1].drawSprite(5, 142);
				break;
			case 1:
				chatButtons[1].drawSprite(71, 142);
				break;
			case 2:
				chatButtons[1].drawSprite(137, 142);
				break;
			case 3:
				chatButtons[1].drawSprite(203, 142);
				break;
			case 4:
				chatButtons[1].drawSprite(269, 142);
				break;
			case 5:
				chatButtons[1].drawSprite(335, 142);
				break;
		}
		if(cButtonHPos == cButtonCPos) {
			switch(cButtonHPos) {
				case 0:
					chatButtons[2].drawSprite(5, 142);
					break;
				case 1:
					chatButtons[2].drawSprite(71, 142);
					break;
				case 2:
					chatButtons[2].drawSprite(137, 142);
					break;
				case 3:
					chatButtons[2].drawSprite(203, 142);
					break;
				case 4:
					chatButtons[2].drawSprite(269, 142);
					break;
				case 5:
					chatButtons[2].drawSprite(335, 142);
					break;
				case 6:
					chatButtons[3].drawSprite(404, 142);
					break;
			}
		} else {
			switch(cButtonHPos) {
				case 0:
					chatButtons[0].drawSprite(5, 142);
					break;
				case 1:
					chatButtons[0].drawSprite(71, 142);
					break;
				case 2:
					chatButtons[0].drawSprite(137, 142);
					break;
				case 3:
					chatButtons[0].drawSprite(203, 142);
					break;
				case 4:
					chatButtons[0].drawSprite(269, 142);
					break;
				case 5:
					chatButtons[0].drawSprite(335, 142);
					break;
				case 6:
					chatButtons[3].drawSprite(404, 142);
					break;
			}
		}
		smallText.drawWaving(true, 425, 0xffffff, "Report Abuse", 157);
		smallText.drawWaving(true, 26, 0xffffff, "All", 157);
		smallText.drawWaving(true, 86, 0xffffff, "Game", 157);
		smallText.drawWaving(true, 150, 0xffffff, "Public", 152);
		smallText.drawWaving(true, 212, 0xffffff, "Private", 152);
		smallText.drawWaving(true, 286, 0xffffff, "Clan", 152);
		smallText.drawWaving(true, 349, 0xffffff, "Trade", 152);
		smallText.drawRightAligned(textColor[publicChatMode], 164, text[publicChatMode], 163, true);
		smallText.drawRightAligned(textColor[privateChatMode], 230, text[privateChatMode], 163, true);
		smallText.drawRightAligned(textColor[clanChatMode], 296, text[clanChatMode], 163, true);
		smallText.drawRightAligned(textColor[tradeMode], 362, text[tradeMode], 163, true);
	}

	private void drawChatArea() {
		topCenterIP.initDrawingArea();
		Texture.scanlineOffset = mapChunkX2;
		chatArea.drawSprite(0, 0);
		drawChannelButtons();
		TextDrawingArea textDrawingArea = boldFont;
		if(messagePromptRaised) {
			chatTextDrawingArea.drawText(0, inputTitle, 60, 259);
			chatTextDrawingArea.drawText(128, promptInput + "*", 80, 259);
		} else if(inputDialogState == 1) {
			chatTextDrawingArea.drawText(0, "Enter amount:", 60, 259);
			chatTextDrawingArea.drawText(128, amountOrNameInput + "*", 80, 259);
		} else if(inputDialogState == 2) {
			chatTextDrawingArea.drawText(0, "Enter name:", 60, 259);
			chatTextDrawingArea.drawText(128, amountOrNameInput + "*", 80, 259);
		} else if(clickToContinueString != null) {
			chatTextDrawingArea.drawText(0, clickToContinueString, 60, 259);
			chatTextDrawingArea.drawText(128, "Click to continue", 80, 259);
		} else if(backDialogID != -1) {
			drawInterface(0, 20, RSInterface.interfaceCache[backDialogID], 20);
		} else if(dialogID != -1) {
			drawInterface(0, 20, RSInterface.interfaceCache[dialogID], 20);
		} else {
			int j77 = -3;
			int j = 0;
			DrawingArea.setDrawingArea(122, 8, 497, 7);
			for(int k = 0; k < 500; k++)
			if(chatMessages[k] != null) {
				int chatType = chatTypes[k];
				int yPos = (70 - j77 * 14) + chatScrollAmount + 5;
				String s1 = chatNames[k];
				byte byte0 = 0;
				if(s1 != null && s1.startsWith("@cr1@")) {
					s1 = s1.substring(5);
					byte0 = 1;
				} else if(s1 != null && s1.startsWith("@cr2@")) {
					s1 = s1.substring(5);
					byte0 = 2;
				} else if(s1 != null && s1.startsWith("@cr3@")) {
					s1 = s1.substring(5);
					byte0 = 3;
				}
				if(chatType == 0) {
					if (chatTypeView == 5 || chatTypeView == 0) {
					if(yPos > 0 && yPos < 210)
						textDrawingArea.drawWaving(false, 11, 0, chatMessages[k], yPos);//chat color enabled
					j++;
					j77++;
					}
				}
				if((chatType == 1 || chatType == 2) && (chatType == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s1))) {
					if (chatTypeView == 1 || chatTypeView == 0) {
						if(yPos > 0 && yPos < 210) {
							int xPos = 11;
							if(byte0 == 1) {
								modIcons[0].drawBackground(xPos + 1, yPos - 12);
								xPos += 14;
							} else if(byte0 == 2) {
								modIcons[1].drawBackground(xPos + 1, yPos - 12);
								xPos += 14;
							} else if(byte0 == 3) {
								modIcons[2].drawBackground(xPos + 1, yPos - 12);
								xPos += 14;
							}
							textDrawingArea.drawText(0, s1 + ":", yPos, xPos);
							xPos += textDrawingArea.getTextWidth(s1) + 8;
							textDrawingArea.drawWaving(false, xPos, 255, chatMessages[k], yPos);
						}
						j++;
						j77++;
					}
				}
				if((chatType == 3 || chatType == 7) && (splitPrivateChat == 0 || chatTypeView == 2) && (chatType == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s1))) {
					if (chatTypeView == 2 || chatTypeView == 0) {
						if(yPos > 0 && yPos < 210) {
							int k1 = 11;
							textDrawingArea.drawText(0, "From", yPos, k1);
							k1 += textDrawingArea.getTextWidth("From ");
							if(byte0 == 1) {
								modIcons[0].drawBackground(k1, yPos - 12);
								k1 += 12;
							} else if(byte0 == 2) {
								modIcons[1].drawBackground(k1, yPos - 12);
								k1 += 12;
							} else if(byte0 == 3) {
								modIcons[2].drawBackground(k1, yPos - 12);
								k1 += 12;
							}
							textDrawingArea.drawText(0, s1 + ":", yPos, k1);
							k1 += textDrawingArea.getTextWidth(s1) + 8;
							textDrawingArea.drawText(0x800000, chatMessages[k], yPos, k1);
						}
						j++;
						j77++;
					}
				}
				if(chatType == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
					if (chatTypeView == 3 || chatTypeView == 0) {
						if(yPos > 0 && yPos < 210)
							textDrawingArea.drawText(0x800080, s1 + " " + chatMessages[k], yPos, 11);
						j++;
						j77++;
					}
				}
				if(chatType == 5 && splitPrivateChat == 0 && privateChatMode < 2) {
					if (chatTypeView == 2 || chatTypeView == 0) {
						if(yPos > 0 && yPos < 210)
							textDrawingArea.drawText(0x800000, chatMessages[k], yPos, 11);
						j++;
						j77++;
					}
				}
				if(chatType == 6 && (splitPrivateChat == 0 || chatTypeView == 2) && privateChatMode < 2) {
					if (chatTypeView == 2 || chatTypeView == 0) {
						if(yPos > 0 && yPos < 210) {
							textDrawingArea.drawText(0, "To " + s1 + ":", yPos, 11);
							textDrawingArea.drawText(0x800000, chatMessages[k], yPos, 15 + textDrawingArea.getTextWidth("To :" + s1));
						}
					j++;
					j77++;
					}
				}
				if(chatType == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
					if (chatTypeView == 3 || chatTypeView == 0) {
						if(yPos > 0 && yPos < 210)
							textDrawingArea.drawText(0x7e3200, s1 + " " + chatMessages[k], yPos, 11);
						j++;
						j77++;
					}
					if(chatType == 11 && (clanChatMode == 0)) {
						if (chatTypeView == 11) {
						if(yPos > 0 && yPos < 210)
							textDrawingArea.drawText(0x7e3200, s1 + " " + chatMessages[k], yPos, 11);
						j++;
						j77++;
					}
					if(chatType == 12) {
						if(yPos > 0 && yPos < 110)
							textDrawingArea.drawText(0x7e3200, chatMessages[k] + " @blu@" + s1, yPos, 11);
							j++;
						}
					}
				}
				if(chatType == 16) {
					int j2 = 40;
					int clanNameWidth = textDrawingArea.getTextWidth(clanname);
					if(chatTypeView == 11 || chatTypeView == 0) {
						if(yPos > 3 && yPos < 130)
							switch(chatRights[k]) {
								case 1:
									j2 += clanNameWidth;
									modIcons[0].drawBackground(j2 - 18, yPos - 12);
									j2 += 14;
									break;
									
								case 2:
									j2 += clanNameWidth;
									modIcons[1].drawBackground(j2 - 18, yPos - 12);
									j2 += 14;
									break;
									
								case 3:
									j2 += clanNameWidth;
									modIcons[1].drawBackground(j2 - 18, yPos - 12);
									j2 += 14;
									break;
				
								default:
									j2 += clanNameWidth;
									break;
							}
							textDrawingArea.drawText(0, "[", yPos, 8);
							textDrawingArea.drawText(255, ""+clanname+"", yPos, 14);
							textDrawingArea.drawText(0, "]", yPos, clanNameWidth + 14);
							
							textDrawingArea.drawText(0, chatNames[k]+":", yPos, j2 - 17); //j2
							j2 += textDrawingArea.getTextWidth(chatNames[k]) + 7;
							textDrawingArea.drawText(0x800000, chatMessages[k], yPos, j2 - 16);//j2
							j++;
							j77++;
						}
					}
			}
			DrawingArea.defaultDrawingAreaSize();
			chatFilterScrollMax = j * 14 + 7 + 5;
			if(chatFilterScrollMax < 111)
				chatFilterScrollMax = 111;
			drawScrollbar(114, chatFilterScrollMax - chatScrollAmount - 113, 7, 496, chatFilterScrollMax);
			String s;
			if(myPlayer != null && myPlayer.name != null)
				s = myPlayer.name;
			else
				s = TextClass.fixName(myUsername);
			textDrawingArea.drawText(0, s + ":", 133, 11);
			textDrawingArea.drawChatInput(255, 12 + textDrawingArea.getTextWidth(s + ": "), inputString + "*", 133, false);
			DrawingArea.drawVerticalLine(121, 0x807660, 506, 7);
		}
		if(menuOpen && menuScreenArea == 2) {
			drawMenu();
		}
		topCenterIP.drawGraphics(clientSize == 0 ? 338 : clientHeight - 165, super.graphics, 0);
		loginMsgIP.initDrawingArea();
		Texture.scanlineOffset = mapChunkLandscapeIds;
	}

	public void init() {
		nodeID = 10;
		portOff = 0;
		setHighMem();
		isMembers = true;
		initClientFrame(503, 765);
	}

	public void startRunnable(Runnable runnable, int i) {
		if(i > 10)
			i = 10;
		if(signlink.mainapp != null) {
			signlink.startthread(runnable, i);
		} else {
			new Thread(runnable).start();
		}
	}

	public Socket openSocket(int port) throws IOException {
			return new Socket(InetAddress.getByName(server), port);
	}

	private void processMenuClick() {
		if(activeInterfaceType != 0)
			return;
		int j = super.clickMode3;
		if(spellSelected == 1 && super.saveClickX >= 516 && super.saveClickY >= 160 && super.saveClickX <= 765 && super.saveClickY <= 205)
			j = 0;
		if(menuOpen) {
			if(j != 1) {
				int k = super.mouseX;
				int j1 = super.mouseY;
				if(menuScreenArea == 0) {
					k -= 4;
					j1 -= 4;
				}
				if(menuScreenArea == 1) {
					k -= 519;
					j1 -= 168;
				}
				if(menuScreenArea == 2) {
					k -= 17;
					j1 -= 338;
				}
				if(menuScreenArea == 3) {
					k -= 519;
					j1 -= 0;
				}
				if(k < menuOffsetX - 10 || k > menuOffsetX + menuWidth + 10 || j1 < menuOffsetY - 10 || j1 > menuOffsetY + menuHeight + 10) {
					menuOpen = false;
					if(menuScreenArea == 1)
						needDrawTabArea = true;
					if(menuScreenArea == 2)
						inputTaken = true;
				}
			}
			if(j == 1) {
				int l = menuOffsetX;
				int k1 = menuOffsetY;
				int i2 = menuWidth;
				int k2 = super.saveClickX;
				int l2 = super.saveClickY;
				if(menuScreenArea == 0) {
					k2 -= 4;
					l2 -= 4;
				}
				if(menuScreenArea == 1) {
					k2 -= 519;
					l2 -= 168;
				}
				if(menuScreenArea == 2) {
					k2 -= 17;
					l2 -= 338;
				}
				if(menuScreenArea == 3) {
					k2 -= 519;
					l2 -= 0;
				}
				int i3 = -1;
				for(int j3 = 0; j3 < menuActionRow; j3++) {
					int k3 = k1 + 31 + (menuActionRow - 1 - j3) * 15;
					if(k2 > l && k2 < l + i2 && l2 > k3 - 13 && l2 < k3 + 3)
						i3 = j3;
				}
				System.out.println(i3);
				if(i3 != -1)
					doAction(i3);
				menuOpen = false;
				if(menuScreenArea == 1)
					needDrawTabArea = true;
				if(menuScreenArea == 2) {
					inputTaken = true;
				}
			}
		} else {
			if(j == 1 && menuActionRow > 0) {
				int i1 = menuActionID[menuActionRow - 1];
				if(i1 == 632 || i1 == 78 || i1 == 867 || i1 == 431 || i1 == 53 || i1 == 74 || i1 == 454 || i1 == 539 || i1 == 493 || i1 == 847 || i1 == 447 || i1 == 1125) {
					int l1 = menuActionCmd2[menuActionRow - 1];
					int j2 = menuActionCmd3[menuActionRow - 1];
					RSInterface class9 = RSInterface.interfaceCache[j2];
					if(class9.replaceItems || class9.filled) {
						aBoolean1242 = false;
						moveItemInterfaceId = 0;
						dragFromSlotInterface = j2;
						dragFromSlot = l1;
						activeInterfaceType = 2;
						dragStartX = super.saveClickX;
						dragStartY = super.saveClickY;
						if(RSInterface.interfaceCache[j2].parentID == openInterfaceID)
							activeInterfaceType = 1;
						if(RSInterface.interfaceCache[j2].parentID == backDialogID)
							activeInterfaceType = 3;
						return;
					}
				}
			}
			if(j == 1 && (clickMode == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
				j = 2;
			if(j == 1 && menuActionRow > 0)
				doAction(menuActionRow - 1);
			if(j == 2 && menuActionRow > 0)
				determineMenuSize();
		}
	}


	public static int totalRead = 0;

	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2 ) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return "";
	}

	public void preloadModels() {
		File file = new File(signlink.findcachedir()+"Raw/");
		File[] fileArray = file.listFiles();
		for(int y = 0; y < fileArray.length; y++) {
			String s = fileArray[y].getName();
			byte[] buffer = ReadFile(signlink.findcachedir()+"Raw/"+s);
			Model.decodeModelHeader(buffer,Integer.parseInt(getFileNameWithoutExtension(s)));
		}
	}



	private void saveMidi(boolean flag, byte abyte0[])
	{
		signlink.midifade = flag ? 1 : 0;
		signlink.midisave(abyte0, abyte0.length);
	}

	private void resetScene()
	{
		try
		{
			activeInterfaceId = -1;
			spotAnimList.removeAll();
			projectileList.removeAll();
			Texture.clearDepthBuffer();
			unlinkMRUNodes();
			worldController.initToNull();
			System.gc();
			for(int i = 0; i < 4; i++)
				aCollisionMapArray1230[i].reset();

			for(int l = 0; l < 4; l++)
			{
				for(int k1 = 0; k1 < 104; k1++)
				{
					for(int j2 = 0; j2 < 104; j2++)
						byteGroundArray[l][k1][j2] = 0;

				}

			}

			ObjectManager objectManager = new ObjectManager(byteGroundArray, intGroundArray);
			int k2 = mapLandscapeData.length;
			stream.createFrame(0);
			if(!aBoolean1159)
			{
				for(int i3 = 0; i3 < k2; i3++)
				{
					int i4 = (chatFilterTypes[i3] >> 8) * 64 - baseX;
					int k5 = (chatFilterTypes[i3] & 0xff) * 64 - baseY;
					byte abyte0[] = mapLandscapeData[i3];
					if (FileOperations.FileExists(signlink.findcachedir()+"maps/"+chatFilterNames[i3]+".dat")) 
						abyte0 = FileOperations.ReadFile(signlink.findcachedir()+"maps/"+chatFilterNames[i3]+".dat");
					if(abyte0 != null)
						objectManager.parseLandscape(abyte0, k5, i4, (mapRegionX - 6) * 8, (mapRegionY - 6) * 8, aCollisionMapArray1230);
				}

				for(int j4 = 0; j4 < k2; j4++)
				{
					int l5 = (chatFilterTypes[j4] >> 8) * 64 - baseX;
					int k7 = (chatFilterTypes[j4] & 0xff) * 64 - baseY;
					byte abyte2[] = mapLandscapeData[j4];
					if(abyte2 == null && mapRegionY < 800)
						objectManager.flattenTerrain(k7, 64, 64, l5);
				}

				anInt1097_counter++;
				if(anInt1097_counter > 160)
				{
					anInt1097_counter = 0;
					stream.createFrame(238);
					stream.writeWordBigEndian(96);
				}
				stream.createFrame(0);
				for(int i6 = 0; i6 < k2; i6++)
				{
					byte abyte1[] = mapObjectData[i6];
					if(abyte1 != null)
					{
						int l8 = (chatFilterTypes[i6] >> 8) * 64 - baseX;
						int k9 = (chatFilterTypes[i6] & 0xff) * 64 - baseY;
						objectManager.parseLocalObjectLocations(l8, aCollisionMapArray1230, k9, worldController, abyte1);
					}
				}

			}
			if(aBoolean1159)
			{
				for(int j3 = 0; j3 < 4; j3++)
				{
					for(int k4 = 0; k4 < 13; k4++)
					{
						for(int j6 = 0; j6 < 13; j6++)
						{
							int l7 = tileFlags[j3][k4][j6];
							if(l7 != -1)
							{
								int i9 = l7 >> 24 & 3;
								int l9 = l7 >> 1 & 3;
								int j10 = l7 >> 14 & 0x3ff;
								int l10 = l7 >> 3 & 0x7ff;
								int j11 = (j10 / 8 << 8) + l10 / 8;
								for(int l11 = 0; l11 < chatFilterTypes.length; l11++)
								{
									if(chatFilterTypes[l11] != j11 || mapLandscapeData[l11] == null)
										continue;
									objectManager.parseInstancedLandscape(i9, l9, aCollisionMapArray1230, k4 * 8, (j10 & 7) * 8, mapLandscapeData[l11], (l10 & 7) * 8, j3, j6 * 8);
									break;
								}

							}
						}

					}

				}

				for(int l4 = 0; l4 < 13; l4++)
				{
					for(int k6 = 0; k6 < 13; k6++)
					{
						int i8 = tileFlags[0][l4][k6];
						if(i8 == -1)
							objectManager.flattenTerrain(k6 * 8, 8, 8, l4 * 8);
					}

				}

				stream.createFrame(0);
				for(int l6 = 0; l6 < 4; l6++)
				{
					for(int j8 = 0; j8 < 13; j8++)
					{
						for(int j9 = 0; j9 < 13; j9++)
						{
							int i10 = tileFlags[l6][j8][j9];
							if(i10 != -1)
							{
								int k10 = i10 >> 24 & 3;
								int i11 = i10 >> 1 & 3;
								int k11 = i10 >> 14 & 0x3ff;
								int i12 = i10 >> 3 & 0x7ff;
								int j12 = (k11 / 8 << 8) + i12 / 8;
								for(int k12 = 0; k12 < chatFilterTypes.length; k12++)
								{
									if(chatFilterTypes[k12] != j12 || mapObjectData[k12] == null)
										continue;
									//objectManager.parseObjectLocations(aCollisionMapArray1230, worldController, k10, j8 * 8, (i12 & 7) * 8, l6, mapObjectData[k12], (k11 & 7) * 8, i11, j9 * 8);
									byte abyte0[] = mapObjectData[k12];
                                    if (FileOperations.FileExists(signlink.findcachedir()+"maps/"+chatFilterNames[k12]+".dat")) 
										abyte0 = FileOperations.ReadFile(signlink.findcachedir()+"maps/"+chatFilterNames[k12]+".dat");
                                    objectManager.parseObjectLocations(aCollisionMapArray1230, worldController, k10, j8 * 8, (i12 & 7) * 8, l6, mapObjectData[k12], (k11 & 7) * 8, i11, j9 * 8);
									break;
								}

							}
						}

					}

				}

			}
			stream.createFrame(0);
			objectManager.applyTerrainCollision(aCollisionMapArray1230, worldController);
			loginMsgIP.initDrawingArea();
			stream.createFrame(0);
			int k3 = ObjectManager.minimumPlane;
			if(k3 > plane)
				k3 = plane;
			if(k3 < plane - 1)
				k3 = plane - 1;
			if(lowMem)
				worldController.setCurrentPlane(ObjectManager.minimumPlane);
			else
				worldController.setCurrentPlane(0);
			for(int i5 = 0; i5 < 104; i5++)
			{
				for(int i7 = 0; i7 < 104; i7++)
					spawnGroundItem(i5, i7);

			}

			anInt1051_counter++;
			if(anInt1051_counter > 98)
			{
				anInt1051_counter = 0;
				stream.createFrame(150);
			}
			resetSpawnObjects();
		}
		catch(Exception exception) { }
		ObjectDef.mruNodes1.unlinkAll();
		if(super.gameFrame != null)
		{
			stream.createFrame(210);
			stream.writeDWord(0x3f008edd);
		}
		if(lowMem && signlink.cache_dat != null)
		{
			int j = onDemandFetcher.getVersionCount(0);
			for(int i1 = 0; i1 < j; i1++)
			{
				int l1 = onDemandFetcher.getModelIndex(i1);
				if((l1 & 0x79) == 0)
					Model.clearModel(i1);
			}

		}
		System.gc();
		Texture.initTextureCache();
		onDemandFetcher.clearQueue();
		int k = (mapRegionX - 6) / 8 - 1;
		int j1 = (mapRegionX + 6) / 8 + 1;
		int i2 = (mapRegionY - 6) / 8 - 1;
		int l2 = (mapRegionY + 6) / 8 + 1;
		if(aBoolean1141)
		{
			k = 49;
			j1 = 50;
			i2 = 49;
			l2 = 50;
		}
		for(int l3 = k; l3 <= j1; l3++)
		{
			for(int j5 = i2; j5 <= l2; j5++)
				if(l3 == k || l3 == j1 || j5 == i2 || j5 == l2)
				{
					int j7 = onDemandFetcher.getMapFile(0, j5, l3);
					if(j7 != -1)
						onDemandFetcher.prefetchFile(j7, 3);
					int k8 = onDemandFetcher.getMapFile(1, j5, l3);
					if(k8 != -1)
						onDemandFetcher.prefetchFile(k8, 3);
				}

		}

	}

	private void unlinkMRUNodes()
	{
		ObjectDef.mruNodes1.unlinkAll();
		ObjectDef.mruNodes2.unlinkAll();
		EntityDef.mruNodes.unlinkAll();
		ItemDef.mruNodes2.unlinkAll();
		ItemDef.mruNodes1.unlinkAll();
		Player.mruNodes.unlinkAll();
		SpotAnim.modelCache.unlinkAll();
	}

	private void drawMiniMapDots(int i)
	{
		int ai[] = minimapSprite.myPixels;
		int j = ai.length;
		for(int k = 0; k < j; k++)
			ai[k] = 0;

		for(int l = 1; l < 103; l++)
		{
			int i1 = 24628 + (103 - l) * 512 * 4;
			for(int k1 = 1; k1 < 103; k1++)
			{
				if((byteGroundArray[i][k1][l] & 0x18) == 0)
					worldController.drawMinimap(ai, i1, i, k1, l);
				if(i < 3 && (byteGroundArray[i + 1][k1][l] & 8) != 0)
					worldController.drawMinimap(ai, i1, i + 1, k1, l);
				i1 += 4;
			}

		}

		int j1 = ((238 + (int)(Math.random() * 20D)) - 10 << 16) + ((238 + (int)(Math.random() * 20D)) - 10 << 8) + ((238 + (int)(Math.random() * 20D)) - 10);
		int l1 = (238 + (int)(Math.random() * 20D)) - 10 << 16;
		minimapSprite.drawInverse();
		for(int i2 = 1; i2 < 103; i2++)
		{
			for(int j2 = 1; j2 < 103; j2++)
			{
				if((byteGroundArray[i][j2][i2] & 0x18) == 0)
					drawMinimapWallObject(i2, j1, j2, l1, i);
				if(i < 3 && (byteGroundArray[i + 1][j2][i2] & 8) != 0)
					drawMinimapWallObject(i2, j1, j2, l1, i + 1);
			}

		}

		loginMsgIP.initDrawingArea();
		mapFunctionCount = 0;
		for(int k2 = 0; k2 < 104; k2++)
		{
			for(int l2 = 0; l2 < 104; l2++)
			{
				int i3 = worldController.getGroundDecorationUID(plane, k2, l2);
				if(i3 != 0)
				{
					i3 = i3 >> 14 & 0x7fff;
					int j3 = ObjectDef.forID(i3).minimapFunction;
					if(j3 >= 0)
					{
						int k3 = k2;
						int l3 = l2;
						if(j3 != 22 && j3 != 29 && j3 != 34 && j3 != 36 && j3 != 46 && j3 != 47 && j3 != 48)
						{
							byte byte0 = 104;
							byte byte1 = 104;
							int ai1[][] = aCollisionMapArray1230[plane].flags;
							for(int i4 = 0; i4 < 10; i4++)
							{
								int j4 = (int)(Math.random() * 4D);
								if(j4 == 0 && k3 > 0 && k3 > k2 - 3 && (ai1[k3 - 1][l3] & 0x1280108) == 0)
									k3--;
								if(j4 == 1 && k3 < byte0 - 1 && k3 < k2 + 3 && (ai1[k3 + 1][l3] & 0x1280180) == 0)
									k3++;
								if(j4 == 2 && l3 > 0 && l3 > l2 - 3 && (ai1[k3][l3 - 1] & 0x1280102) == 0)
									l3--;
								if(j4 == 3 && l3 < byte1 - 1 && l3 < l2 + 3 && (ai1[k3][l3 + 1] & 0x1280120) == 0)
									l3++;
							}

						}
						minimapImages[mapFunctionCount] = mapFunctions[j3];
						mapFunctionX[mapFunctionCount] = k3;
						mapFunctionY[mapFunctionCount] = l3;
						mapFunctionCount++;
					}
				}
			}

		}

	}

	private void spawnGroundItem(int i, int j)
	{
		NodeList class19 = groundArray[plane][i][j];
		if(class19 == null)
		{
			worldController.removeGroundItemPile(plane, i, j);
			return;
		}
		int k = 0xfa0a1f01;
		Object obj = null;
		for(Item item = (Item)class19.reverseGetFirst(); item != null; item = (Item)class19.reverseGetNext())
		{
			ItemDef itemDef = ItemDef.forID(item.ID);
			int l = itemDef.value;
			if(itemDef.stackable)
				l *= item.itemQuantity + 1;
//	notifyItemSpawn(item, i + baseX, j + baseY);
	
			if(l > k)
			{
				k = l;
				obj = item;
			}
		}

		class19.insertTail(((Node) (obj)));
		Object obj1 = null;
		Object obj2 = null;
		for(Item class30_sub2_sub4_sub2_1 = (Item)class19.reverseGetFirst(); class30_sub2_sub4_sub2_1 != null; class30_sub2_sub4_sub2_1 = (Item)class19.reverseGetNext())
		{
			if(class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && obj1 == null)
				obj1 = class30_sub2_sub4_sub2_1;
			if(class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && class30_sub2_sub4_sub2_1.ID != ((Item) (obj1)).ID && obj2 == null)
				obj2 = class30_sub2_sub4_sub2_1;
		}

		int i1 = i + (j << 7) + 0x60000000;
		worldController.addGroundItemPile(i, i1, ((Animable) (obj1)), getTileHeight(plane, j * 128 + 64, i * 128 + 64), ((Animable) (obj2)), ((Animable) (obj)), plane, j);
	}

	private void renderNPCsOnScene(boolean flag)
	{
		for(int j = 0; j < npcCount; j++)
		{
			NPC npc = npcArray[npcIndices[j]];
			int k = 0x20000000 + (npcIndices[j] << 14);
			if(npc == null || !npc.isVisible() || npc.desc.priorityRender != flag)
				continue;
			int l = npc.x >> 7;
			int i1 = npc.y >> 7;
			if(l < 0 || l >= 104 || i1 < 0 || i1 >= 104)
				continue;
			if(npc.tileSize == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64)
			{
				if(constructRegionData[l][i1] == hintIconY)
					continue;
				constructRegionData[l][i1] = hintIconY;
			}
			if(!npc.desc.clickable)
				k += 0x80000000;
			worldController.addTempObject(plane, npc.faceAngle, getTileHeight(plane, npc.y, npc.x), k, npc.y, (npc.tileSize - 1) * 64 + 60, npc.x, npc, npc.animStretches);
		}
	}

	private boolean replayWave()
	{
			return signlink.wavereplay();
	}

	private void loadError()
	{
		String s = "ondemand";//was a constant parameter
		System.out.println(s);
		try
		{
			System.out.println("Load error: " + s);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
		do
			try
			{
				Thread.sleep(1000L);
			}
			catch(Exception _ex) { }
		while(true);
	}
	
	public void drawHoverBox(int xPos, int yPos, String text) {
		String[] results = text.split("\n");
		int height = (results.length * 16) + 6;
		int width;
		width = smallText.getTextWidth(results[0]) + 6;
		for(int i = 1; i < results.length; i++)
			if(width <= smallText.getTextWidth(results[i]) + 6)
				width = smallText.getTextWidth(results[i]) + 6;
		DrawingArea.drawPixels(height, yPos, xPos, 0xFFFFA0, width);
		DrawingArea.fillPixels(xPos, width, height, 0, yPos);
		yPos += 14;
		for(int i = 0; i < results.length; i++) {
			smallText.drawWaving(false, xPos + 3, 0, results[i], yPos);
			yPos += 16;
		}
	}
	
	private void buildInterfaceMenu(int i, RSInterface class9, int k, int l, int i1, int j1)
	{
		if(class9.type != 0 || class9.children == null || class9.isMouseoverTriggered)
			return;
		if(k < i || i1 < l || k > i + class9.width || i1 > l + class9.height)
			return;
		int k1 = class9.children.length;
		for(int l1 = 0; l1 < k1; l1++)
		{
			int i2 = class9.childX[l1] + i;
			int j2 = (class9.childY[l1] + l) - j1;
			if(class9.children[l1] < 0 || class9.children[l1] >= RSInterface.interfaceCache.length) continue;
			RSInterface class9_1 = RSInterface.interfaceCache[class9.children[l1]];
			if(class9_1 == null) continue;
			i2 += class9_1.invSpritePadX;
			j2 += class9_1.invSpritePadY;
			if((class9_1.mOverInterToTrigger >= 0 || class9_1.enabledColor != 0) && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
				if(class9_1.mOverInterToTrigger >= 0)
					lastItemSelectedSlot = class9_1.mOverInterToTrigger;
				else
					lastItemSelectedSlot = class9_1.id;
			if (class9_1.type == 8 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
                anInt1315 = class9_1.id;
            }
			if(class9_1.type == 0)
			{
				buildInterfaceMenu(i2, class9_1, k, j2, i1, class9_1.scrollPosition);
				if(class9_1.scrollMax > class9_1.height)
					processScrollbar(i2 + class9_1.width, class9_1.height, k, i1, class9_1, j2, true, class9_1.scrollMax);
			} else
			{
				if(class9_1.atActionType == 1 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
				{
					boolean flag = false;
					if(class9_1.contentType != 0)
						flag = buildFriendsListMenu(class9_1);
					if(!flag)
					{
						//System.out.println("1"+class9_1.tooltip + ", " + class9_1.interfaceID);
						menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
						menuActionID[menuActionRow] = 315;
						menuActionCmd3[menuActionRow] = class9_1.id;
						menuActionRow++;
					}
				}
				if(class9_1.atActionType == 2 && spellSelected == 0 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
				{
					String s = class9_1.selectedActionName;
					if(s.indexOf(" ") != -1)
						s = s.substring(0, s.indexOf(" "));
					menuActionName[menuActionRow] = s + " @gre@" + class9_1.spellName;
					menuActionID[menuActionRow] = 626;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if(class9_1.atActionType == 3 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
				{
					menuActionName[menuActionRow] = "Close";
					menuActionID[menuActionRow] = 200;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if(class9_1.atActionType == 4 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
				{
					//System.out.println("2"+class9_1.tooltip + ", " + class9_1.interfaceID);
					menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
					menuActionID[menuActionRow] = 169;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
					if (class9_1.hoverText != null) {
						//drawHoverBox(k, l, class9_1.hoverText);
						//System.out.println("DRAWING INTERFACE: " + class9_1.hoverText);
					}
				}
				if(class9_1.atActionType == 5 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
				{
					//System.out.println("3"+class9_1.tooltip + ", " + class9_1.interfaceID);
					menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
					menuActionID[menuActionRow] = 646;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if(class9_1.atActionType == 6 && !aBoolean1149 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
				{
					//System.out.println("4"+class9_1.tooltip + ", " + class9_1.interfaceID);
					menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
					menuActionID[menuActionRow] = 679;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if(class9_1.type == 2)
				{
					int k2 = 0;
					for(int l2 = 0; l2 < class9_1.height; l2++)
					{
						for(int i3 = 0; i3 < class9_1.width; i3++)
						{
							int j3 = i2 + i3 * (32 + class9_1.invSpritePadX);
							int k3 = j2 + l2 * (32 + class9_1.invSpritePadY);
							if(k2 < 20)
							{
								j3 += class9_1.spritesX[k2];
								k3 += class9_1.spritesY[k2];
							}
							if(k >= j3 && i1 >= k3 && k < j3 + 32 && i1 < k3 + 32)
							{
								mouseInvInterfaceIndex = k2;
								lastActiveInvInterface = class9_1.id;
								if(class9_1.inv[k2] > 0)
								{
									ItemDef itemDef = ItemDef.forID(class9_1.inv[k2] - 1);
									if(itemSelected == 1 && class9_1.isInventoryInterface)
									{
										if(class9_1.id != selectedInventoryInterface || k2 != selectedInventorySlot)
										{
											menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
											menuActionID[menuActionRow] = 870;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = class9_1.id;
											menuActionRow++;
										}
									} else
									if(spellSelected == 1 && class9_1.isInventoryInterface)
									{
										if((spellUsableOn & 0x10) == 16)
										{
											menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
											menuActionID[menuActionRow] = 543;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = class9_1.id;
											menuActionRow++;
										}
									} else
									{
										if(class9_1.isInventoryInterface)
										{
											for(int l3 = 4; l3 >= 3; l3--)
												if(itemDef.actions != null && itemDef.actions[l3] != null)
												{
													menuActionName[menuActionRow] = itemDef.actions[l3] + " @lre@" + itemDef.name;
													if(l3 == 3)
														menuActionID[menuActionRow] = 493;
													if(l3 == 4)
														menuActionID[menuActionRow] = 847;
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = class9_1.id;
													menuActionRow++;
												} else
												if(l3 == 4)
												{
													menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
													menuActionID[menuActionRow] = 847;
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = class9_1.id;
													menuActionRow++;
												}

										}
										if(class9_1.usableItemInterface)
										{
											menuActionName[menuActionRow] = "Use @lre@" + itemDef.name;
											menuActionID[menuActionRow] = 447;
											menuActionCmd1[menuActionRow] = itemDef.id;
											//k2 = inventory spot
											//System.out.println(k2);
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = class9_1.id;
											menuActionRow++;
										}
										if(class9_1.isInventoryInterface && itemDef.actions != null)
										{
											for(int i4 = 2; i4 >= 0; i4--)
												if(itemDef.actions[i4] != null)
												{
													menuActionName[menuActionRow] = itemDef.actions[i4] + " @lre@" + itemDef.name;
													if(i4 == 0)
														menuActionID[menuActionRow] = 74;
													if(i4 == 1)
														menuActionID[menuActionRow] = 454;
													if(i4 == 2)
														menuActionID[menuActionRow] = 539;
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = class9_1.id;
													menuActionRow++;
												}

										}
										if(class9_1.actions != null)
										{
											for(int j4 = 4; j4 >= 0; j4--)
												if(class9_1.actions[j4] != null)
												{
													menuActionName[menuActionRow] = class9_1.actions[j4] + " @lre@" + itemDef.name;
													if(j4 == 0)
														menuActionID[menuActionRow] = 632;
													if(j4 == 1)
														menuActionID[menuActionRow] = 78;
													if(j4 == 2)
														menuActionID[menuActionRow] = 867;
													if(j4 == 3)
														menuActionID[menuActionRow] = 431;
													if(j4 == 4)
														menuActionID[menuActionRow] = 53;
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = class9_1.id;
													menuActionRow++;
												}

										}
										//menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + " @gre@(@whi@" + (class9_1.inv[k2] - 1) + "@gre@)";
										menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name;
										menuActionID[menuActionRow] = 1125;
										menuActionCmd1[menuActionRow] = itemDef.id;
										menuActionCmd2[menuActionRow] = k2;
										menuActionCmd3[menuActionRow] = class9_1.id;
										menuActionRow++;
									}
								}
							}
							k2++;
						}

					}

				}
			}
		}

	}

	public void drawScrollbar(int j, int k, int l, int i1, int j1) {
		scrollBar1.drawSprite(i1, l);
		scrollBar2.drawSprite(i1, (l + j) - 16);
		DrawingArea.drawPixels(j - 32, l + 16, i1, 0x000001, 16);
		DrawingArea.drawPixels(j - 32, l + 16, i1, 0x3d3426, 15);
		DrawingArea.drawPixels(j - 32, l + 16, i1, 0x342d21, 13);
		DrawingArea.drawPixels(j - 32, l + 16, i1, 0x2e281d, 11);
		DrawingArea.drawPixels(j - 32, l + 16, i1, 0x29241b, 10);
		DrawingArea.drawPixels(j - 32, l + 16, i1, 0x252019, 9);
		DrawingArea.drawPixels(j - 32, l + 16, i1, 0x000001, 1);
		int k1 = ((j - 32) * j) / j1;
		if(k1 < 8)
			k1 = 8;
		int l1 = ((j - 32 - k1) * k) / (j1 - j);
		DrawingArea.drawPixels(k1, l + 16 + l1, i1, barFillColor, 16);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x000001, k1, i1);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x817051, k1, i1 + 1);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x73654a, k1, i1 + 2);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x6a5c43, k1, i1 + 3);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x6a5c43, k1, i1 + 4);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x655841, k1, i1 + 5);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x655841, k1, i1 + 6);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x61553e, k1, i1 + 7);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x61553e, k1, i1 + 8);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x5d513c, k1, i1 + 9);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x5d513c, k1, i1 + 10);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x594e3a, k1, i1 + 11);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x594e3a, k1, i1 + 12);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x514635, k1, i1 + 13);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x4b4131, k1, i1 + 14);
		DrawingArea.drawVerticalLine(l + 16 + l1, 0x000001, 15, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x000001, 15, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x655841, 14, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x6a5c43, 13, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x6d5f48, 11, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x73654a, 10, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x76684b, 7, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x7b6a4d, 5, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x7e6e50, 4, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x817051, 3, i1);
		DrawingArea.drawVerticalLine(l + 17 + l1, 0x000001, 2, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x000001, 16, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x564b38, 15, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x5d513c, 14, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x625640, 11, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x655841, 10, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x6a5c43, 7, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x6e6046, 5, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x716247, 4, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x7b6a4d, 3, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x817051, 2, i1);
		DrawingArea.drawVerticalLine(l + 18 + l1, 0x000001, 1, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x000001, 16, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x514635, 15, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x564b38, 14, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x5d513c, 11, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x61553e, 9, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x655841, 7, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x6a5c43, 5, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x6e6046, 4, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x73654a, 3, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x817051, 2, i1);
		DrawingArea.drawVerticalLine(l + 19 + l1, 0x000001, 1, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x000001, 16, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x4b4131, 15, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x544936, 14, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x594e3a, 13, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x5d513c, 10, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x61553e, 8, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x655841, 6, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x6a5c43, 4, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x73654a, 3, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x817051, 2, i1);
		DrawingArea.drawVerticalLine(l + 20 + l1, 0x000001, 1, i1);
		DrawingArea.drawHorizontalLine(l + 16 + l1, 0x000001, k1, i1 + 15);
		DrawingArea.drawVerticalLine(l + 15 + l1 + k1, 0x000001, 16, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x000001, 15, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x3f372a, 14, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x443c2d, 10, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x483e2f, 9, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x4a402f, 7, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x4b4131, 4, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x564b38, 3, i1);
		DrawingArea.drawVerticalLine(l + 14 + l1 + k1, 0x000001, 2, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x000001, 16, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x443c2d, 15, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x4b4131, 11, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x514635, 9, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x544936, 7, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x564b38, 6, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x594e3a, 4, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x625640, 3, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x6a5c43, 2, i1);
		DrawingArea.drawVerticalLine(l + 13 + l1 + k1, 0x000001, 1, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x000001, 16, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x443c2d, 15, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x4b4131, 14, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x544936, 12, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x564b38, 11, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x594e3a, 10, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x5d513c, 7, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x61553e, 4, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x6e6046, 3, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x7b6a4d, 2, i1);
		DrawingArea.drawVerticalLine(l + 12 + l1 + k1, 0x000001, 1, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x000001, 16, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x4b4131, 15, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x514635, 14, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x564b38, 13, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x594e3a, 11, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x5d513c, 9, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x61553e, 7, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x655841, 5, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x6a5c43, 4, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x73654a, 3, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x7b6a4d, 2, i1);
		DrawingArea.drawVerticalLine(l + 11 + l1 + k1, 0x000001, 1, i1);
	}

	private void updateNPCs(Stream stream, int i)
	{
		npcUpdateCount = 0;
		entityCount = 0;
		parseNPCRemovals(stream);
		parseNewNPCs(i, stream);
		parseNPCUpdateMasks(stream);
		for(int k = 0; k < npcUpdateCount; k++)
		{
			int l = entityUpdateIndices[k];
			if(npcArray[l].textColor != loopCycle)
			{
				npcArray[l].desc = null;
				npcArray[l] = null;
			}
		}

		if(stream.currentOffset != i)
		{
			signlink.reporterror(myUsername + " size mismatch in getnpcpos - pos:" + stream.currentOffset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for(int i1 = 0; i1 < npcCount; i1++)
			if(npcArray[npcIndices[i1]] == null)
			{
				signlink.reporterror(myUsername + " null entry in npc list - pos:" + i1 + " size:" + npcCount);
				throw new RuntimeException("eek");
			}

	}

	private int cButtonHPos;
	private int cButtonHCPos;
	private int cButtonCPos;

	private void processChatModeClick() {
		if(super.mouseX >= 5 && super.mouseX <= 61 && super.mouseY >= 482 && super.mouseY <= 503) {
			cButtonHPos = 0;
			aBoolean1233 = true;
			inputTaken = true;
		} else if(super.mouseX >= 71 && super.mouseX <= 127 && super.mouseY >= 482 && super.mouseY <= 503) {
			cButtonHPos = 1;
			aBoolean1233 = true;
			inputTaken = true;
		} else if(super.mouseX >= 137 && super.mouseX <= 193 && super.mouseY >= 482 && super.mouseY <= 503) {
			cButtonHPos = 2;
			aBoolean1233 = true;
			inputTaken = true;
		} else if(super.mouseX >= 203 && super.mouseX <= 259 && super.mouseY >= 482 && super.mouseY <= 503) {
			cButtonHPos = 3;
			aBoolean1233 = true;
			inputTaken = true;
		} else if(super.mouseX >= 269 && super.mouseX <= 325 && super.mouseY >= 482 && super.mouseY <= 503) {
			cButtonHPos = 4;
			aBoolean1233 = true;
			inputTaken = true;
		} else if(super.mouseX >= 335 && super.mouseX <= 391 && super.mouseY >= 482 && super.mouseY <= 503) {
			cButtonHPos = 5;
			aBoolean1233 = true;
			inputTaken = true;
		} else if(super.mouseX >= 404 && super.mouseX <= 515 && super.mouseY >= 482 && super.mouseY <= 503) {
			cButtonHPos = 6;
			aBoolean1233 = true;
			inputTaken = true;
		} else {
			cButtonHPos = -1;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(super.clickMode3 == 1) {
			if(super.saveClickX >= 5 && super.saveClickX <= 61 && super.saveClickY >= 482 && super.saveClickY <= 505) {
				cButtonCPos = 0;
				chatTypeView = 0;
				aBoolean1233 = true;
				inputTaken = true;
			} else if(super.saveClickX >= 71 && super.saveClickX <= 127 && super.saveClickY >= 482 && super.saveClickY <= 505) {
				cButtonCPos = 1;
				chatTypeView = 5;
				aBoolean1233 = true;
				inputTaken = true;
			} else if(super.saveClickX >= 137 && super.saveClickX <= 193 && super.saveClickY >= 482 && super.saveClickY <= 505) {
				cButtonCPos = 2;
				chatTypeView = 1;
				aBoolean1233 = true;
				inputTaken = true;
			} else if(super.saveClickX >= 203 && super.saveClickX <= 259 && super.saveClickY >= 482 && super.saveClickY <= 505) {
				cButtonCPos = 3;
				chatTypeView = 2;
				aBoolean1233 = true;
				inputTaken = true;
			} else if(super.saveClickX >= 269 && super.saveClickX <= 325 && super.saveClickY >= 482 && super.saveClickY <= 505) {
				cButtonCPos = 4;
				chatTypeView = 11;
				aBoolean1233 = true;
				inputTaken = true;
			} else if(super.saveClickX >= 335 && super.saveClickX <= 391 && super.saveClickY >= 482 && super.saveClickY <= 505) {
				cButtonCPos = 5;
				chatTypeView = 3;
				aBoolean1233 = true;
				inputTaken = true;
			} else if(super.saveClickX >= 404 && super.saveClickX <= 515 && super.saveClickY >= 482 && super.saveClickY <= 505) {
				if(openInterfaceID == -1) {
					clearTopInterfaces();
					reportAbuseInput = "";
					canMute = false;
					for(int i = 0; i < RSInterface.interfaceCache.length; i++) {
						if(RSInterface.interfaceCache[i] == null || RSInterface.interfaceCache[i].contentType != 600)
							continue;
						reportAbuseInterfaceID = openInterfaceID = RSInterface.interfaceCache[i].parentID;
						break;
					}
				} else {
					pushMessage("Please close the interface you have open before using 'report abuse'", 0, "");
				}
			}
		}
	}

	private void applyVarpSetting(int i)
	{
		int j = Varp.cache[i].usage;
		if(j == 0)
			return;
		int k = variousSettings[i];
		if(j == 1)
		{
			if(k == 1)
				Texture.setBrightness(0.90000000000000002D);
			if(k == 2)
				Texture.setBrightness(0.80000000000000004D);
			if(k == 3)
				Texture.setBrightness(0.69999999999999996D);
			if(k == 4)
				Texture.setBrightness(0.59999999999999998D);
			ItemDef.mruNodes1.unlinkAll();
			welcomeScreenRaised = true;
		}
		if(j == 3)
		{
			boolean flag1 = musicEnabled;
			if(k == 0)
			{
				adjustVolume(musicEnabled, 0);
				musicEnabled = true;
			}
			if(k == 1)
			{
				adjustVolume(musicEnabled, -400);
				musicEnabled = true;
			}
			if(k == 2)
			{
				adjustVolume(musicEnabled, -800);
				musicEnabled = true;
			}
			if(k == 3)
			{
				adjustVolume(musicEnabled, -1200);
				musicEnabled = true;
			}
			if(k == 4)
				musicEnabled = false;
			if(musicEnabled != flag1 && !lowMem)
			{
				if(musicEnabled)
				{
					nextSong = currentSong;
					songChanging = true;
					onDemandFetcher.requestFile(2, nextSong);
				} else
				{
					stopMidi();
				}
				prevSong = 0;
			}
		}
		if(j == 4)
		{
			if(k == 0)
			{
				pendingInput = true;
				setWaveVolume(0);
			}
			if(k == 1)
			{
				pendingInput = true;
				setWaveVolume(-400);
			}
			if(k == 2)
			{
				pendingInput = true;
				setWaveVolume(-800);
			}
			if(k == 3)
			{
				pendingInput = true;
				setWaveVolume(-1200);
			}
			if(k == 4)
				pendingInput = false;
		}
		if(j == 5)
			clickMode = k;
		if(j == 6)
			actionType = k;
		if(j == 8)
		{
			splitPrivateChat = k;
			inputTaken = true;
		}
		if(j == 9)
			terrainDataIndex = k;
	}
	
	private Sprite HPBarFull;
	private Sprite HPBarEmpty;

	private void updateEntities() {
		try{
			int anInt974 = 0;
			for(int j = -1; j < playerCount + npcCount; j++) {
			Object obj;
			if(j == -1)
				obj = myPlayer;
			else
			if(j < playerCount)
				obj = playerArray[playerIndices[j]];
			else
				obj = npcArray[npcIndices[j - playerCount]];
			if(obj == null || !((Entity) (obj)).isVisible())
				continue;
			if(obj instanceof NPC) {
				EntityDef entityDef = ((NPC)obj).desc;
				if(entityDef.childrenIDs != null)
					entityDef = entityDef.getChildDefinition();
				if(entityDef == null)
					continue;
			}
			if(j < playerCount) {
				int l = 30;
				Player player = (Player)obj;
				if(player.headIcon >= 0) {
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
					if(spriteDrawX > -1) {
						if (player.skullIcon < 2) {
							skullIcons[player.skullIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
							l += 25;
						}
						if (player.headIcon < 7) {
							headIcons[player.headIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
							l += 18;
						}
					}
				}
				if(j >= 0 && minimapRotation == 10 && cameraTargetIndex == playerIndices[j]) {
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
					if(spriteDrawX > -1)
						headIconsHint[player.hintIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
				}
			} else {
				EntityDef entityDef_1 = ((NPC)obj).desc;
				if(entityDef_1.headIcon >= 0 && entityDef_1.headIcon < headIcons.length) {
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
					if(spriteDrawX > -1)
						headIcons[entityDef_1.headIcon].drawSprite(spriteDrawX - 12, spriteDrawY - 30);
				}
				if(minimapRotation == 1 && hintIconNpcIndex == npcIndices[j - playerCount] && loopCycle % 20 < 10) {
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
					if(spriteDrawX > -1)
						headIconsHint[0].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
				}
			}
			if(((Entity) (obj)).textSpoken != null && (j >= playerCount || publicChatMode == 0 || publicChatMode == 3 || publicChatMode == 1 && isFriendOrSelf(((Player)obj).name)))
			{
				npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height);
				if(spriteDrawX > -1 && anInt974 < maxOverheadCount)
				{
					overheadWidth[anInt974] = chatTextDrawingArea.getTextWidth(((Entity) (obj)).textSpoken) / 2;
					overheadHeight[anInt974] = chatTextDrawingArea.fontHeight;
					overheadX[anInt974] = spriteDrawX;
					overheadY[anInt974] = spriteDrawY;
					overheadTextColor[anInt974] = ((Entity) (obj)).turnAroundAnimId;
					overheadTextEffect[anInt974] = ((Entity) (obj)).animResetCycle;
					overheadTextCycle[anInt974] = ((Entity) (obj)).textCycle;
					overheadTextStr[anInt974++] = ((Entity) (obj)).textSpoken;
					if(actionType == 0 && ((Entity) (obj)).animResetCycle >= 1 && ((Entity) (obj)).animResetCycle <= 3)
					{
						overheadHeight[anInt974] += 10;
						overheadY[anInt974] += 5;
					}
					if(actionType == 0 && ((Entity) (obj)).animResetCycle == 4)
						overheadWidth[anInt974] = 60;
					if(actionType == 0 && ((Entity) (obj)).animResetCycle == 5)
						overheadHeight[anInt974] += 5;
				}
			}
			if(((Entity) (obj)).loopCycleStatus > loopCycle)
			{
				try{
					npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height + 15);
					if(spriteDrawX > -1)
					{
						int i1 = (((Entity) (obj)).currentHealth * 30) / ((Entity) (obj)).maxHealth;
						if(i1 > 30) {
							i1 = 30;
						}	
							int HpPercent = (((Entity) (obj)).currentHealth * 56) / ((Entity) (obj)).maxHealth;
							if (HpPercent > 56) {
								HpPercent = 56;
							}
							HPBarEmpty.drawSprite(spriteDrawX - 28, spriteDrawY - 5);//3
							HPBarFull = new Sprite(sign.signlink.findcachedir() + "Sprites/Player/HP 0.PNG", HpPercent, 7);
							HPBarFull.drawSprite(spriteDrawX - 28, spriteDrawY - 5);
					}
				}catch(Exception e){ }
				}
				for(int j1 = 0; j1 < 4; j1++)
					if(((Entity) (obj)).hitsLoopCycle[j1] > loopCycle)
					{
						npcScreenPos(((Entity) (obj)), ((Entity) (obj)).height / 2);
						if(spriteDrawX > -1)
						{
							if(j1 == 1)
								spriteDrawY -= 20;
							if(j1 == 2)
							{
								spriteDrawX -= 15;
								spriteDrawY -= 10;
							}
							if(j1 == 3)
							{
								spriteDrawX += 15;
								spriteDrawY -= 10;
							}
			 			    if (((Entity) (obj)).hitArray[j1] == 0 || ((Entity) (obj)).hitMarkTypes[j1] == 3) 
			 			    	hitMark[((Entity) (obj)).hitMarkTypes[j1]].drawSprite(spriteDrawX - 12, spriteDrawY - 12);
			 			    else {
								hitMark[((Entity) (obj)).hitMarkTypes[j1]].drawSprite(spriteDrawX - 12, spriteDrawY - 12);
								smallText.drawText(0, String.valueOf(((Entity) (obj)).hitArray[j1]), spriteDrawY + 4, spriteDrawX);
								smallText.drawText(0xffffff, String.valueOf(((Entity) (obj)).hitArray[j1]), spriteDrawY + 3, spriteDrawX - 1);
			 			    }
						}
					}
			}
			for(int k = 0; k < anInt974; k++) {
				int k1 = overheadX[k];
				int l1 = overheadY[k];
				int j2 = overheadWidth[k];
				int k2 = overheadHeight[k];
				boolean flag = true;
				while(flag) 
				{
					flag = false;
					for(int l2 = 0; l2 < k; l2++)
						if(l1 + 2 > overheadY[l2] - overheadHeight[l2] && l1 - k2 < overheadY[l2] + 2 && k1 - j2 < overheadX[l2] + overheadWidth[l2] && k1 + j2 > overheadX[l2] - overheadWidth[l2] && overheadY[l2] - overheadHeight[l2] < l1)
						{
							l1 = overheadY[l2] - overheadHeight[l2];
							flag = true;
						}

				}
				spriteDrawX = overheadX[k];
				spriteDrawY = overheadY[k] = l1;
				String s = overheadTextStr[k];
				if(actionType == 0)
				{
					int i3 = 0xffff00;
					if(overheadTextColor[k] < 6)
						i3 = chatColors[overheadTextColor[k]];
					if(overheadTextColor[k] == 6)
						i3 = hintIconY % 20 >= 10 ? 0xffff00 : 0xff0000;
					if(overheadTextColor[k] == 7)
						i3 = hintIconY % 20 >= 10 ? 65535 : 255;
					if(overheadTextColor[k] == 8)
						i3 = hintIconY % 20 >= 10 ? 0x80ff80 : 45056;
					if(overheadTextColor[k] == 9) {
						int j3 = 150 - overheadTextCycle[k];
						if(j3 < 50)
							i3 = 0xff0000 + 1280 * j3;
						else
						if(j3 < 100)
							i3 = 0xffff00 - 0x50000 * (j3 - 50);
						else
						if(j3 < 150)
							i3 = 65280 + 5 * (j3 - 100);
					}
					if(overheadTextColor[k] == 10) {
						int k3 = 150 - overheadTextCycle[k];
						if(k3 < 50)
							i3 = 0xff0000 + 5 * k3;
						else
						if(k3 < 100)
							i3 = 0xff00ff - 0x50000 * (k3 - 50);
						else
						if(k3 < 150)
							i3 = (255 + 0x50000 * (k3 - 100)) - 5 * (k3 - 100);
					}
					if(overheadTextColor[k] == 11) {
						int l3 = 150 - overheadTextCycle[k];
						if(l3 < 50)
							i3 = 0xffffff - 0x50005 * l3;
						else
						if(l3 < 100)
							i3 = 65280 + 0x50005 * (l3 - 50);
						else
						if(l3 < 150)
							i3 = 0xffffff - 0x50000 * (l3 - 100);
					}
					if(overheadTextEffect[k] == 0) {
						chatTextDrawingArea.drawText(0, s, spriteDrawY + 1, spriteDrawX);
						chatTextDrawingArea.drawText(i3, s, spriteDrawY, spriteDrawX);
					}
					if(overheadTextEffect[k] == 1) {
						chatTextDrawingArea.drawTextShadow(0, s, spriteDrawX, hintIconY, spriteDrawY + 1);
						chatTextDrawingArea.drawTextShadow(i3, s, spriteDrawX, hintIconY, spriteDrawY);
					}
					if(overheadTextEffect[k] == 2) {
						chatTextDrawingArea.drawCenteredShadow(spriteDrawX, s, hintIconY, spriteDrawY + 1, 0);
						chatTextDrawingArea.drawCenteredShadow(spriteDrawX, s, hintIconY, spriteDrawY, i3);
					}
					if(overheadTextEffect[k] == 3) {
						chatTextDrawingArea.drawShaking(150 - overheadTextCycle[k], s, hintIconY, spriteDrawY + 1, spriteDrawX, 0);
						chatTextDrawingArea.drawShaking(150 - overheadTextCycle[k], s, hintIconY, spriteDrawY, spriteDrawX, i3);
					}
					if(overheadTextEffect[k] == 4) {
						int i4 = chatTextDrawingArea.getTextWidth(s);
						int k4 = ((150 - overheadTextCycle[k]) * (i4 + 100)) / 150;
						DrawingArea.setDrawingArea(334, spriteDrawX - 50, spriteDrawX + 50, 0);
						chatTextDrawingArea.drawText(0, s, spriteDrawY + 1, (spriteDrawX + 50) - k4);
						chatTextDrawingArea.drawText(i3, s, spriteDrawY, (spriteDrawX + 50) - k4);
						DrawingArea.defaultDrawingAreaSize();
					}
					if(overheadTextEffect[k] == 5) {
						int j4 = 150 - overheadTextCycle[k];
						int l4 = 0;
						if(j4 < 25)
							l4 = j4 - 25;
						else
						if(j4 > 125)
							l4 = j4 - 125;
						DrawingArea.setDrawingArea(spriteDrawY + 5, 0, 512, spriteDrawY - chatTextDrawingArea.fontHeight - 1);
						chatTextDrawingArea.drawText(0, s, spriteDrawY + 1 + l4, spriteDrawX);
						chatTextDrawingArea.drawText(i3, s, spriteDrawY + l4, spriteDrawX);
						DrawingArea.defaultDrawingAreaSize();
					}
				} else {
					chatTextDrawingArea.drawText(0, s, spriteDrawY + 1, spriteDrawX);
					chatTextDrawingArea.drawText(0xffff00, s, spriteDrawY, spriteDrawX);
				}
			}
		} catch(Exception e){ }
	}

	private void delFriend(long l)
	{
		try
		{
			if(l == 0L)
				return;
			for(int i = 0; i < friendsCount; i++)
			{
				if(friendsListAsLongs[i] != l)
					continue;
				friendsCount--;
				needDrawTabArea = true;
				for(int j = i; j < friendsCount; j++)
				{
					friendsList[j] = friendsList[j + 1];
					friendsNodeIDs[j] = friendsNodeIDs[j + 1];
					friendsListAsLongs[j] = friendsListAsLongs[j + 1];
				}

				stream.createFrame(215);
				stream.writeQWord(l);
				break;
			}
		}
		catch(RuntimeException runtimeexception)
		{
			signlink.reporterror("18622, " + false + ", " + l + ", " + runtimeexception.toString());
			throw new RuntimeException();
		}
	}

	public void drawSideIcons(){
		/* Top sideIcons */
		if(tabInterfaceIDs[0] != -1)//attack
			sideIcons[0].drawSprite(10, 4);
		if(tabInterfaceIDs[1] != -1)//stat
			sideIcons[1].drawSprite(43, 4);
		if(tabInterfaceIDs[2] != -1)//quest
			sideIcons[2].drawSprite(76, 3);
		if(tabInterfaceIDs[3] != -1)//inventory
			sideIcons[3].drawSprite(111, 5);
		if(tabInterfaceIDs[4] != -1)//equipment
			sideIcons[4].drawSprite(140, 1);
		if(tabInterfaceIDs[5] != -1)//prayer
			sideIcons[5].drawSprite(174, 1);
		if(tabInterfaceIDs[6] != -1)//magic
			sideIcons[6].drawSprite(208, 4);
		/* Bottom sideIcons */
		if(tabInterfaceIDs[7] != -1)//clan
			sideIcons[7].drawSprite(11, 303);
		if(tabInterfaceIDs[8] != -1)//friends
			sideIcons[8].drawSprite(46, 306);
		if(tabInterfaceIDs[9] != -1)//ignore
			sideIcons[9].drawSprite(79, 306);
		if(tabInterfaceIDs[10] != -1)//options
			sideIcons[10].drawSprite(113, 302);
		if(tabInterfaceIDs[11] != -1)//options
			sideIcons[11].drawSprite(145, 304);
		if(tabInterfaceIDs[12] != -1)//emotes
			sideIcons[12].drawSprite(181, 302);
		if(tabInterfaceIDs[13] != -1)//musicL
			sideIcons[13].drawSprite(213, 303);
	}

	public void drawRedStones() {
		if(tabInterfaceIDs[tabID] != -1) {
			switch(tabID) {
				case 0:
					redStones[0].drawSprite(3, 0);
					break;
				case 1:
					redStones[4].drawSprite(41, 0);
					break;
				case 2:
					redStones[4].drawSprite(74, 0);
					break;
				case 3:
					redStones[4].drawSprite(107, 0);
					break;
				case 4:
					redStones[4].drawSprite(140, 0);
					break;
				case 5:
					redStones[4].drawSprite(173, 0);
					break;
				case 6:
					redStones[1].drawSprite(206, 0);
					break;
				case 7:
					redStones[2].drawSprite(3, 298);
					break;
				case 8:
					redStones[4].drawSprite(41, 298);
					break;
				case 9:
					redStones[4].drawSprite(74, 298);
					break;
				case 10:
					redStones[4].drawSprite(107, 298);
					break;
				case 11:
					redStones[4].drawSprite(140, 298);
					break;
				case 12:
					redStones[4].drawSprite(173, 298);
					break;
				case 13:
					redStones[4].drawSprite(206, 298);
					break;
			}
		}
	}

	private void drawTabArea() {
		titleMuralIP.initDrawingArea();
		Texture.scanlineOffset = mapChunkY2;
		tabArea.drawSprite(0, 0);
		if(invOverlayInterfaceID == -1) {
			drawRedStones();
			drawSideIcons();
		}
		if(invOverlayInterfaceID != -1) {
			if(invOverlayInterfaceID >= 0 && invOverlayInterfaceID < RSInterface.interfaceCache.length && RSInterface.interfaceCache[invOverlayInterfaceID] != null)
				drawInterface(0, 28, RSInterface.interfaceCache[invOverlayInterfaceID], 37);
		} else if(tabInterfaceIDs[tabID] != -1) {
			int tid = tabInterfaceIDs[tabID];
			if(tid >= 0 && tid < RSInterface.interfaceCache.length && RSInterface.interfaceCache[tid] != null)
				drawInterface(0, 28, RSInterface.interfaceCache[tid], 37);
		}
		if(menuOpen && menuScreenArea == 1)
			drawMenu();
		titleMuralIP.drawGraphics(clientSize == 0 ? 168 : clientHeight - 335, super.graphics, clientSize == 0 ? 519 : clientWidth - 246);
		loginMsgIP.initDrawingArea();
		Texture.scanlineOffset = mapChunkLandscapeIds;
	}

	private void animateTexture(int j) {
		if(!lowMem) {
			if(Texture.textureLastCycle[17] >= j) {
				Background background = Texture.textures[17];
				int k = background.width * background.height - 1;
				//fire cape apparently?
				int j1 = background.width * cameraTargetLocalZ * 2;
				byte abyte0[] = background.aByteArray1450;
				byte abyte3[] = terrainData;
				for(int i2 = 0; i2 <= k; i2++)
					abyte3[i2] = abyte0[i2 - j1 & k];

				background.aByteArray1450 = abyte3;
				terrainData = abyte0;
				Texture.setTextureActive(17);
				cameraAngle++;
				if(cameraAngle > 1235) {
					cameraAngle = 0;
					stream.createFrame(226);
					stream.writeWordBigEndian(0);
					int l2 = stream.currentOffset;
					stream.writeWord(58722);
					stream.writeWordBigEndian(240);
					stream.writeWord((int)(Math.random() * 65536D));
					stream.writeWordBigEndian((int)(Math.random() * 256D));
					if((int)(Math.random() * 2D) == 0)
						stream.writeWord(51825);
					stream.writeWordBigEndian((int)(Math.random() * 256D));
					stream.writeWord((int)(Math.random() * 65536D));
					stream.writeWord(7130);
					stream.writeWord((int)(Math.random() * 65536D));
					stream.writeWord(61657);
					stream.writeBytes(stream.currentOffset - l2);
				}
			}
			if(Texture.textureLastCycle[24] >= j) {
				Background background_1 = Texture.textures[24];
				int l = background_1.width * background_1.height - 1;
				int k1 = background_1.width * cameraTargetLocalZ * 2;
				byte abyte1[] = background_1.aByteArray1450;
				byte abyte4[] = terrainData;
				for(int j2 = 0; j2 <= l; j2++)
					abyte4[j2] = abyte1[j2 - k1 & l];

				background_1.aByteArray1450 = abyte4;
				terrainData = abyte1;
				Texture.setTextureActive(24);
			}
			if(Texture.textureLastCycle[34] >= j) {
				Background background_2 = Texture.textures[34];
				int i1 = background_2.width * background_2.height - 1;
				int l1 = background_2.width * cameraTargetLocalZ * 2;
				byte abyte2[] = background_2.aByteArray1450;
				byte abyte5[] = terrainData;
				for(int k2 = 0; k2 <= i1; k2++)
					abyte5[k2] = abyte2[k2 - l1 & i1];

				background_2.aByteArray1450 = abyte5;
				terrainData = abyte2;
				Texture.setTextureActive(34);
			}
			if(Texture.textureLastCycle[40] >= j)
            {
				Background background_2 = Texture.textures[40];
				int i1 = background_2.width * background_2.height - 1;
				int l1 = background_2.width * cameraTargetLocalZ * 2;
				byte abyte2[] = background_2.aByteArray1450;
				byte abyte5[] = terrainData;
				for(int k2 = 0; k2 <= i1; k2++)
					abyte5[k2] = abyte2[k2 - l1 & i1];

				background_2.aByteArray1450 = abyte5;
				terrainData = abyte2;
				Texture.setTextureActive(40);
            }
		}
	}

	private void processPlayerChat() {
		for(int i = -1; i < playerCount; i++) {
			int j;
			if(i == -1)
				j = myPlayerIndex;
			else
				j = playerIndices[i];
			Player player = playerArray[j];
			if(player != null && player.textCycle > 0) {
				player.textCycle--;
				if(player.textCycle == 0)
					player.textSpoken = null;
			}
		}
		for(int k = 0; k < npcCount; k++) {
			int l = npcIndices[k];
			NPC npc = npcArray[l];
			if(npc != null && npc.textCycle > 0) {
				npc.textCycle--;
				if(npc.textCycle == 0)
					npc.textSpoken = null;
			}
		}
	}

	private void calcCameraPos() {
		int i = cameraLocX * 128 + 64;
		int j = cameraLocY * 128 + 64;
		int k = getTileHeight(plane, j, i) - cameraLocHeight;
		if(xCameraPos < i) {
			xCameraPos += cameraLocSpeed + ((i - xCameraPos) * cameraLocAccel) / 1000;
			if(xCameraPos > i)
				xCameraPos = i;
		}
		if(xCameraPos > i) {
			xCameraPos -= cameraLocSpeed + ((xCameraPos - i) * cameraLocAccel) / 1000;
			if(xCameraPos < i)
				xCameraPos = i;
		}
		if(zCameraPos < k) {
			zCameraPos += cameraLocSpeed + ((k - zCameraPos) * cameraLocAccel) / 1000;
			if(zCameraPos > k)
				zCameraPos = k;
		}
		if(zCameraPos > k) {
			zCameraPos -= cameraLocSpeed + ((zCameraPos - k) * cameraLocAccel) / 1000;
			if(zCameraPos < k)
				zCameraPos = k;
		}
		if(yCameraPos < j) {
			yCameraPos += cameraLocSpeed + ((j - yCameraPos) * cameraLocAccel) / 1000;
			if(yCameraPos > j)
				yCameraPos = j;
		}
		if(yCameraPos > j) {
			yCameraPos -= cameraLocSpeed + ((yCameraPos - j) * cameraLocAccel) / 1000;
			if(yCameraPos < j)
				yCameraPos = j;
		}
		i = cameraPosX * 128 + 64;
		j = cameraPosY * 128 + 64;
		k = getTileHeight(plane, j, i) - cameraPosHeight;
		int l = i - xCameraPos;
		int i1 = k - zCameraPos;
		int j1 = j - yCameraPos;
		int k1 = (int)Math.sqrt(l * l + j1 * j1);
		int l1 = (int)(Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
		int i2 = (int)(Math.atan2(l, j1) * -325.94900000000001D) & 0x7ff;
		if(l1 < 128)
			l1 = 128;
		if(l1 > 383)
			l1 = 383;
		if(yCameraCurve < l1) {
			yCameraCurve += cameraSpeed + ((l1 - yCameraCurve) * cameraAcceleration) / 1000;
			if(yCameraCurve > l1)
				yCameraCurve = l1;
		}
		if(yCameraCurve > l1) {
			yCameraCurve -= cameraSpeed + ((yCameraCurve - l1) * cameraAcceleration) / 1000;
			if(yCameraCurve < l1)
				yCameraCurve = l1;
		}
		int j2 = i2 - xCameraCurve;
		if(j2 > 1024)
			j2 -= 2048;
		if(j2 < -1024)
			j2 += 2048;
		if(j2 > 0) {
			xCameraCurve += cameraSpeed + (j2 * cameraAcceleration) / 1000;
			xCameraCurve &= 0x7ff;
		}
		if(j2 < 0) {
			xCameraCurve -= cameraSpeed + (-j2 * cameraAcceleration) / 1000;
			xCameraCurve &= 0x7ff;
		}
		int k2 = i2 - xCameraCurve;
		if(k2 > 1024)
			k2 -= 2048;
		if(k2 < -1024)
			k2 += 2048;
		if(k2 < 0 && j2 > 0 || k2 > 0 && j2 < 0)
			xCameraCurve = i2;
	}

	private void drawMenu() {
		int i = menuOffsetX;
		int j = menuOffsetY;
		int k = menuWidth;
		int l = menuHeight + 1;
		int i1 = 0x5d5447;
		//DrawingArea.drawPixels(height, yPos, xPos, color, width);
		//DrawingArea.fillPixels(xPos, width, height, color, yPos);
		DrawingArea.drawPixels(l, j, i, i1, k);
		DrawingArea.drawPixels(16, j + 1, i + 1, 0, k - 2);
		DrawingArea.fillPixels(i + 1, k - 2, l - 19, 0, j + 18);
		chatTextDrawingArea.drawText(i1, "Choose Option", j + 14, i + 3);
		int j1 = super.mouseX;
		int k1 = super.mouseY;
		if(menuScreenArea == 0) {
			j1 -= 4;
			k1 -= 4;
		}
		if(menuScreenArea == 1) {
			j1 -= 519;
			k1 -= 168;
		}
		if(menuScreenArea == 2) {
			j1 -= 17;
			k1 -= 338;
		}
		if(menuScreenArea == 3) {
			j1 -= 519;
			k1 -= 0;
		}
		for(int l1 = 0; l1 < menuActionRow; l1++) {
			int i2 = j + 31 + (menuActionRow - 1 - l1) * 15;
			int j2 = 0xffffff;
			if(j1 > i && j1 < i + k && k1 > i2 - 13 && k1 < i2 + 3)
				j2 = 0xffff00;
			chatTextDrawingArea.drawWaving(true, i + 3, j2, menuActionName[l1], i2);
		}
	}

	private void addFriend(long l) {
		try {
			if(l == 0L)
				return;
			if(friendsCount >= 100 && anInt1046 != 1) {
				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
				return;
			}
			if(friendsCount >= 200) {
				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
				return;
			}
			String s = TextClass.fixName(TextClass.nameForLong(l));
			for(int i = 0; i < friendsCount; i++)
				if(friendsListAsLongs[i] == l) {
					pushMessage(s + " is already on your friend list", 0, "");
					return;
				}
			for(int j = 0; j < ignoreCount; j++)
				if(ignoreListAsLongs[j] == l) {
					pushMessage("Please remove " + s + " from your ignore list first", 0, "");
					return;
				}

			if(s.equals(myPlayer.name)) {
				return;
			} else {
				friendsList[friendsCount] = s;
				friendsListAsLongs[friendsCount] = l;
				friendsNodeIDs[friendsCount] = 0;
				friendsCount++;
				needDrawTabArea = true;
				stream.createFrame(188);
				stream.writeQWord(l);
				return;
			}
		} catch(RuntimeException runtimeexception) {
			signlink.reporterror("15283, " + (byte)68 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private int getTileHeight(int i, int j, int k) {
		int l = k >> 7;
		int i1 = j >> 7;
		if(l < 0 || i1 < 0 || l > 103 || i1 > 103)
			return 0;
		int j1 = i;
		if(j1 < 3 && (byteGroundArray[1][l][i1] & 2) == 2)
			j1++;
		int k1 = k & 0x7f;
		int l1 = j & 0x7f;
		int i2 = intGroundArray[j1][l][i1] * (128 - k1) + intGroundArray[j1][l + 1][i1] * k1 >> 7;
		int j2 = intGroundArray[j1][l][i1 + 1] * (128 - k1) + intGroundArray[j1][l + 1][i1 + 1] * k1 >> 7;
		return i2 * (128 - l1) + j2 * l1 >> 7;
	}

	private static String intToKOrMil(int j) {
		if(j < 0x186a0)
			return String.valueOf(j);
		if(j < 0x989680)
			return j / 1000 + "K";
		else
			return j / 0xf4240 + "M";
	}
	
	public int canWalkDelay = 0;
     public int getDis(int coordX1, int coordY1, int coordX2, int coordY2)
    {
        int deltaX = coordX2 - coordX1;
        int deltaY = coordY2 - coordY1;
        return ((int)Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
    }
	 public int random(int range)
    {
        return (int)(Math.random() * range);
    }
   public boolean withinDistance(int x1, int y1, int x2, int y2, int dis)
    {
        for (int i = 0; i <= dis; i++)
        {
			try{
            if ((x1 + i) == x2 && ((y1 + i) == y2 || (y1 - i) == y2 || y1 == y2))
                return true;
            else
            if ((x1 - i) == x2 && ((x1 + i) == y2 || (y1 - i) == y2 || y1 == y2))
                return true;
            else
            if (x1 == x2 && ((x1 + i) == y2 || (y1 - i) == y2 || y1 == y2))
                return true;
								} catch(Exception ex){
		System.out.println("Exception in following, method : WithingDistance");
		}
        }
        return false;
    }

	private void resetLogout() {
	followPlayer = 0;
followNPC = 0;
followDistance = 1;
		try {
			if(socketStream != null)
				socketStream.close();
		}
		catch(Exception _ex) { }
		socketStream = null;
		loggedIn = false;
		loginScreenState = 0;
 //	   myUsername = "";
 //	   myPassword = "";
		unlinkMRUNodes();
		worldController.initToNull();
		for(int i = 0; i < 4; i++)
			aCollisionMapArray1230[i].reset();
		System.gc();
		stopMidi();
		currentSong = -1;
		nextSong = -1;
		prevSong = 0;
	}

	private void resetDefaultAppearance() {
		aBoolean1031 = true;
		for(int j = 0; j < 7; j++) {
			menuActionTypes[j] = -1;
			for(int k = 0; k < IDK.length; k++) {
				if(IDK.cache[k].aBoolean662 || IDK.cache[k].bodyPartId != j + (aBoolean1047 ? 0 : 7))
					continue;
				menuActionTypes[j] = k;
				break;
			}
		}
	}

	private void parseNewNPCs(int i, Stream stream) {
		while(stream.bitPosition + 21 < i * 8) {
			int k = stream.readBits(14);
			if(k == 16383)
				break;
			if(npcArray[k] == null)
				npcArray[k] = new NPC();
			NPC npc = npcArray[k];
			npcIndices[npcCount++] = k;
			npc.textColor = loopCycle;
			int l = stream.readBits(5);
			if(l > 15)
				l -= 32;
			int i1 = stream.readBits(5);
			if(i1 > 15)
				i1 -= 32;
			int j1 = stream.readBits(1);
			npc.desc = EntityDef.forID(stream.readBits(14));
			int k1 = stream.readBits(1);
			if(k1 == 1)
				entityIndices[entityCount++] = k;
			npc.tileSize = npc.desc.tileSpan;
			npc.turnSpeed = npc.desc.degreesToTurn;
			npc.walkBackAnimId = npc.desc.walkAnim;
			npc.walkLeftAnimId = npc.desc.turnAroundAnim;
			npc.walkRightAnimId = npc.desc.walkRightAnim;
			npc.runAnimId = npc.desc.walkBackAnim;
			npc.standAnimId = npc.desc.standAnim;
			npc.setPos(myPlayer.smallX[0] + i1, myPlayer.smallY[0] + l, j1 == 1);
		}
		stream.finishBitAccess();
	}

	public void processGameLoop() {
		if(rsAlreadyLoaded || loadingError || genericLoadingError)
			return;
		loopCycle++;
		if(!loggedIn)
			processLoginScreenInput();
		else
			mainGameProcessor();
		processOnDemandQueue();
	}

	private void renderPlayersOnScene(boolean flag) {
		if(myPlayer.x >> 7 == destX && myPlayer.y >> 7 == destY)
			destX = 0;
		int j = playerCount;
		if(flag)
			j = 1;
		for(int l = 0; l < j; l++) {
			Player player;
			int i1;
			if(flag) {
				player = myPlayer;
				i1 = myPlayerIndex << 14;
			} else {
				player = playerArray[playerIndices[l]];
				i1 = playerIndices[l] << 14;
			}
			if(player == null || !player.isVisible())
				continue;
			player.lowDetail = (lowMem && playerCount > 50 || playerCount > 200) && !flag && player.movementAnimId == player.standAnimId;
			int j1 = player.x >> 7;
			int k1 = player.y >> 7;
			if(j1 < 0 || j1 >= 104 || k1 < 0 || k1 >= 104)
				continue;
			if(player.attachedModel != null && loopCycle >= player.attachedModelStartCycle && loopCycle < player.attachedModelEndCycle) {
				player.lowDetail = false;
				player.attachedModelHeight = getTileHeight(plane, player.y, player.x);
				worldController.addTempObjectRect(plane, player.y, player, player.faceAngle, player.anInt1722, player.x, player.attachedModelHeight, player.anInt1719, player.anInt1721, i1, player.anInt1720);
				continue;
			}
			if((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
				if(constructRegionData[j1][k1] == hintIconY)
					continue;
				constructRegionData[j1][k1] = hintIconY;
			}
			player.attachedModelHeight = getTileHeight(plane, player.y, player.x);
			worldController.addTempObject(plane, player.faceAngle, player.attachedModelHeight, i1, player.y, 60, player.x, player, player.animStretches);
		}
	}

	private boolean promptUserForInput(RSInterface class9) {
		int j = class9.contentType;
		if(mapRegionCount == 2) {
			if(j == 201) {
				inputTaken = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 1;
				inputTitle = "Enter name of friend to add to list";
			}
			if(j == 202) {
				inputTaken = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 2;
				inputTitle = "Enter name of friend to delete from list";
			}
		}
		if(j == 205) {
			hintIconDelay = 250;
			return true;
		}
		if(j == 501) {
			inputTaken = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 4;
			inputTitle = "Enter name of player to add to list";
		}
		if(j == 502) {
			inputTaken = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 5;
			inputTitle = "Enter name of player to delete from list";
		}
		if(j == 550) {
			inputTaken = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 6;
			inputTitle = "Enter the name of the chat you wish to join";
		}
		if(j >= 300 && j <= 313) {
			int k = (j - 300) / 2;
			int j1 = j & 1;
			int i2 = menuActionTypes[k];
			if(i2 != -1) {
				do {
					if(j1 == 0 && --i2 < 0)
						i2 = IDK.length - 1;
					if(j1 == 1 && ++i2 >= IDK.length)
						i2 = 0;
				} while(IDK.cache[i2].aBoolean662 || IDK.cache[i2].bodyPartId != k + (aBoolean1047 ? 0 : 7));
				menuActionTypes[k] = i2;
				aBoolean1031 = true;
			}
		}
		if(j >= 314 && j <= 323) {
			int l = (j - 314) / 2;
			int k1 = j & 1;
			int j2 = walkingQueueY[l];
			if(k1 == 0 && --j2 < 0)
				j2 = anIntArrayArray1003[l].length - 1;
			if(k1 == 1 && ++j2 >= anIntArrayArray1003[l].length)
				j2 = 0;
			walkingQueueY[l] = j2;
			aBoolean1031 = true;
		}
		if(j == 324 && !aBoolean1047) {
			aBoolean1047 = true;
			resetDefaultAppearance();
		}
		if(j == 325 && aBoolean1047) {
			aBoolean1047 = false;
			resetDefaultAppearance();
		}
		if(j == 326) {
			stream.createFrame(101);
			stream.writeWordBigEndian(aBoolean1047 ? 0 : 1);
			for(int i1 = 0; i1 < 7; i1++)
				stream.writeWordBigEndian(menuActionTypes[i1]);

			for(int l1 = 0; l1 < 5; l1++)
				stream.writeWordBigEndian(walkingQueueY[l1]);

			return true;
		}
		if(j == 613)
			canMute = !canMute;
		if(j >= 601 && j <= 612) {
			clearTopInterfaces();
			if(reportAbuseInput.length() > 0) {
				stream.createFrame(218);
				stream.writeQWord(TextClass.longForName(reportAbuseInput));
				stream.writeWordBigEndian(j - 601);
				stream.writeWordBigEndian(canMute ? 1 : 0);
			}
		}
		return false;
	}

	private void parsePlayerUpdateMasks(Stream stream) {
		for(int j = 0; j < entityCount; j++) {
			int k = entityIndices[j];
			Player player = playerArray[k];
			int l = stream.readUnsignedByte();
			if((l & 0x40) != 0)
				l += stream.readUnsignedByte() << 8;
			parsePlayerMaskData(l, k, stream, player);
		}
	}

	private void drawMinimapWallObject(int i, int k, int l, int i1, int j1) {
		int k1 = worldController.getWallObjectUID(j1, l, i);
		if(k1 != 0) {
			int l1 = worldController.getObjectConfig(j1, l, i, k1);
			int k2 = l1 >> 6 & 3;
			int i3 = l1 & 0x1f;
			int k3 = k;
			if(k1 > 0)
				k3 = i1;
			int ai[] = minimapSprite.myPixels;
			int k4 = 24624 + l * 4 + (103 - i) * 512 * 4;
			int i5 = k1 >> 14 & 0x7fff;
			ObjectDef class46_2 = ObjectDef.forID(i5);
			if(class46_2.mapSceneId != -1) {
				Background background_2 = mapScenes[class46_2.mapSceneId];
				if(background_2 != null) {
					int i6 = (class46_2.sizeX * 4 - background_2.width) / 2;
					int j6 = (class46_2.sizeY * 4 - background_2.height) / 2;
					background_2.drawBackground(48 + l * 4 + i6, 48 + (104 - i - class46_2.sizeY) * 4 + j6);
				}
			} else {
				if(i3 == 0 || i3 == 2)
					if(k2 == 0) {
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else if(k2 == 1) {
						ai[k4] = k3;
						ai[k4 + 1] = k3;
						ai[k4 + 2] = k3;
						ai[k4 + 3] = k3;
					} else if(k2 == 2) {
						ai[k4 + 3] = k3;
						ai[k4 + 3 + 512] = k3;
						ai[k4 + 3 + 1024] = k3;
						ai[k4 + 3 + 1536] = k3;
					} else if(k2 == 3) {
						ai[k4 + 1536] = k3;
						ai[k4 + 1536 + 1] = k3;
						ai[k4 + 1536 + 2] = k3;
						ai[k4 + 1536 + 3] = k3;
					}
				if(i3 == 3)
					if(k2 == 0)
						ai[k4] = k3;
					else if(k2 == 1)
						ai[k4 + 3] = k3;
					else if(k2 == 2)
						ai[k4 + 3 + 1536] = k3;
					else if(k2 == 3)
						ai[k4 + 1536] = k3;
				if(i3 == 2)
					if(k2 == 3) {
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else if(k2 == 0) {
						ai[k4] = k3;
						ai[k4 + 1] = k3;
						ai[k4 + 2] = k3;
						ai[k4 + 3] = k3;
					} else if(k2 == 1) {
						ai[k4 + 3] = k3;
						ai[k4 + 3 + 512] = k3;
						ai[k4 + 3 + 1024] = k3;
						ai[k4 + 3 + 1536] = k3;
					} else if(k2 == 2) {
						ai[k4 + 1536] = k3;
						ai[k4 + 1536 + 1] = k3;
						ai[k4 + 1536 + 2] = k3;
						ai[k4 + 1536 + 3] = k3;
					}
			}
		}
		k1 = worldController.getInteractiveObjectUID(j1, l, i);
		if(k1 != 0) {
			int i2 = worldController.getObjectConfig(j1, l, i, k1);
			int l2 = i2 >> 6 & 3;
			int j3 = i2 & 0x1f;
			int l3 = k1 >> 14 & 0x7fff;
			ObjectDef class46_1 = ObjectDef.forID(l3);
			if(class46_1.mapSceneId != -1) {
				Background background_1 = mapScenes[class46_1.mapSceneId];
				if(background_1 != null) {
					int j5 = (class46_1.sizeX * 4 - background_1.width) / 2;
					int k5 = (class46_1.sizeY * 4 - background_1.height) / 2;
					background_1.drawBackground(48 + l * 4 + j5, 48 + (104 - i - class46_1.sizeY) * 4 + k5);
				}
			} else if(j3 == 9) {
				int l4 = 0xeeeeee;
				if(k1 > 0)
					l4 = 0xee0000;
				int ai1[] = minimapSprite.myPixels;
				int l5 = 24624 + l * 4 + (103 - i) * 512 * 4;
				if(l2 == 0 || l2 == 2) {
					ai1[l5 + 1536] = l4;
					ai1[l5 + 1024 + 1] = l4;
					ai1[l5 + 512 + 2] = l4;
					ai1[l5 + 3] = l4;
				} else {
					ai1[l5] = l4;
					ai1[l5 + 512 + 1] = l4;
					ai1[l5 + 1024 + 2] = l4;
					ai1[l5 + 1536 + 3] = l4;
				}
			}
		}
		k1 = worldController.getGroundDecorationUID(j1, l, i);
		if(k1 != 0) {
			int j2 = k1 >> 14 & 0x7fff;
			ObjectDef class46 = ObjectDef.forID(j2);
			if(class46.mapSceneId != -1) {
				Background background = mapScenes[class46.mapSceneId];
				if(background != null) {
					int i4 = (class46.sizeX * 4 - background.width) / 2;
					int j4 = (class46.sizeY * 4 - background.height) / 2;
					background.drawBackground(48 + l * 4 + i4, 48 + (104 - i - class46.sizeY) * 4 + j4);
				}
			}
		}
	}

	private void loadTitleScreen() {
		loginFireLeft = new Background(titleStreamLoader, "titlebox", 0);
		loginFireRight = new Background(titleStreamLoader, "titlebutton", 0);
		loginScreenSprites = new Background[12];
		int j = 0;
		try {
			j = 0; // fl_icon disabled
		} catch(Exception _ex) {
		}
		if(j == 0) {
			for(int k = 0; k < 12; k++)
				loginScreenSprites[k] = new Background(titleStreamLoader, "runes", k);

		} else {
			for(int l = 0; l < 12; l++)
				loginScreenSprites[l] = new Background(titleStreamLoader, "runes", 12 + (l & 3));

		}
		chatAreaBackground = new Sprite(128, 265);
		chatSettingsBackground = new Sprite(128, 265);
		System.arraycopy(chatAreaIP.pixelData, 0, chatAreaBackground.myPixels, 0, 33920);

		System.arraycopy(chatSettingIP.pixelData, 0, chatSettingsBackground.myPixels, 0, 33920);

		entityUpdateY = new int[256];
		for(int k1 = 0; k1 < 64; k1++)
			entityUpdateY[k1] = k1 * 0x40000;

		for(int l1 = 0; l1 < 64; l1++)
			entityUpdateY[l1 + 64] = 0xff0000 + 1024 * l1;

		for(int i2 = 0; i2 < 64; i2++)
			entityUpdateY[i2 + 128] = 0xffff00 + 4 * i2;

		for(int j2 = 0; j2 < 64; j2++)
			entityUpdateY[j2 + 192] = 0xffffff;

		entityUpdateId = new int[256];
		for(int k2 = 0; k2 < 64; k2++)
			entityUpdateId[k2] = k2 * 1024;

		for(int l2 = 0; l2 < 64; l2++)
			entityUpdateId[l2 + 64] = 65280 + 4 * l2;

		for(int i3 = 0; i3 < 64; i3++)
			entityUpdateId[i3 + 128] = 65535 + 0x40000 * i3;

		for(int j3 = 0; j3 < 64; j3++)
			entityUpdateId[j3 + 192] = 0xffffff;

		entityUpdateFace = new int[256];
		for(int k3 = 0; k3 < 64; k3++)
			entityUpdateFace[k3] = k3 * 4;

		for(int l3 = 0; l3 < 64; l3++)
			entityUpdateFace[l3 + 64] = 255 + 0x40000 * l3;

		for(int i4 = 0; i4 < 64; i4++)
			entityUpdateFace[i4 + 128] = 0xff00ff + 1024 * i4;

		for(int j4 = 0; j4 < 64; j4++)
			entityUpdateFace[j4 + 192] = 0xffffff;

		entityUpdateX = new int[256];
		chatScrollPositions = new int[32768];
		chatHighlights = new int[32768];
		randomizeBackground(null);
		npcUpdateTypes = new int[32768];
		npcLocalIndices = new int[32768];
		drawLoadingText(10, "Connecting to fileserver");
		if(!midiFading) {
			drawFlames = true;
			midiFading = true;
			startRunnable(this, 2);
		}
	}

	private static void setHighMem() {
		WorldController.lowMem = false;
		Texture.lowMem = false;
		lowMem = false;
		ObjectManager.lowMem = false;
		ObjectDef.lowMem = false;
	}

	public static void main(String args[]) {
		try {
			nodeID = 10;
			portOff = 0;
			setHighMem();
			isMembers = true;
			signlink.storeid = 32;
			server = System.getProperty("openrune.server", "127.0.0.1");
			String portStr = System.getProperty("openrune.port", "43594");
			System.out.println("OpenRune Client - connecting to " + server + ":" + portStr);
			signlink.startpriv(InetAddress.getByName(server));
			new Jframe(args);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	private void loadingStages() {
		if(lowMem && loadingStage == 2 && ObjectManager.currentPlane != plane) {
			loginMsgIP.initDrawingArea();
			boldFont.drawText(0, "Loading - please wait.", 151, 257);
			boldFont.drawText(0xffffff, "Loading - please wait.", 150, 256);
			loginMsgIP.drawGraphics(clientSize == 0 ? 4 : 0, super.graphics, clientSize == 0 ? 4 : 0);
			loadingStage = 1;
			serverSeed = System.currentTimeMillis();
		}
		if(loadingStage == 1) {
			int j = checkMapLoadStatus();
			if(j != 0 && System.currentTimeMillis() - serverSeed > 0x57e40L) {
				signlink.reporterror(myUsername + " glcfb " + chatLastTyped + "," + j + "," + lowMem + "," + decompressors[0] + "," + onDemandFetcher.getNodeCount() + "," + plane + "," + mapRegionX + "," + mapRegionY);
				serverSeed = System.currentTimeMillis();
			}
		}
		if(loadingStage == 2 && plane != activeInterfaceId) {
			activeInterfaceId = plane;
			drawMiniMapDots(plane);
		}
	}

	private int checkMapLoadStatus() {
		for(int i = 0; i < mapLandscapeData.length; i++) {
			if(mapLandscapeData[i] == null && chatFilterNames[i] != -1)
				return -1;
			if(mapObjectData[i] == null && chatFilterMessages[i] != -1)
				return -2;
		}
		boolean flag = true;
		for(int j = 0; j < mapLandscapeData.length; j++) {
			byte abyte0[] = mapObjectData[j];
			if(abyte0 != null) {
				int k = (chatFilterTypes[j] >> 8) * 64 - baseX;
				int l = (chatFilterTypes[j] & 0xff) * 64 - baseY;
				if(aBoolean1159) {
					k = 10;
					l = 10;
				}
				flag &= ObjectManager.checkObjectData(k, abyte0, l);
			}
		}
		if(!flag)
			return -3;
		if(aBoolean1080) {
			return -4;
		} else {
			loadingStage = 2;
			ObjectManager.currentPlane = plane;
			resetScene();
			stream.createFrame(121);
			return 0;
		}
	}

	private void processProjectiles()
	{
		for(Animable_Sub4 class30_sub2_sub4_sub4 = (Animable_Sub4)projectileList.reverseGetFirst(); class30_sub2_sub4_sub4 != null; class30_sub2_sub4_sub4 = (Animable_Sub4)projectileList.reverseGetNext())
			if(class30_sub2_sub4_sub4.sourceEntityIndex != plane || loopCycle > class30_sub2_sub4_sub4.endCycle)
				class30_sub2_sub4_sub4.unlink();
			else
			if(loopCycle >= class30_sub2_sub4_sub4.startCycle)
			{
				if(class30_sub2_sub4_sub4.targetLocSize > 0)
				{
					NPC npc = npcArray[class30_sub2_sub4_sub4.targetLocSize - 1];
					if(npc != null && npc.x >= 0 && npc.x < 13312 && npc.y >= 0 && npc.y < 13312)
						class30_sub2_sub4_sub4.trackTarget(loopCycle, npc.y, getTileHeight(class30_sub2_sub4_sub4.sourceEntityIndex, npc.y, npc.x) - class30_sub2_sub4_sub4.targetEntityIndex, npc.x);
				}
				if(class30_sub2_sub4_sub4.targetLocSize < 0)
				{
					int j = -class30_sub2_sub4_sub4.targetLocSize - 1;
					Player player;
					if(j == unknownInt10)
						player = myPlayer;
					else
						player = playerArray[j];
					if(player != null && player.x >= 0 && player.x < 13312 && player.y >= 0 && player.y < 13312)
						class30_sub2_sub4_sub4.trackTarget(loopCycle, player.y, getTileHeight(class30_sub2_sub4_sub4.sourceEntityIndex, player.y, player.x) - class30_sub2_sub4_sub4.targetEntityIndex, player.x);
				}
				class30_sub2_sub4_sub4.advanceProjectile(cameraTargetLocalZ);
				worldController.addTempObject(plane, class30_sub2_sub4_sub4.yawAngle, (int)class30_sub2_sub4_sub4.currentZ, -1, (int)class30_sub2_sub4_sub4.currentY, 60, (int)class30_sub2_sub4_sub4.currentX, class30_sub2_sub4_sub4, false);
			}

	}

	// Applet removed for Java 21

	private void drawLogo() {
		byte abyte0[] = titleStreamLoader.getDataForName("title.dat");
		Sprite sprite = new Sprite(abyte0, this);
		chatAreaIP.initDrawingArea();
		sprite.drawTransparent(0, 0);
		chatSettingIP.initDrawingArea();
		sprite.drawTransparent(-637, 0);
		tabImageProducer.initDrawingArea();
		sprite.drawTransparent(-128, 0);
		mapAreaIP.initDrawingArea();
		sprite.drawTransparent(-202, -371);
		gameScreenIP.initDrawingArea();
		sprite.drawTransparent(-202, -171);
		topSideIP1.initDrawingArea();
		sprite.drawTransparent(0, -265);
		topSideIP2.initDrawingArea();
		sprite.drawTransparent(-562, -265);
		bottomSideIP1.initDrawingArea();
		sprite.drawTransparent(-128, -171);
		bottomSideIP2.initDrawingArea();
		sprite.drawTransparent(-562, -171);
		int ai[] = new int[sprite.myWidth];
		for(int j = 0; j < sprite.myHeight; j++) {
			for(int k = 0; k < sprite.myWidth; k++)
				ai[k] = sprite.myPixels[(sprite.myWidth - k - 1) + sprite.myWidth * j];

			System.arraycopy(ai, 0, sprite.myPixels, sprite.myWidth * j, sprite.myWidth);
		}
		chatAreaIP.initDrawingArea();
		sprite.drawTransparent(382, 0);
		chatSettingIP.initDrawingArea();
		sprite.drawTransparent(-255, 0);
		tabImageProducer.initDrawingArea();
		sprite.drawTransparent(254, 0);
		mapAreaIP.initDrawingArea();
		sprite.drawTransparent(180, -371);
		gameScreenIP.initDrawingArea();
		sprite.drawTransparent(180, -171);
		topSideIP1.initDrawingArea();
		sprite.drawTransparent(382, -265);
		topSideIP2.initDrawingArea();
		sprite.drawTransparent(-180, -265);
		bottomSideIP1.initDrawingArea();
		sprite.drawTransparent(254, -171);
		bottomSideIP2.initDrawingArea();
		sprite.drawTransparent(-180, -171);
		sprite = new Sprite(titleStreamLoader, "logo", 0);
		tabImageProducer.initDrawingArea();
		sprite.drawSprite(382 - sprite.myWidth / 2 - 128, 18);
		sprite = null;
		Object obj = null;
		Object obj1 = null;
		System.gc();
	}

	private void processOnDemandQueue()
	{
		do
		{
			OnDemandData onDemandData;
			do
			{
				onDemandData = onDemandFetcher.getNextNode();
				if(onDemandData == null)
					return;
				if(onDemandData.dataType == 0)
				{
					Model.decodeModelHeader(onDemandData.buffer, onDemandData.ID);
					if((onDemandFetcher.getModelIndex(onDemandData.ID) & 0x62) != 0)
					{
						needDrawTabArea = true;
						if(backDialogID != -1)
							inputTaken = true;
					}
				}
				if(onDemandData.dataType == 1 && onDemandData.buffer != null)
					AnimFrame.decodeFrames(onDemandData.buffer);
				if(onDemandData.dataType == 2 && onDemandData.ID == nextSong && onDemandData.buffer != null)
					saveMidi(songChanging, onDemandData.buffer);
				if(onDemandData.dataType == 3 && loadingStage == 1)
				{
					for(int i = 0; i < mapLandscapeData.length; i++)
					{
						if(chatFilterNames[i] == onDemandData.ID)
						{
							mapLandscapeData[i] = onDemandData.buffer;
							if(onDemandData.buffer == null)
								chatFilterNames[i] = -1;
							break;
						}
						if(chatFilterMessages[i] != onDemandData.ID)
							continue;
						mapObjectData[i] = onDemandData.buffer;
						if(onDemandData.buffer == null)
							chatFilterMessages[i] = -1;
						break;
					}

				}
			} while(onDemandData.dataType != 93 || !onDemandFetcher.isMapObjectFile(onDemandData.ID));
			ObjectManager.preloadObjectModels(new Stream(onDemandData.buffer), onDemandFetcher);
		} while(true);
	}

	private void calcFlamesPosition()
	{
		char c = '\u0100';
		for(int j = 10; j < 117; j++)
		{
			int k = (int)(Math.random() * 100D);
			if(k < 50)
				npcUpdateTypes[j + (c - 2 << 7)] = 255;
		}
		for(int l = 0; l < 100; l++)
		{
			int i1 = (int)(Math.random() * 124D) + 2;
			int k1 = (int)(Math.random() * 128D) + 128;
			int k2 = i1 + (k1 << 7);
			npcUpdateTypes[k2] = 192;
		}

		for(int j1 = 1; j1 < c - 1; j1++)
		{
			for(int l1 = 1; l1 < 127; l1++)
			{
				int l2 = l1 + (j1 << 7);
				npcLocalIndices[l2] = (npcUpdateTypes[l2 - 1] + npcUpdateTypes[l2 + 1] + npcUpdateTypes[l2 - 128] + npcUpdateTypes[l2 + 128]) / 4;
			}

		}

		anInt1275 += 128;
		if(anInt1275 > chatScrollPositions.length)
		{
			anInt1275 -= chatScrollPositions.length;
			int i2 = (int)(Math.random() * 12D);
			randomizeBackground(loginScreenSprites[i2]);
		}
		for(int j2 = 1; j2 < c - 1; j2++)
		{
			for(int i3 = 1; i3 < 127; i3++)
			{
				int k3 = i3 + (j2 << 7);
				int i4 = npcLocalIndices[k3 + 128] - chatScrollPositions[k3 + anInt1275 & chatScrollPositions.length - 1] / 5;
				if(i4 < 0)
					i4 = 0;
				npcUpdateTypes[k3] = i4;
			}

		}

		System.arraycopy(flameRightX, 1, flameRightX, 0, c - 1);

		flameRightX[c - 1] = (int)(Math.sin((double)loopCycle / 14D) * 16D + Math.sin((double)loopCycle / 15D) * 14D + Math.sin((double)loopCycle / 16D) * 12D);
		if(walkDestX > 0)
			walkDestX -= 4;
		if(walkDestY > 0)
			walkDestY -= 4;
		if(walkDestX == 0 && walkDestY == 0)
		{
			int l3 = (int)(Math.random() * 2000D);
			if(l3 == 0)
				walkDestX = 1024;
			if(l3 == 1)
				walkDestY = 1024;
		}
	}

	private boolean saveWave(byte abyte0[], int i)
	{
		return abyte0 == null || signlink.wavesave(abyte0, i);
	}

	private void resetInterfaceAnim(int i)
	{
		RSInterface class9 = RSInterface.interfaceCache[i];
		for(int j = 0; j < class9.children.length; j++)
		{
			if(class9.children[j] == -1)
				break;
			if(class9.children[j] < 0 || class9.children[j] >= RSInterface.interfaceCache.length) continue;
			RSInterface class9_1 = RSInterface.interfaceCache[class9.children[j]];
			if(class9_1 == null) continue;
			if(class9_1.type == 1)
				resetInterfaceAnim(class9_1.id);
			class9_1.enabledSpriteId = 0;
			class9_1.animationId = 0;
		}
	}

	private void drawHeadIcon()
	{
		if(minimapRotation != 2)
			return;
		calcEntityScreenPos((cameraTargetTileX - baseX << 7) + cameraTargetLocalX, cameraTargetHeight * 2, (cameraTargetTileY - baseY << 7) + cameraTargetLocalY);
		if(spriteDrawX > -1 && loopCycle % 20 < 10)
			headIconsHint[0].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
	}

	private void mainGameProcessor()
	{
		if(anInt1104 > 1)
			anInt1104--;
		if(hintIconDelay > 0)
			hintIconDelay--;
		for(int j = 0; j < 5; j++)
			if(!parsePacket())
				break;

		if(!loggedIn)
			return;
			try{
			 canWalkDelay--;
        if (followNPC > 0)
        {
            NPC n = npcArray[followNPC];
            if (n != null)
            {
                if (!withinDistance(myPlayer.smallX[0], myPlayer.smallY[0], n.smallX[0], n.smallY[0], followDistance))
                {
                    doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, n.smallY[0], myPlayer.smallX[0], false, n.smallX[0]);
                }
            }
        }
        else if (followPlayer > 0 && canWalkDelay <= 0)
        {
            Player p = playerArray[followPlayer];
            if (p != null)
            {
                int dis = getDis(myPlayer.smallX[0], myPlayer.smallY[0], p.smallX[0], p.smallY[0]);
                if (dis > followDistance)
                {
                    doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], followDistance, 0, p.smallY[0], myPlayer.smallX[0], false, p.smallX[0]);
                    canWalkDelay = 30;
                }
                else if (dis == 0)
                {
                    int rnd = random(4);
                    if (rnd == 0)
                    {
                        doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, p.smallY[0], myPlayer.smallX[0], false, p.smallX[0] - 2);
                    }
                    else if (rnd == 1)
                    {
                        doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, p.smallY[0], myPlayer.smallX[0], false, p.smallX[0] + 2);
                    }
                    else if (rnd == 2)
                    {
                        doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, p.smallY[0] - 2, myPlayer.smallX[0], false, p.smallX[0]);
                    }
                    else if (rnd == 3)
                    {
                        doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, p.smallY[0] + 2, myPlayer.smallX[0], false, p.smallX[0]);
                    }
                    canWalkDelay = 60;
                }
            }
        }
		} catch(Exception ex){
		System.out.println("Exception in following, method : in process.)");
		}
		synchronized(mouseDetection.syncObject)
		{
			if(flagged)
			{
				if(super.clickMode3 != 0 || mouseDetection.coordsIndex >= 40)
				{
					stream.createFrame(45);
					stream.writeWordBigEndian(0);
					int j2 = stream.currentOffset;
					int j3 = 0;
					for(int j4 = 0; j4 < mouseDetection.coordsIndex; j4++)
					{
						if(j2 - stream.currentOffset >= 240)
							break;
						j3++;
						int l4 = mouseDetection.coordsY[j4];
						if(l4 < 0)
							l4 = 0;
						else
						if(l4 > 502)
							l4 = 502;
						int k5 = mouseDetection.coordsX[j4];
						if(k5 < 0)
							k5 = 0;
						else
						if(k5 > 764)
							k5 = 764;
						int i6 = l4 * 765 + k5;
						if(mouseDetection.coordsY[j4] == -1 && mouseDetection.coordsX[j4] == -1)
						{
							k5 = -1;
							l4 = -1;
							i6 = 0x7ffff;
						}
						if(k5 == mapLoadProgress && l4 == mapLoadState)
						{
							if(chatAreaScrollMax < 2047)
								chatAreaScrollMax++;
						} else
						{
							int j6 = k5 - mapLoadProgress;
							mapLoadProgress = k5;
							int k6 = l4 - mapLoadState;
							mapLoadState = l4;
							if(chatAreaScrollMax < 8 && j6 >= -32 && j6 <= 31 && k6 >= -32 && k6 <= 31)
							{
								j6 += 32;
								k6 += 32;
								stream.writeWord((chatAreaScrollMax << 12) + (j6 << 6) + k6);
								chatAreaScrollMax = 0;
							} else
							if(chatAreaScrollMax < 8)
							{
								stream.writeDWordBigEndian(0x800000 + (chatAreaScrollMax << 19) + i6);
								chatAreaScrollMax = 0;
							} else
							{
								stream.writeDWord(0xc0000000 + (chatAreaScrollMax << 19) + i6);
								chatAreaScrollMax = 0;
							}
						}
					}

					stream.writeBytes(stream.currentOffset - j2);
					if(j3 >= mouseDetection.coordsIndex)
					{
						mouseDetection.coordsIndex = 0;
					} else
					{
						mouseDetection.coordsIndex -= j3;
						for(int i5 = 0; i5 < mouseDetection.coordsIndex; i5++)
						{
							mouseDetection.coordsX[i5] = mouseDetection.coordsX[i5 + j3];
							mouseDetection.coordsY[i5] = mouseDetection.coordsY[i5 + j3];
						}

					}
				}
			} else
			{
				mouseDetection.coordsIndex = 0;
			}
		}
		if(super.clickMode3 != 0)
		{
			long l = (super.aLong29 - aLong1220) / 50L;
			if(l > 4095L)
				l = 4095L;
			aLong1220 = super.aLong29;
			int k2 = super.saveClickY;
			if(k2 < 0)
				k2 = 0;
			else
			if(k2 > 502)
				k2 = 502;
			int k3 = super.saveClickX;
			if(k3 < 0)
				k3 = 0;
			else
			if(k3 > 764)
				k3 = 764;
			int k4 = k2 * 765 + k3;
			int j5 = 0;
			if(super.clickMode3 == 2)
				j5 = 1;
			int l5 = (int)l;
			stream.createFrame(241);
			stream.writeDWord((l5 << 20) + (j5 << 19) + k4);
		}
		if(songSwitchDelay > 0)
			songSwitchDelay--;
		if(super.keyArray[1] == 1 || super.keyArray[2] == 1 || super.keyArray[3] == 1 || super.keyArray[4] == 1)
			songSwitching = true;
		if(songSwitching && songSwitchDelay <= 0)
		{
			songSwitchDelay = 20;
			songSwitching = false;
			stream.createFrame(86);
			stream.writeWord(selectedArea);
			stream.writeWordBigA(minimapInt1);
		}
		if(super.awtFocus && !scrollBarDrag)
		{
			scrollBarDrag = true;
			stream.createFrame(3);
			stream.writeWordBigEndian(1);
		}
		if(!super.awtFocus && scrollBarDrag)
		{
			scrollBarDrag = false;
			stream.createFrame(3);
			stream.writeWordBigEndian(0);
		}
		loadingStages();
		processSpawnObjects();
		processSounds();
		idleTime++;
		if(idleTime > 750)
			dropClient();
		processPlayerMovement();
		processNPCMovement();
		processPlayerChat();
		cameraTargetLocalZ++;
		if(crossType != 0)
		{
			crossIndex += 20;
			if(crossIndex >= 400)
				crossType = 0;
		}
		if(atInventoryInterfaceType != 0)
		{
			atInventoryLoopCycle++;
			if(atInventoryLoopCycle >= 15)
			{
				if(atInventoryInterfaceType == 2)
					needDrawTabArea = true;
				if(atInventoryInterfaceType == 3)
					inputTaken = true;
				atInventoryInterfaceType = 0;
			}
		}
		if(activeInterfaceType != 0)
		{
			moveItemInterfaceId++;
			if(super.mouseX > dragStartX + 5 || super.mouseX < dragStartX - 5 || super.mouseY > dragStartY + 5 || super.mouseY < dragStartY - 5)
				aBoolean1242 = true;
			if(super.clickMode2 == 0)
			{
				if(activeInterfaceType == 2)
					needDrawTabArea = true;
				if(activeInterfaceType == 3)
					inputTaken = true;
				activeInterfaceType = 0;
				if(aBoolean1242 && moveItemInterfaceId >= 10)
				{
					lastActiveInvInterface = -1;
					processRightClick();
					if(lastActiveInvInterface == dragFromSlotInterface && mouseInvInterfaceIndex != dragFromSlot)
					{
						RSInterface class9 = RSInterface.interfaceCache[dragFromSlotInterface];
						int j1 = 0;
						if(terrainDataIndex == 1 && class9.contentType == 206)
							j1 = 1;
						if(class9.inv[mouseInvInterfaceIndex] <= 0)
							j1 = 0;
						if(class9.filled)
						{
							int l2 = dragFromSlot;
							int l3 = mouseInvInterfaceIndex;
							class9.inv[l3] = class9.inv[l2];
							class9.invStackSizes[l3] = class9.invStackSizes[l2];
							class9.inv[l2] = -1;
							class9.invStackSizes[l2] = 0;
						} else
						if(j1 == 1)
						{
							int i3 = dragFromSlot;
							for(int i4 = mouseInvInterfaceIndex; i3 != i4;)
								if(i3 > i4)
								{
									class9.swapInventoryItems(i3, i3 - 1);
									i3--;
								} else
								if(i3 < i4)
								{
									class9.swapInventoryItems(i3, i3 + 1);
									i3++;
								}

						} else
						{
							class9.swapInventoryItems(dragFromSlot, mouseInvInterfaceIndex);
						}
						stream.createFrame(214);
						stream.writeWordLEBigA(dragFromSlotInterface);
						stream.writeNegByte(j1);
						stream.writeWordLEBigA(dragFromSlot);
						stream.writeWordLEA(mouseInvInterfaceIndex);
					}
				} else
				if((clickMode == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
					determineMenuSize();
				else
				if(menuActionRow > 0)
					doAction(menuActionRow - 1);
				atInventoryLoopCycle = 10;
				super.clickMode3 = 0;
			}
		}
		if(WorldController.clickedTileX != -1)
		{
			int k = WorldController.clickedTileX;
			int k1 = WorldController.clickedTileY;
			boolean flag = doWalkTo(0, 0, 0, 0, myPlayer.smallY[0], 0, 0, k1, myPlayer.smallX[0], true, k);
			WorldController.clickedTileX = -1;
			if(flag)
			{
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 1;
				crossIndex = 0;
			}
		}
		if(super.clickMode3 == 1 && clickToContinueString != null)
		{
			clickToContinueString = null;
			inputTaken = true;
			super.clickMode3 = 0;
		}
		processMenuClick();
		processMainScreenClick();
		processTabClick();
		processChatModeClick();
		if(super.clickMode2 == 1 || super.clickMode3 == 1)
			menuActionCounter++;
		if (anInt1500 != 0 || anInt1044 != 0 || anInt1129 != 0) {
			if (anInt1501 < 100) {
				anInt1501++;
				if (anInt1501 == 100) {
					if (anInt1500 != 0) {
						inputTaken = true;
					}
					if (anInt1044 != 0) {
						needDrawTabArea = true;
					}
				}
			}
		} else if (anInt1501 > 0) {
			anInt1501--;
		}
		if(loadingStage == 2)
			updateCamera();
		if(loadingStage == 2 && aBoolean1160)
			calcCameraPos();
		for(int i1 = 0; i1 < 5; i1++)
			chatRights[i1]++;

		processKeyInput();
		super.idleTime++;
		if(super.idleTime > 4500)
		{
			hintIconDelay = 250;
			super.idleTime -= 500;
			stream.createFrame(202);
		}
		moveItemSlotEnd++;
		if(moveItemSlotEnd > 500)
		{
			moveItemSlotEnd = 0;
			int l1 = (int)(Math.random() * 8D);
			if((l1 & 1) == 1)
				anInt1278 += anInt1279;
			if((l1 & 2) == 2)
				cameraOscillationH += cameraOscillationSpeed;
			if((l1 & 4) == 4)
				lastItemSelectedInterface += lastChatId;
		}
		if(anInt1278 < -50)
			anInt1279 = 2;
		if(anInt1278 > 50)
			anInt1279 = -2;
		if(cameraOscillationH < -55)
			cameraOscillationSpeed = 2;
		if(cameraOscillationH > 55)
			cameraOscillationSpeed = -2;
		if(lastItemSelectedInterface < -40)
			lastChatId = 1;
		if(lastItemSelectedInterface > 40)
			lastChatId = -1;
		anInt1254++;
		if(anInt1254 > 500)
		{
			anInt1254 = 0;
			int i2 = (int)(Math.random() * 8D);
			if((i2 & 1) == 1)
				minimapInt2 += minimapRotationDelta;
			if((i2 & 2) == 2)
				minimapInt3 += minimapZoomDelta;
		}
		if(minimapInt2 < -60)
			minimapRotationDelta = 2;
		if(minimapInt2 > 60)
			minimapRotationDelta = -2;
		if(minimapInt3 < -20)
			minimapZoomDelta = 1;
		if(minimapInt3 > 10)
			minimapZoomDelta = -1;
		idleLogout++;
		if(idleLogout > 50)
			stream.createFrame(0);
		try
		{
			if(socketStream != null && stream.currentOffset > 0)
			{
				socketStream.queueBytes(stream.currentOffset, stream.buffer);
				stream.currentOffset = 0;
				idleLogout = 0;
			}
		}
		catch(IOException _ex)
		{
			dropClient();
		}
		catch(Exception exception)
		{
			resetLogout();
		}
	}

	private void resetSpawnObjects()
	{
		SpawnObjectNode spawnObjectNode = (SpawnObjectNode)spawnObjectList.reverseGetFirst();
		for(; spawnObjectNode != null; spawnObjectNode = (SpawnObjectNode)spawnObjectList.reverseGetNext())
			if(spawnObjectNode.delay == -1)
			{
				spawnObjectNode.longestDelay = 0;
				updateSpawnObjectInfo(spawnObjectNode);
			} else
			{
				spawnObjectNode.unlink();
			}

	}

	private void resetImageProducers()
	{
		if(tabImageProducer != null)
			return;
		super.fullGameScreen = null;
		topCenterIP = null;
		titleButtonIP = null;
		titleMuralIP = null;
		loginMsgIP = null;
		titleIP1 = null;
		titleIP2 = null;
		titleIP3 = null;
		chatAreaIP = new RSImageProducer(128, 265, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		chatSettingIP = new RSImageProducer(128, 265, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		tabImageProducer = new RSImageProducer(509, 171, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		mapAreaIP = new RSImageProducer(360, 132, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		gameScreenIP = new RSImageProducer(360, 200, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		topSideIP1 = new RSImageProducer(202, 238, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		topSideIP2 = new RSImageProducer(203, 238, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		bottomSideIP1 = new RSImageProducer(74, 94, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		bottomSideIP2 = new RSImageProducer(75, 94, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		if(titleStreamLoader != null)
		{
			drawLogo();
			loadTitleScreen();
		}
		welcomeScreenRaised = true;
	}

	void drawLoadingText(int i, String s)
	{
		anInt1079 = i;
		hintText = s;
		resetImageProducers();
		if(titleStreamLoader == null)
		{
			super.drawLoadingText(i, s);
			return;
		}
		gameScreenIP.initDrawingArea();
		char c = '\u0168';
		char c1 = '\310';
		byte byte1 = 20;
		chatTextDrawingArea.drawText(0xffffff, "RuneScape is loading - please wait...", c1 / 2 - 26 - byte1, c / 2);
		int j = c1 / 2 - 18 - byte1;
		DrawingArea.fillPixels(c / 2 - 152, 304, 34, 0x8c1111, j);
		DrawingArea.fillPixels(c / 2 - 151, 302, 32, 0, j + 1);
		DrawingArea.drawPixels(30, j + 2, c / 2 - 150, 0x8c1111, i * 3);
		DrawingArea.drawPixels(30, j + 2, (c / 2 - 150) + i * 3, 0, 300 - i * 3);
		chatTextDrawingArea.drawText(0xffffff, s, (c1 / 2 + 5) - byte1, c / 2);
		gameScreenIP.drawGraphics(171, super.graphics, 202);
		if(welcomeScreenRaised)
		{
			welcomeScreenRaised = false;
			if(!midiFading)
			{
				chatAreaIP.drawGraphics(0, super.graphics, 0);
				chatSettingIP.drawGraphics(0, super.graphics, 637);
			}
			tabImageProducer.drawGraphics(0, super.graphics, 128);
			mapAreaIP.drawGraphics(371, super.graphics, 202);
			topSideIP1.drawGraphics(265, super.graphics, 0);
			topSideIP2.drawGraphics(265, super.graphics, 562);
			bottomSideIP1.drawGraphics(171, super.graphics, 128);
			bottomSideIP2.drawGraphics(171, super.graphics, 562);
		}
	}

	private void processScrollbar(int i, int j, int k, int l, RSInterface class9, int i1, boolean flag,
						  int j1)
	{
		int tabToReplyPM;
		if(aBoolean972)
			tabToReplyPM = 32;
		else
			tabToReplyPM = 0;
		aBoolean972 = false;
		if(k >= i && k < i + 16 && l >= i1 && l < i1 + 16)
		{
			class9.scrollPosition -= menuActionCounter * 4;
			if(flag)
			{
				needDrawTabArea = true;
			}
		} else
		if(k >= i && k < i + 16 && l >= (i1 + j) - 16 && l < i1 + j)
		{
			class9.scrollPosition += menuActionCounter * 4;
			if(flag)
			{
				needDrawTabArea = true;
			}
		} else
		if(k >= i - tabToReplyPM && k < i + 16 + tabToReplyPM && l >= i1 + 16 && l < (i1 + j) - 16 && menuActionCounter > 0)
		{
			int l1 = ((j - 32) * j) / j1;
			if(l1 < 8)
				l1 = 8;
			int i2 = l - i1 - 16 - l1 / 2;
			int j2 = j - 32 - l1;
			class9.scrollPosition = ((j1 - j) * i2) / j2;
			if(flag)
				needDrawTabArea = true;
			aBoolean972 = true;
		}
	}

	private boolean objectActionAtTile(int i, int j, int k)
	{
		int i1 = i >> 14 & 0x7fff;
		int j1 = worldController.getObjectConfig(plane, k, j, i);
		if(j1 == -1)
			return false;
		int k1 = j1 & 0x1f;
		int l1 = j1 >> 6 & 3;
		if(k1 == 10 || k1 == 11 || k1 == 22)
		{
			ObjectDef class46 = ObjectDef.forID(i1);
			int i2;
			int j2;
			if(l1 == 0 || l1 == 2)
			{
				i2 = class46.sizeX;
				j2 = class46.sizeY;
			} else
			{
				i2 = class46.sizeY;
				j2 = class46.sizeX;
			}
			int k2 = class46.surroundings;
			if(l1 != 0)
				k2 = (k2 << l1 & 0xf) + (k2 >> 4 - l1);
			doWalkTo(2, 0, j2, 0, myPlayer.smallY[0], i2, k2, j, myPlayer.smallX[0], false, k);
		} else
		{
			doWalkTo(2, l1, 0, k1 + 1, myPlayer.smallY[0], 0, 0, j, myPlayer.smallX[0], false, k);
		}
		crossX = super.saveClickX;
		crossY = super.saveClickY;
		crossType = 2;
		crossIndex = 0;
		return true;
	}

	private StreamLoader streamLoaderForName(int i, String s, String s1, int j, int k)
	{
		byte abyte0[] = null;
		int l = 5;
		try
		{
			if(decompressors[0] != null)
				abyte0 = decompressors[0].decompress(i);
		}
		catch(Exception _ex) { }
		if(abyte0 != null)
		{
	//		aCRC32_930.reset();
	//		aCRC32_930.update(abyte0);
	//		int i1 = (int)aCRC32_930.getValue();
	//		if(i1 != j)
		}
		if(abyte0 != null)
		{
			StreamLoader streamLoader = new StreamLoader(abyte0);
			return streamLoader;
		}
		int j1 = 0;
		while(abyte0 == null)
		{
			String s2 = "Unknown error";
			drawLoadingText(k, "Requesting " + s);
			Object obj = null;
			try
			{
				int k1 = 0;
				DataInputStream datainputstream = openJagGrabInputStream(s1 + j);
				byte abyte1[] = new byte[6];
				datainputstream.readFully(abyte1, 0, 6);
				Stream stream = new Stream(abyte1);
				stream.currentOffset = 3;
				int i2 = stream.read3Bytes() + 6;
				int j2 = 6;
				abyte0 = new byte[i2];
				System.arraycopy(abyte1, 0, abyte0, 0, 6);

				while(j2 < i2) 
				{
					int l2 = i2 - j2;
					if(l2 > 1000)
						l2 = 1000;
					int j3 = datainputstream.read(abyte0, j2, l2);
					if(j3 < 0)
					{
						s2 = "Length error: " + j2 + "/" + i2;
						throw new IOException("EOF");
					}
					j2 += j3;
					int k3 = (j2 * 100) / i2;
					if(k3 != k1)
						drawLoadingText(k, "Loading " + s + " - " + k3 + "%");
					k1 = k3;
				}
				datainputstream.close();
				try
				{
					if(decompressors[0] != null)
						decompressors[0].readCacheData(abyte0.length, abyte0, i);
				}
				catch(Exception _ex)
				{
					decompressors[0] = null;
				}
   /*			 if(abyte0 != null)
				{
					aCRC32_930.reset();
					aCRC32_930.update(abyte0);
					int i3 = (int)aCRC32_930.getValue();
					if(i3 != j)
					{
						abyte0 = null;
						j1++;
						s2 = "Checksum error: " + i3;
					}
				}
  */
			}
			catch(IOException ioexception)
			{
				if(s2.equals("Unknown error"))
					s2 = "Connection error";
				abyte0 = null;
			}
			catch(NullPointerException _ex)
			{
				s2 = "Null error";
				abyte0 = null;
				if(!signlink.reporterror)
					return null;
			}
			catch(ArrayIndexOutOfBoundsException _ex)
			{
				s2 = "Bounds error";
				abyte0 = null;
				if(!signlink.reporterror)
					return null;
			}
			catch(Exception _ex)
			{
				s2 = "Unexpected error";
				abyte0 = null;
				if(!signlink.reporterror)
					return null;
			}
			if(abyte0 == null)
			{
				for(int l1 = l; l1 > 0; l1--)
				{
					if(j1 >= 3)
					{
						drawLoadingText(k, "Game updated - please reload page");
						l1 = 10;
					} else
					{
						drawLoadingText(k, s2 + " - Retrying in " + l1);
					}
					try
					{
						Thread.sleep(1000L);
					}
					catch(Exception _ex) { }
				}

				l *= 2;
				if(l > 60)
					l = 60;
				continuedDialogue = !continuedDialogue;
			}

		}

		StreamLoader streamLoader_1 = new StreamLoader(abyte0);
			return streamLoader_1;
	}

	private void dropClient()
	{
		if(hintIconDelay > 0)
		{
			resetLogout();
			return;
		}
		loginMsgIP.initDrawingArea();
		boldFont.drawText(0, "Connection lost", 144, 257);
		boldFont.drawText(0xffffff, "Connection lost", 143, 256);
		boldFont.drawText(0, "Please wait - attempting to reestablish", 159, 257);
		boldFont.drawText(0xffffff, "Please wait - attempting to reestablish", 158, 256);
		loginMsgIP.drawGraphics(clientSize == 0 ? 4 : 0, super.graphics, clientSize == 0 ? 4 : 0);
		chatAreaScrollPos = 0;
		destX = 0;
		RSSocket rsSocket = socketStream;
		loggedIn = false;
		loginFailures = 0;
		login(myUsername, myPassword, true);
		if(!loggedIn)
			resetLogout();
		try
		{
			rsSocket.close();
		}
		catch(Exception _ex)
		{
		}
	}

	private void doAction(int i)
	{
		if(i < 0)
			return;
		if(inputDialogState != 0)
		{
			inputDialogState = 0;
			inputTaken = true;
		}
		int j = menuActionCmd2[i];
		int k = menuActionCmd3[i];
		int l = menuActionID[i];
		int i1 = menuActionCmd1[i];
		if(l >= 2000)
			l -= 2000;
		if(l == 582)
		{
			NPC npc = npcArray[i1];
			if(npc != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, npc.smallY[0], myPlayer.smallX[0], false, npc.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(57);
				stream.writeWordBigA(anInt1285);
				stream.writeWordBigA(i1);
				stream.writeWordLEA(selectedInventorySlot);
				stream.writeWordBigA(selectedInventoryInterface);
			}
		}
		if(l == 234)
		{
			boolean flag1 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if(!flag1)
				flag1 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(236);
			stream.writeWordLEA(k + baseY);
			stream.writeWord(i1);
			stream.writeWordLEA(j + baseX);
		}
		if(l == 62 && objectActionAtTile(i1, k, j))
		{
			stream.createFrame(192);
			stream.writeWord(selectedInventoryInterface);
			stream.writeWordLEA(i1 >> 14 & 0x7fff);
			stream.writeWordLEBigA(k + baseY);
			stream.writeWordLEA(selectedInventorySlot);
			stream.writeWordLEBigA(j + baseX);
			stream.writeWord(anInt1285);
		}
		if(l == 511)
		{
			boolean flag2 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if(!flag2)
				flag2 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(25);
			stream.writeWordLEA(selectedInventoryInterface);
			stream.writeWordBigA(anInt1285);
			stream.writeWord(i1);
			stream.writeWordBigA(k + baseY);
			stream.writeWordLEBigA(selectedInventorySlot);
			stream.writeWord(j + baseX);
		}
		if(l == 74)
		{
			stream.createFrame(122);
			stream.writeWordLEBigA(k);
			stream.writeWordBigA(j);
			stream.writeWordLEA(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if (l == 315) {
            RSInterface class9 = RSInterface.interfaceCache[k];
            boolean flag8 = true;
            if (class9.contentType > 0)
                flag8 = promptUserForInput(class9);
            if (flag8) {
				
				switch(k){
					case 19144:
						sendFrame248(15106,3213);
						resetInterfaceAnim(15106);
						inputTaken = true;
						break;
					default:
						stream.createFrame(185);
						stream.writeWord(k);
						break;
					
				}
            }
        }
		if(l == 561)
		{
			Player player = playerArray[i1];
			if(player != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, player.smallY[0], myPlayer.smallX[0], false, player.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1188_static += i1;
				if(anInt1188_static >= 90)
				{
					stream.createFrame(136);
					anInt1188_static = 0;
				}
				stream.createFrame(128);
				stream.writeWord(i1);
			}
		}
		if(l == 20)
		{
			NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[i1];
			if(class30_sub2_sub4_sub1_sub1_1 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_1.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_1.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(155);
				stream.writeWordLEA(i1);
			}
		}
		if(l == 779)
		{
			Player class30_sub2_sub4_sub1_sub2_1 = playerArray[i1];
			if(class30_sub2_sub4_sub1_sub2_1 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_1.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_1.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(153);
				stream.writeWordLEA(i1);
			}
		}
		if(l == 516)
			if(!menuOpen)
				worldController.setClick(super.saveClickY - 4, super.saveClickX - 4);
			else
				worldController.setClick(k - 4, j - 4);
		if(l == 1062)
		{
			anInt924_static += baseX;
			if(anInt924_static >= 113)
			{
				stream.createFrame(183);
				stream.writeDWordBigEndian(0xe63271);
				anInt924_static = 0;
			}
			objectActionAtTile(i1, k, j);
			stream.createFrame(228);
			stream.writeWordBigA(i1 >> 14 & 0x7fff);
			stream.writeWordBigA(k + baseY);
			stream.writeWord(j + baseX);
		}
		if(l == 679 && !aBoolean1149)
		{
			stream.createFrame(40);
			stream.writeWord(k);
			aBoolean1149 = true;
		}
		if(l == 431)
		{
			stream.createFrame(129);
			stream.writeWordBigA(j);
			stream.writeWord(k);
			stream.writeWordBigA(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 337 || l == 42 || l == 792 || l == 322)
		{
			String s = menuActionName[i];
			int k1 = s.indexOf("@whi@");
			if(k1 != -1)
			{
				long l3 = TextClass.longForName(s.substring(k1 + 5).trim());
				if(l == 337)
					addFriend(l3);
				if(l == 42)
					addIgnore(l3);
				if(l == 792)
					delFriend(l3);
				if(l == 322)
					delIgnore(l3);
			}
		}
		if(l == 53)
		{
			stream.createFrame(135);
			stream.writeWordLEA(j);
			stream.writeWordBigA(k);
			stream.writeWordLEA(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 539)
		{
			stream.createFrame(16);
			stream.writeWordBigA(i1);
			stream.writeWordLEBigA(j);
			stream.writeWordLEBigA(k);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 484 || l == 6)
		{
			String s1 = menuActionName[i];
			int l1 = s1.indexOf("@whi@");
			if(l1 != -1)
			{
				s1 = s1.substring(l1 + 5).trim();
				String s7 = TextClass.fixName(TextClass.nameForLong(TextClass.longForName(s1)));
				boolean flag9 = false;
				for(int j3 = 0; j3 < playerCount; j3++)
				{
					Player class30_sub2_sub4_sub1_sub2_7 = playerArray[playerIndices[j3]];
					if(class30_sub2_sub4_sub1_sub2_7 == null || class30_sub2_sub4_sub1_sub2_7.name == null || !class30_sub2_sub4_sub1_sub2_7.name.equalsIgnoreCase(s7))
						continue;
					doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_7.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_7.smallX[0]);
					if(l == 484)
					{
						stream.createFrame(139);
						stream.writeWordLEA(playerIndices[j3]);
					}
					if(l == 6)
					{
						anInt1188_static += i1;
						if(anInt1188_static >= 90)
						{
							stream.createFrame(136);
							anInt1188_static = 0;
						}
						stream.createFrame(128);
						stream.writeWord(playerIndices[j3]);
					}
					flag9 = true;
					break;
				}

				if(!flag9)
					pushMessage("Unable to find " + s7, 0, "");
			}
		}
		if(l == 870)
		{
			stream.createFrame(53);
			stream.writeWord(j);
			stream.writeWordBigA(selectedInventorySlot);
			stream.writeWordLEBigA(i1);
			stream.writeWord(selectedInventoryInterface);
			stream.writeWordLEA(anInt1285);
			stream.writeWord(k);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 847)
		{
			stream.createFrame(87);
			stream.writeWordBigA(i1);
			stream.writeWord(k);
			stream.writeWordBigA(j);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 626)
		{
			RSInterface class9_1 = RSInterface.interfaceCache[k];
			spellSelected = 1;
			spellID = class9_1.id;
			spellCastOnType = k;
			spellUsableOn = class9_1.spellUsableOn;
			itemSelected = 0;
			needDrawTabArea = true;
			String s4 = class9_1.selectedActionName;
			if(s4.indexOf(" ") != -1)
				s4 = s4.substring(0, s4.indexOf(" "));
			String s8 = class9_1.selectedActionName;
			if(s8.indexOf(" ") != -1)
				s8 = s8.substring(s8.indexOf(" ") + 1);
			spellTooltip = s4 + " " + class9_1.spellName + " " + s8;
			//class9_1.sprite1.drawSprite(class9_1.invSpritePadX, class9_1.invSpritePadY, 0xffffff);
			//class9_1.sprite1.drawSprite(200,200);
			//System.out.println("Sprite: " + class9_1.sprite1.toString());
			if(spellUsableOn == 16)
			{
				needDrawTabArea = true;
				tabID = 3;
				tabAreaAltered = true;
			}
			return;
		}
		if(l == 78)
		{
			stream.createFrame(117);
			stream.writeWordLEBigA(k);
			stream.writeWordLEBigA(i1);
			stream.writeWordLEA(j);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 27)
		{
			Player class30_sub2_sub4_sub1_sub2_2 = playerArray[i1];
			if(class30_sub2_sub4_sub1_sub2_2 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_2.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_2.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt986_static += i1;
				if(anInt986_static >= 54)
				{
					stream.createFrame(189);
					stream.writeWordBigEndian(234);
					anInt986_static = 0;
				}
				stream.createFrame(73);
				stream.writeWordLEA(i1);
			}
		}
		if(l == 213)
		{
			boolean flag3 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if(!flag3)
				flag3 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(79);
			stream.writeWordLEA(k + baseY);
			stream.writeWord(i1);
			stream.writeWordBigA(j + baseX);
		}
		if(l == 632)
		{
			stream.createFrame(145);
			stream.writeWordBigA(k);
			stream.writeWordBigA(j);
			stream.writeWordBigA(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 1004) {
			if(tabInterfaceIDs[10] != -1) {
				needDrawTabArea = true;
				tabID = 10;
				tabAreaAltered = true;
			}
		}
		if(l == 1003) {
			clanChatMode = 2;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 1002) {
			clanChatMode = 1;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 1001) {
			clanChatMode = 0;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 1000) {
			cButtonCPos = 4;
			chatTypeView = 11;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 999) {
			cButtonCPos = 0;
			chatTypeView = 0;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 998) {
			cButtonCPos = 1;
			chatTypeView = 5;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 997) {
			publicChatMode = 3;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 996) {
			publicChatMode = 2;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 995) {
			publicChatMode = 1;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 994) {
			publicChatMode = 0;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 993) {
			cButtonCPos = 2;
			chatTypeView = 1;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 992) {
			privateChatMode = 2;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 991) {
			privateChatMode = 1;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 990) {
			privateChatMode = 0;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 989) {
			cButtonCPos = 3;
			chatTypeView = 2;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 987) {
			tradeMode = 2;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 986) {
			tradeMode = 1;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 985) {
			tradeMode = 0;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 984) {
			cButtonCPos = 5;
			chatTypeView = 3;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 983) {
			duelMode = 2;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 982) {
			duelMode = 1;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 981) {
			duelMode = 0;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 980) {
			cButtonCPos = 6;
			chatTypeView = 4;
			aBoolean1233 = true;
			inputTaken = true;
		}
		if(l == 493)
		{
			stream.createFrame(75);
			stream.writeWordLEBigA(k);
			stream.writeWordLEA(j);
			stream.writeWordBigA(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 652)
		{
			boolean flag4 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if(!flag4)
				flag4 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(156);
			stream.writeWordBigA(j + baseX);
			stream.writeWordLEA(k + baseY);
			stream.writeWordLEBigA(i1);
		}
		if(l == 94)
		{
			boolean flag5 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if(!flag5)
				flag5 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(181);
			stream.writeWordLEA(k + baseY);
			stream.writeWord(i1);
			stream.writeWordLEA(j + baseX);
			stream.writeWordBigA(spellCastOnType);
		}
		if(l == 646)
		{
			stream.createFrame(185);
			stream.writeWord(k);
			RSInterface class9_2 = RSInterface.interfaceCache[k];
			if(class9_2.valueIndexArray != null && class9_2.valueIndexArray[0][0] == 5)
			{
				int i2 = class9_2.valueIndexArray[0][1];
				if(variousSettings[i2] != class9_2.scriptDefaults[0])
				{
					variousSettings[i2] = class9_2.scriptDefaults[0];
					applyVarpSetting(i2);
					needDrawTabArea = true;
				}
			}
		}
		if(l == 225)
		{
			NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[i1];
			if(class30_sub2_sub4_sub1_sub1_2 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_2.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_2.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1226_static += i1;
				if(anInt1226_static >= 85)
				{
					stream.createFrame(230);
					stream.writeWordBigEndian(239);
					anInt1226_static = 0;
				}
				stream.createFrame(17);
				stream.writeWordLEBigA(i1);
			}
		}
		if(l == 965)
		{
			NPC class30_sub2_sub4_sub1_sub1_3 = npcArray[i1];
			if(class30_sub2_sub4_sub1_sub1_3 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_3.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_3.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1134_static++;
				if(anInt1134_static >= 96)
				{
					stream.createFrame(152);
					stream.writeWordBigEndian(88);
					anInt1134_static = 0;
				}
				stream.createFrame(21);
				stream.writeWord(i1);
			}
		}
		if(l == 413)
		{
			NPC class30_sub2_sub4_sub1_sub1_4 = npcArray[i1];
			if(class30_sub2_sub4_sub1_sub1_4 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_4.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_4.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(131);
				stream.writeWordLEBigA(i1);
				stream.writeWordBigA(spellCastOnType);
			}
		}
		if(l == 200)
			clearTopInterfaces();
		if(l == 1025)
		{
			NPC class30_sub2_sub4_sub1_sub1_5 = npcArray[i1];
			if(class30_sub2_sub4_sub1_sub1_5 != null)
			{
				EntityDef entityDef = class30_sub2_sub4_sub1_sub1_5.desc;
				if(entityDef.childrenIDs != null)
					entityDef = entityDef.getChildDefinition();
				if(entityDef != null)
				{
					String s9;
					if(entityDef.description != null)
						s9 = new String(entityDef.description);
					else
						s9 = "It's a " + entityDef.name + ".";
					pushMessage(s9, 0, "");
				}
			}
		}
		if(l == 900)
		{
			objectActionAtTile(i1, k, j);
			stream.createFrame(252);
			stream.writeWordLEBigA(i1 >> 14 & 0x7fff);
			stream.writeWordLEA(k + baseY);
			stream.writeWordBigA(j + baseX);
		}
		if(l == 412)
		{
			NPC class30_sub2_sub4_sub1_sub1_6 = npcArray[i1];
			if(class30_sub2_sub4_sub1_sub1_6 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_6.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_6.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(72);
				stream.writeWordBigA(i1);
			}
		}
		if(l == 365)
		{
			Player class30_sub2_sub4_sub1_sub2_3 = playerArray[i1];
			if(class30_sub2_sub4_sub1_sub2_3 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_3.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_3.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(249);
				stream.writeWordBigA(i1);
				stream.writeWordLEA(spellCastOnType);
			}
		}
		if(l == 729)
		{
			Player class30_sub2_sub4_sub1_sub2_4 = playerArray[i1];
			if(class30_sub2_sub4_sub1_sub2_4 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_4.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_4.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(39);
				stream.writeWordLEA(i1);
			}
		}
		if(l == 577)
		{
			Player class30_sub2_sub4_sub1_sub2_5 = playerArray[i1];
			if(class30_sub2_sub4_sub1_sub2_5 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_5.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_5.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(139);
				stream.writeWordLEA(i1);
			}
		}
		if(l == 956 && objectActionAtTile(i1, k, j))
		{
			stream.createFrame(35);
			stream.writeWordLEA(j + baseX);
			stream.writeWordBigA(spellCastOnType);
			stream.writeWordBigA(k + baseY);
			stream.writeWordLEA(i1 >> 14 & 0x7fff);
		}
		if(l == 567)
		{
			boolean flag6 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if(!flag6)
				flag6 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(23);
			stream.writeWordLEA(k + baseY);
			stream.writeWordLEA(i1);
			stream.writeWordLEA(j + baseX);
		}
		if(l == 867)
		{
			if((i1 & 3) == 0)
				anInt1175_static++;
			if(anInt1175_static >= 59)
			{
				stream.createFrame(200);
				stream.writeWord(25501);
				anInt1175_static = 0;
			}
			stream.createFrame(43);
			stream.writeWordLEA(k);
			stream.writeWordBigA(i1);
			stream.writeWordBigA(j);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 543)
		{
			stream.createFrame(237);
			stream.writeWord(j);
			stream.writeWordBigA(i1);
			stream.writeWord(k);
			stream.writeWordBigA(spellCastOnType);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 606)
		{
			String s2 = menuActionName[i];
			int j2 = s2.indexOf("@whi@");
			if(j2 != -1)
				if(openInterfaceID == -1)
				{
					clearTopInterfaces();
					reportAbuseInput = s2.substring(j2 + 5).trim();
					canMute = false;
					for(int i3 = 0; i3 < RSInterface.interfaceCache.length; i3++)
					{
						if(RSInterface.interfaceCache[i3] == null || RSInterface.interfaceCache[i3].contentType != 600)
							continue;
						reportAbuseInterfaceID = openInterfaceID = RSInterface.interfaceCache[i3].parentID;
						break;
					}

				} else
				{
					pushMessage("Please close the interface you have open before using 'report abuse'", 0, "");
				}
		}
		if(l == 491)
		{
			Player class30_sub2_sub4_sub1_sub2_6 = playerArray[i1];
			if(class30_sub2_sub4_sub1_sub2_6 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_6.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_6.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(14);
				stream.writeWordBigA(selectedInventoryInterface);
				stream.writeWord(i1);
				stream.writeWord(anInt1285);
				stream.writeWordLEA(selectedInventorySlot);
			}
		}
		if(l == 639)
		{
			String s3 = menuActionName[i];
			int k2 = s3.indexOf("@whi@");
			if(k2 != -1)
			{
				long l4 = TextClass.longForName(s3.substring(k2 + 5).trim());
				int k3 = -1;
				for(int i4 = 0; i4 < friendsCount; i4++)
				{
					if(friendsListAsLongs[i4] != l4)
						continue;
					k3 = i4;
					break;
				}

				if(k3 != -1 && friendsNodeIDs[k3] > 0)
				{
					inputTaken = true;
					inputDialogState = 0;
					messagePromptRaised = true;
					promptInput = "";
					friendsListAction = 3;
					lastClickTime = friendsListAsLongs[k3];
					inputTitle = "Enter message to send to " + friendsList[k3];
				}
			}
		}
		if(l == 454)
		{
			stream.createFrame(41);
			stream.writeWord(i1);
			stream.writeWordBigA(j);
			stream.writeWordBigA(k);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if(RSInterface.interfaceCache[k].parentID == openInterfaceID)
				atInventoryInterfaceType = 1;
			if(RSInterface.interfaceCache[k].parentID == backDialogID)
				atInventoryInterfaceType = 3;
		}
		if(l == 478)
		{
			NPC class30_sub2_sub4_sub1_sub1_7 = npcArray[i1];
			if(class30_sub2_sub4_sub1_sub1_7 != null)
			{
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_7.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_7.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				if((i1 & 3) == 0)
					anInt1155_static++;
				if(anInt1155_static >= 53)
				{
					stream.createFrame(85);
					stream.writeWordBigEndian(66);
					anInt1155_static = 0;
				}
				stream.createFrame(18);
				stream.writeWordLEA(i1);
			}
		}
		if(l == 113)
		{
			objectActionAtTile(i1, k, j);
			stream.createFrame(70);
			stream.writeWordLEA(j + baseX);
			stream.writeWord(k + baseY);
			stream.writeWordLEBigA(i1 >> 14 & 0x7fff);
		}
		if(l == 872)
		{
			objectActionAtTile(i1, k, j);
			stream.createFrame(234);
			stream.writeWordLEBigA(j + baseX);
			stream.writeWordBigA(i1 >> 14 & 0x7fff);
			stream.writeWordLEBigA(k + baseY);
		}
		if(l == 502)
		{
			objectActionAtTile(i1, k, j);
			stream.createFrame(132);
			stream.writeWordLEBigA(j + baseX);
			stream.writeWord(i1 >> 14 & 0x7fff);
			stream.writeWordBigA(k + baseY);
		}
		if(l == 1125)
		{
			ItemDef itemDef = ItemDef.forID(i1);
			RSInterface class9_4 = RSInterface.interfaceCache[k];
			String s5;
			if(class9_4 != null && class9_4.invStackSizes[j] >= 0x186a0)
				s5 = class9_4.invStackSizes[j] + " x " + itemDef.name;
			else
			if(itemDef.description != null)
				s5 = new String(itemDef.description);
			else
				s5 = "It's a " + itemDef.name + ".";
			pushMessage(s5, 0, "");
		}
		if(l == 169)
		{
			stream.createFrame(185);
			stream.writeWord(k);
			RSInterface class9_3 = RSInterface.interfaceCache[k];
			if(class9_3.valueIndexArray != null && class9_3.valueIndexArray[0][0] == 5)
			{
				int l2 = class9_3.valueIndexArray[0][1];
				variousSettings[l2] = 1 - variousSettings[l2];
				applyVarpSetting(l2);
				needDrawTabArea = true;
			}
		}
		if(l == 447)
		{
			itemSelected = 1;
			selectedInventorySlot = j;
			selectedInventoryInterface = k;
			anInt1285 = i1;
			selectedItemName = ItemDef.forID(i1).name;
			spellSelected = 0;
			needDrawTabArea = true;
			return;
		}
		if(l == 1226)
		{
			int j1 = i1 >> 14 & 0x7fff;
			ObjectDef class46 = ObjectDef.forID(j1);
			String s10;
			if(class46.description != null)
				s10 = new String(class46.description);
			else
				s10 = "It's a " + class46.name + ".";
			pushMessage(s10, 0, "");
		}
		if(l == 244)
		{
			boolean flag7 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if(!flag7)
				flag7 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.createFrame(253);
			stream.writeWordLEA(j + baseX);
			stream.writeWordLEBigA(k + baseY);
			stream.writeWordBigA(i1);
		}
		if(l == 1448)
		{
			ItemDef itemDef_1 = ItemDef.forID(i1);
			String s6;
			if(itemDef_1.description != null)
				s6 = new String(itemDef_1.description);
			else
				s6 = "It's a " + itemDef_1.name + ".";
			pushMessage(s6, 0, "");
		}
		itemSelected = 0;
			spellSelected = 0;
			needDrawTabArea = true;

	}

	private void checkWildernessStatus()
	{
		anInt1251 = 0;
		int j = (myPlayer.x >> 7) + baseX;
		int k = (myPlayer.y >> 7) + baseY;
		if(j >= 3053 && j <= 3156 && k >= 3056 && k <= 3136)
			anInt1251 = 1;
		if(j >= 3072 && j <= 3118 && k >= 9492 && k <= 9535)
			anInt1251 = 1;
		if(anInt1251 == 1 && j >= 3139 && j <= 3199 && k >= 3008 && k <= 3062)
			anInt1251 = 0;
	}

	public void run() {
		if(drawFlames) {
			drawFlames();
		} else {
			super.run();
		}
	}
	
		public void loadExtraSprites(){
		for(int i4 = 0; i4 < 3; i4++) {
			hitMark[i4] = new Sprite("Player/Hits "+i4+"");
			}
		}

	private void build3dScreenMenu()
	{
		if(itemSelected == 0 && spellSelected == 0)
		{
			menuActionName[menuActionRow] = "Walk here";
			menuActionID[menuActionRow] = 516;
			menuActionCmd2[menuActionRow] = super.mouseX;
			menuActionCmd3[menuActionRow] = super.mouseY;
			menuActionRow++;
		}
		int j = -1;
		for(int k = 0; k < Model.mousePickCount; k++)
		{
			int l = Model.mousePickResults[k];
			int i1 = l & 0x7f;
			int j1 = l >> 7 & 0x7f;
			int k1 = l >> 29 & 3;
			int l1 = l >> 14 & 0x7fff;
			if(l == j)
				continue;
			j = l;
			if(k1 == 2 && worldController.getObjectConfig(plane, i1, j1, l) >= 0)
			{
				ObjectDef class46 = ObjectDef.forID(l1);
				if(class46.childrenIDs != null)
					class46 = class46.getChildDef();
				if(class46 == null)
					continue;
				if(itemSelected == 1)
				{
					menuActionName[menuActionRow] = "Use " + selectedItemName + " with @cya@" + class46.name;
					menuActionID[menuActionRow] = 62;
					menuActionCmd1[menuActionRow] = l;
					menuActionCmd2[menuActionRow] = i1;
					menuActionCmd3[menuActionRow] = j1;
					menuActionRow++;
				} else
				if(spellSelected == 1)
				{
					if((spellUsableOn & 4) == 4)
					{
						menuActionName[menuActionRow] = spellTooltip + " @cya@" + class46.name;
						menuActionID[menuActionRow] = 956;
						menuActionCmd1[menuActionRow] = l;
						menuActionCmd2[menuActionRow] = i1;
						menuActionCmd3[menuActionRow] = j1;
						menuActionRow++;
					}
				} else
				{
					if(class46.actions != null)
					{
						for(int i2 = 4; i2 >= 0; i2--)
							if(class46.actions[i2] != null)
							{
								menuActionName[menuActionRow] = class46.actions[i2] + " @cya@" + class46.name;
								if(i2 == 0)
									menuActionID[menuActionRow] = 502;
								if(i2 == 1)
									menuActionID[menuActionRow] = 900;
								if(i2 == 2)
									menuActionID[menuActionRow] = 113;
								if(i2 == 3)
									menuActionID[menuActionRow] = 872;
								if(i2 == 4)
									menuActionID[menuActionRow] = 1062;
								menuActionCmd1[menuActionRow] = l;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}

					}
					//menuActionName[menuActionRow] = "Examine @cya@" + class46.name + " @gre@(@whi@" + l1 + "@gre@) (@whi@" + (i1 + baseX) + "," + (j1 + baseY) + "@gre@)";
					menuActionName[menuActionRow] = "Examine @cya@" + class46.name;
					menuActionID[menuActionRow] = 1226;
					menuActionCmd1[menuActionRow] = class46.type << 14;
					menuActionCmd2[menuActionRow] = i1;
					menuActionCmd3[menuActionRow] = j1;
					menuActionRow++;
				}
			}
			if(k1 == 1)
			{
				NPC npc = npcArray[l1];
				if(npc.desc.tileSpan == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64)
				{
					for(int j2 = 0; j2 < npcCount; j2++)
					{
						NPC npc2 = npcArray[npcIndices[j2]];
						if(npc2 != null && npc2 != npc && npc2.desc.tileSpan == 1 && npc2.x == npc.x && npc2.y == npc.y)
							buildAtNPCMenu(npc2.desc, npcIndices[j2], j1, i1);
					}

					for(int l2 = 0; l2 < playerCount; l2++)
					{
						Player player = playerArray[playerIndices[l2]];
						if(player != null && player.x == npc.x && player.y == npc.y)
							buildAtPlayerMenu(i1, playerIndices[l2], player, j1);
					}

				}
				buildAtNPCMenu(npc.desc, l1, j1, i1);
			}
			if(k1 == 0)
			{
				Player player = playerArray[l1];
				if((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64)
				{
					for(int k2 = 0; k2 < npcCount; k2++)
					{
						NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[npcIndices[k2]];
						if(class30_sub2_sub4_sub1_sub1_2 != null && class30_sub2_sub4_sub1_sub1_2.desc.tileSpan == 1 && class30_sub2_sub4_sub1_sub1_2.x == player.x && class30_sub2_sub4_sub1_sub1_2.y == player.y)
							buildAtNPCMenu(class30_sub2_sub4_sub1_sub1_2.desc, npcIndices[k2], j1, i1);
					}

					for(int i3 = 0; i3 < playerCount; i3++)
					{
						Player class30_sub2_sub4_sub1_sub2_2 = playerArray[playerIndices[i3]];
						if(class30_sub2_sub4_sub1_sub2_2 != null && class30_sub2_sub4_sub1_sub2_2 != player && class30_sub2_sub4_sub1_sub2_2.x == player.x && class30_sub2_sub4_sub1_sub2_2.y == player.y)
							buildAtPlayerMenu(i1, playerIndices[i3], class30_sub2_sub4_sub1_sub2_2, j1);
					}

				}
				buildAtPlayerMenu(i1, l1, player, j1);
			}
			if(k1 == 3)
			{
				NodeList class19 = groundArray[plane][i1][j1];
				if(class19 != null)
				{
					for(Item item = (Item)class19.getFirst(); item != null; item = (Item)class19.getNext())
					{
						ItemDef itemDef = ItemDef.forID(item.ID);
						if(itemSelected == 1)
						{
							menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
							menuActionID[menuActionRow] = 511;
							menuActionCmd1[menuActionRow] = item.ID;
							menuActionCmd2[menuActionRow] = i1;
							menuActionCmd3[menuActionRow] = j1;
							menuActionRow++;
						} else
						if(spellSelected == 1)
						{
							if((spellUsableOn & 1) == 1)
							{
								menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
								menuActionID[menuActionRow] = 94;
								menuActionCmd1[menuActionRow] = item.ID;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}
						} else
						{
							for(int j3 = 4; j3 >= 0; j3--)
								if(itemDef.groundActions != null && itemDef.groundActions[j3] != null)
								{
									menuActionName[menuActionRow] = itemDef.groundActions[j3] + " @lre@" + itemDef.name;
									if(j3 == 0)
										menuActionID[menuActionRow] = 652;
									if(j3 == 1)
										menuActionID[menuActionRow] = 567;
									if(j3 == 2)
										menuActionID[menuActionRow] = 234;
									if(j3 == 3)
										menuActionID[menuActionRow] = 244;
									if(j3 == 4)
										menuActionID[menuActionRow] = 213;
									menuActionCmd1[menuActionRow] = item.ID;
									menuActionCmd2[menuActionRow] = i1;
									menuActionCmd3[menuActionRow] = j1;
									menuActionRow++;
								} else
								if(j3 == 2)
								{
									menuActionName[menuActionRow] = "Take @lre@" + itemDef.name;
									menuActionID[menuActionRow] = 234;
									menuActionCmd1[menuActionRow] = item.ID;
									menuActionCmd2[menuActionRow] = i1;
									menuActionCmd3[menuActionRow] = j1;
									menuActionRow++;
								}

							//menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + " @gre@(@whi@" + item.ID + "@gre@)";
							menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name;
							menuActionID[menuActionRow] = 1448;
							menuActionCmd1[menuActionRow] = item.ID;
							menuActionCmd2[menuActionRow] = i1;
							menuActionCmd3[menuActionRow] = j1;
							menuActionRow++;
						}
					}

				}
			}
		}
	}

	public void cleanUpForQuit()
	{
		signlink.reporterror = false;
		try
		{
			if(socketStream != null)
				socketStream.close();
		}
		catch(Exception _ex) { }
		socketStream = null;
		stopMidi();
		if(mouseDetection != null)
			mouseDetection.running = false;
		mouseDetection = null;
		onDemandFetcher.disable();
		onDemandFetcher = null;
		loginStream = null;
		stream = null;
		outStream = null;
		inStream = null;
		chatFilterTypes = null;
		mapLandscapeData = null;
		mapObjectData = null;
		chatFilterNames = null;
		chatFilterMessages = null;
		intGroundArray = null;
		byteGroundArray = null;
		worldController = null;
		aCollisionMapArray1230 = null;
		mapRegions = null;
		constructMapTiles = null;
		bigX = null;
		bigY = null;
		terrainData = null;
		titleMuralIP = null;
		mapEdgeIP = null;
		leftFrame = null;
		topFrame = null;
		rightFrame = null;
		titleButtonIP = null;
		loginMsgIP = null;
		topCenterIP = null;
		titleIP1 = null;
		titleIP2 = null;
		titleIP3 = null;
		/* Null pointers for custom sprites */
		chatArea = null;
		chatButtons = null;
		tabArea = null;
		HPBarFull = null;
		HPBarEmpty = null;
		mapArea = null;
		/**/
		mapBack = null;
		sideIcons = null;
		redStones = null;
		compass = null;
		hitMarks = null;
		hitMark = null;
		headIcons = null;
		skullIcons = null;
		headIconsHint = null;
		crosses = null;
		mapDotItem = null;
		mapDotNPC = null;
		mapDotPlayer = null;
		mapDotFriend = null;
		mapDotTeam = null;
		mapScenes = null;
		mapFunctions = null;
		constructRegionData = null;
		playerArray = null;
		playerIndices = null;
		entityIndices = null;
		playerBuffers = null;
		entityUpdateIndices = null;
		npcArray = null;
		npcIndices = null;
		groundArray = null;
		spawnObjectList = null;
		projectileList = null;
		spotAnimList = null;
		menuActionCmd2 = null;
		menuActionCmd3 = null;
		menuActionID = null;
		menuActionCmd1 = null;
		menuActionName = null;
		variousSettings = null;
		mapFunctionX = null;
		mapFunctionY = null;
		minimapImages = null;
		minimapSprite = null;
		friendsList = null;
		friendsListAsLongs = null;
		friendsNodeIDs = null;
		chatAreaIP = null;
		chatSettingIP = null;
		tabImageProducer = null;
		mapAreaIP = null;
		gameScreenIP = null;
		topSideIP1 = null;
		topSideIP2 = null;
		bottomSideIP1 = null;
		bottomSideIP2 = null;
		multiOverlay = null;
		nullLoader();
		ObjectDef.nullLoader();
		EntityDef.nullLoader();
		ItemDef.nullLoader();
		Flo.cache = null;
		IDK.cache = null;
		RSInterface.interfaceCache = null;
		DummyClass.cache = null;
		Animation.anims = null;
		SpotAnim.cache = null;
		SpotAnim.modelCache = null;
		Varp.cache = null;
		super.fullGameScreen = null;
		Player.mruNodes = null;
		Texture.nullLoader();
		WorldController.nullLoader();
		Model.nullLoader();
		AnimFrame.nullLoader();
		System.gc();
	}

	private void printDebug()
	{
		System.out.println("============");
		System.out.println("flame-cycle:" + lastMapRegionY);
		if(onDemandFetcher != null)
			System.out.println("Od-cycle:" + onDemandFetcher.onDemandCycle);
		System.out.println("loop-cycle:" + loopCycle);
		System.out.println("draw-cycle:" + anInt1061_static);
		System.out.println("ptype:" + pktType);
		System.out.println("psize:" + pktSize);
		if(socketStream != null)
			socketStream.printDebug();
		super.shouldDebug = true;
	}

	Component getGameComponent() {
		if(signlink.mainapp instanceof Component)
			return (Component)signlink.mainapp;
		if(super.gameFrame != null)
			return super.gameFrame;
		else
			return this;
	}

	private void processKeyInput() {
		do {
			int j = readChar(-796);
			if(j == -1)
				break;
			if(openInterfaceID != -1 && openInterfaceID == reportAbuseInterfaceID) {
				if(j == 8 && reportAbuseInput.length() > 0)
					reportAbuseInput = reportAbuseInput.substring(0, reportAbuseInput.length() - 1);
				if((j >= 97 && j <= 122 || j >= 65 && j <= 90 || j >= 48 && j <= 57 || j == 32) && reportAbuseInput.length() < 12)
					reportAbuseInput += (char)j;
			} else if(messagePromptRaised) {
				if(j >= 32 && j <= 122 && promptInput.length() < 80) {
					promptInput += (char)j;
					inputTaken = true;
				}
				if(j == 8 && promptInput.length() > 0) {
					promptInput = promptInput.substring(0, promptInput.length() - 1);
					inputTaken = true;
				}
				if(j == 13 || j == 10) {
					messagePromptRaised = false;
					inputTaken = true;
					if(friendsListAction == 1) {
						long l = TextClass.longForName(promptInput);
						addFriend(l);
					}
					if(friendsListAction == 2 && friendsCount > 0) {
						long l1 = TextClass.longForName(promptInput);
						delFriend(l1);
					}
					if(friendsListAction == 3 && promptInput.length() > 0) {
						stream.createFrame(126);
						stream.writeWordBigEndian(0);
						int k = stream.currentOffset;
						stream.writeQWord(lastClickTime);
						TextInput.encodeText(promptInput, stream);
						stream.writeBytes(stream.currentOffset - k);
						promptInput = TextInput.processText(promptInput);
						//promptInput = Censor.doCensor(promptInput);
						pushMessage(promptInput, 6, TextClass.fixName(TextClass.nameForLong(lastClickTime)));
						if(privateChatMode == 2) {
							privateChatMode = 1;
							aBoolean1233 = true;
							stream.createFrame(95);
							stream.writeWordBigEndian(publicChatMode);
							stream.writeWordBigEndian(privateChatMode);
							stream.writeWordBigEndian(tradeMode);
						}
					}
					if(friendsListAction == 4 && ignoreCount < 100) {
						long l2 = TextClass.longForName(promptInput);
						addIgnore(l2);
					}
					if(friendsListAction == 5 && ignoreCount > 0) {
						long l3 = TextClass.longForName(promptInput);
						delIgnore(l3);
					}
					if(friendsListAction == 6) {
						long l3 = TextClass.longForName(promptInput);
						chatJoin(l3);
					}
				}
			} else if(inputDialogState == 1) {
				if(j >= 48 && j <= 57 && amountOrNameInput.length() < 10) {
					amountOrNameInput += (char)j;
					inputTaken = true;
				}
				if(j == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					inputTaken = true;
				}
				if(j == 13 || j == 10) {
					if(amountOrNameInput.length() > 0) {
						int i1 = 0;
						try {
							i1 = Integer.parseInt(amountOrNameInput);
						}
						catch(Exception _ex) { }
						stream.createFrame(208);
						stream.writeDWord(i1);
					}
					inputDialogState = 0;
					inputTaken = true;
				}
			} else if(inputDialogState == 2) {
				if(j >= 32 && j <= 122 && amountOrNameInput.length() < 12) {
					amountOrNameInput += (char)j;
					inputTaken = true;
				}
				if(j == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					inputTaken = true;
				}
				if(j == 13 || j == 10) {
					if(amountOrNameInput.length() > 0) {
						stream.createFrame(60);
						stream.writeQWord(TextClass.longForName(amountOrNameInput));
					}
					inputDialogState = 0;
					inputTaken = true;
				}
			} else if(backDialogID == -1) {
				if(j >= 32 && j <= 122 && inputString.length() < 80) {
					inputString += (char)j;
					inputTaken = true;
				}
				if(j == 8 && inputString.length() > 0) {
					inputString = inputString.substring(0, inputString.length() - 1);
					inputTaken = true;
				}
				if((j == 13 || j == 10) && inputString.length() > 0) {
					if(myPrivilege == 2 || server.equals("127.0.0.1") || 1 == 1/*to remove*/) {
						if(inputString.startsWith("//setspecto")) {
							int amt = Integer.parseInt(inputString.substring(12));
							tabFlashTimer[300] = amt;
							if(variousSettings[300] != amt) {
								variousSettings[300] = amt;
								applyVarpSetting(300);
								needDrawTabArea = true;
								if(dialogID != -1)
									inputTaken = true;
							}
						}
						if(inputString.equals("clientdrop"))
							dropClient();
						if(inputString.equals("::dumpmodels"))
							models();
						if(inputString.equals("dumpnpcs"))
							EntityDef.rewriteNpcs();
						if (inputString.startsWith("full")) {
							try {
								String[] args = inputString.split(" ");
								int id1 = Integer.parseInt(args[1]);
								int id2 = Integer.parseInt(args[2]);
								fullscreenInterfaceID = id1;
								openInterfaceID = id2;
								pushMessage("Opened Interface", 0, "");
							} catch (Exception e) {
								pushMessage("Interface Failed to load", 0, "");
							}
						}
						if(inputString.equals("::lag"))
							printDebug();
						if(inputString.equals("::prefetchmusic")) {
							for(int j1 = 0; j1 < onDemandFetcher.getVersionCount(2); j1++)
								onDemandFetcher.requestArchive((byte)1, 2, j1);

						}
						if(inputString.equals("::x10on"))
							newDamage = true;
						if(inputString.equals("::x10off"))
							newDamage = false;
						if(inputString.equals("::fpson"))
							fpsOn = true;
						if(inputString.equals("::fpsoff"))
							fpsOn = false;
						if(inputString.equals("::dataon"))
							clientData = true;
						if(inputString.equals("::dataoff"))
							clientData = false;
						if(inputString.equals("::zoomreset")) {
					cameraZoom = 0;
					pushMessage("Camera zoom reset.", 0, "");
				}
				if(inputString.equals("::fixed")) {
					toggleSize(0);
					pushMessage("Switched to fixed mode.", 0, "");
				}
				if(inputString.equals("::resize")) {
					toggleSize(1);
					pushMessage("Switched to resizable mode.", 0, "");
				}
				if(inputString.equals("::fullscreen")) {
					toggleSize(2);
					pushMessage("Switched to fullscreen mode.", 0, "");
				}
				if(inputString.equals("::noclip")) {
							for(int k1 = 0; k1 < 4; k1++) {
								for(int i2 = 1; i2 < 103; i2++) {
									for(int k2 = 1; k2 < 103; k2++)
										aCollisionMapArray1230[k1].flags[i2][k2] = 0;

								}
							}
						}
					}
					if(inputString.startsWith("/"))
						inputString = "::" + inputString;
					if(inputString.equals("add model")) {
						try {
							int ModelIndex = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter model ID", "Model", 3));
							byte[] abyte0 = getModel(ModelIndex);
							if(abyte0 != null && abyte0.length > 0) {
								decompressors[1].readCacheData(abyte0.length, abyte0, ModelIndex);
								pushMessage("Model: [" + ModelIndex + "] added successfully!", 0, "");
							} else {
								pushMessage("Unable to find the model. "+ModelIndex, 0, "");
							}
						} catch(Exception e) {
							pushMessage("Syntax - ::add model <path>", 0, "");
						}
					}
					if(inputString.startsWith("::")) {
						stream.createFrame(103);
						stream.writeWordBigEndian(inputString.length() - 1);
						stream.writeString(inputString.substring(2));
					} else {
						String s = inputString.toLowerCase();	
						int j2 = 0;
						if(s.startsWith("yellow:"))
						{
							j2 = 0;
							inputString = inputString.substring(7);
						} else if(s.startsWith("red:"))
						{
							j2 = 1;
							inputString = inputString.substring(4);
						} else if(s.startsWith("green:"))
						{
							j2 = 2;
							inputString = inputString.substring(6);
						} else if(s.startsWith("cyan:"))
						{
							j2 = 3;
							inputString = inputString.substring(5);
						} else if(s.startsWith("purple:"))
						{
							j2 = 4;
							inputString = inputString.substring(7);
						} else if(s.startsWith("white:"))
						{
							j2 = 5;
							inputString = inputString.substring(6);
						} else if(s.startsWith("flash1:"))
						{
							j2 = 6;
							inputString = inputString.substring(7);
						} else if(s.startsWith("flash2:"))
						{
							j2 = 7;
							inputString = inputString.substring(7);
						} else if(s.startsWith("flash3:"))
						{
							j2 = 8;
							inputString = inputString.substring(7);
						} else if(s.startsWith("glow1:"))
						{
							j2 = 9;
							inputString = inputString.substring(6);
						} else if(s.startsWith("glow2:"))
						{
							j2 = 10;
							inputString = inputString.substring(6);
						} else if(s.startsWith("glow3:"))
						{
							j2 = 11;
							inputString = inputString.substring(6);
						}
						s = inputString.toLowerCase();
						int i3 = 0;
						if(s.startsWith("wave:"))
						{
							i3 = 1;
							inputString = inputString.substring(5);
						} else if(s.startsWith("wave2:"))
						{
							i3 = 2;
							inputString = inputString.substring(6);
						} else if(s.startsWith("shake:"))
						{
							i3 = 3;
							inputString = inputString.substring(6);
						} else if(s.startsWith("scroll:"))
						{
							i3 = 4;
							inputString = inputString.substring(7);
						} else if(s.startsWith("slide:"))
						{
							i3 = 5;
							inputString = inputString.substring(6);
						}
						stream.createFrame(4);
						stream.writeWordBigEndian(0);
						int j3 = stream.currentOffset;
						stream.write128MinusByte(i3);
						stream.write128MinusByte(j2);
						loginStream.currentOffset = 0;
						TextInput.encodeText(inputString, loginStream);
						stream.writeBytesReverse128(0, loginStream.buffer, loginStream.currentOffset);
						stream.writeBytes(stream.currentOffset - j3);
						inputString = TextInput.processText(inputString);
						//inputString = Censor.doCensor(inputString);
						myPlayer.textSpoken = inputString;
						myPlayer.turnAroundAnimId = j2;
						myPlayer.animResetCycle = i3;
						myPlayer.textCycle = 150;
						if(myPrivilege == 2)
							pushMessage(myPlayer.textSpoken, 2, "@cr2@" + myPlayer.name);
						else
						if(myPrivilege == 1)
							pushMessage(myPlayer.textSpoken, 2, "@cr1@" + myPlayer.name);
						else
							pushMessage(myPlayer.textSpoken, 2, myPlayer.name);
						if(publicChatMode == 2)
						{
							publicChatMode = 3;
							aBoolean1233 = true;
							stream.createFrame(95);
							stream.writeWordBigEndian(publicChatMode);
							stream.writeWordBigEndian(privateChatMode);
							stream.writeWordBigEndian(tradeMode);
						}
					}
					inputString = "";
					inputTaken = true;
				}
			}
		} while(true);
	}

	private void buildPublicChat(int j)
	{
		int l = 0;
		for(int i1 = 0; i1 < 500; i1++)
		{
			if(chatMessages[i1] == null)
				continue;
			if(chatTypeView != 1)
				continue;
			int j1 = chatTypes[i1];
			String s = chatNames[i1];
			String ct = chatMessages[i1];
			int k1 = (70 - l * 14 + 42) + chatScrollAmount + 4 + 5;
			if(k1 < -23)
				break;
			if(s != null && s.startsWith("@cr1@"))
				s = s.substring(5);
			if(s != null && s.startsWith("@cr2@"))
				s = s.substring(5);
			if(s != null && s.startsWith("@cr3@"))
				s = s.substring(5);
			if((j1 == 1 || j1 == 2) && (j1 == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s))) {
			if(j > k1 - 14 && j <= k1 && !s.equals(myPlayer.name)) {
				if(myPrivilege >= 1) {
					menuActionName[menuActionRow] = "Report abuse @whi@" + s;
					menuActionID[menuActionRow] = 606;
					menuActionRow++;
				}
				menuActionName[menuActionRow] = "Add ignore @whi@" + s;
				menuActionID[menuActionRow] = 42;
				menuActionRow++;
				menuActionName[menuActionRow] = "Add friend @whi@" + s;
				menuActionID[menuActionRow] = 337;
				menuActionRow++;
			}
			l++;
			}
		}
	}

	private void buildFriendChat(int j)
	{
		int l = 0;
		for(int i1 = 0; i1 < 500; i1++) {
			if(chatMessages[i1] == null)
				continue;
			if(chatTypeView != 2)
				continue;
			int j1 = chatTypes[i1];
			String s = chatNames[i1];
			String ct = chatMessages[i1];
			int k1 = (70 - l * 14 + 42) + chatScrollAmount + 4 + 5;
			if(k1 < -23)
				break;
			if(s != null && s.startsWith("@cr1@"))
				s = s.substring(5);
			if(s != null && s.startsWith("@cr2@"))
				s = s.substring(5);
			if(s != null && s.startsWith("@cr3@"))
				s = s.substring(5);
			if((j1 == 5 || j1 == 6) && (splitPrivateChat == 0 || chatTypeView == 2) && (j1 == 6 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s)))
				l++;
			if((j1 == 3 || j1 == 7) && (splitPrivateChat == 0 || chatTypeView == 2) && (j1 == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s)))
			{
				if(j > k1 - 14 && j <= k1) {
					if(myPrivilege >= 1) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + s;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + s;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + s;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
			l++;
			}
		}
	}

	private void buildDuelorTrade(int j) {
		int l = 0;
		for(int i1 = 0; i1 < 500; i1++) {
			if(chatMessages[i1] == null)
				continue;
			if(chatTypeView != 3 && chatTypeView != 4)
				continue;
			int j1 = chatTypes[i1];
			String s = chatNames[i1];
			int k1 = (70 - l * 14 + 42) + chatScrollAmount + 4 + 5;
			if(k1 < -23)
				break;
			if(s != null && s.startsWith("@cr1@"))
				s = s.substring(5);
			if(s != null && s.startsWith("@cr2@"))
				s = s.substring(5);
			if(s != null && s.startsWith("@cr3@"))
				s = s.substring(5);
			if(chatTypeView == 3 && j1 == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
				if(j > k1 - 14 && j <= k1) {
					menuActionName[menuActionRow] = "Accept trade @whi@" + s;
					menuActionID[menuActionRow] = 484;
					menuActionRow++;
				}
				l++;
			}
			if(chatTypeView == 4 && j1 == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
				if(j > k1 - 14 && j <= k1) {
					menuActionName[menuActionRow] = "Accept challenge @whi@" + s;
					menuActionID[menuActionRow] = 6;
					menuActionRow++;
				}
				l++;
			}
			if(j1 == 12) {
				if(j > k1 - 14 && j <= k1) {
					menuActionName[menuActionRow] = "Go-to @blu@" + s;
					menuActionID[menuActionRow] = 915;
					menuActionRow++;
				}
				l++;
			}
		}
	}

	private void buildChatAreaMenu(int j) {
		int l = 0;
		int test = 0;
		for(int i1 = 0; i1 < 500; i1++) {
			if(chatMessages[i1] == null)
				continue;
			int j1 = chatTypes[i1];
			int k1 = (70 - l * 14 + 42) + chatScrollAmount + 4 + 5;
			if(k1 < -23)
				break;
			String s = chatNames[i1];
			String ct = chatMessages[i1];
			boolean flag = false;
			if(chatTypeView == 1) {
				buildPublicChat(j);
				break;
			}
			if(chatTypeView == 2) {
				buildFriendChat(j);
				break;
			}
			if(chatTypeView == 3 || chatTypeView == 4) {
				buildDuelorTrade(j);
				break;
			}
			if(chatTypeView == 5) {
				break;
			}
			if(s != null && s.startsWith("@cr1@")) {
				s = s.substring(5);
				boolean flag1 = true;
				byte byte0 = 1;
			}
			if(s != null && s.startsWith("@cr2@")) {
				s = s.substring(5);
				byte byte0 = 2;
			}
			if(s != null && s.startsWith("@cr3@")) {
				s = s.substring(5);
				byte byte0 = 3;
			}
			if(j1 == 0)
				l++;
			if((j1 == 1 || j1 == 2) && (j1 == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s))) {
				if(j > k1 - 14 && j <= k1 && !s.equals(myPlayer.name)) {
					if(myPrivilege >= 1) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + s;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + s;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + s;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				l++;
			}
			if((j1 == 3 || j1 == 7) && splitPrivateChat == 0 && (j1 == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
				if(j > k1 - 14 && j <= k1) {
					if(myPrivilege >= 1) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + s;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + s;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + s;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				l++;
			}
			if(j1 == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
				if(j > k1 - 14 && j <= k1) {
					menuActionName[menuActionRow] = "Accept trade @whi@" + s;
					menuActionID[menuActionRow] = 484;
					menuActionRow++;
				}
				l++;
			}
			if((j1 == 5 || j1 == 6) && splitPrivateChat == 0 && privateChatMode < 2)
				l++;
			if(j1 == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
				if(j > k1 - 14 && j <= k1) {
					menuActionName[menuActionRow] = "Accept challenge @whi@" + s;
					menuActionID[menuActionRow] = 6;
					menuActionRow++;
				}
				l++;
			}
		}
	}
	
	
	
	private void drawFriendsListOrWelcomeScreen(RSInterface class9)
	{
		int j = class9.contentType;
		if(j >= 1 && j <= 100 || j >= 701 && j <= 800)
		{
			if(j == 1 && mapRegionCount == 0)
			{
				class9.message = "Loading friend list";
				class9.atActionType = 0;
				return;
			}
			if(j == 1 && mapRegionCount == 1)
			{
				class9.message = "Connecting to friendserver";
				class9.atActionType = 0;
				return;
			}
			if(j == 2 && mapRegionCount != 2)
			{
				class9.message = "Please wait...";
				class9.atActionType = 0;
				return;
			}
			int k = friendsCount;
			if(mapRegionCount != 2)
				k = 0;
			if(j > 700)
				j -= 601;
			else
				j--;
			if(j >= k)
			{
				class9.message = "";
				class9.atActionType = 0;
				return;
			} else
			{
				class9.message = friendsList[j];
				class9.atActionType = 1;
				return;
			}
		}
		if(j >= 101 && j <= 200 || j >= 801 && j <= 900)
		{
			int l = friendsCount;
			if(mapRegionCount != 2)
				l = 0;
			if(j > 800)
				j -= 701;
			else
				j -= 101;
			if(j >= l)
			{
				class9.message = "";
				class9.atActionType = 0;
				return;
			}
			if(friendsNodeIDs[j] == 0)
				class9.message = "@red@Offline";
			else if(friendsNodeIDs[j] == nodeID)
				class9.message = "@gre@Online"/* + (friendsNodeIDs[j] - 9)*/;
			else
				class9.message = "@red@Offline"/* + (friendsNodeIDs[j] - 9)*/;
			class9.atActionType = 1;
			return;
		}
		if(j == 203)
		{
			int i1 = friendsCount;
			if(mapRegionCount != 2)
				i1 = 0;
			class9.scrollMax = i1 * 15 + 20;
			if(class9.scrollMax <= class9.height)
				class9.scrollMax = class9.height + 1;
			return;
		}
		if(j >= 401 && j <= 500)
		{
			if((j -= 401) == 0 && mapRegionCount == 0)
			{
				class9.message = "Loading ignore list";
				class9.atActionType = 0;
				return;
			}
			if(j == 1 && mapRegionCount == 0)
			{
				class9.message = "Please wait...";
				class9.atActionType = 0;
				return;
			}
			int j1 = ignoreCount;
			if(mapRegionCount == 0)
				j1 = 0;
			if(j >= j1)
			{
				class9.message = "";
				class9.atActionType = 0;
				return;
			} else
			{
				class9.message = TextClass.fixName(TextClass.nameForLong(ignoreListAsLongs[j]));
				class9.atActionType = 1;
				return;
			}
		}
		if(j == 503)
		{
			class9.scrollMax = ignoreCount * 15 + 20;
			if(class9.scrollMax <= class9.height)
				class9.scrollMax = class9.height + 1;
			return;
		}
		if(j == 327)
		{
			class9.modelRotation1 = 150;
			class9.modelRotation2 = (int)(Math.sin((double)loopCycle / 40D) * 256D) & 0x7ff;
			if(aBoolean1031)
			{
				for(int k1 = 0; k1 < 7; k1++)
				{
					int l1 = menuActionTypes[k1];
					if(l1 >= 0 && !IDK.cache[l1].isIDKHeadModelReady())
						return;
				}

				aBoolean1031 = false;
				Model aclass30_sub2_sub4_sub6s[] = new Model[7];
				int i2 = 0;
				for(int j2 = 0; j2 < 7; j2++)
				{
					int k2 = menuActionTypes[j2];
					if(k2 >= 0)
						aclass30_sub2_sub4_sub6s[i2++] = IDK.cache[k2].getIDKHeadModel();
				}

				Model model = new Model(i2, aclass30_sub2_sub4_sub6s);
				for(int l2 = 0; l2 < 5; l2++)
					if(walkingQueueY[l2] != 0)
					{
						model.replaceColor(anIntArrayArray1003[l2][0], anIntArrayArray1003[l2][walkingQueueY[l2]]);
						if(l2 == 1)
							model.replaceColor(anIntArray1204[0], anIntArray1204[walkingQueueY[l2]]);
					}

				model.buildLabelGroups();
				model.applyTransform(Animation.anims[myPlayer.standAnimId].frameIds[0]);
				model.calculateLighting(64, 850, -30, -50, -30, true);
				class9.enabledMediaType = 5;
				class9.mediaID = 0;
				RSInterface.clearModelCache(aBoolean994, model);
			}
			return;
		}
		if(j == 328) {
			RSInterface rsInterface = class9;
			int verticleTilt = 150;
			int animationSpeed = (int)(Math.sin((double)loopCycle / 40D) * 256D) & 0x7ff;
			rsInterface.modelRotation1 = verticleTilt;
			rsInterface.modelRotation2 = animationSpeed;
			if(aBoolean1031) {
				Model characterDisplay = myPlayer.getPlayerModel();
				for(int l2 = 0; l2 < 5; l2++)
					if(walkingQueueY[l2] != 0) {
						characterDisplay.replaceColor(anIntArrayArray1003[l2][0], anIntArrayArray1003[l2][walkingQueueY[l2]]);
						if(l2 == 1)
							characterDisplay.replaceColor(anIntArray1204[0], anIntArray1204[walkingQueueY[l2]]);
					}
				int staticFrame = myPlayer.standAnimId;
				characterDisplay.buildLabelGroups();
				characterDisplay.applyTransform(Animation.anims[staticFrame].frameIds[0]);
				//characterDisplay.calculateLighting(64, 850, -30, -50, -30, true);
				rsInterface.enabledMediaType = 5;
				rsInterface.mediaID = 0;
				RSInterface.clearModelCache(aBoolean994, characterDisplay);
			}
			return;
		}
		if(j == 324)
		{
			if(loginBoxSprite == null)
			{
				loginBoxSprite = class9.sprite1;
				loginDetailSprite = class9.sprite2;
			}
			if(aBoolean1047)
			{
				class9.sprite1 = loginDetailSprite;
				return;
			} else
			{
				class9.sprite1 = loginBoxSprite;
				return;
			}
		}
		if(j == 325)
		{
			if(loginBoxSprite == null)
			{
				loginBoxSprite = class9.sprite1;
				loginDetailSprite = class9.sprite2;
			}
			if(aBoolean1047)
			{
				class9.sprite1 = loginBoxSprite;
				return;
			} else
			{
				class9.sprite1 = loginDetailSprite;
				return;
			}
		}
		if(j == 600)
		{
			class9.message = reportAbuseInput;
			if(loopCycle % 20 < 10)
			{
				class9.message += "|";
				return;
			} else
			{
				class9.message += " ";
				return;
			}
		}
		if(j == 613)
			if(myPrivilege >= 1)
			{
				if(canMute)
				{
					class9.textColor = 0xff0000;
					class9.message = "Moderator option: Mute player for 48 hours: <ON>";
				} else
				{
					class9.textColor = 0xffffff;
					class9.message = "Moderator option: Mute player for 48 hours: <OFF>";
				}
			} else
			{
				class9.message = "";
			}
		if(j == 650 || j == 655)
			if(walkQueueLength != 0)
			{
				String s;
				if(daysSinceLastLogin == 0)
					s = "earlier today";
				else
				if(daysSinceLastLogin == 1)
					s = "yesterday";
				else
					s = daysSinceLastLogin + " days ago";
				class9.message = "You last logged in " + s + " from: " + signlink.dns;
			} else
			{
				class9.message = "";
			}
		if(j == 651)
		{
			if(unreadMessages == 0)
			{
				class9.message = "0 unread messages";
				class9.textColor = 0xffff00;
			}
			if(unreadMessages == 1)
			{
				class9.message = "1 unread message";
				class9.textColor = 65280;
			}
			if(unreadMessages > 1)
			{
				class9.message = unreadMessages + " unread messages";
				class9.textColor = 65280;
			}
		}
		if(j == 652)
			if(daysSinceRecovChange == 201)
			{
				if(membersInt == 1)
					class9.message = "@yel@This is a non-members world: @whi@Since you are a member we";
				else
					class9.message = "";
			} else
			if(daysSinceRecovChange == 200)
			{
				class9.message = "You have not yet set any password recovery questions.";
			} else
			{
				String s1;
				if(daysSinceRecovChange == 0)
					s1 = "Earlier today";
				else
				if(daysSinceRecovChange == 1)
					s1 = "Yesterday";
				else
					s1 = daysSinceRecovChange + " days ago";
				class9.message = s1 + " you changed your recovery questions";
			}
		if(j == 653)
			if(daysSinceRecovChange == 201)
			{
				if(membersInt == 1)
					class9.message = "@whi@recommend you use a members world instead. You may use";
				else
					class9.message = "";
			} else
			if(daysSinceRecovChange == 200)
				class9.message = "We strongly recommend you do so now to secure your account.";
			else
				class9.message = "If you do not remember making this change then cancel it immediately";
		if(j == 654)
		{
			if(daysSinceRecovChange == 201)
				if(membersInt == 1)
				{
					class9.message = "@whi@this world but member benefits are unavailable whilst here.";
					return;
				} else
				{
					class9.message = "";
					return;
				}
			if(daysSinceRecovChange == 200)
			{
				class9.message = "Do this from the 'account management' area on our front webpage";
				return;
			}
			class9.message = "Do this from the 'account management' area on our front webpage";
		}
	}

	private void drawSplitPrivateChat()
	{
		if(splitPrivateChat == 0)
			return;
		TextDrawingArea textDrawingArea = boldFont;
		int i = 0;
		if(anInt1104 != 0)
			i = 1;
		for(int j = 0; j < 100; j++)
			if(chatMessages[j] != null)
			{
				int k = chatTypes[j];
				String s = chatNames[j];
				byte byte1 = 0;
				if(s != null && s.startsWith("@cr1@"))
				{
					s = s.substring(5);
					byte1 = 1;
				}
				if(s != null && s.startsWith("@cr2@"))
				{
					s = s.substring(5);
					byte1 = 2;
				}
				if((k == 3 || k == 7) && (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s)))
				{
					int l = 329 - i * 13;
					int k1 = 4;
					textDrawingArea.drawText(0, "From", l, k1);
					textDrawingArea.drawText(65535, "From", l - 1, k1);
					k1 += textDrawingArea.getTextWidth("From ");
					if(byte1 == 1)
					{
						modIcons[0].drawBackground(k1, l - 12);
						k1 += 12;
					}
					if(byte1 == 2)
					{
						modIcons[1].drawBackground(k1, l - 12);
						k1 += 12;
					}
					textDrawingArea.drawText(0, s + ": " + chatMessages[j], l, k1);
					textDrawingArea.drawText(65535, s + ": " + chatMessages[j], l - 1, k1);
					if(++i >= 5)
						return;
				}
				if(k == 5 && privateChatMode < 2)
				{
					int i1 = 329 - i * 13;
					textDrawingArea.drawText(0, chatMessages[j], i1, 4);
					textDrawingArea.drawText(65535, chatMessages[j], i1 - 1, 4);
					if(++i >= 5)
						return;
				}
				if(k == 6 && privateChatMode < 2)
				{
					int j1 = 329 - i * 13;
					textDrawingArea.drawText(0, "To " + s + ": " + chatMessages[j], j1, 4);
					textDrawingArea.drawText(65535, "To " + s + ": " + chatMessages[j], j1 - 1, 4);
					if(++i >= 5)
						return;
				}
			}

	}

	public void pushMessage(String s, int i, String s1) {
		if(i == 0 && dialogID != -1) {
			clickToContinueString = s;
			super.clickMode3 = 0;
		}
		if(backDialogID == -1)
			inputTaken = true;
		for(int j = 499; j > 0; j--) {
			chatTypes[j] = chatTypes[j - 1];
			chatNames[j] = chatNames[j - 1];
			chatMessages[j] = chatMessages[j - 1];
			chatRights[j] = chatRights[j - 1];
		}
		chatTypes[0] = i;
		chatNames[0] = s1;
		chatMessages[0] = s;
		chatRights[0] = rights;
	}
	
	public static void setTab(int id) {
        needDrawTabArea = true;
        tabID = id;
        tabAreaAltered = true;
    }
	
	private void processTabClick() {
		if(super.clickMode3 == 1) {
			if(super.saveClickX >= 524 && super.saveClickX <= 561 && super.saveClickY >= 169 && super.saveClickY < 205 && tabInterfaceIDs[0] != -1)
			{
				needDrawTabArea = true;
				tabID = 0;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 562 && super.saveClickX <= 594 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[1] != -1)
			{
				needDrawTabArea = true;
				tabID = 1;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 595 && super.saveClickX <= 626 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[2] != -1)
			{
				needDrawTabArea = true;
				tabID = 2;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 627 && super.saveClickX <= 660 && super.saveClickY >= 168 && super.saveClickY < 203 && tabInterfaceIDs[3] != -1)
			{
				needDrawTabArea = true;
				tabID = 3;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 661 && super.saveClickX <= 693 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[4] != -1)
			{
				needDrawTabArea = true;
				tabID = 4;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 694 && super.saveClickX <= 725 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[5] != -1)
			{
				needDrawTabArea = true;
				tabID = 5;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 726 && super.saveClickX <= 765 && super.saveClickY >= 169 && super.saveClickY < 205 && tabInterfaceIDs[6] != -1)
			{
				needDrawTabArea = true;
				tabID = 6;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 524 && super.saveClickX <= 561 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[7] != -1)
			{
				needDrawTabArea = true;
				tabID = 7;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 562 && super.saveClickX <= 594 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[8] != -1)
			{
				needDrawTabArea = true;
				tabID = 8;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 595 && super.saveClickX <= 627 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[9] != -1)
			{
				needDrawTabArea = true;
				tabID = 9;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 627 && super.saveClickX <= 664 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[10] != -1)
			{
				needDrawTabArea = true;
				tabID = 10;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 661 && super.saveClickX <= 694 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[11] != -1)
			{
				needDrawTabArea = true;
				tabID = 11;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 695 && super.saveClickX <= 725 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[12] != -1)
			{
				needDrawTabArea = true;
				tabID = 12;
				tabAreaAltered = true;
			}
			if(super.saveClickX >= 726 && super.saveClickX <= 765 && super.saveClickY >= 466 && super.saveClickY < 502 && tabInterfaceIDs[13] != -1)
			{
				needDrawTabArea = true;
				tabID = 13;
				tabAreaAltered = true;
			}
		}
	}

	private void resetImageProducers2() {
		if(topCenterIP != null)
			return;
		nullLoader();
		super.fullGameScreen = null;
		tabImageProducer = null;
		mapAreaIP = null;
		gameScreenIP = null;
		chatAreaIP = null;
		chatSettingIP = null;
		topSideIP1 = null;
		topSideIP2 = null;
		bottomSideIP1 = null;
		bottomSideIP2 = null;
		topCenterIP = new RSImageProducer(519, 165, getGameComponent());
		titleButtonIP = new RSImageProducer(246, 164, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		mapBack.drawBackground(0, 0);
		titleMuralIP = new RSImageProducer(246, 335, getGameComponent());
		loginMsgIP = new RSImageProducer(512, 334, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		titleIP1 = new RSImageProducer(496, 50, getGameComponent());
		titleIP2 = new RSImageProducer(269, 37, getGameComponent());
		titleIP3 = new RSImageProducer(249, 45, getGameComponent());
		welcomeScreenRaised = true;
	}

	public String getDocumentBaseHost() {
		if (signlink.mainapp != null) {
			return "";
		}
		if (super.gameFrame != null) {
			return ""; // runescape.com <- removed for Jframe to work
		} else {
			return ""; // super.getDocumentBase().getHost().toLowerCase() <- removed for Jframe to work
		}
	}

	private void drawMinimapEdgeSprite(Sprite sprite, int j, int k) {
		int l = k * k + j * j;
		if(l > 4225 && l < 0x15f90) {
			int i1 = minimapInt1 + minimapInt2 & 0x7ff;
			int j1 = Model.SINE[i1];
			int k1 = Model.COSINE[i1];
			j1 = (j1 * 256) / (minimapInt3 + 256);
			k1 = (k1 * 256) / (minimapInt3 + 256);
			int l1 = j * j1 + k * k1 >> 16;
			int i2 = j * k1 - k * j1 >> 16;
			double d = Math.atan2(l1, i2);
			int j2 = (int)(Math.sin(d) * 63D);
			int k2 = (int)(Math.cos(d) * 57D);
			mapEdge.drawRotated(83 - k2 - 20, d, (94 + j2 + 4) - 10);
		} else {
			markMinimap(sprite, k, j);
		}
	}

	private void rightClickMapArea() {
			if(super.mouseX > 703 && super.mouseX < 755 && super.mouseY > 85 && super.mouseY < 118) {
				menuActionName[1] = "Run";
				menuActionID[1] = 1051;
				menuActionRow = 2;
			}
	}


	private void rightClickChatButtons() {
		if(super.mouseX >= 5 && super.mouseX <= 61 && super.mouseY >= 482 && super.mouseY <= 503) {
			menuActionName[1] = "View All";
			menuActionID[1] = 999;
			menuActionRow = 2;
		} else if(super.mouseX >= 71 && super.mouseX <= 127 && super.mouseY >= 482 && super.mouseY <= 503) {
			menuActionName[1] = "View Game";
			menuActionID[1] = 998;
			menuActionRow = 2;
		} else if(super.mouseX >= 137 && super.mouseX <= 193 && super.mouseY >= 482 && super.mouseY <= 503) {
			menuActionName[1] = "Hide public";
			menuActionID[1] = 997;
			menuActionName[2] = "Off public";
			menuActionID[2] = 996;
			menuActionName[3] = "Friends public";
			menuActionID[3] = 995;
			menuActionName[4] = "On public";
			menuActionID[4] = 994;
			menuActionName[5] = "View public";
			menuActionID[5] = 993;
			menuActionRow = 6;
		} else if(super.mouseX >= 203 && super.mouseX <= 259 && super.mouseY >= 482 && super.mouseY <= 503) {
			menuActionName[1] = "Off private";
			menuActionID[1] = 992;
			menuActionName[2] = "Friends private";
			menuActionID[2] = 991;
			menuActionName[3] = "On private";
			menuActionID[3] = 990;
			menuActionName[4] = "View private";
			menuActionID[4] = 989;
			menuActionRow = 5;
		} else if(super.mouseX >= 269 && super.mouseX <= 325 && super.mouseY >= 482 && super.mouseY <= 503) {
			menuActionName[1] = "Off clan chat";
			menuActionID[1] = 1003;
			menuActionName[2] = "Friends clan chat";
			menuActionID[2] = 1002;
			menuActionName[3] = "On clan chat";
			menuActionID[3] = 1001;
			menuActionName[4] = "View clan chat";
			menuActionID[4] = 1000;
			menuActionRow = 5;
		} else if(super.mouseX >= 335 && super.mouseX <= 391 && super.mouseY >= 482 && super.mouseY <= 503) {
			menuActionName[1] = "Off trade";
			menuActionID[1] = 987;
			menuActionName[2] = "Friends trade";
			menuActionID[2] = 986;
			menuActionName[3] = "On trade";
			menuActionID[3] = 985;
			menuActionName[4] = "View trade";
			menuActionID[4] = 984;
			menuActionRow = 5;
		}
	}

	public void processRightClick() {
		if (activeInterfaceType != 0) {
			return;
		}
		menuActionName[0] = "Cancel";
		menuActionID[0] = 1107;
		menuActionRow = 1;
		if (fullscreenInterfaceID != -1) {
			lastItemSelectedSlot = 0;
			anInt1315 = 0;
			buildInterfaceMenu(8, RSInterface.interfaceCache[fullscreenInterfaceID], super.mouseX, 8, super.mouseY, 0);
			if (lastItemSelectedSlot != tabFlashCycleAlt) {
				tabFlashCycleAlt = lastItemSelectedSlot;
			}
			if (anInt1315 != anInt1129) {
				anInt1129 = anInt1315;
			}
			return;
		}
		buildSplitPrivateChatMenu();
		lastItemSelectedSlot = 0;
		anInt1315 = 0;
		if (super.mouseX > 0 && super.mouseY > 0 && super.mouseX < (clientSize == 0 ? 516 : clientWidth) && super.mouseY < (clientSize == 0 ? 338 : clientHeight)) {
			if (openInterfaceID != -1) {
				buildInterfaceMenu(clientSize == 0 ? 4 : 0, RSInterface.interfaceCache[openInterfaceID], super.mouseX, clientSize == 0 ? 4 : 0, super.mouseY, 0);
			} else {
				build3dScreenMenu();
			}
		}
		if (lastItemSelectedSlot != tabFlashCycleAlt) {
			tabFlashCycleAlt = lastItemSelectedSlot;
		}
		if (anInt1315 != anInt1129) {
			anInt1129 = anInt1315;
		}
		lastItemSelectedSlot = 0;
		anInt1315 = 0;
	   if(super.mouseX > (clientSize == 0 ? 548 : clientWidth - 217) && super.mouseY > (clientSize == 0 ? 207 : clientHeight - 296) && super.mouseX < (clientSize == 0 ? 740 : clientWidth) && super.mouseY < (clientSize == 0 ? 468 : clientHeight - 35)) {
			if(invOverlayInterfaceID != -1) {
				buildInterfaceMenu(548, RSInterface.interfaceCache[invOverlayInterfaceID], super.mouseX, 207, super.mouseY, 0);
			} else if(tabInterfaceIDs[tabID] != -1) {
				buildInterfaceMenu(548, RSInterface.interfaceCache[tabInterfaceIDs[tabID]], super.mouseX, 207, super.mouseY, 0);
			}
		}
		if (lastItemSelectedSlot != hintArrowType) {
			needDrawTabArea = true;
			tabAreaAltered = true;
			hintArrowType = lastItemSelectedSlot;
		}
		if (anInt1315 != anInt1044) {
			needDrawTabArea = true;
			tabAreaAltered = true;
			anInt1044 = anInt1315;
		}
		lastItemSelectedSlot = 0;
		anInt1315 = 0;
		if(super.mouseX > 0 && super.mouseY > (clientSize == 0 ? 338 : clientHeight - 165) && super.mouseX < 490 && super.mouseY < (clientSize == 0 ? 463 : clientHeight - 40)) {
			if(backDialogID != -1) {
				buildInterfaceMenu(20, RSInterface.interfaceCache[backDialogID], super.mouseX, 358, super.mouseY, 0);
			} else if(super.mouseY < 463 && super.mouseX < 490) {
				buildChatAreaMenu(super.mouseY - 338);
			}
		}
		if (backDialogID != -1 && lastItemSelectedSlot != walkDest) {
			inputTaken = true;
			walkDest = lastItemSelectedSlot;
		}
		if (backDialogID != -1 && anInt1315 != anInt1500) {
			inputTaken = true;
			anInt1500 = anInt1315;
		}
		/* Enable custom right click areas */
		if(super.mouseX > 4 && super.mouseY > (clientSize == 0 ? 480 : clientHeight - 23) && super.mouseX < (clientSize == 0 ? 516 : 516) && super.mouseY < (clientSize == 0 ? 503 : clientHeight))
			rightClickChatButtons();
		if(super.mouseX > (clientSize == 0 ? 519 : clientWidth - 246) && super.mouseY > 0 && super.mouseX < (clientSize == 0 ? 765 : clientWidth) && super.mouseY < 168)
			rightClickMapArea();

		/**/
		boolean flag = false;
		while (!flag) {
			flag = true;
			for (int j = 0; j < menuActionRow - 1; j++) {
				if (menuActionID[j] < 1000 && menuActionID[j + 1] > 1000) {
					String s = menuActionName[j];
					menuActionName[j] = menuActionName[j + 1];
					menuActionName[j + 1] = s;
					int k = menuActionID[j];
					menuActionID[j] = menuActionID[j + 1];
					menuActionID[j + 1] = k;
					k = menuActionCmd2[j];
					menuActionCmd2[j] = menuActionCmd2[j + 1];
					menuActionCmd2[j + 1] = k;
					k = menuActionCmd3[j];
					menuActionCmd3[j] = menuActionCmd3[j + 1];
					menuActionCmd3[j + 1] = k;
					k = menuActionCmd1[j];
					menuActionCmd1[j] = menuActionCmd1[j + 1];
					menuActionCmd1[j + 1] = k;
					flag = false;
				}
			}
		}
	}

	private int blendColors(int i, int j, int k)
	{
		int l = 256 - k;
		return ((i & 0xff00ff) * l + (j & 0xff00ff) * k & 0xff00ff00) + ((i & 0xff00) * l + (j & 0xff00) * k & 0xff0000) >> 8;
	}

	private void login(String s, String s1, boolean flag)
	{
		signlink.errorname = s;
		try
		{
			if(!flag)
			{
				loginMessage1 = "";
				loginMessage2 = "Connecting to server...";
				drawLoginScreen(true);
			}
			socketStream = new RSSocket(this, openSocket(43594 + portOff));
			long l = TextClass.longForName(s);
			int i = (int)(l >> 16 & 31L);
			stream.currentOffset = 0;
			stream.writeWordBigEndian(14);
			stream.writeWordBigEndian(i);
			socketStream.queueBytes(2, stream.buffer);
			for(int j = 0; j < 8; j++)
				socketStream.read();

			int k = socketStream.read();
			int i1 = k;
			if(k == 0)
			{
				socketStream.flushInputStream(inStream.buffer, 8);
				inStream.currentOffset = 0;
				chatLastTyped = inStream.readQWord();
				int ai[] = new int[4];
				ai[0] = (int)(Math.random() * 99999999D);
				ai[1] = (int)(Math.random() * 99999999D);
				ai[2] = (int)(chatLastTyped >> 32);
				ai[3] = (int)chatLastTyped;
				stream.currentOffset = 0;
				stream.writeWordBigEndian(10);
				stream.writeDWord(ai[0]);
				stream.writeDWord(ai[1]);
				stream.writeDWord(ai[2]);
				stream.writeDWord(ai[3]);
				stream.writeDWord(/*signlink.uid*/999999);
				stream.writeString(s);
				stream.writeString(s1);
				stream.doKeys();
				outStream.currentOffset = 0;
				if(flag)
					outStream.writeWordBigEndian(18);
				else
					outStream.writeWordBigEndian(16);
				outStream.writeWordBigEndian(stream.currentOffset + 36 + 1 + 1 + 2);
				outStream.writeWordBigEndian(255);
				outStream.writeWord(317);
				outStream.writeWordBigEndian(lowMem ? 1 : 0);
				for(int l1 = 0; l1 < 9; l1++)
					outStream.writeDWord(expectedCRCs[l1]);

				outStream.writeBytes(stream.buffer, stream.currentOffset, 0);
				stream.encryption = new ISAACRandomGen(ai);
				for(int j2 = 0; j2 < 4; j2++)
					ai[j2] += 50;

				encryption = new ISAACRandomGen(ai);
				socketStream.queueBytes(outStream.currentOffset, outStream.buffer);
				k = socketStream.read();
			}
			if(k == 1)
			{
				try
				{
					Thread.sleep(2000L);
				}
				catch(Exception _ex) { }
				login(s, s1, flag);
				return;
			}
			if(k == 2)
			{
				myPrivilege = socketStream.read();
				flagged = socketStream.read() == 1;
				aLong1220 = 0L;
				chatAreaScrollMax = 0;
				mouseDetection.coordsIndex = 0;
				super.awtFocus = true;
				scrollBarDrag = true;
				loggedIn = true;
				stream.currentOffset = 0;
				inStream.currentOffset = 0;
				pktType = -1;
				entityUpdateCount = -1;
				entityUpdateIndex = -1;
				chatScrollMax = -1;
				pktSize = 0;
				idleTime = 0;
				anInt1104 = 0;
				hintIconDelay = 0;
				minimapRotation = 0;
				menuActionRow = 0;
				menuOpen = false;
				super.idleTime = 0;
				for(int j1 = 0; j1 < 100; j1++)
					chatMessages[j1] = null;

				itemSelected = 0;
				spellSelected = 0;
				loadingStage = 0;
				lastMapRegionX = 0;
				anInt1278 = (int)(Math.random() * 100D) - 50;
				cameraOscillationH = (int)(Math.random() * 110D) - 55;
				lastItemSelectedInterface = (int)(Math.random() * 80D) - 40;
				minimapInt2 = (int)(Math.random() * 120D) - 60;
				minimapInt3 = (int)(Math.random() * 30D) - 20;
				minimapInt1 = (int)(Math.random() * 20D) - 10 & 0x7ff;
				chatAreaScrollPos = 0;
				activeInterfaceId = -1;
				destX = 0;
				destY = 0;
				playerCount = 0;
				npcCount = 0;
				for(int i2 = 0; i2 < maxPlayers; i2++)
				{
					playerArray[i2] = null;
					playerBuffers[i2] = null;
				}

				for(int k2 = 0; k2 < 16384; k2++)
					npcArray[k2] = null;

				myPlayer = playerArray[myPlayerIndex] = new Player();
				projectileList.removeAll();
				spotAnimList.removeAll();
				for(int l2 = 0; l2 < 4; l2++)
				{
					for(int i3 = 0; i3 < 104; i3++)
					{
						for(int k3 = 0; k3 < 104; k3++)
							groundArray[l2][i3][k3] = null;

					}

				}

				spawnObjectList = new NodeList();
				fullscreenInterfaceID = -1;
				mapRegionCount = 0;
				friendsCount = 0;
				dialogID = -1;
				backDialogID = -1;
				openInterfaceID = -1;
				invOverlayInterfaceID = -1;
				anInt1018 = -1;
				aBoolean1149 = false;
				tabID = 3;
				inputDialogState = 0;
				menuOpen = false;
				messagePromptRaised = false;
				clickToContinueString = null;
				flashingSideicon = 0;
				flashingTab = -1;
				aBoolean1047 = true;
				resetDefaultAppearance();
				for(int j3 = 0; j3 < 5; j3++)
					walkingQueueY[j3] = 0;

				for(int l3 = 0; l3 < 5; l3++)
				{
					atPlayerActions[l3] = null;
					atPlayerArray[l3] = false;
				}

				anInt1175_static = 0;
				anInt1134_static = 0;
				anInt986_static = 0;
				anInt1288_static = 0;
				anInt924_static = 0;
				anInt1188_static = 0;
				anInt1155_static = 0;
				anInt1226_static = 0;
				int serverUpdateCounter = 0;
				int hintIconPlayerIndex = 0;
				resetImageProducers2();
				return;
			}
			if(k == 3)
			{
				loginMessage1 = "";
				loginMessage2 = "Invalid username or password.";
				return;
			}
			if(k == 4)
			{
				loginMessage1 = "Your account has been disabled.";
				loginMessage2 = "Please check your message-center for details.";
				return;
			}
			if(k == 5)
			{
				loginMessage1 = "Your account is already logged in.";
				loginMessage2 = "Try again in 60 secs...";
				return;
			}
			if(k == 6)
			{
				loginMessage1 = "RuneScape has been updated!";
				loginMessage2 = "Please reload this page.";
				return;
			}
			if(k == 7)
			{
				loginMessage1 = "This world is full.";
				loginMessage2 = "Please use a different world.";
				return;
			}
			if(k == 8)
			{
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Login server offline.";
				return;
			}
			if(k == 9)
			{
				loginMessage1 = "Login limit exceeded.";
				loginMessage2 = "Too many connections from your address.";
				return;
			}
			if(k == 10)
			{
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Bad session id.";
				return;
			}
			if(k == 11)
			{
				loginMessage2 = "Login server rejected session.";
				loginMessage2 = "Please try again.";
				return;
			}
			if(k == 12)
			{
				loginMessage1 = "You need a members account to login to this world.";
				loginMessage2 = "Please subscribe, or use a different world.";
				return;
			}
			if(k == 13)
			{
				loginMessage1 = "Could not complete login.";
				loginMessage2 = "Please try using a different world.";
				return;
			}
			if(k == 14)
			{
				loginMessage1 = "The server is being updated.";
				loginMessage2 = "Please wait 1 minute and try again.";
				return;
			}
			if(k == 15)
			{
				loggedIn = true;
				stream.currentOffset = 0;
				inStream.currentOffset = 0;
				pktType = -1;
				entityUpdateCount = -1;
				entityUpdateIndex = -1;
				chatScrollMax = -1;
				pktSize = 0;
				idleTime = 0;
				anInt1104 = 0;
				menuActionRow = 0;
				menuOpen = false;
				serverSeed = System.currentTimeMillis();
				return;
			}
			if(k == 16)
			{
				loginMessage1 = "Login attempts exceeded.";
				loginMessage2 = "Please wait 1 minute and try again.";
				return;
			}
			if(k == 17)
			{
				loginMessage1 = "You are standing in a members-only area.";
				loginMessage2 = "To play on this world move to a free area first";
				return;
			}
			if(k == 20)
			{
				loginMessage1 = "Invalid loginserver requested";
				loginMessage2 = "Please try using a different world.";
				return;
			}
			if(k == 21)
			{
				for(int k1 = socketStream.read(); k1 >= 0; k1--)
				{
					loginMessage1 = "You have only just left another world";
					loginMessage2 = "Your profile will be transferred in: " + k1 + " seconds";
					drawLoginScreen(true);
					try
					{
						Thread.sleep(1000L);
					}
					catch(Exception _ex) { }
				}

				login(s, s1, flag);
				return;
			}
			if(k == -1)
			{
				if(i1 == 0)
				{
					if(loginFailures < 2)
					{
						try
						{
							Thread.sleep(2000L);
						}
						catch(Exception _ex) { }
						loginFailures++;
						login(s, s1, flag);
						return;
					} else
					{
						loginMessage1 = "No response from loginserver";
						loginMessage2 = "Please wait 1 minute and try again.";
						return;
					}
				} else
				{
					loginMessage1 = "No response from server";
					loginMessage2 = "Please try using a different world.";
					return;
				}
			} else
			{
				System.out.println("response:" + k);
				loginMessage1 = "Unexpected server response";
				loginMessage2 = "Please try using a different world.";
				return;
			}
		}
		catch(IOException _ex)
		{
			loginMessage1 = "";
		}
		loginMessage2 = "Error connecting to server.";
	}

	private boolean doWalkTo(int i, int j, int k, int i1, int j1, int k1, int l1, int i2, int j2, boolean flag, int k2) {
		byte byte0 = 104;
		byte byte1 = 104;
		for(int l2 = 0; l2 < byte0; l2++) {
			for(int i3 = 0; i3 < byte1; i3++) {
				mapRegions[l2][i3] = 0;
				constructMapTiles[l2][i3] = 0x5f5e0ff;
			}
		}
		int j3 = j2;
		int k3 = j1;
		mapRegions[j2][j1] = 99;
		constructMapTiles[j2][j1] = 0;
		int l3 = 0;
		int i4 = 0;
		bigX[l3] = j2;
		bigY[l3++] = j1;
		boolean flag1 = false;
		int j4 = bigX.length;
		int ai[][] = aCollisionMapArray1230[plane].flags;
		while(i4 != l3) 
		{
			j3 = bigX[i4];
			k3 = bigY[i4];
			i4 = (i4 + 1) % j4;
			if(j3 == k2 && k3 == i2)
			{
				flag1 = true;
				break;
			}
			if(i1 != 0)
			{
				if((i1 < 5 || i1 == 10) && aCollisionMapArray1230[plane].canReachWall(k2, j3, k3, j, i1 - 1, i2))
				{
					flag1 = true;
					break;
				}
				if(i1 < 10 && aCollisionMapArray1230[plane].canReachDeco(k2, i2, k3, i1 - 1, j, j3))
				{
					flag1 = true;
					break;
				}
			}
			if(k1 != 0 && k != 0 && aCollisionMapArray1230[plane].canReachObject(i2, k2, j3, k, l1, k1, k3))
			{
				flag1 = true;
				break;
			}
			int l4 = constructMapTiles[j3][k3] + 1;
			if(j3 > 0 && mapRegions[j3 - 1][k3] == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0)
			{
				bigX[l3] = j3 - 1;
				bigY[l3] = k3;
				l3 = (l3 + 1) % j4;
				mapRegions[j3 - 1][k3] = 2;
				constructMapTiles[j3 - 1][k3] = l4;
			}
			if(j3 < byte0 - 1 && mapRegions[j3 + 1][k3] == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0)
			{
				bigX[l3] = j3 + 1;
				bigY[l3] = k3;
				l3 = (l3 + 1) % j4;
				mapRegions[j3 + 1][k3] = 8;
				constructMapTiles[j3 + 1][k3] = l4;
			}
			if(k3 > 0 && mapRegions[j3][k3 - 1] == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0)
			{
				bigX[l3] = j3;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				mapRegions[j3][k3 - 1] = 1;
				constructMapTiles[j3][k3 - 1] = l4;
			}
			if(k3 < byte1 - 1 && mapRegions[j3][k3 + 1] == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0)
			{
				bigX[l3] = j3;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				mapRegions[j3][k3 + 1] = 4;
				constructMapTiles[j3][k3 + 1] = l4;
			}
			if(j3 > 0 && k3 > 0 && mapRegions[j3 - 1][k3 - 1] == 0 && (ai[j3 - 1][k3 - 1] & 0x128010e) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0)
			{
				bigX[l3] = j3 - 1;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				mapRegions[j3 - 1][k3 - 1] = 3;
				constructMapTiles[j3 - 1][k3 - 1] = l4;
			}
			if(j3 < byte0 - 1 && k3 > 0 && mapRegions[j3 + 1][k3 - 1] == 0 && (ai[j3 + 1][k3 - 1] & 0x1280183) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0)
			{
				bigX[l3] = j3 + 1;
				bigY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				mapRegions[j3 + 1][k3 - 1] = 9;
				constructMapTiles[j3 + 1][k3 - 1] = l4;
			}
			if(j3 > 0 && k3 < byte1 - 1 && mapRegions[j3 - 1][k3 + 1] == 0 && (ai[j3 - 1][k3 + 1] & 0x1280138) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0)
			{
				bigX[l3] = j3 - 1;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				mapRegions[j3 - 1][k3 + 1] = 6;
				constructMapTiles[j3 - 1][k3 + 1] = l4;
			}
			if(j3 < byte0 - 1 && k3 < byte1 - 1 && mapRegions[j3 + 1][k3 + 1] == 0 && (ai[j3 + 1][k3 + 1] & 0x12801e0) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0)
			{
				bigX[l3] = j3 + 1;
				bigY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				mapRegions[j3 + 1][k3 + 1] = 12;
				constructMapTiles[j3 + 1][k3 + 1] = l4;
			}
		}
		hintIconX = 0;
		if(!flag1)
		{
			if(flag)
			{
				int i5 = 100;
				for(int k5 = 1; k5 < 2; k5++)
				{
					for(int i6 = k2 - k5; i6 <= k2 + k5; i6++)
					{
						for(int l6 = i2 - k5; l6 <= i2 + k5; l6++)
							if(i6 >= 0 && l6 >= 0 && i6 < 104 && l6 < 104 && constructMapTiles[i6][l6] < i5)
							{
								i5 = constructMapTiles[i6][l6];
								j3 = i6;
								k3 = l6;
								hintIconX = 1;
								flag1 = true;
							}

					}

					if(flag1)
						break;
				}

			}
			if(!flag1)
				return false;
		}
		i4 = 0;
		bigX[i4] = j3;
		bigY[i4++] = k3;
		int l5;
		for(int j5 = l5 = mapRegions[j3][k3]; j3 != j2 || k3 != j1; j5 = mapRegions[j3][k3])
		{
			if(j5 != l5)
			{
				l5 = j5;
				bigX[i4] = j3;
				bigY[i4++] = k3;
			}
			if((j5 & 2) != 0)
				j3++;
			else
			if((j5 & 8) != 0)
				j3--;
			if((j5 & 1) != 0)
				k3++;
			else
			if((j5 & 4) != 0)
				k3--;
		}
//	if(cancelWalk) { return i4 > 0; }
	

		if(i4 > 0)
		{
			int k4 = i4;
			if(k4 > 25)
				k4 = 25;
			i4--;
			int k6 = bigX[i4];
			int i7 = bigY[i4];
			anInt1288_static += k4;
			if(anInt1288_static >= 92)
			{
				stream.createFrame(36);
				stream.writeDWord(0);
				anInt1288_static = 0;
			}
			if(i == 0)
			{
				stream.createFrame(164);
				stream.writeWordBigEndian(k4 + k4 + 3);
			}
			if(i == 1)
			{
				stream.createFrame(248);
				stream.writeWordBigEndian(k4 + k4 + 3 + 14);
			}
			if(i == 2)
			{
				stream.createFrame(98);
				stream.writeWordBigEndian(k4 + k4 + 3);
			}
			stream.writeWordLEBigA(k6 + baseX);
			destX = bigX[0];
			destY = bigY[0];
			for(int j7 = 1; j7 < k4; j7++)
			{
				i4--;
				stream.writeWordBigEndian(bigX[i4] - k6);
				stream.writeWordBigEndian(bigY[i4] - i7);
			}

			stream.writeWordLEA(i7 + baseY);
			stream.writeNegByte(super.keyArray[5] != 1 ? 0 : 1);
			return true;
		}
		return i != 1;
	}

	private void parseNPCUpdateMasks(Stream stream)
	{
		for(int j = 0; j < entityCount; j++)
		{
			int k = entityIndices[j];
			NPC npc = npcArray[k];
			int l = stream.readUnsignedByte();
			if((l & 0x10) != 0)
			{
				int i1 = stream.readWordLE();
				if(i1 == 65535)
					i1 = -1;
				int i2 = stream.readUnsignedByte();
				if(i1 == npc.anim && i1 != -1)
				{
					int l2 = Animation.anims[i1].replayMode;
					if(l2 == 1)
					{
						npc.animFrame = 0;
						npc.animCycle = 0;
						npc.animDelay = i2;
						npc.animFrameCount = 0;
					}
					if(l2 == 2)
						npc.animFrameCount = 0;
				} else
				if(i1 == -1 || npc.anim == -1 || Animation.anims[i1].priority >= Animation.anims[npc.anim].priority)
				{
					npc.anim = i1;
					npc.animFrame = 0;
					npc.animCycle = 0;
					npc.animDelay = i2;
					npc.animFrameCount = 0;
					npc.pathRemainder = npc.smallXYIndex;
				}
			}
			if((l & 8) != 0)
			{
				int j1 = stream.readUnsignedByteAdd();
				int j2 = stream.readUnsignedByteNeg();
				npc.updateHitData(j2, j1, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
				npc.currentHealth = stream.readUnsignedByteAdd();
				npc.maxHealth = stream.readUnsignedByte();
			}
			if((l & 0x80) != 0)
			{
				npc.spotAnimId = stream.readUnsignedWord();
				int k1 = stream.readDWord();
				npc.spotAnimHeight = k1 >> 16;
				npc.spotAnimDelay = loopCycle + (k1 & 0xffff);
				npc.spotAnimFrame = 0;
				npc.spotAnimCycle = 0;
				if(npc.spotAnimDelay > loopCycle)
					npc.spotAnimFrame = -1;
				if(npc.spotAnimId == 65535)
					npc.spotAnimId = -1;
			}
			if((l & 0x20) != 0)
			{
				npc.interactingEntity = stream.readUnsignedWord();
				if(npc.interactingEntity == 65535)
					npc.interactingEntity = -1;
			}
			if((l & 1) != 0)
			{
				npc.textSpoken = stream.readString();
				npc.textCycle = 100;
//	entityMessage(npc);
	
			}
			if((l & 0x40) != 0)
			{
				int l1 = stream.readUnsignedByteNeg();
				int k2 = stream.readUnsignedByteSub();
				npc.updateHitData(k2, l1, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
				npc.currentHealth = stream.readUnsignedByteSub();
				npc.maxHealth = stream.readUnsignedByteNeg();
			}
			if((l & 2) != 0)
			{
				npc.desc = EntityDef.forID(stream.readWordLEBigA());
				npc.tileSize = npc.desc.tileSpan;
				npc.turnSpeed = npc.desc.degreesToTurn;
				npc.walkBackAnimId = npc.desc.walkAnim;
				npc.walkLeftAnimId = npc.desc.turnAroundAnim;
				npc.walkRightAnimId = npc.desc.walkRightAnim;
				npc.runAnimId = npc.desc.walkBackAnim;
				npc.standAnimId = npc.desc.standAnim;
			}
			if((l & 4) != 0)
			{
				npc.textEffect = stream.readWordLE();
				npc.textAlpha = stream.readWordLE();
			}
		}
	}

	private void buildAtNPCMenu(EntityDef entityDef, int i, int j, int k)
	{
		if(menuActionRow >= 400)
			return;
		if(entityDef.childrenIDs != null)
			entityDef = entityDef.getChildDefinition();
		if(entityDef == null)
			return;
		if(!entityDef.clickable)
			return;
		String s = entityDef.name;
		if(entityDef.combatLevel != 0)
			s = s + combatDiffColor(myPlayer.combatLevel, entityDef.combatLevel) + " (level-" + entityDef.combatLevel + ")";
		if(itemSelected == 1)
		{
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @yel@" + s;
			menuActionID[menuActionRow] = 582;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
			return;
		}
		if(spellSelected == 1)
		{
			if((spellUsableOn & 2) == 2)
			{
				menuActionName[menuActionRow] = spellTooltip + " @yel@" + s;
				menuActionID[menuActionRow] = 413;
				menuActionCmd1[menuActionRow] = i;
				menuActionCmd2[menuActionRow] = k;
				menuActionCmd3[menuActionRow] = j;
				menuActionRow++;
			}
		} else
		{
			if(entityDef.actions != null)
			{
				for(int l = 4; l >= 0; l--)
					if(entityDef.actions[l] != null && !entityDef.actions[l].equalsIgnoreCase("attack"))
					{
						menuActionName[menuActionRow] = entityDef.actions[l] + " @yel@" + s;
						if(l == 0)
							menuActionID[menuActionRow] = 20;
						if(l == 1)
							menuActionID[menuActionRow] = 412;
						if(l == 2)
							menuActionID[menuActionRow] = 225;
						if(l == 3)
							menuActionID[menuActionRow] = 965;
						if(l == 4)
							menuActionID[menuActionRow] = 478;
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}

			}
			if(entityDef.actions != null)
			{
				for(int i1 = 4; i1 >= 0; i1--)
					if(entityDef.actions[i1] != null && entityDef.actions[i1].equalsIgnoreCase("attack"))
					{
						char c = '\0';
						if(entityDef.combatLevel > myPlayer.combatLevel)
							c = '\u07D0';
						menuActionName[menuActionRow] = entityDef.actions[i1] + " @yel@" + s;
						if(i1 == 0)
							menuActionID[menuActionRow] = 20 + c;
						if(i1 == 1)
							menuActionID[menuActionRow] = 412 + c;
						if(i1 == 2)
							menuActionID[menuActionRow] = 225 + c;
						if(i1 == 3)
							menuActionID[menuActionRow] = 965 + c;
						if(i1 == 4)
							menuActionID[menuActionRow] = 478 + c;
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}

			}
			//menuActionName[menuActionRow] = "Examine @yel@" + s + " @gre@(@whi@" + entityDef.type + "@gre@)";
			menuActionName[menuActionRow] = "Examine @yel@" + s;
			menuActionID[menuActionRow] = 1025;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
		}
	}

	private void buildAtPlayerMenu(int i, int j, Player player, int k)
	{
		if(player == myPlayer)
			return;
		if(menuActionRow >= 400)
			return;
		String s;
		if(player.skill == 0)
			s = player.name + combatDiffColor(myPlayer.combatLevel, player.combatLevel) + " (level-" + player.combatLevel + ")";
		else
			s = player.name + " (skill-" + player.skill + ")";
		if(itemSelected == 1)
		{
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @whi@" + s;
			menuActionID[menuActionRow] = 491;
			menuActionCmd1[menuActionRow] = j;
			menuActionCmd2[menuActionRow] = i;
			menuActionCmd3[menuActionRow] = k;
			menuActionRow++;
		} else
		if(spellSelected == 1)
		{
			if((spellUsableOn & 8) == 8)
			{
				menuActionName[menuActionRow] = spellTooltip + " @whi@" + s;
				menuActionID[menuActionRow] = 365;
				menuActionCmd1[menuActionRow] = j;
				menuActionCmd2[menuActionRow] = i;
				menuActionCmd3[menuActionRow] = k;
				menuActionRow++;
			}
		} else
		{
			for(int l = 4; l >= 0; l--)
				if(atPlayerActions[l] != null)
				{
					menuActionName[menuActionRow] = atPlayerActions[l] + " @whi@" + s;
					char c = '\0';
					if(atPlayerActions[l].equalsIgnoreCase("attack"))
					{
						if(player.combatLevel > myPlayer.combatLevel)
							c = '\u07D0';
						if(myPlayer.team != 0 && player.team != 0)
							if(myPlayer.team == player.team)
								c = '\u07D0';
							else
								c = '\0';
					} else
					if(atPlayerArray[l])
						c = '\u07D0';
					if(l == 0)
						menuActionID[menuActionRow] = 561 + c;
					if(l == 1)
						menuActionID[menuActionRow] = 779 + c;
					if(l == 2)
						menuActionID[menuActionRow] = 27 + c;
					if(l == 3)
						menuActionID[menuActionRow] = 577 + c;
					if(l == 4)
						menuActionID[menuActionRow] = 729 + c;
					menuActionCmd1[menuActionRow] = j;
					menuActionCmd2[menuActionRow] = i;
					menuActionCmd3[menuActionRow] = k;
					menuActionRow++;
				}

		}
		for(int i1 = 0; i1 < menuActionRow; i1++)
			if(menuActionID[i1] == 516)
			{
				menuActionName[i1] = "Walk here @whi@" + s;
				return;
			}

	}

	private void updateSpawnObjectInfo(SpawnObjectNode spawnObjectNode)
	{
		int i = 0;
		int j = -1;
		int k = 0;
		int l = 0;
		if(spawnObjectNode.group == 0)
			i = worldController.getWallObjectUID(spawnObjectNode.objectPlane, spawnObjectNode.objectX, spawnObjectNode.objectY);
		if(spawnObjectNode.group == 1)
			i = worldController.getWallDecorationUID(spawnObjectNode.objectPlane, spawnObjectNode.objectX, spawnObjectNode.objectY);
		if(spawnObjectNode.group == 2)
			i = worldController.getInteractiveObjectUID(spawnObjectNode.objectPlane, spawnObjectNode.objectX, spawnObjectNode.objectY);
		if(spawnObjectNode.group == 3)
			i = worldController.getGroundDecorationUID(spawnObjectNode.objectPlane, spawnObjectNode.objectX, spawnObjectNode.objectY);
		if(i != 0)
		{
			int i1 = worldController.getObjectConfig(spawnObjectNode.objectPlane, spawnObjectNode.objectX, spawnObjectNode.objectY, i);
			j = i >> 14 & 0x7fff;
			k = i1 & 0x1f;
			l = i1 >> 6;
		}
		spawnObjectNode.previousId = j;
		spawnObjectNode.previousType = k;
		spawnObjectNode.previousOrientation = l;
	}

	private void processSounds()
	{
		for(int i = 0; i < lastMapRegionX; i++)
			if(mapObjectIds[i] <= 0)
			{
				boolean flag1 = false;
				try
				{
					if(tabAreaY[i] == tabFlashIndex && menuActionCmd5[i] == anInt1289)
					{
						if(!replayWave())
							flag1 = true;
					} else
					{
						Stream stream = Sounds.getSoundBuffer(menuActionCmd5[i], tabAreaY[i]);
						if(System.currentTimeMillis() + (long)(stream.currentOffset / 22) > loginTimer + (long)(anInt1257 / 22))
						{
							anInt1257 = stream.currentOffset;
							loginTimer = System.currentTimeMillis();
							if(saveWave(stream.buffer, stream.currentOffset))
							{
								tabFlashIndex = tabAreaY[i];
								anInt1289 = menuActionCmd5[i];
							} else
							{
								flag1 = true;
							}
						}
					}
				}
				catch(Exception exception) { }
				if(!flag1 || mapObjectIds[i] == -5)
				{
					lastMapRegionX--;
					for(int j = i; j < lastMapRegionX; j++)
					{
						tabAreaY[j] = tabAreaY[j + 1];
						menuActionCmd5[j] = menuActionCmd5[j + 1];
						mapObjectIds[j] = mapObjectIds[j + 1];
					}

					i--;
				} else
				{
					mapObjectIds[i] = -5;
				}
			} else
			{
				mapObjectIds[i]--;
			}

		if(prevSong > 0)
		{
			prevSong -= 20;
			if(prevSong < 0)
				prevSong = 0;
			if(prevSong == 0 && musicEnabled && !lowMem)
			{
				nextSong = currentSong;
				songChanging = true;
				onDemandFetcher.requestFile(2, nextSong);
			}
		}
	}

	void startUp()
	{
		drawLoadingText(20, "Starting up");
		if(signlink.sunjava)
			super.minDelay = 5;
		if(aBoolean993_static)
		{
 //		   rsAlreadyLoaded = true;
 //		   return;
		}
		aBoolean993_static = true;
		boolean flag = true;
		String s = getDocumentBaseHost();
		if(signlink.cache_dat != null)
		{
			for(int i = 0; i < 5; i++)
				decompressors[i] = new Decompressor(signlink.cache_dat, signlink.cache_idx[i], i + 1);
		} try {
			titleStreamLoader = streamLoaderForName(1, "title screen", "title", expectedCRCs[1], 25);
			smallText = new TextDrawingArea(false, "p11_full", titleStreamLoader);
			boldFont = new TextDrawingArea(false, "p12_full", titleStreamLoader);
			chatTextDrawingArea = new TextDrawingArea(false, "b12_full", titleStreamLoader);
			TextDrawingArea fancyFont = new TextDrawingArea(true, "q8_full", titleStreamLoader);
			drawLogo();
			loadTitleScreen();
			StreamLoader streamLoader = streamLoaderForName(2, "config", "config", expectedCRCs[2], 30);
			StreamLoader streamLoader_1 = streamLoaderForName(3, "interface", "interface", expectedCRCs[3], 35);
			StreamLoader streamLoader_2 = streamLoaderForName(4, "2d graphics", "media", expectedCRCs[4], 40);
			StreamLoader streamLoader_3 = streamLoaderForName(6, "textures", "textures", expectedCRCs[6], 45);
			StreamLoader streamLoader_4 = streamLoaderForName(7, "chat system", "wordenc", expectedCRCs[7], 50);
			StreamLoader streamLoader_5 = streamLoaderForName(8, "sound effects", "sounds", expectedCRCs[8], 55);
			byteGroundArray = new byte[4][104][104];
			intGroundArray = new int[4][105][105];
			worldController = new WorldController(intGroundArray);
			for(int j = 0; j < 4; j++)
				aCollisionMapArray1230[j] = new CollisionMap();

			minimapSprite = new Sprite(512, 512);
			StreamLoader streamLoader_6 = streamLoaderForName(5, "update list", "versionlist", expectedCRCs[5], 60);
			drawLoadingText(60, "Connecting to update server");
			onDemandFetcher = new OnDemandFetcher();
			onDemandFetcher.start(streamLoader_6, this);
			AnimFrame.loadCustomAnimations(onDemandFetcher.getAnimCount());
			Model.initModelStorage(onDemandFetcher.getVersionCount(0), onDemandFetcher);
			ModelDecompressor.loadModels();
			ModelDecompressor.loadModels2();
			preloadModels();
			if(!lowMem)
			{
				nextSong = 0;
				try
				{
					nextSong = 0; // music param disabled
				}
				catch(Exception _ex) { }
				songChanging = true;
				onDemandFetcher.requestFile(2, nextSong);
				while(onDemandFetcher.getNodeCount() > 0)
				{
					processOnDemandQueue();
					try
					{
						Thread.sleep(100L);
					}
					catch(Exception _ex) { }
					if(onDemandFetcher.anInt1349 > 3)
					{
						loadError();
						return;
					}
				}
			}
			drawLoadingText(65, "Requesting animations");
			int k = onDemandFetcher.getVersionCount(1);
			for(int i1 = 0; i1 < k; i1++)
				onDemandFetcher.requestFile(1, i1);

			while(onDemandFetcher.getNodeCount() > 0)
			{
				int j1 = k - onDemandFetcher.getNodeCount();
				if(j1 > 0)
					drawLoadingText(65, "Loading animations - " + (j1 * 100) / k + "%");
				processOnDemandQueue();
				try
				{
					Thread.sleep(100L);
				}
				catch(Exception _ex) { }
				if(onDemandFetcher.anInt1349 > 3)
				{
					loadError();
					return;
				}
			}
			drawLoadingText(70, "Requesting models");
			k = onDemandFetcher.getVersionCount(0);
			for(int k1 = 0; k1 < k; k1++)
			{
				int l1 = onDemandFetcher.getModelIndex(k1);
				if((l1 & 1) != 0)
					onDemandFetcher.requestFile(0, k1);
			}

			k = onDemandFetcher.getNodeCount();
			while(onDemandFetcher.getNodeCount() > 0)
			{
				int i2 = k - onDemandFetcher.getNodeCount();
				if(i2 > 0)
					drawLoadingText(70, "Loading models - " + (i2 * 100) / k + "%");
				processOnDemandQueue();
				try
				{
					Thread.sleep(100L);
				}
				catch(Exception _ex) { }
			}
			if(decompressors[0] != null)
			{
				drawLoadingText(75, "Requesting maps");
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(0, 48, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(1, 48, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(0, 48, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(1, 48, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(0, 48, 49));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(1, 48, 49));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(0, 47, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(1, 47, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(0, 47, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(1, 47, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(0, 148, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFile(1, 148, 48));
				k = onDemandFetcher.getNodeCount();
				while(onDemandFetcher.getNodeCount() > 0)
				{
					int j2 = k - onDemandFetcher.getNodeCount();
					if(j2 > 0)
						drawLoadingText(75, "Loading maps - " + (j2 * 100) / k + "%");
					processOnDemandQueue();
					try
					{
						Thread.sleep(100L);
					}
					catch(Exception _ex) { }
				}
			}
			k = onDemandFetcher.getVersionCount(0);
			for(int k2 = 0; k2 < k; k2++)
			{
				int l2 = onDemandFetcher.getModelIndex(k2);
				byte byte0 = 0;
				if((l2 & 8) != 0)
					byte0 = 10;
				else
				if((l2 & 0x20) != 0)
					byte0 = 9;
				else
				if((l2 & 0x10) != 0)
					byte0 = 8;
				else
				if((l2 & 0x40) != 0)
					byte0 = 7;
				else
				if((l2 & 0x80) != 0)
					byte0 = 6;
				else
				if((l2 & 2) != 0)
					byte0 = 5;
				else
				if((l2 & 4) != 0)
					byte0 = 4;
				if((l2 & 1) != 0)
					byte0 = 3;
				if(byte0 != 0)
					onDemandFetcher.requestArchive(byte0, 0, k2);
			}

			onDemandFetcher.prefetchMaps(isMembers);
			if(!lowMem)
			{
				int l = onDemandFetcher.getVersionCount(2);
				for(int i3 = 1; i3 < l; i3++)
					if(onDemandFetcher.isFileReady(i3))
						onDemandFetcher.requestArchive((byte)1, 2, i3);

			}
			drawLoadingText(80, "Unpacking media");
			/* Custom sprite unpacking */
			loadExtraSprites();
			emptyOrb = new Sprite("emptyorb");
			hoverOrb = new Sprite("hoverorb");
			hoverorbrun2 = new Sprite("hoverorbrun2");
			hoverorbrun = new Sprite("hoverorbrun");
			runClick = new Sprite("runclick");
			runorb = new Sprite("runorb");
			hitPointsFill = new Sprite("hitpointsfill");
			prayerFill = new Sprite("prayerfill");
			
			HPBarFull = new Sprite(signlink.findcachedir()+"Sprites/Player/HP 0.PNG", 1);
			HPBarEmpty = new Sprite(signlink.findcachedir()+"Sprites/Player/HP 1.PNG", 1);

			chatArea = new Sprite("chatarea");
			tabArea = new Sprite("tabarea");
			mapArea = new Sprite("maparea");
			multiOverlay = new Sprite(streamLoader_2, "overlay_multiway", 0);
			/**/
			mapBack = new Background(streamLoader_2, "mapback", 0);
			for(int c1 = 0; c1 <= 3; c1++)
				chatButtons[c1] = new Sprite(streamLoader_2, "chatbuttons", c1);
			for(int j3 = 0; j3 <= 14; j3++)
				sideIcons[j3] = new Sprite(streamLoader_2, "sideicons", j3);
			for(int r1 = 0; r1 < 5; r1++)
				redStones[r1] = new Sprite("redstones " + r1);
			compass = new Sprite(streamLoader_2, "compass", 0);
			mapEdge = new Sprite(streamLoader_2, "mapedge", 0);
			mapEdge.drawCentered();
			try
			{
				for(int k3 = 0; k3 < 100; k3++)
					mapScenes[k3] = new Background(streamLoader_2, "mapscene", k3);
			}
			catch(Exception _ex) { }
			try
			{
				for(int l3 = 0; l3 < 100; l3++)
					mapFunctions[l3] = new Sprite(streamLoader_2, "mapfunction", l3);
			}
			catch(Exception _ex) { }
			try
			{
				for(int i4 = 0; i4 < 20; i4++)
					hitMarks[i4] = new Sprite(streamLoader_2, "hitmarks", i4);
			}
			catch(Exception _ex) { }
			try
			{
				for(int h1 = 0; h1 < 6; h1++)
					headIconsHint[h1] = new Sprite(streamLoader_2, "headicons_hint", h1);
			} catch(Exception _ex) { }
			try {
				for(int j4 = 0; j4 < 8; j4++)
					headIcons[j4] = new Sprite(streamLoader_2, "headicons_prayer", j4);
				for(int j45 = 0; j45 < 3; j45++)
					skullIcons[j45] = new Sprite(streamLoader_2, "headicons_pk", j45 );
			}
			catch(Exception _ex) { }
			mapFlag = new Sprite(streamLoader_2, "mapmarker", 0);
			mapMarker = new Sprite(streamLoader_2, "mapmarker", 1);
			for(int k4 = 0; k4 < 8; k4++)
				crosses[k4] = new Sprite(streamLoader_2, "cross", k4);

			mapDotItem = new Sprite(streamLoader_2, "mapdots", 0);
			mapDotNPC = new Sprite(streamLoader_2, "mapdots", 1);
			mapDotPlayer = new Sprite(streamLoader_2, "mapdots", 2);
			mapDotFriend = new Sprite(streamLoader_2, "mapdots", 3);
			mapDotTeam = new Sprite(streamLoader_2, "mapdots", 4);
			mapDotClan = new Sprite(streamLoader_2, "mapdots", 5);
			scrollBar1 = new Sprite(streamLoader_2, "scrollbar", 0);
			scrollBar2 = new Sprite(streamLoader_2, "scrollbar", 1);

			for(int l4 = 0; l4 < 2; l4++)
				modIcons[l4] = new Background(streamLoader_2, "mod_icons", l4);

			Sprite sprite = new Sprite(streamLoader_2, "screenframe", 0);
			leftFrame = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawTransparent(0, 0);
			sprite = new Sprite(streamLoader_2, "screenframe", 1);
			topFrame = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawTransparent(0, 0);
			sprite = new Sprite(streamLoader_2, "screenframe", 2);
			rightFrame = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawTransparent(0, 0);
			sprite = new Sprite(streamLoader_2, "mapedge", 0);
			mapEdgeIP = new RSImageProducer(sprite.myWidth, sprite.myHeight, getGameComponent());
			sprite.drawTransparent(0, 0);

			int i5 = (int)(Math.random() * 21D) - 10;
			int j5 = (int)(Math.random() * 21D) - 10;
			int k5 = (int)(Math.random() * 21D) - 10;
			int l5 = (int)(Math.random() * 41D) - 20;
			for(int i6 = 0; i6 < 100; i6++)
			{
				if(mapFunctions[i6] != null)
					mapFunctions[i6].drawShadowed(i5 + l5, j5 + l5, k5 + l5);
				if(mapScenes[i6] != null)
					mapScenes[i6].drawBackground(i5 + l5, j5 + l5, k5 + l5);
			}

			drawLoadingText(83, "Unpacking textures");
			Texture.loadTextures(streamLoader_3);
			Texture.setBrightness(0.80000000000000004D);
			Texture.initTextureCache();
			drawLoadingText(86, "Unpacking config");
			Animation.unpackConfig(streamLoader);
			ObjectDef.unpackConfig(streamLoader);
			Flo.unpackConfig(streamLoader);
			ItemDef.unpackConfig(streamLoader);
			EntityDef.unpackConfig(streamLoader);
			IDK.unpackConfig(streamLoader);
			SpotAnim.unpackConfig(streamLoader);
			Varp.unpackConfig(streamLoader);
			VarBit.unpackConfig(streamLoader);
			ItemDef.isMembers = isMembers;
			if(!lowMem)
			{
				drawLoadingText(90, "Unpacking sounds");
				byte abyte0[] = streamLoader_5.getDataForName("sounds.dat");
				Stream stream = new Stream(abyte0);
				Sounds.unpack(stream);
			}
			drawLoadingText(95, "Unpacking interfaces");
			TextDrawingArea aclass30_sub2_sub1_sub4s[] = {
					smallText, boldFont, chatTextDrawingArea, fancyFont
			};
			RSInterface.unpack(streamLoader_1, aclass30_sub2_sub1_sub4s, streamLoader_2);
			drawLoadingText(100, "Preparing game engine");
			for(int j6 = 0; j6 < 33; j6++)
			{
				int k6 = 999;
				int i7 = 0;
				for(int k7 = 0; k7 < 34; k7++)
				{
					if(mapBack.aByteArray1450[k7 + j6 * mapBack.width] == 0)
					{
						if(k6 == 999)
							k6 = k7;
						continue;
					}
					if(k6 == 999)
						continue;
					i7 = k7;
					break;
				}

				flameLeftX[j6] = k6;
				minimapHintY[j6] = i7 - k6;
			}

			for(int l6 = 5; l6 < 156; l6++)
			{
				int j7 = 999;
				int l7 = 0;
				for(int j8 = 25; j8 < 172; j8++)
				{
					if(mapBack.aByteArray1450[j8 + l6 * mapBack.width] == 0 && (j8 > 34 || l6 > 34))
					{
						if(j7 == 999)
							j7 = j8;
						continue;
					}
					if(j7 == 999)
						continue;
					l7 = j8;
					break;
				}

				minimapHintX[l6 - 5] = j7 - 25;
				chatFilterOffsets[l6 - 5] = l7 - j7;
			}

			Texture.setViewport(765, 503);
			fullScreenTextureArray = Texture.scanlineOffset;
			Texture.setViewport(519, 165);
			mapChunkX2 = Texture.scanlineOffset;
			Texture.setViewport(246, 335);
			mapChunkY2 = Texture.scanlineOffset;
			Texture.setViewport(512, 334);
			mapChunkLandscapeIds = Texture.scanlineOffset;
			int ai[] = new int[9];
			for(int i8 = 0; i8 < 9; i8++)
			{
				int k8 = 128 + i8 * 32 + 15;
				int l8 = 600 + k8 * 3;
				int i9 = Texture.SINE[k8];
				ai[i8] = l8 * i9 >> 16;
			}

			WorldController.drawMinimapTile(500, 800, 512, 334, ai);
			Censor.loadConfig(streamLoader_4);
			mouseDetection = new MouseDetection(this);
			startRunnable(mouseDetection, 10);
			Animable_Sub5.clientInstance = this;

			// Load resizable UI sprites
			try {
				String spriteDir = signlink.findcachedir() + "Sprites/";
				cacheSprite = new Sprite[35];
				for (int sprIdx = 0; sprIdx < 35; sprIdx++) {
					java.io.File sprFile = new java.io.File(spriteDir + sprIdx + ".png");
					if (sprFile.exists()) {
						cacheSprite[sprIdx] = new Sprite("" + sprIdx);
					}
				}
				System.out.println("Loaded " + cacheSprite.length + " cache sprites");
			} catch (Exception sprEx) {
				System.out.println("Failed to load cache sprites: " + sprEx.getMessage());
			}
			ObjectDef.clientInstance = this;
			EntityDef.clientInstance = this;
			return;
		}
		catch(Exception exception)
		{
			signlink.reporterror("loaderror " + hintText + " " + anInt1079);
		}
		loadingError = true;
	}

	private void parseNewPlayers(Stream stream, int i)
	{
		while(stream.bitPosition + 10 < i * 8)
		{
			int j = stream.readBits(11);
			if(j == 2047)
				break;
			if(playerArray[j] == null)
			{
				playerArray[j] = new Player();
				if(playerBuffers[j] != null)
					playerArray[j].updatePlayer(playerBuffers[j]);
			}
			playerIndices[playerCount++] = j;
			Player player = playerArray[j];
			player.textColor = loopCycle;
			int k = stream.readBits(1);
			if(k == 1)
				entityIndices[entityCount++] = j;
			int l = stream.readBits(1);
			int i1 = stream.readBits(5);
			if(i1 > 15)
				i1 -= 32;
			int j1 = stream.readBits(5);
			if(j1 > 15)
				j1 -= 32;
			player.setPos(myPlayer.smallX[0] + j1, myPlayer.smallY[0] + i1, l == 1);
		}
		stream.finishBitAccess();
	}

	private void processMainScreenClick() {
		if(chatAreaScrollPos != 0)
			return;
		if(super.clickMode3 == 1) {
			int i = super.saveClickX - 25 - 545;
			int j = super.saveClickY - 5 - 4;
			if(i >= 0 && j >= 0 && i < 146 && j < 151) {
				i -= 73;
				j -= 75;
				int k = minimapInt1 + minimapInt2 & 0x7ff;
				int i1 = Texture.SINE[k];
				int j1 = Texture.COSINE[k];
				i1 = i1 * (minimapInt3 + 256) >> 8;
				j1 = j1 * (minimapInt3 + 256) >> 8;
				int k1 = j * i1 + i * j1 >> 11;
				int l1 = j * j1 - i * i1 >> 11;
				int i2 = myPlayer.x + k1 >> 7;
				int j2 = myPlayer.y - l1 >> 7;
				boolean flag1 = doWalkTo(1, 0, 0, 0, myPlayer.smallY[0], 0, 0, j2, myPlayer.smallX[0], true, i2);
				if(flag1) {
					stream.writeWordBigEndian(i);
					stream.writeWordBigEndian(j);
					stream.writeWord(minimapInt1);
					stream.writeWordBigEndian(57);
					stream.writeWordBigEndian(minimapInt2);
					stream.writeWordBigEndian(minimapInt3);
					stream.writeWordBigEndian(89);
					stream.writeWord(myPlayer.x);
					stream.writeWord(myPlayer.y);
					stream.writeWordBigEndian(hintIconX);
					stream.writeWordBigEndian(63);
				}
			}
			anInt1117_static++;
			if(anInt1117_static > 1151) {
				anInt1117_static = 0;
				stream.createFrame(246);
				stream.writeWordBigEndian(0);
				int l = stream.currentOffset;
				if((int)(Math.random() * 2D) == 0)
					stream.writeWordBigEndian(101);
				stream.writeWordBigEndian(197);
				stream.writeWord((int)(Math.random() * 65536D));
				stream.writeWordBigEndian((int)(Math.random() * 256D));
				stream.writeWordBigEndian(67);
				stream.writeWord(14214);
				if((int)(Math.random() * 2D) == 0)
					stream.writeWord(29487);
				stream.writeWord((int)(Math.random() * 65536D));
				if((int)(Math.random() * 2D) == 0)
					stream.writeWordBigEndian(220);
				stream.writeWordBigEndian(180);
				stream.writeBytes(stream.currentOffset - l);
			}
		}
	}

	private String interfaceIntToString(int j) {
		if(j < 0x3b9ac9ff)
			return String.valueOf(j);
		else
			return "*";
	}

	private void showErrorScreen()
	{
		Graphics g = getGameComponent().getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, 765, 503);
		initDrawingArea(1);
		if(loadingError)
		{
			midiFading = false;
			g.setFont(new Font("Helvetica", 1, 16));
			g.setColor(Color.yellow);
			int k = 35;
			g.drawString("Sorry, an error has occured whilst loading RuneScape", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, k);
			k += 30;
			g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, k);
			k += 30;
			g.drawString("3: Try using a different game-world", 30, k);
			k += 30;
			g.drawString("4: Try rebooting your computer", 30, k);
			k += 30;
			g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, k);
		}
		if(genericLoadingError)
		{
			midiFading = false;
			g.setFont(new Font("Helvetica", 1, 20));
			g.setColor(Color.white);
			g.drawString("Error - unable to load game!", 50, 50);
			g.drawString("To play RuneScape make sure you play from", 50, 100);
			g.drawString("http://www.runescape.com", 50, 150);
		}
		if(rsAlreadyLoaded)
		{
			midiFading = false;
			g.setColor(Color.yellow);
			int l = 35;
			g.drawString("Error a copy of RuneScape already appears to be loaded", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, l);
			l += 30;
			g.drawString("2: Try rebooting your computer, and reloading", 30, l);
			l += 30;
		}
	}

	public URL getCodeBase() {		
		try {
			return new java.io.File(sign.signlink.findcachedir()).toURI().toURL();
		} catch(Exception _ex) {
		}
		return null;
	}

	private void processNPCMovement() {
		for(int j = 0; j < npcCount; j++) {
			int k = npcIndices[j];
			NPC npc = npcArray[k];
			if(npc != null)
				processEntityMovement(npc);
		}
	}

	private void processEntityMovement(Entity entity)
	{
		if(entity.x < 128 || entity.y < 128 || entity.x >= 13184 || entity.y >= 13184)
		{
			entity.anim = -1;
			entity.spotAnimId = -1;
			entity.forceMoveEndCycle = 0;
			entity.forceMoveStartCycle = 0;
			entity.x = entity.smallX[0] * 128 + entity.tileSize * 64;
			entity.y = entity.smallY[0] * 128 + entity.tileSize * 64;
			entity.resetPath();
		}
		if(entity == myPlayer && (entity.x < 1536 || entity.y < 1536 || entity.x >= 11776 || entity.y >= 11776))
		{
			entity.anim = -1;
			entity.spotAnimId = -1;
			entity.forceMoveEndCycle = 0;
			entity.forceMoveStartCycle = 0;
			entity.x = entity.smallX[0] * 128 + entity.tileSize * 64;
			entity.y = entity.smallY[0] * 128 + entity.tileSize * 64;
			entity.resetPath();
		}
		if(entity.forceMoveEndCycle > loopCycle)
			processEntityForcedMove(entity);
		else
		if(entity.forceMoveStartCycle >= loopCycle)
			processEntityForcedWalk(entity);
		else
			processEntityWalkAnim(entity);
		processEntityTurnDir(entity);
		processEntityAnimation(entity);
	}

	private void processEntityForcedMove(Entity entity)
	{
		int i = entity.forceMoveEndCycle - loopCycle;
		int j = entity.forceMoveStartX * 128 + entity.tileSize * 64;
		int k = entity.forceMoveStartY * 128 + entity.tileSize * 64;
		entity.x += (j - entity.x) / i;
		entity.y += (k - entity.y) / i;
		entity.stepDelayCounter = 0;
		if(entity.forceMoveDirection == 0)
			entity.turnDirection = 1024;
		if(entity.forceMoveDirection == 1)
			entity.turnDirection = 1536;
		if(entity.forceMoveDirection == 2)
			entity.turnDirection = 0;
		if(entity.forceMoveDirection == 3)
			entity.turnDirection = 512;
	}

	private void processEntityForcedWalk(Entity entity)
	{
		if(entity.forceMoveStartCycle == loopCycle || entity.anim == -1 || entity.animDelay != 0 || entity.animCycle + 1 > Animation.anims[entity.anim].getFrameDuration(entity.animFrame))
		{
			int i = entity.forceMoveStartCycle - entity.forceMoveEndCycle;
			int j = loopCycle - entity.forceMoveEndCycle;
			int k = entity.forceMoveStartX * 128 + entity.tileSize * 64;
			int l = entity.forceMoveStartY * 128 + entity.tileSize * 64;
			int i1 = entity.forceMoveEndX * 128 + entity.tileSize * 64;
			int j1 = entity.forceMoveEndY * 128 + entity.tileSize * 64;
			entity.x = (k * (i - j) + i1 * j) / i;
			entity.y = (l * (i - j) + j1 * j) / i;
		}
		entity.stepDelayCounter = 0;
		if(entity.forceMoveDirection == 0)
			entity.turnDirection = 1024;
		if(entity.forceMoveDirection == 1)
			entity.turnDirection = 1536;
		if(entity.forceMoveDirection == 2)
			entity.turnDirection = 0;
		if(entity.forceMoveDirection == 3)
			entity.turnDirection = 512;
		entity.faceAngle = entity.turnDirection;
	}

	private void processEntityWalkAnim(Entity entity)
	{
		entity.movementAnimId = entity.standAnimId;
		if(entity.smallXYIndex == 0)
		{
			entity.stepDelayCounter = 0;
			return;
		}
		if(entity.anim != -1 && entity.animDelay == 0)
		{
			Animation animation = Animation.anims[entity.anim];
			if(entity.pathRemainder > 0 && animation.precedenceAnimating == 0)
			{
				entity.stepDelayCounter++;
				return;
			}
			if(entity.pathRemainder <= 0 && animation.walkMerge == 0)
			{
				entity.stepDelayCounter++;
				return;
			}
		}
		int i = entity.x;
		int j = entity.y;
		int k = entity.smallX[entity.smallXYIndex - 1] * 128 + entity.tileSize * 64;
		int l = entity.smallY[entity.smallXYIndex - 1] * 128 + entity.tileSize * 64;
		if(k - i > 256 || k - i < -256 || l - j > 256 || l - j < -256)
		{
			entity.x = k;
			entity.y = l;
			return;
		}
		if(i < k)
		{
			if(j < l)
				entity.turnDirection = 1280;
			else
			if(j > l)
				entity.turnDirection = 1792;
			else
				entity.turnDirection = 1536;
		} else
		if(i > k)
		{
			if(j < l)
				entity.turnDirection = 768;
			else
			if(j > l)
				entity.turnDirection = 256;
			else
				entity.turnDirection = 512;
		} else
		if(j < l)
			entity.turnDirection = 1024;
		else
			entity.turnDirection = 0;
		int i1 = entity.turnDirection - entity.faceAngle & 0x7ff;
		if(i1 > 1024)
			i1 -= 2048;
		int j1 = entity.walkLeftAnimId;
		if(i1 >= -256 && i1 <= 256)
			j1 = entity.walkBackAnimId;
		else
		if(i1 >= 256 && i1 < 768)
			j1 = entity.runAnimId;
		else
		if(i1 >= -768 && i1 <= -256)
			j1 = entity.walkRightAnimId;
		if(j1 == -1)
			j1 = entity.walkBackAnimId;
		entity.movementAnimId = j1;
		int k1 = 4;
		if(entity.faceAngle != entity.turnDirection && entity.interactingEntity == -1 && entity.turnSpeed != 0)
			k1 = 2;
		if(entity.smallXYIndex > 2)
			k1 = 6;
		if(entity.smallXYIndex > 3)
			k1 = 8;
		if(entity.stepDelayCounter > 0 && entity.smallXYIndex > 1)
		{
			k1 = 8;
			entity.stepDelayCounter--;
		}
		if(entity.pathRunning[entity.smallXYIndex - 1])
			k1 <<= 1;
		if(k1 >= 8 && entity.movementAnimId == entity.walkBackAnimId && entity.walkAnimId != -1)
			entity.movementAnimId = entity.walkAnimId;
		if(i < k)
		{
			entity.x += k1;
			if(entity.x > k)
				entity.x = k;
		} else
		if(i > k)
		{
			entity.x -= k1;
			if(entity.x < k)
				entity.x = k;
		}
		if(j < l)
		{
			entity.y += k1;
			if(entity.y > l)
				entity.y = l;
		} else
		if(j > l)
		{
			entity.y -= k1;
			if(entity.y < l)
				entity.y = l;
		}
		if(entity.x == k && entity.y == l)
		{
			entity.smallXYIndex--;
			if(entity.pathRemainder > 0)
				entity.pathRemainder--;
		}
	}

	private void processEntityTurnDir(Entity entity)
	{
		if(entity.turnSpeed == 0)
			return;
		if(entity.interactingEntity != -1 && entity.interactingEntity < 32768)
		{
			NPC npc = npcArray[entity.interactingEntity];
			if(npc != null)
			{
				int i1 = entity.x - npc.x;
				int k1 = entity.y - npc.y;
				if(i1 != 0 || k1 != 0)
					entity.turnDirection = (int)(Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
			}
		}
		if(entity.interactingEntity >= 32768)
		{
			int j = entity.interactingEntity - 32768;
			if(j == unknownInt10)
				j = myPlayerIndex;
			Player player = playerArray[j];
			if(player != null)
			{
				int l1 = entity.x - player.x;
				int i2 = entity.y - player.y;
				if(l1 != 0 || i2 != 0)
					entity.turnDirection = (int)(Math.atan2(l1, i2) * 325.94900000000001D) & 0x7ff;
			}
		}
		if((entity.textEffect != 0 || entity.textAlpha != 0) && (entity.smallXYIndex == 0 || entity.stepDelayCounter > 0))
		{
			int k = entity.x - (entity.textEffect - baseX - baseX) * 64;
			int j1 = entity.y - (entity.textAlpha - baseY - baseY) * 64;
			if(k != 0 || j1 != 0)
				entity.turnDirection = (int)(Math.atan2(k, j1) * 325.94900000000001D) & 0x7ff;
			entity.textEffect = 0;
			entity.textAlpha = 0;
		}
		int l = entity.turnDirection - entity.faceAngle & 0x7ff;
		if(l != 0)
		{
			if(l < entity.turnSpeed || l > 2048 - entity.turnSpeed)
				entity.faceAngle = entity.turnDirection;
			else
			if(l > 1024)
				entity.faceAngle -= entity.turnSpeed;
			else
				entity.faceAngle += entity.turnSpeed;
			entity.faceAngle &= 0x7ff;
			if(entity.movementAnimId == entity.standAnimId && entity.faceAngle != entity.turnDirection)
			{
				if(entity.turnAnimId != -1)
				{
					entity.movementAnimId = entity.turnAnimId;
					return;
				}
				entity.movementAnimId = entity.walkBackAnimId;
			}
		}
	}

	private void processEntityAnimation(Entity entity)
	{
		entity.animStretches = false;
		if(entity.movementAnimId != -1)
		{
			Animation animation = Animation.anims[entity.movementAnimId];
			entity.movementAnimCycle++;
			if(entity.movementAnimFrame < animation.frameCount && entity.movementAnimCycle > animation.getFrameDuration(entity.movementAnimFrame))
			{
				entity.movementAnimCycle = 0;
				entity.movementAnimFrame++;
			}
			if(entity.movementAnimFrame >= animation.frameCount)
			{
				entity.movementAnimCycle = 0;
				entity.movementAnimFrame = 0;
			}
		}
		if(entity.spotAnimId != -1 && loopCycle >= entity.spotAnimDelay)
		{
			if(entity.spotAnimFrame < 0)
				entity.spotAnimFrame = 0;
			Animation animation_1 = SpotAnim.cache[entity.spotAnimId].animation;
			for(entity.spotAnimCycle++; entity.spotAnimFrame < animation_1.frameCount && entity.spotAnimCycle > animation_1.getFrameDuration(entity.spotAnimFrame); entity.spotAnimFrame++)
				entity.spotAnimCycle -= animation_1.getFrameDuration(entity.spotAnimFrame);

			if(entity.spotAnimFrame >= animation_1.frameCount && (entity.spotAnimFrame < 0 || entity.spotAnimFrame >= animation_1.frameCount))
				entity.spotAnimId = -1;
		}
		if(entity.anim != -1 && entity.animDelay <= 1)
		{
			Animation animation_2 = Animation.anims[entity.anim];
			if(animation_2.precedenceAnimating == 1 && entity.pathRemainder > 0 && entity.forceMoveEndCycle <= loopCycle && entity.forceMoveStartCycle < loopCycle)
			{
				entity.animDelay = 1;
				return;
			}
		}
		if(entity.anim != -1 && entity.animDelay == 0)
		{
			Animation animation_3 = Animation.anims[entity.anim];
			for(entity.animCycle++; entity.animFrame < animation_3.frameCount && entity.animCycle > animation_3.getFrameDuration(entity.animFrame); entity.animFrame++)
				entity.animCycle -= animation_3.getFrameDuration(entity.animFrame);

			if(entity.animFrame >= animation_3.frameCount)
			{
				entity.animFrame -= animation_3.loopOffset;
				entity.animFrameCount++;
				if(entity.animFrameCount >= animation_3.maxLoops)
					entity.anim = -1;
				if(entity.animFrame < 0 || entity.animFrame >= animation_3.frameCount)
					entity.anim = -1;
			}
			entity.animStretches = animation_3.stretches;
		}
		if(entity.animDelay > 0)
			entity.animDelay--;
	}

	private void drawGameScreen()
	{
		if (fullscreenInterfaceID != -1 && (loadingStage == 2 || super.fullGameScreen != null)) {
			if (loadingStage == 2) {
				animateInterface(cameraTargetLocalZ, fullscreenInterfaceID);
				if (openInterfaceID != -1) {
					animateInterface(cameraTargetLocalZ, openInterfaceID);
				}
				cameraTargetLocalZ = 0;
				resetAllImageProducers();
				super.fullGameScreen.initDrawingArea();
				Texture.scanlineOffset = fullScreenTextureArray;
				DrawingArea.setAllPixelsToZero();
				welcomeScreenRaised = true;
				if (openInterfaceID != -1) {
					RSInterface rsInterface_1 = RSInterface.interfaceCache[openInterfaceID];
					if (rsInterface_1.width == 512 && rsInterface_1.height == 334 && rsInterface_1.type == 0) {
						rsInterface_1.width = 765;
						rsInterface_1.height = 503;
					}
					drawInterface(0, 0, rsInterface_1, 8);
				}
				RSInterface rsInterface = RSInterface.interfaceCache[fullscreenInterfaceID];
				if (rsInterface.width == 512 && rsInterface.height == 334 && rsInterface.type == 0) {
					rsInterface.width = 765;
					rsInterface.height = 503;
				}
				drawInterface(0, 0, rsInterface, 8);

				if (!menuOpen) {
					processRightClick();
					drawTooltip();
				} else {
					drawMenu();
				}
			}
			drawCount++;
			super.fullGameScreen.drawGraphics(0, super.graphics, 0);
			return;
		} else {
			if (drawCount != 0) {
				resetImageProducers2();
			}
		}
		if(welcomeScreenRaised) {
			welcomeScreenRaised = false;
			if (clientSize == 0) {
				topFrame.drawGraphics(0, super.graphics, 0);
				leftFrame.drawGraphics(4, super.graphics, 0);
				rightFrame.drawGraphics(4, super.graphics, 516);
				mapEdgeIP.drawGraphics(4, super.graphics, 519);
			} else {
				// Clear background in resizable mode
				if (super.graphics != null) {
					super.graphics.setColor(java.awt.Color.BLACK);
					super.graphics.fillRect(0, 0, clientWidth, clientHeight);
				}
			}
			needDrawTabArea = true;
			inputTaken = true;
			tabAreaAltered = true;
			aBoolean1233 = true;
			if(loadingStage != 2) {
				loginMsgIP.drawGraphics(clientSize == 0 ? 4 : 0, super.graphics, clientSize == 0 ? 4 : 0);
				titleButtonIP.drawGraphics(4, super.graphics, 545);
			}
		}
		if(menuOpen && menuScreenArea == 1)
			needDrawTabArea = true;
		if(invOverlayInterfaceID != -1)
		{
			boolean flag1 = animateInterface(cameraTargetLocalZ, invOverlayInterfaceID);
			if(flag1)
				needDrawTabArea = true;
		}
		if(atInventoryInterfaceType == 2)
			needDrawTabArea = true;
		if(activeInterfaceType == 2)
			needDrawTabArea = true;
		/*if(needDrawTabArea)
		{*/
			if (clientSize == 0) drawTabArea();
			/*needDrawTabArea = false;
		}*/
		if(backDialogID == -1)
		{
			chatboxInterface.scrollPosition = chatFilterScrollMax - chatScrollAmount - 110;
			if(super.mouseX > 478 && super.mouseX < 580 && super.mouseY > 342)
				processScrollbar(494, 110, super.mouseX - 0, super.mouseY - 348, chatboxInterface, 0, false, chatFilterScrollMax);
			int i = chatFilterScrollMax - 110 - chatboxInterface.scrollPosition;
			if(i < 0)
				i = 0;
			if(i > chatFilterScrollMax - 110)
				i = chatFilterScrollMax - 110;
			if(chatScrollAmount != i)
			{
				chatScrollAmount = i;
				inputTaken = true;
			}
		}
		if(backDialogID != -1) {
			boolean flag2 = animateInterface(cameraTargetLocalZ, backDialogID);
			if(flag2)
				inputTaken = true;
		}
		if(atInventoryInterfaceType == 3)
			inputTaken = true;
		if(activeInterfaceType == 3)
			inputTaken = true;
		if(clickToContinueString != null)
			inputTaken = true;
		if(menuOpen && menuScreenArea == 2)
			inputTaken = true;
		if(inputTaken) {
			if (clientSize == 0) drawChatArea();
			inputTaken = false;
		}
		if(loadingStage == 2)
			processSceneEntities();
		if(loadingStage == 2 && clientSize == 0) {
			drawMinimap();
			titleButtonIP.drawGraphics(4, super.graphics, 545);
		}
		if(flashingTab != -1)
			tabAreaAltered = true;
		if(tabAreaAltered)
		{
			if(flashingTab != -1 && flashingTab == tabID)
			{
				flashingTab = -1;
				stream.createFrame(120);
				stream.writeWordBigEndian(tabID);
			}
			tabAreaAltered = false;
			titleIP3.initDrawingArea();
			loginMsgIP.initDrawingArea();
		}
		cameraTargetLocalZ = 0;
	}

	private boolean buildFriendsListMenu(RSInterface class9)
	{
		int i = class9.contentType;
		if(i >= 1 && i <= 200 || i >= 701 && i <= 900)
		{
			if(i >= 801)
				i -= 701;
			else
			if(i >= 701)
				i -= 601;
			else
			if(i >= 101)
				i -= 101;
			else
				i--;
			menuActionName[menuActionRow] = "Remove @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 792;
			menuActionRow++;
			menuActionName[menuActionRow] = "Message @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 639;
			menuActionRow++;
			return true;
		}
		if(i >= 401 && i <= 500)
		{
			menuActionName[menuActionRow] = "Remove @whi@" + class9.message;
			menuActionID[menuActionRow] = 322;
			menuActionRow++;
			return true;
		} else
		{
			return false;
		}
	}

	private void processStationaryGfx()
	{
		Animable_Sub3 class30_sub2_sub4_sub3 = (Animable_Sub3)spotAnimList.reverseGetFirst();
		for(; class30_sub2_sub4_sub3 != null; class30_sub2_sub4_sub3 = (Animable_Sub3)spotAnimList.reverseGetNext())
			if(class30_sub2_sub4_sub3.plane != plane || class30_sub2_sub4_sub3.finished)
				class30_sub2_sub4_sub3.unlink();
			else
			if(loopCycle >= class30_sub2_sub4_sub3.endCycle)
			{
				class30_sub2_sub4_sub3.advanceSpotAnimFrame(cameraTargetLocalZ);
				if(class30_sub2_sub4_sub3.finished)
					class30_sub2_sub4_sub3.unlink();
				else
					worldController.addTempObject(class30_sub2_sub4_sub3.plane, 0, class30_sub2_sub4_sub3.startZ, -1, class30_sub2_sub4_sub3.startY, 60, class30_sub2_sub4_sub3.startX, class30_sub2_sub4_sub3, false);
			}

	}
	
	public void drawBlackBox(int xPos, int yPos) {
		DrawingArea.drawPixels(71, yPos - 1, xPos - 2, 0x726451, 1);
		DrawingArea.drawPixels(69, yPos, xPos + 174, 0x726451, 1);
		DrawingArea.drawPixels(1, yPos - 2, xPos - 2, 0x726451, 178);
		DrawingArea.drawPixels(1, yPos + 68, xPos, 0x726451, 174);
		DrawingArea.drawPixels(71, yPos - 1, xPos - 1, 0x2E2B23, 1);
		DrawingArea.drawPixels(71, yPos - 1, xPos + 175, 0x2E2B23, 1);
		DrawingArea.drawPixels(1, yPos - 1, xPos, 0x2E2B23, 175);
		DrawingArea.drawPixels(1, yPos + 69, xPos, 0x2E2B23, 175);
		DrawingArea.fillRect(0, yPos, 174, 68, 220, xPos);
	}
	
	private void drawInterface(int j, int k, RSInterface class9, int l) {
		if(class9.type != 0 || class9.children == null)
			return;
		if(class9.isMouseoverTriggered && tabFlashCycleAlt != class9.id && hintArrowType != class9.id && walkDest != class9.id)
			return;
		int i1 = DrawingArea.topX;
		int j1 = DrawingArea.topY;
		int k1 = DrawingArea.bottomX;
		int l1 = DrawingArea.bottomY;
		DrawingArea.setDrawingArea(l + class9.height, k, k + class9.width, l);
		int i2 = class9.children.length;
		for(int j2 = 0; j2 < i2; j2++) 	{
			int k2 = class9.childX[j2] + k;
			int l2 = (class9.childY[j2] + l) - j;
			if(class9.children[j2] < 0 || class9.children[j2] >= RSInterface.interfaceCache.length) continue;
			RSInterface class9_1 = RSInterface.interfaceCache[class9.children[j2]];
			if(class9_1 == null) continue;
			k2 += class9_1.invSpritePadX;
			l2 += class9_1.invSpritePadY;
			if(class9_1.contentType > 0)
				drawFriendsListOrWelcomeScreen(class9_1);
			//here
			int[] IDs = {
				1196, 1199, 1206, 1215, 1224, 1231, 1240, 1249, 1258, 1267, 1274, 1283, 1573,
				1290, 1299, 1308, 1315, 1324, 1333, 1340, 1349, 1358, 1367, 1374, 1381, 1388,
				1397, 1404, 1583, 12038, 1414, 1421, 1430, 1437, 1446, 1453, 1460, 1469, 15878,
				1602, 1613, 1624, 7456, 1478, 1485, 1494, 1503, 1512, 1521, 1530, 1544, 1553,
				1563, 1593, 1635, 12426, 12436, 12446, 12456, 6004, 18471,
				/* Ancients */
				12940, 12988, 13036, 12902, 12862, 13046, 12964, 13012, 13054, 12920, 12882, 13062,
				12952, 13000, 13070, 12912, 12872, 13080, 12976, 13024, 13088, 12930, 12892, 13096
			};
			for(int m5 = 0; m5 < IDs.length; m5++) {
				if(class9_1.id == IDs[m5] + 1) {
					if(m5 > 61)
						drawBlackBox(k2 + 1, l2);
					else
						drawBlackBox(k2, l2 + 1);
				}
			}
			int[] runeChildren = {
				1202, 1203, 1209, 1210, 1211, 1218, 1219, 1220, 1227, 1228, 1234, 1235, 1236, 1243, 1244, 1245,
				1252, 1253, 1254, 1261, 1262, 1263, 1270, 1271, 1277, 1278, 1279, 1286, 1287, 1293, 1294, 1295,
				1302, 1303, 1304, 1311, 1312, 1318, 1319, 1320, 1327, 1328, 1329, 1336, 1337, 1343, 1344, 1345,
				1352, 1353, 1354, 1361, 1362, 1363, 1370, 1371, 1377, 1378, 1384, 1385, 1391, 1392, 1393, 1400,
				1401, 1407, 1408, 1410, 1417, 1418, 1424, 1425, 1426, 1433, 1434, 1440, 1441, 1442, 1449, 1450,
				1456, 1457, 1463, 1464, 1465, 1472, 1473, 1474, 1481, 1482, 1488, 1489, 1490, 1497, 1498, 1499,
				1506, 1507, 1508, 1515, 1516, 1517, 1524, 1525, 1526, 1533, 1534, 1535, 1547, 1548, 1549, 1556,
				1557, 1558, 1566, 1567, 1568, 1576, 1577, 1578, 1586, 1587, 1588, 1596, 1597, 1598, 1605, 1606,
				1607, 1616, 1617, 1618, 1627, 1628, 1629, 1638, 1639, 1640, 6007, 6008, 6011, 8673, 8674, 12041,
				12042, 12429, 12430, 12431, 12439, 12440, 12441, 12449, 12450, 12451, 12459, 12460, 15881, 15882,
				15885, 18474, 18475, 18478
			};
			for(int r = 0; r < runeChildren.length; r++)
				if(class9_1.id == runeChildren[r])
					class9_1.modelZoom = 775;
			if(class9_1.type == 0) {
				if(class9_1.scrollPosition > class9_1.scrollMax - class9_1.height)
					class9_1.scrollPosition = class9_1.scrollMax - class9_1.height;
				if(class9_1.scrollPosition < 0)
					class9_1.scrollPosition = 0;
				drawInterface(class9_1.scrollPosition, k2, class9_1, l2);
				if(class9_1.scrollMax > class9_1.height)
					drawScrollbar(class9_1.height, class9_1.scrollPosition, l2, k2 + class9_1.width, class9_1.scrollMax);
			} else if(class9_1.type != 1)
				if(class9_1.type == 2) {
					int i3 = 0;
					for(int l3 = 0; l3 < class9_1.height; l3++) {
						for(int l4 = 0; l4 < class9_1.width; l4++) {
							int k5 = k2 + l4 * (32 + class9_1.invSpritePadX);
							int j6 = l2 + l3 * (32 + class9_1.invSpritePadY);
							if(i3 < 20) {
								k5 += class9_1.spritesX[i3];
								j6 += class9_1.spritesY[i3];
							}
							if(class9_1.inv[i3] > 0) {
								int k6 = 0;
								int j7 = 0;
								int j9 = class9_1.inv[i3] - 1;
								if(k5 > DrawingArea.topX - 32 && k5 < DrawingArea.bottomX && j6 > DrawingArea.topY - 32 && j6 < DrawingArea.bottomY || activeInterfaceType != 0 && dragFromSlot == i3) {
									int l9 = 0;
									if(itemSelected == 1 && selectedInventorySlot == i3 && selectedInventoryInterface == class9_1.id)
										l9 = 0xffffff;
									Sprite class30_sub2_sub1_sub1_2 = ItemDef.getSprite(j9, class9_1.invStackSizes[i3], l9);
									if(class30_sub2_sub1_sub1_2 != null) {
										if(activeInterfaceType != 0 && dragFromSlot == i3 && dragFromSlotInterface == class9_1.id) {
											k6 = super.mouseX - dragStartX;
											j7 = super.mouseY - dragStartY;
											if(k6 < 5 && k6 > -5)
												k6 = 0;
											if(j7 < 5 && j7 > -5)
												j7 = 0;
											if(moveItemInterfaceId < 10) {
												k6 = 0;
												j7 = 0;
											}
											class30_sub2_sub1_sub1_2.drawSprite1(k5 + k6, j6 + j7);
											if(j6 + j7 < DrawingArea.topY && class9.scrollPosition > 0) {
												int i10 = (cameraTargetLocalZ * (DrawingArea.topY - j6 - j7)) / 3;
												if(i10 > cameraTargetLocalZ * 10)
													i10 = cameraTargetLocalZ * 10;
												if(i10 > class9.scrollPosition)
													i10 = class9.scrollPosition;
												class9.scrollPosition -= i10;
												dragStartY += i10;
											}
											if(j6 + j7 + 32 > DrawingArea.bottomY && class9.scrollPosition < class9.scrollMax - class9.height) {
												int j10 = (cameraTargetLocalZ * ((j6 + j7 + 32) - DrawingArea.bottomY)) / 3;
												if(j10 > cameraTargetLocalZ * 10)
													j10 = cameraTargetLocalZ * 10;
												if(j10 > class9.scrollMax - class9.height - class9.scrollPosition)
													j10 = class9.scrollMax - class9.height - class9.scrollPosition;
												class9.scrollPosition += j10;
												dragStartY -= j10;
											}
										} else if(atInventoryInterfaceType != 0 && atInventoryIndex == i3 && atInventoryInterface == class9_1.id)
											class30_sub2_sub1_sub1_2.drawSprite1(k5, j6);
										else
											class30_sub2_sub1_sub1_2.drawSprite(k5, j6);
										if(class30_sub2_sub1_sub1_2.maxWidth == 33 || class9_1.invStackSizes[i3] != 1)
										{
											int k10 = class9_1.invStackSizes[i3];
											if(k10 >= 1)
												smallText.drawText(0xFFFF00, intToKOrMil(k10), j6 + 9 + j7, k5 + k6);
											if(k10 >= 100000)
												smallText.drawText(0xFFFFFF, intToKOrMil(k10), j6 + 9 + j7, k5 + k6);
											if(k10 >= 10000000)
												smallText.drawText(0x49E20E, intToKOrMil(k10), j6 + 9 + j7, k5 + k6);

											/*smallText.drawText(0, intToKOrMil(k10), j6 + 10 + j7, k5 + 1 + k6);
											smallText.drawText(0xffff00, intToKOrMil(k10), j6 + 9 + j7, k5 + k6);*/
										}
									}
								}
							} else if(class9_1.sprites != null && i3 < 20) {
								Sprite class30_sub2_sub1_sub1_1 = class9_1.sprites[i3];
								if(class30_sub2_sub1_sub1_1 != null)
									class30_sub2_sub1_sub1_1.drawSprite(k5, j6);
							}
							i3++;
						}
					}
				} else if(class9_1.type == 3) {
					boolean flag = false;
					if(walkDest == class9_1.id || hintArrowType == class9_1.id || tabFlashCycleAlt == class9_1.id)
						flag = true;
					int j3;
					if(interfaceIsSelected(class9_1)) {
						j3 = class9_1.disabledColor;
						if(flag && class9_1.hoverColor != 0)
							j3 = class9_1.hoverColor;
					} else {
						j3 = class9_1.textColor;
						if(flag && class9_1.enabledColor != 0)
							j3 = class9_1.enabledColor;
					}
					if(class9_1.opacity == 0) {
						if(class9_1.textCentered)
							DrawingArea.drawPixels(class9_1.height, l2, k2, j3, class9_1.width);
						else
							DrawingArea.fillPixels(k2, class9_1.width, class9_1.height, j3, l2);
					} else if(class9_1.textCentered)
						DrawingArea.fillRect(j3, l2, class9_1.width, class9_1.height, 256 - (class9_1.opacity & 0xff), k2);
					else
						DrawingArea.drawRect(l2, class9_1.height, 256 - (class9_1.opacity & 0xff), j3, class9_1.width, k2);
				} else if(class9_1.type == 4) {
					TextDrawingArea textDrawingArea = class9_1.textDrawingAreas;
					String s = class9_1.message;
					boolean flag1 = false;
					if(walkDest == class9_1.id || hintArrowType == class9_1.id || tabFlashCycleAlt == class9_1.id)
						flag1 = true;
					int i4;
					if(interfaceIsSelected(class9_1)) {
						i4 = class9_1.disabledColor;
						if(flag1 && class9_1.hoverColor != 0)
							i4 = class9_1.hoverColor;
						if(class9_1.enabledText.length() > 0)
							s = class9_1.enabledText;
					} else {
						i4 = class9_1.textColor;
						if(flag1 && class9_1.enabledColor != 0)
							i4 = class9_1.enabledColor;
					}
					if(class9_1.atActionType == 6 && aBoolean1149) {
						s = "Please wait...";
						i4 = class9_1.textColor;
					}
					if(DrawingArea.width == 519) {
						if(i4 == 0xffff00)
							i4 = 255;
						if(i4 == 49152)
							i4 = 0xffffff;
					}
					if((class9_1.parentID == 1151) || (class9_1.parentID == 12855)) {
						switch (i4) {
							case 16773120: i4 = 0xFE981F; break;
							case 7040819: i4 = 0xAF6A1A; break;
						}
					}
					for(int l6 = l2 + textDrawingArea.fontHeight; s.length() > 0; l6 += textDrawingArea.fontHeight)
					{
						if(s.indexOf("%") != -1)
						{
							do
							{
								int k7 = s.indexOf("%1");
								if(k7 == -1)
									break;
								if(class9_1.id < 4000 || class9_1.id > 5000 && class9_1.id !=13921 && class9_1.id !=13922 && class9_1.id !=12171 && class9_1.id !=12172)
									s = s.substring(0, k7) + methodR(extractInterfaceValues(class9_1, 0)) + s.substring(k7 + 2);
								else
									s = s.substring(0, k7) + interfaceIntToString(extractInterfaceValues(class9_1, 0)) + s.substring(k7 + 2);
							} while(true);
							do
							{
								int l7 = s.indexOf("%2");
								if(l7 == -1)
									break;
								s = s.substring(0, l7) + interfaceIntToString(extractInterfaceValues(class9_1, 1)) + s.substring(l7 + 2);
							} while(true);
							do
							{
								int i8 = s.indexOf("%3");
								if(i8 == -1)
									break;
								s = s.substring(0, i8) + interfaceIntToString(extractInterfaceValues(class9_1, 2)) + s.substring(i8 + 2);
							} while(true);
							do
							{
								int j8 = s.indexOf("%4");
								if(j8 == -1)
									break;
								s = s.substring(0, j8) + interfaceIntToString(extractInterfaceValues(class9_1, 3)) + s.substring(j8 + 2);
							} while(true);
							do
							{
								int k8 = s.indexOf("%5");
								if(k8 == -1)
									break;
								s = s.substring(0, k8) + interfaceIntToString(extractInterfaceValues(class9_1, 4)) + s.substring(k8 + 2);
							} while(true);
						}
						int l8 = s.indexOf("\\n");
						String s1;
						if(l8 != -1)
						{
							s1 = s.substring(0, l8);
							s = s.substring(l8 + 2);
						} else
						{
							s1 = s;
							s = "";
						}
						if(class9_1.centerText)
							textDrawingArea.drawRightAligned(i4, k2 + class9_1.width / 2, s1, l6, class9_1.textShadow);
						else
							textDrawingArea.drawWaving(class9_1.textShadow, k2, i4, s1, l6);
					}
				} else if(class9_1.type == 5) {
					Sprite sprite;
					if(interfaceIsSelected(class9_1))
                        			sprite = class9_1.sprite2;
                    			else
                        			sprite = class9_1.sprite1;
					if(spellSelected == 1 && class9_1.id == spellID && spellID != 0 && sprite != null) { 
						sprite.drawSprite(k2, l2, 0xffffff);
					} else {
						if (sprite != null)
							sprite.drawSprite(k2, l2);
					}
                    if(sprite != null)
                        sprite.drawSprite(k2, l2);
				} else if(class9_1.type == 6) {
					int k3 = Texture.textureInt1;
					int j4 = Texture.textureInt2;
					Texture.textureInt1 = k2 + class9_1.width / 2;
					Texture.textureInt2 = l2 + class9_1.height / 2;
					int i5 = Texture.SINE[class9_1.modelRotation1] * class9_1.modelZoom >> 16;
					int l5 = Texture.COSINE[class9_1.modelRotation1] * class9_1.modelZoom >> 16;
					boolean flag2 = interfaceIsSelected(class9_1);
					int i7;
					if(flag2)
						i7 = class9_1.disabledAnimation;
					else
						i7 = class9_1.enabledAnimation;
					Model model;
					if(i7 == -1) {
						model = class9_1.getWidgetModel(-1, -1, flag2);
					} else {
						Animation animation = Animation.anims[i7];
						model = class9_1.getWidgetModel(animation.frameDelays[class9_1.enabledSpriteId], animation.frameIds[class9_1.enabledSpriteId], flag2);
					}
					if(model != null)
						model.renderModel2D(class9_1.modelRotation2, 0, class9_1.modelRotation1, 0, i5, l5);
					Texture.textureInt1 = k3;
					Texture.textureInt2 = j4;
				} else if(class9_1.type == 7) {
					TextDrawingArea textDrawingArea_1 = class9_1.textDrawingAreas;
					int k4 = 0;
					for(int j5 = 0; j5 < class9_1.height; j5++) {
						for(int i6 = 0; i6 < class9_1.width; i6++) {
							if(class9_1.inv[k4] > 0) {
								ItemDef itemDef = ItemDef.forID(class9_1.inv[k4] - 1);
								String s2 = itemDef.name;
								if(itemDef.stackable || class9_1.invStackSizes[k4] != 1)
									s2 = s2 + " x" + intToKOrMilLongName(class9_1.invStackSizes[k4]);
								int i9 = k2 + i6 * (115 + class9_1.invSpritePadX);
								int k9 = l2 + j5 * (12 + class9_1.invSpritePadY);
								if(class9_1.centerText)
									textDrawingArea_1.drawRightAligned(class9_1.textColor, i9 + class9_1.width / 2, s2, k9, class9_1.textShadow);
								else
									textDrawingArea_1.drawWaving(class9_1.textShadow, i9, class9_1.textColor, s2, k9);
							}
							k4++;
						}
					}
				} else if (class9_1.type == 8) {
					drawHoverBox(k2, l2, class9_1.popupString);
				}
		}
		DrawingArea.setDrawingArea(l1, i1, k1, j1);
	}

	private void randomizeBackground(Background background) {
		int j = 256;
		for(int k = 0; k < chatScrollPositions.length; k++)
			chatScrollPositions[k] = 0;

		for(int l = 0; l < 5000; l++) {
			int i1 = (int)(Math.random() * 128D * (double)j);
			chatScrollPositions[i1] = (int)(Math.random() * 256D);
		}
		for(int j1 = 0; j1 < 20; j1++) {
			for(int k1 = 1; k1 < j - 1; k1++) {
				for(int i2 = 1; i2 < 127; i2++) {
					int k2 = i2 + (k1 << 7);
					chatHighlights[k2] = (chatScrollPositions[k2 - 1] + chatScrollPositions[k2 + 1] + chatScrollPositions[k2 - 128] + chatScrollPositions[k2 + 128]) / 4;
				}

			}
			int ai[] = chatScrollPositions;
			chatScrollPositions = chatHighlights;
			chatHighlights = ai;
		}
		if(background != null) {
			int l1 = 0;
			for(int j2 = 0; j2 < background.height; j2++) {
				for(int l2 = 0; l2 < background.width; l2++)
					if(background.aByteArray1450[l1++] != 0) {
						int i3 = l2 + 16 + background.offsetX;
						int j3 = j2 + 16 + background.offsetY;
						int k3 = i3 + (j3 << 7);
						chatScrollPositions[k3] = 0;
					}
			}
		}
	}

	private void parsePlayerMaskData(int i, int j, Stream stream, Player player)
	{
		if((i & 0x400) != 0)
		{
			player.forceMoveStartX = stream.readUnsignedByteSub();
			player.forceMoveStartY = stream.readUnsignedByteSub();
			player.forceMoveEndX = stream.readUnsignedByteSub();
			player.forceMoveEndY = stream.readUnsignedByteSub();
			player.forceMoveEndCycle = stream.readWordLEBigA() + loopCycle;
			player.forceMoveStartCycle = stream.readWordBigA() + loopCycle;
			player.forceMoveDirection = stream.readUnsignedByteSub();
			player.resetPath();
		}
		if((i & 0x100) != 0)
		{
			player.spotAnimId = stream.readWordLE();
			int k = stream.readDWord();
			player.spotAnimHeight = k >> 16;
			player.spotAnimDelay = loopCycle + (k & 0xffff);
			player.spotAnimFrame = 0;
			player.spotAnimCycle = 0;
			if(player.spotAnimDelay > loopCycle)
				player.spotAnimFrame = -1;
			if(player.spotAnimId == 65535)
				player.spotAnimId = -1;
		}
		if((i & 8) != 0)
		{
			int l = stream.readWordLE();
			if(l == 65535)
				l = -1;
			int i2 = stream.readUnsignedByteNeg();
			if(l == player.anim && l != -1)
			{
				int i3 = Animation.anims[l].replayMode;
				if(i3 == 1)
				{
					player.animFrame = 0;
					player.animCycle = 0;
					player.animDelay = i2;
					player.animFrameCount = 0;
				}
				if(i3 == 2)
					player.animFrameCount = 0;
			} else
			if(l == -1 || player.anim == -1 || Animation.anims[l].priority >= Animation.anims[player.anim].priority)
			{
				player.anim = l;
				player.animFrame = 0;
				player.animCycle = 0;
				player.animDelay = i2;
				player.animFrameCount = 0;
				player.pathRemainder = player.smallXYIndex;
			}
		}
		if((i & 4) != 0)
		{
			player.textSpoken = stream.readString();
			if(player.textSpoken.charAt(0) == '~')
			{
				player.textSpoken = player.textSpoken.substring(1);
				pushMessage(player.textSpoken, 2, player.name);
			} else
			if(player == myPlayer)
				pushMessage(player.textSpoken, 2, player.name);
			player.turnAroundAnimId = 0;
			player.animResetCycle = 0;
			player.textCycle = 150;
		}
		if((i & 0x80) != 0)
		{
			//right fucking here
			int i1 = stream.readWordLE();
			int j2 = stream.readUnsignedByte();
			int j3 = stream.readUnsignedByteNeg();
			int k3 = stream.currentOffset;
			if(player.name != null && player.visible)
			{
				long l3 = TextClass.longForName(player.name);
				boolean flag = false;
				if(j2 <= 1)
				{
					for(int i4 = 0; i4 < ignoreCount; i4++)
					{
						if(ignoreListAsLongs[i4] != l3)
							continue;
						flag = true;
						break;
					}

				}
				if(!flag && anInt1251 == 0)
					try
					{
						loginStream.currentOffset = 0;
						stream.readBytesReverse(j3, 0, loginStream.buffer);
						loginStream.currentOffset = 0;
						String s = TextInput.decodeText(j3, loginStream);
						//s = Censor.doCensor(s);
						player.textSpoken = s;
						player.turnAroundAnimId = i1 >> 8;
						player.privelage = j2;
						player.animResetCycle = i1 & 0xff;
						player.textCycle = 150;
						if(j2 == 2 || j2 == 3)
							pushMessage(s, 1, "@cr2@" + player.name);
						else if(j2 == 1)
							pushMessage(s, 1, "@cr1@" + player.name);
						else
							pushMessage(s, 2, player.name);
					}
					catch(Exception exception)
					{
						signlink.reporterror("cde2");
					}
			}
			stream.currentOffset = k3 + j3;
		}
		if((i & 1) != 0)
		{
			player.interactingEntity = stream.readWordLE();
			if(player.interactingEntity == 65535)
				player.interactingEntity = -1;
		}
		if((i & 0x10) != 0)
		{
			int j1 = stream.readUnsignedByteNeg();
			byte abyte0[] = new byte[j1];
			Stream stream_1 = new Stream(abyte0);
			stream.readBytes(j1, 0, abyte0);
			playerBuffers[j] = stream_1;
			player.updatePlayer(stream_1);
		}
		if((i & 2) != 0)
		{
			player.textEffect = stream.readWordLEBigA();
			player.textAlpha = stream.readWordLE();
		}
		if((i & 0x20) != 0)
		{
			int k1 = stream.readUnsignedByte();
			int k2 = stream.readUnsignedByteAdd();
			player.updateHitData(k2, k1, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.readUnsignedByteNeg();
			player.maxHealth = stream.readUnsignedByte();
		}
		if((i & 0x200) != 0)
		{
			int l1 = stream.readUnsignedByte();
			int l2 = stream.readUnsignedByteSub();
			player.updateHitData(l2, l1, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.readUnsignedByte();
			player.maxHealth = stream.readUnsignedByteNeg();
		}
	}

	private void updateCamera()
	{
		try
		{
			int j = myPlayer.x + anInt1278;
			int k = myPlayer.y + cameraOscillationH;
			if(cameraSmoothedX - j < -500 || cameraSmoothedX - j > 500 || cameraSmoothedY - k < -500 || cameraSmoothedY - k > 500)
			{
				cameraSmoothedX = j;
				cameraSmoothedY = k;
			}
			if(cameraSmoothedX != j)
				cameraSmoothedX += (j - cameraSmoothedX) / 16;
			if(cameraSmoothedY != k)
				cameraSmoothedY += (k - cameraSmoothedY) / 16;
			if(super.keyArray[1] == 1)
				minimapZoomTarget += (-24 - minimapZoomTarget) / 2;
			else
			if(super.keyArray[2] == 1)
				minimapZoomTarget += (24 - minimapZoomTarget) / 2;
			else
				minimapZoomTarget /= 2;
			if(super.keyArray[3] == 1)
				minimapZoom += (12 - minimapZoom) / 2;
			else
			if(super.keyArray[4] == 1)
				minimapZoom += (-12 - minimapZoom) / 2;
			else
				minimapZoom /= 2;
			  minimapInt1 = minimapInt1 + minimapZoomTarget / 2 & 0x7ff;
			  selectedArea += minimapZoom / 2;
			if(selectedArea < 128)
				selectedArea = 128;
			if(selectedArea > 383)
				selectedArea = 383;
			int l = cameraSmoothedX >> 7;
			int i1 = cameraSmoothedY >> 7;
			int j1 = getTileHeight(plane, cameraSmoothedY, cameraSmoothedX);
			int k1 = 0;
			if(l > 3 && i1 > 3 && l < 100 && i1 < 100)
			{
				for(int l1 = l - 4; l1 <= l + 4; l1++)
				{
					for(int k2 = i1 - 4; k2 <= i1 + 4; k2++)
					{
						int l2 = plane;
						if(l2 < 3 && (byteGroundArray[1][l1][k2] & 2) == 2)
							l2++;
						int i3 = j1 - intGroundArray[l2][l1][k2];
						if(i3 > k1)
							k1 = i3;
					}

				}

			}
			anInt1005_static++;
			if(anInt1005_static > 1512)
			{
				anInt1005_static = 0;
				stream.createFrame(77);
				stream.writeWordBigEndian(0);
				int i2 = stream.currentOffset;
				stream.writeWordBigEndian((int)(Math.random() * 256D));
				stream.writeWordBigEndian(101);
				stream.writeWordBigEndian(233);
				stream.writeWord(45092);
				if((int)(Math.random() * 2D) == 0)
					stream.writeWord(35784);
				stream.writeWordBigEndian((int)(Math.random() * 256D));
				stream.writeWordBigEndian(64);
				stream.writeWordBigEndian(38);
				stream.writeWord((int)(Math.random() * 65536D));
				stream.writeWord((int)(Math.random() * 65536D));
				stream.writeBytes(stream.currentOffset - i2);
			}
			int j2 = k1 * 192;
			if(j2 > 0x17f00)
				j2 = 0x17f00;
			if(j2 < 32768)
				j2 = 32768;
			if(j2 > moveItemSlotStart)
			{
				moveItemSlotStart += (j2 - moveItemSlotStart) / 24;
				return;
			}
			if(j2 < moveItemSlotStart)
			{
				moveItemSlotStart += (j2 - moveItemSlotStart) / 80;
			}
		}
		catch(Exception _ex)
		{
			signlink.reporterror("glfc_ex " + myPlayer.x + "," + myPlayer.y + "," + cameraSmoothedX + "," + cameraSmoothedY + "," + mapRegionX + "," + mapRegionY + "," + baseX + "," + baseY);
			throw new RuntimeException("eek");
		}
	}

	public void processDrawing()
	{
		checkSize();
		if(rsAlreadyLoaded || loadingError || genericLoadingError)
		{
			showErrorScreen();
			return;
		}
		anInt1061_static++;
		if(!loggedIn)
			drawLoginScreen(false);
		else
			drawGameScreen();
		menuActionCounter = 0;
	}

	private boolean isFriendOrSelf(String s)
	{
		if(s == null)
			return false;
		for(int i = 0; i < friendsCount; i++)
			if(s.equalsIgnoreCase(friendsList[i]))
				return true;
		return s.equalsIgnoreCase(myPlayer.name);
	}

	private static String combatDiffColor(int i, int j)
	{
		int k = i - j;
		if(k < -9)
			return "@red@";
		if(k < -6)
			return "@or3@";
		if(k < -3)
			return "@or2@";
		if(k < 0)
			return "@or1@";
		if(k > 9)
			return "@gre@";
		if(k > 6)
			return "@gr3@";
		if(k > 3)
			return "@gr2@";
		if(k > 0)
			return "@gr1@";
		else
			return "@yel@";
	}

	private void setWaveVolume(int i)
	{
		signlink.wavevol = i;
	}

	private void draw3dScreen()
	{
		drawSplitPrivateChat();
		if(crossType == 1)
		{
			crosses[crossIndex / 100].drawSprite(crossX - 8 - (clientSize == 0 ? 4 : 0), crossY - 8 - (clientSize == 0 ? 4 : 0));
			anInt1142_static++;
			if(anInt1142_static > 67)
			{
				anInt1142_static = 0;
				stream.createFrame(78);
			}
		}
		if(crossType == 2)
			crosses[4 + crossIndex / 100].drawSprite(crossX - 8 - (clientSize == 0 ? 4 : 0), crossY - 8 - (clientSize == 0 ? 4 : 0));
		if(anInt1018 != -1)
		{
			animateInterface(cameraTargetLocalZ, anInt1018);
			drawInterface(0, 0, RSInterface.interfaceCache[anInt1018], 0);
		}
		if(openInterfaceID != -1)
		{
			animateInterface(cameraTargetLocalZ, openInterfaceID);
			drawInterface(0, 0, RSInterface.interfaceCache[openInterfaceID], 0);
		}
		checkWildernessStatus();
		if(!menuOpen)
		{
			processRightClick();
			drawTooltip();
		} else
		if(menuScreenArea == 0)
			drawMenu();
		if(flashingSideicon == 1)
			multiOverlay.drawSprite(472, 296);
		if(fpsOn)
		{
			char c = '\u01FB';
			int k = 20;
			int i1 = 0xffff00;
			if(super.fps < 15)
				i1 = 0xff0000;
			boldFont.drawCenteredText("Fps:" + super.fps, c, i1, k);
			k += 15;
			Runtime runtime = Runtime.getRuntime();
			int j1 = (int)((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
			i1 = 0xffff00;
			if(j1 > 0x2000000 && lowMem)
				i1 = 0xff0000;
			boldFont.drawCenteredText("Mem:" + j1 + "k", c, 0xffff00, k);
			k += 15;
		}
		int i1 = 0xffff00;
		int x = baseX + (myPlayer.x - 6 >> 7);
		int y = baseY + (myPlayer.y - 6 >> 7);
		if (clientData)
		{
			char c = '\u01FB';
			int k = 20;
			if(super.fps < 15)
			i1 = 0xff0000;
			boldFont.drawText(0xffff00, "Fps: " + super.fps, 285, 5);
			Runtime runtime = Runtime.getRuntime();
			int j1 = (int)((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
			i1 = 0xffff00;
			if(j1 > 0x2000000 && lowMem)
			i1 = 0xff0000;
			k += 15;
			boldFont.drawText(0xffff00, "Mem: " + j1 + "k", 299, 5);
			boldFont.drawText(0xffff00, "Mouse X: " + super.mouseX + " , Mouse Y: " + super.mouseY, 314, 5);
			boldFont.drawText(0xffff00, "Coords: " + x + ", " + y, 329, 5);
		}
		if(anInt1104 != 0)
		{
			int j = anInt1104 / 50;
			int l = j / 60;
			j %= 60;
			if(j < 10)
				boldFont.drawText(0xffff00, "System update in: " + l + ":0" + j, 329, 4);
			else
				boldFont.drawText(0xffff00, "System update in: " + l + ":" + j, 329, 4);
			lastKnownPlane++;
			if(lastKnownPlane > 75)
			{
				lastKnownPlane = 0;
				stream.createFrame(148);
			}
		}
	}

	private void addIgnore(long l)
	{
		try
		{
			if(l == 0L)
				return;
			if(ignoreCount >= 100)
			{
				pushMessage("Your ignore list is full. Max of 100 hit", 0, "");
				return;
			}
			String s = TextClass.fixName(TextClass.nameForLong(l));
			for(int j = 0; j < ignoreCount; j++)
				if(ignoreListAsLongs[j] == l)
				{
					pushMessage(s + " is already on your ignore list", 0, "");
					return;
				}
			for(int k = 0; k < friendsCount; k++)
				if(friendsListAsLongs[k] == l)
				{
					pushMessage("Please remove " + s + " from your friend list first", 0, "");
					return;
				}

			ignoreListAsLongs[ignoreCount++] = l;
			needDrawTabArea = true;
			stream.createFrame(133);
			stream.writeQWord(l);
			return;
		}
		catch(RuntimeException runtimeexception)
		{
			signlink.reporterror("45688, " + l + ", " + 4 + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private void processPlayerMovement()
	{
		for(int i = -1; i < playerCount; i++)
		{
			int j;
			if(i == -1)
				j = myPlayerIndex;
			else
				j = playerIndices[i];
			Player player = playerArray[j];
			if(player != null)
				processEntityMovement(player);
		}

	}

	private void processSpawnObjects()
	{
		if(loadingStage == 2)
		{
			for(SpawnObjectNode spawnObjectNode = (SpawnObjectNode)spawnObjectList.reverseGetFirst(); spawnObjectNode != null; spawnObjectNode = (SpawnObjectNode)spawnObjectList.reverseGetNext())
			{
				if(spawnObjectNode.delay > 0)
					spawnObjectNode.delay--;
				if(spawnObjectNode.delay == 0)
				{
					if(spawnObjectNode.previousId < 0 || ObjectManager.objectHasActions(spawnObjectNode.previousId, spawnObjectNode.previousType))
					{
						spawnOrRemoveObject(spawnObjectNode.objectY, spawnObjectNode.objectPlane, spawnObjectNode.previousOrientation, spawnObjectNode.previousType, spawnObjectNode.objectX, spawnObjectNode.group, spawnObjectNode.previousId);
						spawnObjectNode.unlink();
					}
				} else
				{
					if(spawnObjectNode.longestDelay > 0)
						spawnObjectNode.longestDelay--;
					if(spawnObjectNode.longestDelay == 0 && spawnObjectNode.objectX >= 1 && spawnObjectNode.objectY >= 1 && spawnObjectNode.objectX <= 102 && spawnObjectNode.objectY <= 102 && (spawnObjectNode.objectId < 0 || ObjectManager.objectHasActions(spawnObjectNode.objectId, spawnObjectNode.objectOrientation)))
					{
						spawnOrRemoveObject(spawnObjectNode.objectY, spawnObjectNode.objectPlane, spawnObjectNode.objectType, spawnObjectNode.objectOrientation, spawnObjectNode.objectX, spawnObjectNode.group, spawnObjectNode.objectId);
						spawnObjectNode.longestDelay = -1;
						if(spawnObjectNode.objectId == spawnObjectNode.previousId && spawnObjectNode.previousId == -1)
							spawnObjectNode.unlink();
						else
						if(spawnObjectNode.objectId == spawnObjectNode.previousId && spawnObjectNode.objectType == spawnObjectNode.previousOrientation && spawnObjectNode.objectOrientation == spawnObjectNode.previousType)
							spawnObjectNode.unlink();
					}
				}
			}

		}
	}
	
	
	//stops the click from going over sprite
	private void determineMenuSize()
	{
		int i = chatTextDrawingArea.getTextWidth("Choose Option");
		for(int j = 0; j < menuActionRow; j++)
		{
			int k = chatTextDrawingArea.getTextWidth(menuActionName[j]);
			if(k > i)
				i = k;
		}

		i += 8;
		int l = 15 * menuActionRow + 21;
		if(super.saveClickX > (clientSize == 0 ? 4 : 0) && super.saveClickY > (clientSize == 0 ? 4 : 0) && super.saveClickX < (clientSize == 0 ? 516 : clientWidth) && super.saveClickY < (clientSize == 0 ? 338 : clientHeight))
		{
			int i1 = super.saveClickX - 4 - i / 2;
			if(i1 + i > 512)
				i1 = 512 - i;
			if(i1 < 0)
				i1 = 0;
			int l1 = super.saveClickY - 4;
			if(l1 + l > 334)
				l1 = 334 - l;
			if(l1 < 0)
				l1 = 0;
			menuOpen = true;
			menuScreenArea = 0;
			menuOffsetX = i1;
			menuOffsetY = l1;
			menuWidth = i;
			menuHeight = 15 * menuActionRow + 22;
		}
		if(super.saveClickX > (clientSize == 0 ? 519 : clientWidth - 246) && super.saveClickY > (clientSize == 0 ? 168 : clientHeight - 335) && super.saveClickX < (clientSize == 0 ? 765 : clientWidth) && super.saveClickY < (clientSize == 0 ? 503 : clientHeight))
		{
			int j1 = super.saveClickX - (clientSize == 0 ? 519 : clientWidth - 246) - i / 2;
			if(j1 < 0)
				j1 = 0;
			else
			if(j1 + i > 245)
				j1 = 245 - i;
			int i2 = super.saveClickY - (clientSize == 0 ? 168 : clientHeight - 335);
			if(i2 < 0)
				i2 = 0;
			else
			if(i2 + l > 333)
				i2 = 333 - l;
			menuOpen = true;
			menuScreenArea = 1;
			menuOffsetX = j1;
			menuOffsetY = i2;
			menuWidth = i;
			menuHeight = 15 * menuActionRow + 22;
		}
		if(super.saveClickX > 0 && super.saveClickY > (clientSize == 0 ? 338 : clientHeight - 165) && super.saveClickX < (clientSize == 0 ? 516 : 516) && super.saveClickY < (clientSize == 0 ? 503 : clientHeight))
		{
			int k1 = super.saveClickX - 0 - i / 2;
			if(k1 < 0)
				k1 = 0;
			else
			if(k1 + i > (clientSize == 0 ? 516 : clientWidth))
				k1 = 516 - i;
			int j2 = super.saveClickY - (clientSize == 0 ? 338 : clientHeight - 165);
			if(j2 < 0)
				j2 = 0;
			else
			if(j2 + l > 165)
				j2 = 165 - l;
			menuOpen = true;
			menuScreenArea = 2;
			menuOffsetX = k1;
			menuOffsetY = j2;
			menuWidth = i;
			menuHeight = 15 * menuActionRow + 22;
		}
	}

	private void parseLocalPlayerMovement(Stream stream)
	{
		stream.initBitAccess();
		int j = stream.readBits(1);
		if(j == 0)
			return;
		int k = stream.readBits(2);
		if(k == 0)
		{
			entityIndices[entityCount++] = myPlayerIndex;
			return;
		}
		if(k == 1)
		{
			int l = stream.readBits(3);
			myPlayer.moveInDir(false, l);
			int k1 = stream.readBits(1);
			if(k1 == 1)
				entityIndices[entityCount++] = myPlayerIndex;
			return;
		}
		if(k == 2)
		{
			int i1 = stream.readBits(3);
			myPlayer.moveInDir(true, i1);
			int l1 = stream.readBits(3);
			myPlayer.moveInDir(true, l1);
			int j2 = stream.readBits(1);
			if(j2 == 1)
				entityIndices[entityCount++] = myPlayerIndex;
			return;
		}
		if(k == 3)
		{
			plane = stream.readBits(2);
			int j1 = stream.readBits(1);
			int i2 = stream.readBits(1);
			if(i2 == 1)
				entityIndices[entityCount++] = myPlayerIndex;
			int k2 = stream.readBits(7);
			int l2 = stream.readBits(7);
			myPlayer.setPos(l2, k2, j1 == 1);
		}
	}

	private void nullLoader()
	{
		midiFading = false;
		while(drawingFlames)
		{
			midiFading = false;
			try
			{
				Thread.sleep(50L);
			}
			catch(Exception _ex) { }
		}
		loginFireLeft = null;
		loginFireRight = null;
		loginScreenSprites = null;
		entityUpdateX = null;
		entityUpdateY = null;
		entityUpdateId = null;
		entityUpdateFace = null;
		chatScrollPositions = null;
		chatHighlights = null;
		npcUpdateTypes = null;
		npcLocalIndices = null;
		chatAreaBackground = null;
		chatSettingsBackground = null;
	}

	private boolean animateInterface(int i, int j)
	{
		boolean flag1 = false;
		if(j < 0 || j >= RSInterface.interfaceCache.length)
			return false;
		RSInterface class9 = RSInterface.interfaceCache[j];
		if(class9 == null || class9.children == null)
			return false;
		for(int k = 0; k < class9.children.length; k++)
		{
			if(class9.children[k] == -1)
				break;
			if(class9.children[k] < 0 || class9.children[k] >= RSInterface.interfaceCache.length)
				continue;
			RSInterface class9_1 = RSInterface.interfaceCache[class9.children[k]];
			if(class9_1.type == 1)
				flag1 |= animateInterface(i, class9_1.id);
			if(class9_1.type == 6 && (class9_1.enabledAnimation != -1 || class9_1.disabledAnimation != -1))
			{
				boolean flag2 = interfaceIsSelected(class9_1);
				int l;
				if(flag2)
					l = class9_1.disabledAnimation;
				else
					l = class9_1.enabledAnimation;
				if(l != -1)
				{
					Animation animation = Animation.anims[l];
					for(class9_1.animationId += i; class9_1.animationId > animation.getFrameDuration(class9_1.enabledSpriteId);)
					{
						class9_1.animationId -= animation.getFrameDuration(class9_1.enabledSpriteId) + 1;
						class9_1.enabledSpriteId++;
						if(class9_1.enabledSpriteId >= animation.frameCount)
						{
							class9_1.enabledSpriteId -= animation.loopOffset;
							if(class9_1.enabledSpriteId < 0 || class9_1.enabledSpriteId >= animation.frameCount)
								class9_1.enabledSpriteId = 0;
						}
						flag1 = true;
					}

				}
			}
		}

		return flag1;
	}

	private boolean groundFlag(int plane, int x, int y) {
		if(x < 0 || x >= 104 || y < 0 || y >= 104)
			return false;
		if(byteGroundArray == null || byteGroundArray[plane] == null)
			return false;
		return (byteGroundArray[plane][x][y] & 4) != 0;
	}

	private int getCameraPlane()
	{
		int j = 3;
		if(yCameraCurve < 310)
		{
			int k = xCameraPos >> 7;
			int l = yCameraPos >> 7;
			int i1 = myPlayer.x >> 7;
			int j1 = myPlayer.y >> 7;
			if(k < 0 || k >= 104 || l < 0 || l >= 104 || i1 < 0 || i1 >= 104 || j1 < 0 || j1 >= 104)
				return j;
			if(groundFlag(plane, k, l))
				j = plane;
			int k1;
			if(i1 > k)
				k1 = i1 - k;
			else
				k1 = k - i1;
			int l1;
			if(j1 > l)
				l1 = j1 - l;
			else
				l1 = l - j1;
			if(k1 > l1)
			{
				int i2 = (l1 * 0x10000) / k1;
				int k2 = 32768;
				while(k != i1) 
				{
					if(k < i1)
						k++;
					else
					if(k > i1)
						k--;
					if(groundFlag(plane, k, l))
						j = plane;
					k2 += i2;
					if(k2 >= 0x10000)
					{
						k2 -= 0x10000;
						if(l < j1)
							l++;
						else
						if(l > j1)
							l--;
						if(groundFlag(plane, k, l))
							j = plane;
					}
				}
			} else if(l1 > 0)
			{
				int j2 = (k1 * 0x10000) / l1;
				int l2 = 32768;
				while(l != j1) 
				{
					if(l < j1)
						l++;
					else
					if(l > j1)
						l--;
					if(groundFlag(plane, k, l))
						j = plane;
					l2 += j2;
					if(l2 >= 0x10000)
					{
						l2 -= 0x10000;
						if(k < i1)
							k++;
						else
						if(k > i1)
							k--;
						if(groundFlag(plane, k, l))
							j = plane;
					}
				}
			}
		}
		if(groundFlag(plane, myPlayer.x >> 7, myPlayer.y >> 7))
			j = plane;
		return j;
	}

	private int getCameraRenderPlane()
	{
		int j = getTileHeight(plane, yCameraPos, xCameraPos);
		if(j - zCameraPos < 800 && groundFlag(plane, xCameraPos >> 7, yCameraPos >> 7))
			return plane;
		else
			return 3;
	}

	private void delIgnore(long l)
	{
		try
		{
			if(l == 0L)
				return;
			for(int j = 0; j < ignoreCount; j++)
				if(ignoreListAsLongs[j] == l)
				{
					ignoreCount--;
					needDrawTabArea = true;
					System.arraycopy(ignoreListAsLongs, j + 1, ignoreListAsLongs, j, ignoreCount - j);

					stream.createFrame(74);
					stream.writeQWord(l);
					return;
				}

			return;
		}
		catch(RuntimeException runtimeexception)
		{
			signlink.reporterror("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}
	
	
	private void chatJoin(long l) {
		try {
			if(l == 0L)
				return;
			stream.createFrame(60);
			stream.writeQWord(l);
			return;
		}
		catch(RuntimeException runtimeexception)
		{
			signlink.reporterror("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	
	}
	
	public String getParameter(String s)
	{
		if(signlink.mainapp != null)
			return "";
		else
			return super.getParameter(s);
	}

	private void adjustVolume(boolean flag, int i)
	{
		signlink.midivol = i;
		if(flag)
			signlink.midi = "voladjust";
	}

	private int extractInterfaceValues(RSInterface class9, int j)
	{
		if(class9.valueIndexArray == null || j >= class9.valueIndexArray.length)
			return -2;
		try
		{
			int ai[] = class9.valueIndexArray[j];
			int k = 0;
			int l = 0;
			int i1 = 0;
			do
			{
				int j1 = ai[l++];
				int k1 = 0;
				byte byte0 = 0;
				if(j1 == 0)
					return k;
				if(j1 == 1)
					k1 = currentStats[ai[l++]];
				if(j1 == 2)
					k1 = maxStats[ai[l++]];
				if(j1 == 3)
					k1 = currentExp[ai[l++]];
				if(j1 == 4)
				{
					RSInterface class9_1 = RSInterface.interfaceCache[ai[l++]];
					int k2 = ai[l++];
					if(k2 >= 0 && k2 < ItemDef.totalItems && (!ItemDef.forID(k2).membersObject || isMembers))
					{
						for(int j3 = 0; j3 < class9_1.inv.length; j3++)
							if(class9_1.inv[j3] == k2 + 1)
								k1 += class9_1.invStackSizes[j3];

					}
				}
				if(j1 == 5)
					k1 = variousSettings[ai[l++]];
				if(j1 == 6)
					k1 = XP_TABLE[maxStats[ai[l++]] - 1];
				if(j1 == 7)
					k1 = (variousSettings[ai[l++]] * 100) / 46875;
				if(j1 == 8)
					k1 = myPlayer.combatLevel;
				if(j1 == 9)
				{
					for(int l1 = 0; l1 < Skills.skillsCount; l1++)
						if(Skills.skillEnabled[l1])
							k1 += maxStats[l1];

				}
				if(j1 == 10)
				{
					RSInterface class9_2 = RSInterface.interfaceCache[ai[l++]];
					int l2 = ai[l++] + 1;
					if(l2 >= 0 && l2 < ItemDef.totalItems && (!ItemDef.forID(l2).membersObject || isMembers))
					{
						for(int k3 = 0; k3 < class9_2.inv.length; k3++)
						{
							if(class9_2.inv[k3] != l2)
								continue;
							k1 = 0x3b9ac9ff;
							break;
						}

					}
				}
				if(j1 == 11)
					k1 = energy;
				if(j1 == 12)
					k1 = weight;
				if(j1 == 13)
				{
					int i2 = variousSettings[ai[l++]];
					int i3 = ai[l++];
					k1 = (i2 & 1 << i3) == 0 ? 0 : 1;
				}
				if(j1 == 14)
				{
					int j2 = ai[l++];
					VarBit varBit = VarBit.cache[j2];
					int l3 = varBit.settingIndex;
					int i4 = varBit.lowBit;
					int j4 = varBit.highBit;
					int k4 = BIT_MASKS[j4 - i4];
					k1 = variousSettings[l3] >> i4 & k4;
				}
				if(j1 == 15)
					byte0 = 1;
				if(j1 == 16)
					byte0 = 2;
				if(j1 == 17)
					byte0 = 3;
				if(j1 == 18)
					k1 = (myPlayer.x >> 7) + baseX;
				if(j1 == 19)
					k1 = (myPlayer.y >> 7) + baseY;
				if(j1 == 20)
					k1 = ai[l++];
				if(byte0 == 0)
				{
					if(i1 == 0)
						k += k1;
					if(i1 == 1)
						k -= k1;
					if(i1 == 2 && k1 != 0)
						k /= k1;
					if(i1 == 3)
						k *= k1;
					i1 = 0;
				} else
				{
					i1 = byte0;
				}
			} while(true);
		}
		catch(Exception _ex)
		{
			return -1;
		}
	}

	private void drawTooltip() {
		if(menuActionRow < 2 && itemSelected == 0 && spellSelected == 0)
			return;
		String s;
		if(itemSelected == 1 && menuActionRow < 2)
			s = "Use " + selectedItemName + " with...";
		else if(spellSelected == 1 && menuActionRow < 2)
			s = spellTooltip + "...";
		else
			s = menuActionName[menuActionRow - 1];
		if(menuActionRow > 2)
			s = s + "@whi@ / " + (menuActionRow - 2) + " more options";
		chatTextDrawingArea.drawTextAlpha(4, 0xffffff, s, loopCycle / 1000, 15);
	}

	private void drawMinimap() {
		titleButtonIP.initDrawingArea();
		if(chatAreaScrollPos == 2) {
			byte abyte0[] = mapBack.aByteArray1450;
			int ai[] = DrawingArea.pixels;
			int k2 = abyte0.length;
			for(int i5 = 0; i5 < k2; i5++)
				if(abyte0[i5] == 0)
					ai[i5] = 0;
			compass.drawClipped(33, minimapInt1, minimapHintY, 256, flameLeftX, 25, 0, 0, 33, 25);
			loginMsgIP.initDrawingArea();
			return;
		}
		int i = minimapInt1 + minimapInt2 & 0x7ff;
		int j = 48 + myPlayer.x / 32;
		int l2 = 464 - myPlayer.y / 32;
		minimapSprite.drawClipped(151, i, chatFilterOffsets, 256 + minimapInt3, minimapHintX, l2, 5, 25, 146, j);
		compass.drawClipped(33, minimapInt1, minimapHintY, 256, flameLeftX, 25, 0, 0, 33, 25);
		for(int j5 = 0; j5 < mapFunctionCount; j5++) {
			int k = (mapFunctionX[j5] * 4 + 2) - myPlayer.x / 32;
			int i3 = (mapFunctionY[j5] * 4 + 2) - myPlayer.y / 32;
			markMinimap(minimapImages[j5], k, i3);
		}
		for(int k5 = 0; k5 < 104; k5++) {
			for(int l5 = 0; l5 < 104; l5++) {
				NodeList class19 = groundArray[plane][k5][l5];
				if(class19 != null) {
					int l = (k5 * 4 + 2) - myPlayer.x / 32;
					int j3 = (l5 * 4 + 2) - myPlayer.y / 32;
					markMinimap(mapDotItem, l, j3);
				}
			}
		}
		for(int i6 = 0; i6 < npcCount; i6++) {
			NPC npc = npcArray[npcIndices[i6]];
			if(npc != null && npc.isVisible()) {
				EntityDef entityDef = npc.desc;
				if(entityDef.childrenIDs != null)
					entityDef = entityDef.getChildDefinition();
				if(entityDef != null && entityDef.drawOnMinimap && entityDef.clickable) {
					int i1 = npc.x / 32 - myPlayer.x / 32;
					int k3 = npc.y / 32 - myPlayer.y / 32;
					markMinimap(mapDotNPC, i1, k3);
				}
			}
		}
		for(int j6 = 0; j6 < playerCount; j6++) {
			Player player = playerArray[playerIndices[j6]];
			if(player != null && player.isVisible()) {
				int j1 = player.x / 32 - myPlayer.x / 32;
				int l3 = player.y / 32 - myPlayer.y / 32;
				boolean flag1 = false;
				boolean flag3 = false;
				for (int j3 = 0; j3 < clanList.length; j3++) {
					if (clanList[j3] == null)
						continue;
					if (!clanList[j3].equalsIgnoreCase(player.name))
						continue;
					flag3 = true;
					break;
				}
				long l6 = TextClass.longForName(player.name);
				for(int k6 = 0; k6 < friendsCount; k6++) {
					if(l6 != friendsListAsLongs[k6] || friendsNodeIDs[k6] == 0)
						continue;
					flag1 = true;
					break;
				}
				boolean flag2 = false;
				if(myPlayer.team != 0 && player.team != 0 && myPlayer.team == player.team)
					flag2 = true;
				if(flag1)
					markMinimap(mapDotFriend, j1, l3);
				else if(flag3)
					markMinimap(mapDotClan, j1, l3);
				else if(flag2)
					markMinimap(mapDotTeam, j1, l3);
				else
					markMinimap(mapDotPlayer, j1, l3);
			}
		}
		if(minimapRotation != 0 && loopCycle % 20 < 10) {
			if(minimapRotation == 1 && hintIconNpcIndex >= 0 && hintIconNpcIndex < npcArray.length) {
				NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[hintIconNpcIndex];
				if(class30_sub2_sub4_sub1_sub1_1 != null) {
					int k1 = class30_sub2_sub4_sub1_sub1_1.x / 32 - myPlayer.x / 32;
					int i4 = class30_sub2_sub4_sub1_sub1_1.y / 32 - myPlayer.y / 32;
					drawMinimapEdgeSprite(mapMarker, i4, k1);
				}
			}
			if(minimapRotation == 2) {
				int l1 = ((cameraTargetTileX - baseX) * 4 + 2) - myPlayer.x / 32;
				int j4 = ((cameraTargetTileY - baseY) * 4 + 2) - myPlayer.y / 32;
				drawMinimapEdgeSprite(mapMarker, j4, l1);
			}
			if(minimapRotation == 10 && cameraTargetIndex >= 0 && cameraTargetIndex < playerArray.length) {
				Player class30_sub2_sub4_sub1_sub2_1 = playerArray[cameraTargetIndex];
				if(class30_sub2_sub4_sub1_sub2_1 != null) {
					int i2 = class30_sub2_sub4_sub1_sub2_1.x / 32 - myPlayer.x / 32;
					int k4 = class30_sub2_sub4_sub1_sub2_1.y / 32 - myPlayer.y / 32;
					drawMinimapEdgeSprite(mapMarker, k4, i2);
				}
			}
		}
		if(destX != 0) {
			int j2 = (destX * 4 + 2) - myPlayer.x / 32;
			int l4 = (destY * 4 + 2) - myPlayer.y / 32;
			markMinimap(mapFlag, j2, l4);
		}
		DrawingArea.drawPixels(3, 78, 97, 0xffffff, 3);
		mapBack.drawBackground(0, 0);
		loadOrbs();
		loginMsgIP.initDrawingArea();
	}

	private void npcScreenPos(Entity entity, int i) {
		calcEntityScreenPos(entity.x, i, entity.y);
	}

	private void calcEntityScreenPos(int i, int j, int l) {
		if(i < 128 || l < 128 || i > 13056 || l > 13056) {
			spriteDrawX = -1;
			spriteDrawY = -1;
			return;
		}
		int i1 = getTileHeight(plane, l, i) - j;
		i -= xCameraPos;
		i1 -= zCameraPos;
		l -= yCameraPos;
		int j1 = Model.SINE[yCameraCurve];
		int k1 = Model.COSINE[yCameraCurve];
		int l1 = Model.SINE[xCameraCurve];
		int i2 = Model.COSINE[xCameraCurve];
		int j2 = l * l1 + i * i2 >> 16;
		l = l * i2 - i * l1 >> 16;
		i = j2;
		j2 = i1 * k1 - l * j1 >> 16;
		l = i1 * j1 + l * k1 >> 16;
		i1 = j2;
		if(l >= 50) {
			spriteDrawX = Texture.textureInt1 + (i << 9) / l;
			spriteDrawY = Texture.textureInt2 + (i1 << 9) / l;
		} else {
			spriteDrawX = -1;
			spriteDrawY = -1;
		}
	}

	private void buildSplitPrivateChatMenu()
	{
		if(splitPrivateChat == 0)
			return;
		int i = 0;
		if(anInt1104 != 0)
			i = 1;
		for(int j = 0; j < 100; j++)
			if(chatMessages[j] != null)
			{
				int k = chatTypes[j];
				String s = chatNames[j];
				boolean flag1 = false;
				if(s != null && s.startsWith("@cr1@"))
				{
					s = s.substring(5);
					boolean flag2 = true;
				}
				if(s != null && s.startsWith("@cr2@"))
				{
					s = s.substring(5);
					byte byte0 = 2;
				}
				if((k == 3 || k == 7) && (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s)))
				{
					int l = 329 - i * 13;
					if(super.mouseX > 4 && super.mouseY - 4 > l - 10 && super.mouseY - 4 <= l + 3)
					{
						int i1 = boldFont.getTextWidth("From:  " + s + chatMessages[j]) + 25;
						if(i1 > 450)
							i1 = 450;
						if(super.mouseX < 4 + i1)
						{
							if(myPrivilege >= 1)
							{
								menuActionName[menuActionRow] = "Report abuse @whi@" + s;
								menuActionID[menuActionRow] = 2606;
								menuActionRow++;
							}
							menuActionName[menuActionRow] = "Add ignore @whi@" + s;
							menuActionID[menuActionRow] = 2042;
							menuActionRow++;
							menuActionName[menuActionRow] = "Add friend @whi@" + s;
							menuActionID[menuActionRow] = 2337;
							menuActionRow++;
						}
					}
					if(++i >= 5)
						return;
				}
				if((k == 5 || k == 6) && privateChatMode < 2 && ++i >= 5)
					return;
			}

	}

	private void handleObjectSpawn(int j, int k, int l, int i1, int j1, int k1,
						   int l1, int i2, int j2)
	{
		SpawnObjectNode spawnObjectNode = null;
		for(SpawnObjectNode spawnObjectNode_1 = (SpawnObjectNode)spawnObjectList.reverseGetFirst(); spawnObjectNode_1 != null; spawnObjectNode_1 = (SpawnObjectNode)spawnObjectList.reverseGetNext())
		{
			if(spawnObjectNode_1.objectPlane != l1 || spawnObjectNode_1.objectX != i2 || spawnObjectNode_1.objectY != j1 || spawnObjectNode_1.group != i1)
				continue;
			spawnObjectNode = spawnObjectNode_1;
			break;
		}

		if(spawnObjectNode == null)
		{
			spawnObjectNode = new SpawnObjectNode();
			spawnObjectNode.objectPlane = l1;
			spawnObjectNode.group = i1;
			spawnObjectNode.objectX = i2;
			spawnObjectNode.objectY = j1;
			updateSpawnObjectInfo(spawnObjectNode);
			spawnObjectList.insertHead(spawnObjectNode);
		}
		spawnObjectNode.objectId = k;
		spawnObjectNode.objectOrientation = k1;
		spawnObjectNode.objectType = l;
		spawnObjectNode.longestDelay = j2;
		spawnObjectNode.delay = j;
	}

	private boolean interfaceIsSelected(RSInterface class9)
	{
		if(class9.scriptCompareType == null)
			return false;
		for(int i = 0; i < class9.scriptCompareType.length; i++)
		{
			int j = extractInterfaceValues(class9, i);
			int k = class9.scriptDefaults[i];
			if(class9.scriptCompareType[i] == 2)
			{
				if(j >= k)
					return false;
			} else
			if(class9.scriptCompareType[i] == 3)
			{
				if(j <= k)
					return false;
			} else
			if(class9.scriptCompareType[i] == 4)
			{
				if(j == k)
					return false;
			} else
			if(j != k)
				return false;
		}

		return true;
	}

	private DataInputStream openJagGrabInputStream(String s)
		throws IOException
	{
 //	   if(!continuedDialogue)
 //		   if(signlink.mainapp != null)
 //			   return signlink.openurl(s);
 //		   else
 //			   return new DataInputStream((new URL(getCodeBase(), s)).openStream());
		if(aSocket832 != null)
		{
			try
			{
				aSocket832.close();
			}
			catch(Exception _ex) { }
			aSocket832 = null;
		}
		aSocket832 = openSocket(43595);
		aSocket832.setSoTimeout(10000);
		java.io.InputStream inputstream = aSocket832.getInputStream();
		OutputStream outputstream = aSocket832.getOutputStream();
		outputstream.write(("JAGGRAB /" + s + "\n\n").getBytes());
		return new DataInputStream(inputstream);
	}

	private void doFlamesDrawing()
	{
		char c = '\u0100';
		if(walkDestX > 0)
		{
			for(int i = 0; i < 256; i++)
				if(walkDestX > 768)
					entityUpdateX[i] = blendColors(entityUpdateY[i], entityUpdateId[i], 1024 - walkDestX);
				else
				if(walkDestX > 256)
					entityUpdateX[i] = entityUpdateId[i];
				else
					entityUpdateX[i] = blendColors(entityUpdateId[i], entityUpdateY[i], 256 - walkDestX);

		} else
		if(walkDestY > 0)
		{
			for(int j = 0; j < 256; j++)
				if(walkDestY > 768)
					entityUpdateX[j] = blendColors(entityUpdateY[j], entityUpdateFace[j], 1024 - walkDestY);
				else
				if(walkDestY > 256)
					entityUpdateX[j] = entityUpdateFace[j];
				else
					entityUpdateX[j] = blendColors(entityUpdateFace[j], entityUpdateY[j], 256 - walkDestY);

		} else
		{
			System.arraycopy(entityUpdateY, 0, entityUpdateX, 0, 256);

		}
		System.arraycopy(chatAreaBackground.myPixels, 0, chatAreaIP.pixelData, 0, 33920);

		int i1 = 0;
		int j1 = 1152;
		for(int k1 = 1; k1 < c - 1; k1++)
		{
			int l1 = (flameRightX[k1] * (c - k1)) / c;
			int j2 = 22 + l1;
			if(j2 < 0)
				j2 = 0;
			i1 += j2;
			for(int l2 = j2; l2 < 128; l2++)
			{
				int j3 = npcUpdateTypes[i1++];
				if(j3 != 0)
				{
					int l3 = j3;
					int j4 = 256 - j3;
					j3 = entityUpdateX[j3];
					int l4 = chatAreaIP.pixelData[j1];
					chatAreaIP.pixelData[j1++] = ((j3 & 0xff00ff) * l3 + (l4 & 0xff00ff) * j4 & 0xff00ff00) + ((j3 & 0xff00) * l3 + (l4 & 0xff00) * j4 & 0xff0000) >> 8;
				} else
				{
					j1++;
				}
			}

			j1 += j2;
		}

		chatAreaIP.drawGraphics(0, super.graphics, 0);
		System.arraycopy(chatSettingsBackground.myPixels, 0, chatSettingIP.pixelData, 0, 33920);

		i1 = 0;
		j1 = 1176;
		for(int k2 = 1; k2 < c - 1; k2++)
		{
			int i3 = (flameRightX[k2] * (c - k2)) / c;
			int k3 = 103 - i3;
			j1 += i3;
			for(int i4 = 0; i4 < k3; i4++)
			{
				int k4 = npcUpdateTypes[i1++];
				if(k4 != 0)
				{
					int i5 = k4;
					int j5 = 256 - k4;
					k4 = entityUpdateX[k4];
					int k5 = chatSettingIP.pixelData[j1];
					chatSettingIP.pixelData[j1++] = ((k4 & 0xff00ff) * i5 + (k5 & 0xff00ff) * j5 & 0xff00ff00) + ((k4 & 0xff00) * i5 + (k5 & 0xff00) * j5 & 0xff0000) >> 8;
				} else
				{
					j1++;
				}
			}

			i1 += 128 - k3;
			j1 += 128 - k3 - i3;
		}

		chatSettingIP.drawGraphics(0, super.graphics, 637);
	}

	private void parsePlayerRemovals(Stream stream)
	{
		int j = stream.readBits(8);
		if(j < playerCount)
		{
			for(int k = j; k < playerCount; k++)
				entityUpdateIndices[npcUpdateCount++] = playerIndices[k];

		}
		if(j > playerCount)
		{
			signlink.reporterror(myUsername + " Too many players");
			throw new RuntimeException("eek");
		}
		playerCount = 0;
		for(int l = 0; l < j; l++)
		{
			int i1 = playerIndices[l];
			Player player = playerArray[i1];
			int j1 = stream.readBits(1);
			if(j1 == 0)
			{
				playerIndices[playerCount++] = i1;
				player.textColor = loopCycle;
			} else
			{
				int k1 = stream.readBits(2);
				if(k1 == 0)
				{
					playerIndices[playerCount++] = i1;
					player.textColor = loopCycle;
					entityIndices[entityCount++] = i1;
				} else
				if(k1 == 1)
				{
					playerIndices[playerCount++] = i1;
					player.textColor = loopCycle;
					int l1 = stream.readBits(3);
					player.moveInDir(false, l1);
					int j2 = stream.readBits(1);
					if(j2 == 1)
						entityIndices[entityCount++] = i1;
				} else
				if(k1 == 2)
				{
					playerIndices[playerCount++] = i1;
					player.textColor = loopCycle;
					int i2 = stream.readBits(3);
					player.moveInDir(true, i2);
					int k2 = stream.readBits(3);
					player.moveInDir(true, k2);
					int l2 = stream.readBits(1);
					if(l2 == 1)
						entityIndices[entityCount++] = i1;
				} else
				if(k1 == 3)
					entityUpdateIndices[npcUpdateCount++] = i1;
			}
		}
	}

	private void drawLoginScreen(boolean flag)
	{
		resetImageProducers();
		gameScreenIP.initDrawingArea();
		loginFireLeft.drawBackground(0, 0);
		char c = '\u0168';
		char c1 = '\310';
		if(loginScreenState == 0)
		{
			int i = c1 / 2 + 80;
			smallText.drawRightAligned(0x75a9a9, c / 2, onDemandFetcher.statusString, i, true);
			i = c1 / 2 - 20;
			chatTextDrawingArea.drawRightAligned(0xffff00, c / 2, "Welcome to RuneScape", i, true);
			i += 30;
			int l = c / 2 - 80;
			int k1 = c1 / 2 + 20;
			loginFireRight.drawBackground(l - 73, k1 - 20);
			chatTextDrawingArea.drawRightAligned(0xffffff, l, "New User", k1 + 5, true);
			l = c / 2 + 80;
			loginFireRight.drawBackground(l - 73, k1 - 20);
			chatTextDrawingArea.drawRightAligned(0xffffff, l, "Existing User", k1 + 5, true);
		}
		if(loginScreenState == 2)
		{
			int j = c1 / 2 - 40;
			if(loginMessage1.length() > 0)
			{
				chatTextDrawingArea.drawRightAligned(0xffff00, c / 2, loginMessage1, j - 15, true);
				chatTextDrawingArea.drawRightAligned(0xffff00, c / 2, loginMessage2, j, true);
				j += 30;
			} else
			{
				chatTextDrawingArea.drawRightAligned(0xffff00, c / 2, loginMessage2, j - 7, true);
				j += 30;
			}
			chatTextDrawingArea.drawWaving(true, c / 2 - 90, 0xffffff, "Username: " + myUsername + ((loginScreenCursorPos == 0) & (loopCycle % 40 < 20) ? "@yel@|" : ""), j);
			j += 15;
			chatTextDrawingArea.drawWaving(true, c / 2 - 88, 0xffffff, "Password: " + TextClass.passwordAsterisks(myPassword) + ((loginScreenCursorPos == 1) & (loopCycle % 40 < 20) ? "@yel@|" : ""), j);
			j += 15;
			if(!flag)
			{
				int i1 = c / 2 - 80;
				int l1 = c1 / 2 + 50;
				loginFireRight.drawBackground(i1 - 73, l1 - 20);
				chatTextDrawingArea.drawRightAligned(0xffffff, i1, "Login", l1 + 5, true);
				i1 = c / 2 + 80;
				loginFireRight.drawBackground(i1 - 73, l1 - 20);
				chatTextDrawingArea.drawRightAligned(0xffffff, i1, "Cancel", l1 + 5, true);
			}
		}
		if(loginScreenState == 3)
		{
						chatTextDrawingArea.drawRightAligned(0xffff00, c / 2, "Create a free account", c1 / 2 - 60, true);
			int k = c1 / 2 - 35;
			chatTextDrawingArea.drawRightAligned(0xffffff, c / 2, "To create a new account you need to", k, true);
			k += 15;
			chatTextDrawingArea.drawRightAligned(0xffffff, c / 2, "go back to the main RuneScape webpage", k, true);
			k += 15;
			chatTextDrawingArea.drawRightAligned(0xffffff, c / 2, "and choose the red 'create account'", k, true);
			k += 15;
			chatTextDrawingArea.drawRightAligned(0xffffff, c / 2, "button at the top right of that page.", k, true);
			k += 15;
			int j1 = c / 2;
			int i2 = c1 / 2 + 50;
			loginFireRight.drawBackground(j1 - 73, i2 - 20);
			chatTextDrawingArea.drawRightAligned(0xffffff, j1, "Cancel", i2 + 5, true);
		}
		gameScreenIP.drawGraphics(171, super.graphics, 202);
		if(welcomeScreenRaised)
		{
			welcomeScreenRaised = false;
			tabImageProducer.drawGraphics(0, super.graphics, 128);
			mapAreaIP.drawGraphics(371, super.graphics, 202);
			topSideIP1.drawGraphics(265, super.graphics, 0);
			topSideIP2.drawGraphics(265, super.graphics, 562);
			bottomSideIP1.drawGraphics(171, super.graphics, 128);
			bottomSideIP2.drawGraphics(171, super.graphics, 562);
		}
	}

	private void drawFlames()
	{
		drawingFlames = true;
		try
		{
			long l = System.currentTimeMillis();
			int i = 0;
			int j = 20;
			while(midiFading) 
			{
				lastMapRegionY++;
				calcFlamesPosition();
				calcFlamesPosition();
				doFlamesDrawing();
				if(++i > 10)
				{
					long l1 = System.currentTimeMillis();
					int k = (int)(l1 - l) / 10 - j;
					j = 40 - k;
					if(j < 5)
						j = 5;
					i = 0;
					l = l1;
				}
				try
				{
					Thread.sleep(j);
				}
				catch(Exception _ex) { }
			}
		}
		catch(Exception _ex) { }
		drawingFlames = false;
	}

	public void raiseWelcomeScreen()
	{
		welcomeScreenRaised = true;
	}

	private void parseGroupPacket(Stream stream, int j)
	{
		if(j == 84)
		{
			int k = stream.readUnsignedByte();
			int j3 = hintIconDrawX + (k >> 4 & 7);
			int i6 = hintIconDrawY + (k & 7);
			int l8 = stream.readUnsignedWord();
			int k11 = stream.readUnsignedWord();
			int l13 = stream.readUnsignedWord();
			if(j3 >= 0 && i6 >= 0 && j3 < 104 && i6 < 104)
			{
				NodeList class19_1 = groundArray[plane][j3][i6];
				if(class19_1 != null)
				{
					for(Item class30_sub2_sub4_sub2_3 = (Item)class19_1.reverseGetFirst(); class30_sub2_sub4_sub2_3 != null; class30_sub2_sub4_sub2_3 = (Item)class19_1.reverseGetNext())
					{
						if(class30_sub2_sub4_sub2_3.ID != (l8 & 0x7fff) || class30_sub2_sub4_sub2_3.itemQuantity != k11)
							continue;
						class30_sub2_sub4_sub2_3.itemQuantity = l13;
						break;
					}

					spawnGroundItem(j3, i6);
				}
			}
			return;
		}
		if(j == 105)
		{
			int l = stream.readUnsignedByte();
			int k3 = hintIconDrawX + (l >> 4 & 7);
			int j6 = hintIconDrawY + (l & 7);
			int i9 = stream.readUnsignedWord();
			int l11 = stream.readUnsignedByte();
			int i14 = l11 >> 4 & 0xf;
			int i16 = l11 & 7;
			if(myPlayer.smallX[0] >= k3 - i14 && myPlayer.smallX[0] <= k3 + i14 && myPlayer.smallY[0] >= j6 - i14 && myPlayer.smallY[0] <= j6 + i14 && pendingInput && !lowMem && lastMapRegionX < 50)
			{
				tabAreaY[lastMapRegionX] = i9;
				menuActionCmd5[lastMapRegionX] = i16;
				mapObjectIds[lastMapRegionX] = Sounds.delays[i9];
				lastMapRegionX++;
			}
		}
		if(j == 215)
		{
			int i1 = stream.readWordBigA();
			int l3 = stream.readUnsignedByteSub();
			int k6 = hintIconDrawX + (l3 >> 4 & 7);
			int j9 = hintIconDrawY + (l3 & 7);
			int i12 = stream.readWordBigA();
			int j14 = stream.readUnsignedWord();
			if(k6 >= 0 && j9 >= 0 && k6 < 104 && j9 < 104 && i12 != unknownInt10)
			{
				Item class30_sub2_sub4_sub2_2 = new Item();
				class30_sub2_sub4_sub2_2.ID = i1;
				class30_sub2_sub4_sub2_2.itemQuantity = j14;
				if(groundArray[plane][k6][j9] == null)
					groundArray[plane][k6][j9] = new NodeList();
				groundArray[plane][k6][j9].insertHead(class30_sub2_sub4_sub2_2);
				spawnGroundItem(k6, j9);
			}
			return;
		}
		if(j == 156)
		{
			int j1 = stream.readUnsignedByteAdd();
			int i4 = hintIconDrawX + (j1 >> 4 & 7);
			int l6 = hintIconDrawY + (j1 & 7);
			int k9 = stream.readUnsignedWord();
			if(i4 >= 0 && l6 >= 0 && i4 < 104 && l6 < 104)
			{
				NodeList class19 = groundArray[plane][i4][l6];
				if(class19 != null)
				{
					for(Item item = (Item)class19.reverseGetFirst(); item != null; item = (Item)class19.reverseGetNext())
					{
						if(item.ID != (k9 & 0x7fff))
							continue;
						item.unlink();
						break;
					}

					if(class19.reverseGetFirst() == null)
						groundArray[plane][i4][l6] = null;
					spawnGroundItem(i4, l6);
				}
			}
			return;
		}
		if(j == 160)
		{
			int k1 = stream.readUnsignedByteSub();
			int j4 = hintIconDrawX + (k1 >> 4 & 7);
			int i7 = hintIconDrawY + (k1 & 7);
			int l9 = stream.readUnsignedByteSub();
			int j12 = l9 >> 2;
			int k14 = l9 & 3;
			int j16 = mapChunkX[j12];
			int j17 = stream.readWordBigA();
			if(j4 >= 0 && i7 >= 0 && j4 < 103 && i7 < 103)
			{
				int j18 = intGroundArray[plane][j4][i7];
				int i19 = intGroundArray[plane][j4 + 1][i7];
				int l19 = intGroundArray[plane][j4 + 1][i7 + 1];
				int k20 = intGroundArray[plane][j4][i7 + 1];
				if(j16 == 0)
				{
					WallObject class10 = worldController.getWallObject(plane, j4, i7);
					if(class10 != null)
					{
						int k21 = class10.uid >> 14 & 0x7fff;
						if(j12 == 2)
						{
							class10.renderable1 = new Animable_Sub5(k21, 4 + k14, 2, i19, l19, j18, k20, j17, false);
							class10.renderable2 = new Animable_Sub5(k21, k14 + 1 & 3, 2, i19, l19, j18, k20, j17, false);
						} else
						{
							class10.renderable1 = new Animable_Sub5(k21, k14, j12, i19, l19, j18, k20, j17, false);
						}
					}
				}
				if(j16 == 1)
				{
					WallDecoration class26 = worldController.getWallDecoration(j4, i7, plane);
					if(class26 != null)
						class26.renderable = new Animable_Sub5(class26.uid >> 14 & 0x7fff, 0, 4, i19, l19, j18, k20, j17, false);
				}
				if(j16 == 2)
				{
					InteractiveObject class28 = worldController.getInteractiveObject(j4, i7, plane);
					if(j12 == 11)
						j12 = 10;
					if(class28 != null)
						class28.renderable = new Animable_Sub5(class28.uid >> 14 & 0x7fff, k14, j12, i19, l19, j18, k20, j17, false);
				}
				if(j16 == 3)
				{
					GroundDecoration class49 = worldController.getGroundDecoration(i7, j4, plane);
					if(class49 != null)
						class49.renderable = new Animable_Sub5(class49.uid >> 14 & 0x7fff, k14, 22, i19, l19, j18, k20, j17, false);
				}
			}
			return;
		}
		if(j == 147)
		{
			int l1 = stream.readUnsignedByteSub();
			int k4 = hintIconDrawX + (l1 >> 4 & 7);
			int j7 = hintIconDrawY + (l1 & 7);
			int i10 = stream.readUnsignedWord();
			byte byte0 = stream.readSubByte();
			int l14 = stream.readWordLE();
			byte byte1 = stream.readNegByte();
			int k17 = stream.readUnsignedWord();
			int k18 = stream.readUnsignedByteSub();
			int j19 = k18 >> 2;
			int i20 = k18 & 3;
			int l20 = mapChunkX[j19];
			byte byte2 = stream.readSignedByte();
			int l21 = stream.readUnsignedWord();
			byte byte3 = stream.readNegByte();
			Player player;
			if(i10 == unknownInt10)
				player = myPlayer;
			else
				player = playerArray[i10];
			if(player != null)
			{
				ObjectDef class46 = ObjectDef.forID(l21);
				int i22 = intGroundArray[plane][k4][j7];
				int j22 = intGroundArray[plane][k4 + 1][j7];
				int k22 = intGroundArray[plane][k4 + 1][j7 + 1];
				int l22 = intGroundArray[plane][k4][j7 + 1];
				Model model = class46.getObjectModel(j19, i20, i22, j22, k22, l22, -1);
				if(model != null)
				{
					handleObjectSpawn(k17 + 1, -1, 0, l20, j7, 0, plane, k4, l14 + 1);
					player.attachedModelStartCycle = l14 + loopCycle;
					player.attachedModelEndCycle = k17 + loopCycle;
					player.attachedModel = model;
					int i23 = class46.sizeX;
					int j23 = class46.sizeY;
					if(i20 == 1 || i20 == 3)
					{
						i23 = class46.sizeY;
						j23 = class46.sizeX;
					}
					player.attachedModelX = k4 * 128 + i23 * 64;
					player.attachedModelY = j7 * 128 + j23 * 64;
					player.attachedModelOffsetY = getTileHeight(plane, player.attachedModelY, player.attachedModelX);
					if(byte2 > byte0)
					{
						byte byte4 = byte2;
						byte2 = byte0;
						byte0 = byte4;
					}
					if(byte3 > byte1)
					{
						byte byte5 = byte3;
						byte3 = byte1;
						byte1 = byte5;
					}
					player.anInt1719 = k4 + byte2;
					player.anInt1721 = k4 + byte0;
					player.anInt1720 = j7 + byte3;
					player.anInt1722 = j7 + byte1;
				}
			}
		}
		if(j == 151)
		{
			int i2 = stream.readUnsignedByteAdd();
			int l4 = hintIconDrawX + (i2 >> 4 & 7);
			int k7 = hintIconDrawY + (i2 & 7);
			int j10 = stream.readWordLE();
			int k12 = stream.readUnsignedByteSub();
			int i15 = k12 >> 2;
			int k16 = k12 & 3;
			int l17 = mapChunkX[i15];
			if(l4 >= 0 && k7 >= 0 && l4 < 104 && k7 < 104)
				handleObjectSpawn(-1, j10, k16, l17, k7, i15, plane, l4, 0);
			return;
		}
		if(j == 4)
		{
			int j2 = stream.readUnsignedByte();
			int i5 = hintIconDrawX + (j2 >> 4 & 7);
			int l7 = hintIconDrawY + (j2 & 7);
			int k10 = stream.readUnsignedWord();
			int l12 = stream.readUnsignedByte();
			int j15 = stream.readUnsignedWord();
			if(i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104)
			{
				i5 = i5 * 128 + 64;
				l7 = l7 * 128 + 64;
				Animable_Sub3 class30_sub2_sub4_sub3 = new Animable_Sub3(plane, loopCycle, j15, k10, getTileHeight(plane, l7, i5) - l12, l7, i5);
				spotAnimList.insertHead(class30_sub2_sub4_sub3);
			}
			return;
		}
		if(j == 44)
		{
			int k2 = stream.readWordLEBigA();
			int j5 = stream.readUnsignedWord();
			int i8 = stream.readUnsignedByte();
			int l10 = hintIconDrawX + (i8 >> 4 & 7);
			int i13 = hintIconDrawY + (i8 & 7);
			if(l10 >= 0 && i13 >= 0 && l10 < 104 && i13 < 104)
			{
				Item class30_sub2_sub4_sub2_1 = new Item();
				class30_sub2_sub4_sub2_1.ID = k2;
				class30_sub2_sub4_sub2_1.itemQuantity = j5;
				if(groundArray[plane][l10][i13] == null)
					groundArray[plane][l10][i13] = new NodeList();
				groundArray[plane][l10][i13].insertHead(class30_sub2_sub4_sub2_1);
				spawnGroundItem(l10, i13);
			}
			return;
		}
		if(j == 101)
		{
			int l2 = stream.readUnsignedByteNeg();
			int k5 = l2 >> 2;
			int j8 = l2 & 3;
			int i11 = mapChunkX[k5];
			int j13 = stream.readUnsignedByte();
			int k15 = hintIconDrawX + (j13 >> 4 & 7);
			int l16 = hintIconDrawY + (j13 & 7);
			if(k15 >= 0 && l16 >= 0 && k15 < 104 && l16 < 104)
				handleObjectSpawn(-1, -1, j8, i11, l16, k5, plane, k15, 0);
			return;
		}
		if(j == 117)
		{
			int i3 = stream.readUnsignedByte();
			int l5 = hintIconDrawX + (i3 >> 4 & 7);
			int k8 = hintIconDrawY + (i3 & 7);
			int j11 = l5 + stream.readSignedByte();
			int k13 = k8 + stream.readSignedByte();
			int l15 = stream.readSignedWord();
			int i17 = stream.readUnsignedWord();
			int i18 = stream.readUnsignedByte() * 4;
			int l18 = stream.readUnsignedByte() * 4;
			int k19 = stream.readUnsignedWord();
			int j20 = stream.readUnsignedWord();
			int i21 = stream.readUnsignedByte();
			int j21 = stream.readUnsignedByte();
			if(l5 >= 0 && k8 >= 0 && l5 < 104 && k8 < 104 && j11 >= 0 && k13 >= 0 && j11 < 104 && k13 < 104 && i17 != 65535)
			{
				l5 = l5 * 128 + 64;
				k8 = k8 * 128 + 64;
				j11 = j11 * 128 + 64;
				k13 = k13 * 128 + 64;
				Animable_Sub4 class30_sub2_sub4_sub4 = new Animable_Sub4(i21, l18, k19 + loopCycle, j20 + loopCycle, j21, plane, getTileHeight(plane, k8, l5) - i18, k8, l5, l15, i17);
				class30_sub2_sub4_sub4.trackTarget(k19 + loopCycle, k13, getTileHeight(plane, k13, j11) - l18, j11);
				projectileList.insertHead(class30_sub2_sub4_sub4);
			}
		}
	}

	private static void setLowMem()
	{
		WorldController.lowMem = true;
		Texture.lowMem = true;
		lowMem = true;
		ObjectManager.lowMem = true;
		ObjectDef.lowMem = true;
	}

	private void parseNPCRemovals(Stream stream)
	{
		stream.initBitAccess();
		int k = stream.readBits(8);
		if(k < npcCount)
		{
			for(int l = k; l < npcCount; l++)
				entityUpdateIndices[npcUpdateCount++] = npcIndices[l];

		}
		if(k > npcCount)
		{
			signlink.reporterror(myUsername + " Too many npcs");
			throw new RuntimeException("eek");
		}
		npcCount = 0;
		for(int i1 = 0; i1 < k; i1++)
		{
			int j1 = npcIndices[i1];
			NPC npc = npcArray[j1];
			int k1 = stream.readBits(1);
			if(k1 == 0)
			{
				npcIndices[npcCount++] = j1;
				npc.textColor = loopCycle;
			} else
			{
				int l1 = stream.readBits(2);
				if(l1 == 0)
				{
					npcIndices[npcCount++] = j1;
					npc.textColor = loopCycle;
					entityIndices[entityCount++] = j1;
				} else
				if(l1 == 1)
				{
					npcIndices[npcCount++] = j1;
					npc.textColor = loopCycle;
					int i2 = stream.readBits(3);
					npc.moveInDir(false, i2);
					int k2 = stream.readBits(1);
					if(k2 == 1)
						entityIndices[entityCount++] = j1;
				} else
				if(l1 == 2)
				{
					npcIndices[npcCount++] = j1;
					npc.textColor = loopCycle;
					int j2 = stream.readBits(3);
					npc.moveInDir(true, j2);
					int l2 = stream.readBits(3);
					npc.moveInDir(true, l2);
					int i3 = stream.readBits(1);
					if(i3 == 1)
						entityIndices[entityCount++] = j1;
				} else
				if(l1 == 3)
					entityUpdateIndices[npcUpdateCount++] = j1;
			}
		}

	}

	private void processLoginScreenInput()
	{
		if(loginScreenState == 0)
		{
			int i = super.myWidth / 2 - 80;
			int l = super.myHeight / 2 + 20;
			l += 20;
			if(super.clickMode3 == 1 && super.saveClickX >= i - 75 && super.saveClickX <= i + 75 && super.saveClickY >= l - 20 && super.saveClickY <= l + 20)
			{
				loginScreenState = 3;
				loginScreenCursorPos = 0;
			}
			i = super.myWidth / 2 + 80;
			if(super.clickMode3 == 1 && super.saveClickX >= i - 75 && super.saveClickX <= i + 75 && super.saveClickY >= l - 20 && super.saveClickY <= l + 20)
			{
				loginMessage1 = "";
				loginMessage2 = "Enter your username & password.";
				loginScreenState = 2;
				loginScreenCursorPos = 0;
			}
		} else
		{
			if(loginScreenState == 2)
			{
				int j = super.myHeight / 2 - 40;
				j += 30;
				j += 25;
				if(super.clickMode3 == 1 && super.saveClickY >= j - 15 && super.saveClickY < j)
					loginScreenCursorPos = 0;
				j += 15;
				if(super.clickMode3 == 1 && super.saveClickY >= j - 15 && super.saveClickY < j)
					loginScreenCursorPos = 1;
				j += 15;
				int i1 = super.myWidth / 2 - 80;
				int k1 = super.myHeight / 2 + 50;
				k1 += 20;
				if(super.clickMode3 == 1 && super.saveClickX >= i1 - 75 && super.saveClickX <= i1 + 75 && super.saveClickY >= k1 - 20 && super.saveClickY <= k1 + 20)
				{
					loginFailures = 0;
					login(myUsername, myPassword, false);
					if(loggedIn)
						return;
				}
				i1 = super.myWidth / 2 + 80;
				if(super.clickMode3 == 1 && super.saveClickX >= i1 - 75 && super.saveClickX <= i1 + 75 && super.saveClickY >= k1 - 20 && super.saveClickY <= k1 + 20)
				{
					loginScreenState = 0;
 //				   myUsername = "";
 //				   myPassword = "";
				}
				do
				{
					int l1 = readChar(-796);
					if(l1 == -1)
						break;
					boolean flag1 = false;
					for(int i2 = 0; i2 < validUserPassChars.length(); i2++)
					{
						if(l1 != validUserPassChars.charAt(i2))
							continue;
						flag1 = true;
						break;
					}

					if(loginScreenCursorPos == 0)
					{
						if(l1 == 8 && myUsername.length() > 0)
							myUsername = myUsername.substring(0, myUsername.length() - 1);
						if(l1 == 9 || l1 == 10 || l1 == 13)
							loginScreenCursorPos = 1;
						if(flag1)
							myUsername += (char)l1;
						if(myUsername.length() > 12)
							myUsername = myUsername.substring(0, 12);
					} else
					if(loginScreenCursorPos == 1)
					{
						if(l1 == 8 && myPassword.length() > 0)
							myPassword = myPassword.substring(0, myPassword.length() - 1);
						if(l1 == 9 || l1 == 10 || l1 == 13)
							loginScreenCursorPos = 0;
						if(flag1)
							myPassword += (char)l1;
						if(myPassword.length() > 20)
							myPassword = myPassword.substring(0, 20);
					}
				} while(true);
				return;
			}
			if(loginScreenState == 3)
			{
				int k = super.myWidth / 2;
				int j1 = super.myHeight / 2 + 50;
				j1 += 20;
				if(super.clickMode3 == 1 && super.saveClickX >= k - 75 && super.saveClickX <= k + 75 && super.saveClickY >= j1 - 20 && super.saveClickY <= j1 + 20)
					loginScreenState = 0;
			}
		}
	}

	private void markMinimap(Sprite sprite, int i, int j) {
		int k = minimapInt1 + minimapInt2 & 0x7ff;
		int l = i * i + j * j;
		if(l > 6400)
			return;
		int i1 = Model.SINE[k];
		int j1 = Model.COSINE[k];
		i1 = (i1 * 256) / (minimapInt3 + 256);
		j1 = (j1 * 256) / (minimapInt3 + 256);
		int k1 = j * i1 + i * j1 >> 16;
		int l1 = j * j1 - i * i1 >> 16;
		if(l > 2500) {
			sprite.drawSprite(((94 + k1) - sprite.maxWidth / 2) + 4 , 83 - l1 - sprite.maxHeight / 2 - 4);
		} else {
			sprite.drawSprite(((94 + k1) - sprite.maxWidth / 2) + 4, 83 - l1 - sprite.maxHeight / 2 - 4);
		}
	}

	private void spawnOrRemoveObject(int i, int j, int k, int l, int i1, int j1, int k1
	)
	{
		if(i1 >= 1 && i >= 1 && i1 <= 102 && i <= 102)
		{
			if(lowMem && j != plane)
				return;
			int i2 = 0;
			if(j1 == 0)
				i2 = worldController.getWallObjectUID(j, i1, i);
			if(j1 == 1)
				i2 = worldController.getWallDecorationUID(j, i1, i);
			if(j1 == 2)
				i2 = worldController.getInteractiveObjectUID(j, i1, i);
			if(j1 == 3)
				i2 = worldController.getGroundDecorationUID(j, i1, i);
			if(i2 != 0)
			{
				int i3 = worldController.getObjectConfig(j, i1, i, i2);
				int j2 = i2 >> 14 & 0x7fff;
				int k2 = i3 & 0x1f;
				int l2 = i3 >> 6;
				if(j1 == 0)
				{
					worldController.removeWallObject(i1, j, i, (byte)-119);
					ObjectDef class46 = ObjectDef.forID(j2);
					if(class46.blocksProjectile)
						aCollisionMapArray1230[j].removeWallFlags(l2, k2, class46.impenetrable, i1, i);
				}
				if(j1 == 1)
					worldController.removeWallDecoration(i, j, i1);
				if(j1 == 2)
				{
					worldController.removeInteractiveObjectAt(j, i1, i);
					ObjectDef class46_1 = ObjectDef.forID(j2);
					if(i1 + class46_1.sizeX > 103 || i + class46_1.sizeX > 103 || i1 + class46_1.sizeY > 103 || i + class46_1.sizeY > 103)
						return;
					if(class46_1.blocksProjectile)
						aCollisionMapArray1230[j].removeObjectFlags(l2, class46_1.sizeX, i1, i, class46_1.sizeY, class46_1.impenetrable);
				}
				if(j1 == 3)
				{
					worldController.removeGroundDecoration(j, i, i1);
					ObjectDef class46_2 = ObjectDef.forID(j2);
					if(class46_2.blocksProjectile && class46_2.hasActions)
						aCollisionMapArray1230[j].unmarkBlocked(i, i1);
				}
			}
			if(k1 >= 0)
			{
				int j3 = j;
				if(j3 < 3 && (byteGroundArray[1][i1][i] & 2) == 2)
					j3++;
				ObjectManager.placeObjectStatic(worldController, k, i, l, j3, aCollisionMapArray1230[j], intGroundArray, i1, k1, j);
			}
		}
	}

	private void updatePlayers(int i, Stream stream)
	{
		npcUpdateCount = 0;
		entityCount = 0;
		parseLocalPlayerMovement(stream);
		parsePlayerRemovals(stream);
		parseNewPlayers(stream, i);
		parsePlayerUpdateMasks(stream);
		for(int k = 0; k < npcUpdateCount; k++)
		{
			int l = entityUpdateIndices[k];
			if(playerArray[l].textColor != loopCycle)
				playerArray[l] = null;
		}

		if(stream.currentOffset != i)
		{
			signlink.reporterror("Error packet size mismatch in getplayer pos:" + stream.currentOffset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for(int i1 = 0; i1 < playerCount; i1++)
			if(playerArray[playerIndices[i1]] == null)
			{
				signlink.reporterror(myUsername + " null entry in pl list - pos:" + i1 + " size:" + playerCount);
				throw new RuntimeException("eek");
			}

	}

	private void setCameraPos(int j, int k, int l, int i1, int j1, int k1)
	{
		int l1 = 2048 - k & 0x7ff;
		int i2 = 2048 - j1 & 0x7ff;
		int j2 = 0;
		int k2 = 0;
		int l2 = j;
		if(l1 != 0)
		{
			int i3 = Model.SINE[l1];
			int k3 = Model.COSINE[l1];
			int i4 = k2 * k3 - l2 * i3 >> 16;
			l2 = k2 * i3 + l2 * k3 >> 16;
			k2 = i4;
		}
		if(i2 != 0)
		{
/* xxx			if(cameratoggle){
				if(zoom == 0)
				zoom = k2;
			  if(lftrit == 0)
				lftrit = j2;
			  if(fwdbwd == 0)
				fwdbwd = l2;
			  k2 = zoom;
			  j2 = lftrit;
			  l2 = fwdbwd;
			}
*/
			int j3 = Model.SINE[i2];
			int l3 = Model.COSINE[i2];
			int j4 = l2 * j3 + j2 * l3 >> 16;
			l2 = l2 * l3 - j2 * j3 >> 16;
			j2 = j4;
		}
		xCameraPos = l - j2;
		zCameraPos = i1 - k2;
		yCameraPos = k1 - l2;
		yCameraCurve = k;
		xCameraCurve = j1;
	}

	public void updateStrings(String str, int i) {
		switch(i) {
			case 1675: sendFrame126(str, 17508); break;//Stab
			case 1676: sendFrame126(str, 17509); break;//Slash
			case 1677: sendFrame126(str, 17510); break;//Cursh
			case 1678: sendFrame126(str, 17511); break;//Magic
			case 1679: sendFrame126(str, 17512); break;//Range
			case 1680: sendFrame126(str, 17513); break;//Stab
			case 1681: sendFrame126(str, 17514); break;//Slash
			case 1682: sendFrame126(str, 17515); break;//Crush
			case 1683: sendFrame126(str, 17516); break;//Magic
			case 1684: sendFrame126(str, 17517); break;//Range
			case 1686: sendFrame126(str, 17518); break;//Strength
			case 1687: sendFrame126(str, 17519); break;//Prayer
		}
	}

	public void sendFrame126(String str,int i) {
		RSInterface.interfaceCache[i].message = str;
		if(RSInterface.interfaceCache[i].parentID == tabInterfaceIDs[tabID])
			needDrawTabArea = true;
	}

	public void sendPacket185(int button,int toggle,int type) {
		switch(type) {
			case 135:
				RSInterface class9 = RSInterface.interfaceCache[button];
				boolean flag8 = true;
				if(class9.contentType > 0)
					flag8 = promptUserForInput(class9);
				if(flag8) {
					stream.createFrame(185);
					stream.writeWord(button);
				}
				break;
			case 646:
				stream.createFrame(185);
				stream.writeWord(button);
				RSInterface class9_2 = RSInterface.interfaceCache[button];
				if(class9_2.valueIndexArray != null && class9_2.valueIndexArray[0][0] == 5) {
					if(variousSettings[toggle] != class9_2.scriptDefaults[0]) {
						variousSettings[toggle] = class9_2.scriptDefaults[0];
						applyVarpSetting(toggle);
						needDrawTabArea = true;
					}
				}
				break;
			case 169:
				stream.createFrame(185);
				stream.writeWord(button);
				RSInterface class9_3 = RSInterface.interfaceCache[button];
				if(class9_3.valueIndexArray != null && class9_3.valueIndexArray[0][0] == 5) {
					variousSettings[toggle] = 1 - variousSettings[toggle];
					applyVarpSetting(toggle);
					needDrawTabArea = true;
				}
				switch(button) {
					case 19136:
						System.out.println("toggle = "+toggle);
						if(toggle == 0)
							sendFrame36(173,toggle);
						if(toggle == 1)
							sendPacket185(153,173,646);
						break;
				}
				break;
		}
	}

	public void sendFrame36(int id,int state) {
		tabFlashTimer[id] = state;
		if(variousSettings[id] != state) {
			variousSettings[id] = state;
			applyVarpSetting(id);
			needDrawTabArea = true;
			if(dialogID != -1)
				inputTaken = true;
		}
	}

	public void sendFrame219() {
		if(invOverlayInterfaceID != -1) {
			invOverlayInterfaceID = -1;
			needDrawTabArea = true;
			tabAreaAltered = true;
		}
		if(backDialogID != -1) {
			backDialogID = -1;
			inputTaken = true;
		}
		if(inputDialogState != 0) {
			inputDialogState = 0;
			inputTaken = true;
		}
		openInterfaceID = -1;
		aBoolean1149 = false;
	}

	public void sendFrame248(int interfaceID,int sideInterfaceID) 	{
		if(backDialogID != -1) {
			backDialogID = -1;
			inputTaken = true;
		}
		if(inputDialogState != 0) {
			inputDialogState = 0;
			inputTaken = true;
		}
		openInterfaceID = interfaceID;
		invOverlayInterfaceID = sideInterfaceID;
		needDrawTabArea = true;
		tabAreaAltered = true;
		aBoolean1149 = false;
	}

	private boolean parsePacket() {
		if(socketStream == null)
			return false;
		try {
			int i = socketStream.available();
			if(i == 0)
				return false;
			if(pktType == -1) {
				socketStream.flushInputStream(inStream.buffer, 1);
				pktType = inStream.buffer[0] & 0xff;
				if(encryption != null)
					pktType = pktType - encryption.getNextKey() & 0xff;
				pktSize = SizeConstants.packetSizes[pktType];
				i--;
			}
			if(pktSize == -1)
				if(i > 0) {
					socketStream.flushInputStream(inStream.buffer, 1);
					pktSize = inStream.buffer[0] & 0xff;
					i--;
				} else {
					return false;
				}
			if(pktSize == -2)
				if(i > 1) {
					socketStream.flushInputStream(inStream.buffer, 2);
					inStream.currentOffset = 0;
					pktSize = inStream.readUnsignedWord();
					i -= 2;
				} else {
					return false;
				}
			if(i < pktSize)
				return false;
			inStream.currentOffset = 0;
			socketStream.flushInputStream(inStream.buffer, pktSize);
			idleTime = 0;
			chatScrollMax = entityUpdateIndex;
			entityUpdateIndex = entityUpdateCount;
			entityUpdateCount = pktType;
			switch(pktType) {
				case 81:
					updatePlayers(pktSize, inStream);
					aBoolean1080 = false;
					pktType = -1;
					return true;
					
				case 176:
					daysSinceRecovChange = inStream.readUnsignedByteNeg();
					unreadMessages = inStream.readWordBigA();
					membersInt = inStream.readUnsignedByte();
					walkQueueLength = inStream.readDWordMixed2();
					daysSinceLastLogin = inStream.readUnsignedWord();
					if(walkQueueLength != 0 && openInterfaceID == -1) {
						signlink.dnslookup(TextClass.intToIpAddress(walkQueueLength));
						clearTopInterfaces();
						char c = '\u028A';
						if(daysSinceRecovChange != 201 || membersInt == 1)
							c = '\u028F';
						reportAbuseInput = "";
						canMute = false;
						for(int k9 = 0; k9 < RSInterface.interfaceCache.length; k9++) {
							if(RSInterface.interfaceCache[k9] == null || RSInterface.interfaceCache[k9].contentType != c)
								continue;
							openInterfaceID = RSInterface.interfaceCache[k9].parentID;
							
						}
					}
					pktType = -1;
					return true;
					
				case 64:
					hintIconDrawX = inStream.readUnsignedByteNeg();
					hintIconDrawY = inStream.readUnsignedByteSub();
					for(int j = hintIconDrawX; j < hintIconDrawX + 8; j++) {
						for(int l9 = hintIconDrawY; l9 < hintIconDrawY + 8; l9++)
							if(groundArray[plane][j][l9] != null) {
								groundArray[plane][j][l9] = null;
								spawnGroundItem(j, l9);
							}
					}
					for(SpawnObjectNode spawnObjectNode = (SpawnObjectNode)spawnObjectList.reverseGetFirst(); spawnObjectNode != null; spawnObjectNode = (SpawnObjectNode)spawnObjectList.reverseGetNext())
						if(spawnObjectNode.objectX >= hintIconDrawX && spawnObjectNode.objectX < hintIconDrawX + 8 && spawnObjectNode.objectY >= hintIconDrawY && spawnObjectNode.objectY < hintIconDrawY + 8 && spawnObjectNode.objectPlane == plane)
							spawnObjectNode.delay = 0;
					pktType = -1;
					return true;
					
				case 185:
					int k = inStream.readWordLEBigA();
					RSInterface.interfaceCache[k].enabledMediaType = 3;
					if(myPlayer.desc == null)
						RSInterface.interfaceCache[k].mediaID = (myPlayer.bodyColors[0] << 25) + (myPlayer.bodyColors[4] << 20) + (myPlayer.equipment[0] << 15) + (myPlayer.equipment[8] << 10) + (myPlayer.equipment[11] << 5) + myPlayer.equipment[1];
					else
						RSInterface.interfaceCache[k].mediaID = (int)(0x12345678L + myPlayer.desc.type);
					pktType = -1;
					return true;
					
				/* Clan chat packet */
				case 217:
					try {
						name = inStream.readString();
						message = inStream.readString();
						clanname = inStream.readString();
						rights = inStream.readUnsignedWord();
						//message = TextInput.processText(message);
						//message = Censor.doCensor(message);
						System.out.println(clanname);
						pushMessage(message, 16, name);
					} catch(Exception e) {
						e.printStackTrace();
					}
					pktType = -1;
					return true;
					
				case 107:
					aBoolean1160 = false;
					for(int l = 0; l < 5; l++)
						sidebarFlashing[l] = false;
					pktType = -1;
					return true;
					
				case 72:
					int i1 = inStream.readWordLE();
					RSInterface class9 = RSInterface.interfaceCache[i1];
					for(int k15 = 0; k15 < class9.inv.length; k15++) {
						class9.inv[k15] = -1;
						class9.inv[k15] = 0;
					}
					pktType = -1;
					return true;
					
				case 214:
					ignoreCount = pktSize / 8;
					for(int j1 = 0; j1 < ignoreCount; j1++)
						ignoreListAsLongs[j1] = inStream.readQWord();
					pktType = -1;
					return true;
					
				case 166:
					aBoolean1160 = true;
					cameraLocX = inStream.readUnsignedByte();
					cameraLocY = inStream.readUnsignedByte();
					cameraLocHeight = inStream.readUnsignedWord();
					cameraLocSpeed = inStream.readUnsignedByte();
					cameraLocAccel = inStream.readUnsignedByte();
					if(cameraLocAccel >= 100) {
						xCameraPos = cameraLocX * 128 + 64;
						yCameraPos = cameraLocY * 128 + 64;
						zCameraPos = getTileHeight(plane, yCameraPos, xCameraPos) - cameraLocHeight;
					}
					pktType = -1;
					return true;
					
				case 134:
					needDrawTabArea = true;
					int k1 = inStream.readUnsignedByte();
					int i10 = inStream.readDWordMixed1();
					int l15 = inStream.readUnsignedByte();
					currentExp[k1] = i10;
					currentStats[k1] = l15;
					maxStats[k1] = 1;
					for(int k20 = 0; k20 < 98; k20++)
						if(i10 >= XP_TABLE[k20])
							maxStats[k1] = k20 + 2;
					pktType = -1;
					return true;
					
				case 71:
					int l1 = inStream.readUnsignedWord();
					int j10 = inStream.readUnsignedByteAdd();
					if(l1 == 65535)
						l1 = -1;
					tabInterfaceIDs[j10] = l1;
					needDrawTabArea = true;
					tabAreaAltered = true;
					pktType = -1;
					return true;
					
				case 74:
					int i2 = inStream.readWordLE();
					if(i2 == 65535)
						i2 = -1;
					if(i2 != currentSong && musicEnabled && !lowMem && prevSong == 0) {
						nextSong = i2;
						songChanging = true;
						onDemandFetcher.requestFile(2, nextSong);
					}
					currentSong = i2;
					pktType = -1;
					return true;
					
				case 121:
					int j2 = inStream.readWordLEBigA();
					int k10 = inStream.readWordBigA();
					if(musicEnabled && !lowMem) {
						nextSong = j2;
						songChanging = false;
						onDemandFetcher.requestFile(2, nextSong);
						prevSong = k10;
					}
					pktType = -1;
					return true;
					
				case 109:
					resetLogout();
					pktType = -1;
					return false;
					
				case 70:
					int k2 = inStream.readSignedWord();
					int l10 = inStream.readSignedWordLE();
					int i16 = inStream.readWordLE();
					RSInterface class9_5 = RSInterface.interfaceCache[i16];
					class9_5.invSpritePadX = k2;
					class9_5.invSpritePadY = l10;
					pktType = -1;
					return true;
					
				case 73:
				case 241:
					int l2 = mapRegionX;
					int i11 = mapRegionY;
					if(pktType == 73) {
						l2 = inStream.readWordBigA();
						i11 = inStream.readUnsignedWord();
						aBoolean1159 = false;
					}
					if(pktType == 241) {
						i11 = inStream.readWordBigA();
						inStream.initBitAccess();
						for(int j16 = 0; j16 < 4; j16++) {
							for(int l20 = 0; l20 < 13; l20++) {
								for(int j23 = 0; j23 < 13; j23++) {
									int i26 = inStream.readBits(1);
									if(i26 == 1)
										tileFlags[j16][l20][j23] = inStream.readBits(26);
									else
										tileFlags[j16][l20][j23] = -1;
								}
							}
						}
						inStream.finishBitAccess();
						l2 = inStream.readUnsignedWord();
						aBoolean1159 = true;
					}
					if(mapRegionX == l2 && mapRegionY == i11 && loadingStage == 2) {
						pktType = -1;
						return true;
					}
					mapRegionX = l2;
					mapRegionY = i11;
					baseX = (mapRegionX - 6) * 8;
					baseY = (mapRegionY - 6) * 8;
					aBoolean1141 = (mapRegionX / 8 == 48 || mapRegionX / 8 == 49) && mapRegionY / 8 == 48;
					if(mapRegionX / 8 == 48 && mapRegionY / 8 == 148)
						aBoolean1141 = true;
					loadingStage = 1;
					serverSeed = System.currentTimeMillis();
					loginMsgIP.initDrawingArea();
					boldFont.drawText(0, "Loading - please wait.", 151, 257);
					boldFont.drawText(0xffffff, "Loading - please wait.", 150, 256);
					loginMsgIP.drawGraphics(clientSize == 0 ? 4 : 0, super.graphics, clientSize == 0 ? 4 : 0);
					if(pktType == 73) {
						int k16 = 0;
						for(int i21 = (mapRegionX - 6) / 8; i21 <= (mapRegionX + 6) / 8; i21++) {
							for(int k23 = (mapRegionY - 6) / 8; k23 <= (mapRegionY + 6) / 8; k23++)
								k16++;
						}
						mapLandscapeData = new byte[k16][];
						mapObjectData = new byte[k16][];
						chatFilterTypes = new int[k16];
						chatFilterNames = new int[k16];
						chatFilterMessages = new int[k16];
						k16 = 0;
						for(int l23 = (mapRegionX - 6) / 8; l23 <= (mapRegionX + 6) / 8; l23++) {
							for(int j26 = (mapRegionY - 6) / 8; j26 <= (mapRegionY + 6) / 8; j26++) {
								chatFilterTypes[k16] = (l23 << 8) + j26;
								if(aBoolean1141 && (j26 == 49 || j26 == 149 || j26 == 147 || l23 == 50 || l23 == 49 && j26 == 47)) {
									chatFilterNames[k16] = -1;
									chatFilterMessages[k16] = -1;
									k16++;
								} else {
									int k28 = chatFilterNames[k16] = onDemandFetcher.getMapFile(0, j26, l23);
									if(k28 != -1)
										onDemandFetcher.requestFile(3, k28);
									int j30 = chatFilterMessages[k16] = onDemandFetcher.getMapFile(1, j26, l23);
									if(j30 != -1)
										onDemandFetcher.requestFile(3, j30);
									k16++;
								}
							}
						}
					}
					if(pktType == 241) {
						int l16 = 0;
						int ai[] = new int[676];
						for(int i24 = 0; i24 < 4; i24++) {
							for(int k26 = 0; k26 < 13; k26++) {
								for(int l28 = 0; l28 < 13; l28++) {
									int k30 = tileFlags[i24][k26][l28];
									if(k30 != -1) {
										int k31 = k30 >> 14 & 0x3ff;
										int i32 = k30 >> 3 & 0x7ff;
										int k32 = (k31 / 8 << 8) + i32 / 8;
										for(int j33 = 0; j33 < l16; j33++) {
											if(ai[j33] != k32)
												continue;
											k32 = -1;
											
										}
										if(k32 != -1)
											ai[l16++] = k32;
									}
								}
							}
						}
						mapLandscapeData = new byte[l16][];
						mapObjectData = new byte[l16][];
						chatFilterTypes = new int[l16];
						chatFilterNames = new int[l16];
						chatFilterMessages = new int[l16];
						for(int l26 = 0; l26 < l16; l26++) {
							int i29 = chatFilterTypes[l26] = ai[l26];
							int l30 = i29 >> 8 & 0xff;
							int l31 = i29 & 0xff;
							int j32 = chatFilterNames[l26] = onDemandFetcher.getMapFile(0, l31, l30);
							if(j32 != -1)
								onDemandFetcher.requestFile(3, j32);
							int i33 = chatFilterMessages[l26] = onDemandFetcher.getMapFile(1, l31, l30);
							if(i33 != -1)
								onDemandFetcher.requestFile(3, i33);
						}
					}
					int i17 = baseX - regionAbsBaseX;
					int j21 = baseY - regionAbsBaseY;
					regionAbsBaseX = baseX;
					regionAbsBaseY = baseY;
					for(int j24 = 0; j24 < 16384; j24++) {
						NPC npc = npcArray[j24];
						if(npc != null) {
							for(int j29 = 0; j29 < 10; j29++) {
								npc.smallX[j29] -= i17;
								npc.smallY[j29] -= j21;
							}
							npc.x -= i17 * 128;
							npc.y -= j21 * 128;
						}
					}
					for(int i27 = 0; i27 < maxPlayers; i27++) {
						Player player = playerArray[i27];
						if(player != null) {
							for(int i31 = 0; i31 < 10; i31++) {
								player.smallX[i31] -= i17;
								player.smallY[i31] -= j21;
							}
							player.x -= i17 * 128;
							player.y -= j21 * 128;
						}
					}
					aBoolean1080 = true;
					byte byte1 = 0;
					byte byte2 = 104;
					byte byte3 = 1;
					if(i17 < 0) {
						byte1 = 103;
						byte2 = -1;
						byte3 = -1;
					}
					byte byte4 = 0;
					byte byte5 = 104;
					byte byte6 = 1;
					if(j21 < 0) {
						byte4 = 103;
						byte5 = -1;
						byte6 = -1;
					}
					for(int k33 = byte1; k33 != byte2; k33 += byte3) {
						for(int l33 = byte4; l33 != byte5; l33 += byte6) {
							int i34 = k33 + i17;
							int j34 = l33 + j21;
							for(int k34 = 0; k34 < 4; k34++)
								if(i34 >= 0 && j34 >= 0 && i34 < 104 && j34 < 104)
									groundArray[k34][k33][l33] = groundArray[k34][i34][j34];
								else
									groundArray[k34][k33][l33] = null;
						}
					}
					for(SpawnObjectNode spawnObjectNode_1 = (SpawnObjectNode)spawnObjectList.reverseGetFirst(); spawnObjectNode_1 != null; spawnObjectNode_1 = (SpawnObjectNode)spawnObjectList.reverseGetNext()) {
						spawnObjectNode_1.objectX -= i17;
						spawnObjectNode_1.objectY -= j21;
						if(spawnObjectNode_1.objectX < 0 || spawnObjectNode_1.objectY < 0 || spawnObjectNode_1.objectX >= 104 || spawnObjectNode_1.objectY >= 104)
							spawnObjectNode_1.unlink();
					}
					if(destX != 0) {
						destX -= i17;
						destY -= j21;
					}
					aBoolean1160 = false;
					pktType = -1;
					return true;
					
				case 208:
					int i3 = inStream.readSignedWordLE();
					if(i3 >= 0)
						resetInterfaceAnim(i3);
					anInt1018 = i3;
					pktType = -1;
					return true;
					
				case 99:
					chatAreaScrollPos = inStream.readUnsignedByte();
					pktType = -1;
					return true;
					
				case 75:
					int j3 = inStream.readWordLEBigA();
					int j11 = inStream.readWordLEBigA();
					RSInterface.interfaceCache[j11].enabledMediaType = 2;
					RSInterface.interfaceCache[j11].mediaID = j3;
					pktType = -1;
					return true;
					
				case 114:
					anInt1104 = inStream.readWordLE() * 30;
					pktType = -1;
					return true;
					
				case 60:
					hintIconDrawY = inStream.readUnsignedByte();
					hintIconDrawX = inStream.readUnsignedByteNeg();
					while(inStream.currentOffset < pktSize) {
						int k3 = inStream.readUnsignedByte();
						parseGroupPacket(inStream, k3);
					}
					pktType = -1;
					return true;
					
				case 35:
					int l3 = inStream.readUnsignedByte();
					int k11 = inStream.readUnsignedByte();
					int j17 = inStream.readUnsignedByte();
					int k21 = inStream.readUnsignedByte();
					sidebarFlashing[l3] = true;
					tabAreaFlashCycle[l3] = k11;
					tabAreaX[l3] = j17;
					walkingQueueX[l3] = k21;
					chatRights[l3] = 0;
					pktType = -1;
					return true;
					
case 174:

/*Empty Following Packet*/

                followPlayer = 0;

                followNPC = 0;

                int l11z = inStream.readUnsignedWord();

                int iq = inStream.readUnsignedByte();

                followDistance = inStream.readUnsignedWord();

                if (iq == 0)

                {

                    followNPC = l11z;

                }

                else if (iq == 1)

                {

                    followPlayer = l11z;

                }

                pktType = -1;

                return true;
					
				case 104:
					int j4 = inStream.readUnsignedByteNeg();
					int i12 = inStream.readUnsignedByteAdd();
					String s6 = inStream.readString();
					if(j4 >= 1 && j4 <= 5) {
						if(s6.equalsIgnoreCase("null"))
							s6 = null;
						atPlayerActions[j4 - 1] = s6;
						atPlayerArray[j4 - 1] = i12 == 0;
					}
					pktType = -1;
					return true;
					
				case 78:
					destX = 0;
					pktType = -1;
					return true;
					
				case 253:
					String s = inStream.readString();
					if(s.endsWith(":tradereq:")) {
						String s3 = s.substring(0, s.indexOf(":"));
						long l17 = TextClass.longForName(s3);
						boolean flag2 = false;
						for(int j27 = 0; j27 < ignoreCount; j27++) {
							if(ignoreListAsLongs[j27] != l17)
								continue;
							flag2 = true;
							
						}
						if(!flag2 && anInt1251 == 0)
							pushMessage("wishes to trade with you.", 4, s3);
					} else if (s.endsWith(":clan:")) {
						String s4 = s.substring(0, s.indexOf(":"));
						long l18 = TextClass.longForName(s4);
						pushMessage("Clan: ", 8, s4);	
					} else if(s.endsWith("#url#")) {
						String link = s.substring(0, s.indexOf("#"));
						pushMessage("Join us at: ", 9, link);
					} else if(s.endsWith(":duelreq:")) {
						String s4 = s.substring(0, s.indexOf(":"));
						long l18 = TextClass.longForName(s4);
						boolean flag3 = false;
						for(int k27 = 0; k27 < ignoreCount; k27++) {
							if(ignoreListAsLongs[k27] != l18)
								continue;
							flag3 = true;
							
						}
						if(!flag3 && anInt1251 == 0)
							pushMessage("wishes to duel with you.", 8, s4);
					} else if(s.endsWith(":chalreq:")) {
						String s5 = s.substring(0, s.indexOf(":"));
						long l19 = TextClass.longForName(s5);
						boolean flag4 = false;
						for(int l27 = 0; l27 < ignoreCount; l27++) {
							if(ignoreListAsLongs[l27] != l19)
								continue;
							flag4 = true;
							
						}
						if(!flag4 && anInt1251 == 0) {
							String s8 = s.substring(s.indexOf(":") + 1, s.length() - 9);
							pushMessage(s8, 8, s5);
						}
					} else {
						pushMessage(s, 0, "");
					}
					pktType = -1;
					return true;
					
				case 1:
					for(int k4 = 0; k4 < playerArray.length; k4++)
						if(playerArray[k4] != null)
							playerArray[k4].anim = -1;
					for(int j12 = 0; j12 < npcArray.length; j12++)
						if(npcArray[j12] != null)
							npcArray[j12].anim = -1;
					pktType = -1;
					return true;
					
				case 50:
					long l4 = inStream.readQWord();
					int i18 = inStream.readUnsignedByte();
					String s7 = TextClass.fixName(TextClass.nameForLong(l4));
					for(int k24 = 0; k24 < friendsCount; k24++) {
						if(l4 != friendsListAsLongs[k24])
							continue;
						if(friendsNodeIDs[k24] != i18) {
							friendsNodeIDs[k24] = i18;
							needDrawTabArea = true;
							if(i18 >= 2) {
								pushMessage(s7 + " has logged in.", 5, "");
							}
							if(i18 <= 1) {
								pushMessage(s7 + " has logged out.", 5, "");
							}
						}
						s7 = null;
						
					}
					if(s7 != null && friendsCount < 200) {
						friendsListAsLongs[friendsCount] = l4;
						friendsList[friendsCount] = s7;
						friendsNodeIDs[friendsCount] = i18;
						friendsCount++;
						needDrawTabArea = true;
					}
					for(boolean flag6 = false; !flag6;) {
						flag6 = true;
						for(int k29 = 0; k29 < friendsCount - 1; k29++)
							if(friendsNodeIDs[k29] != nodeID && friendsNodeIDs[k29 + 1] == nodeID || friendsNodeIDs[k29] == 0 && friendsNodeIDs[k29 + 1] != 0) {
								int j31 = friendsNodeIDs[k29];
								friendsNodeIDs[k29] = friendsNodeIDs[k29 + 1];
								friendsNodeIDs[k29 + 1] = j31;
								String s10 = friendsList[k29];
								friendsList[k29] = friendsList[k29 + 1];
								friendsList[k29 + 1] = s10;
								long l32 = friendsListAsLongs[k29];
								friendsListAsLongs[k29] = friendsListAsLongs[k29 + 1];
								friendsListAsLongs[k29 + 1] = l32;
								needDrawTabArea = true;
								flag6 = false;
							}
					}
					pktType = -1;
					return true;
					
				case 110:
					if(tabID == 12)
						needDrawTabArea = true;
					energy = inStream.readUnsignedByte();
					pktType = -1;
					return true;
					
				case 254:
					minimapRotation = inStream.readUnsignedByte();
					if(minimapRotation == 1)
						hintIconNpcIndex = inStream.readUnsignedWord();
					if(minimapRotation >= 2 && minimapRotation <= 6) {
						if(minimapRotation == 2) {
							cameraTargetLocalX = 64;
							cameraTargetLocalY = 64;
						}
						if(minimapRotation == 3) {
							cameraTargetLocalX = 0;
							cameraTargetLocalY = 64;
						}
						if(minimapRotation == 4) {
							cameraTargetLocalX = 128;
							cameraTargetLocalY = 64;
						}
						if(minimapRotation == 5) {
							cameraTargetLocalX = 64;
							cameraTargetLocalY = 0;
						}
						if(minimapRotation == 6) {
							cameraTargetLocalX = 64;
							cameraTargetLocalY = 128;
						}
						minimapRotation = 2;
						cameraTargetTileX = inStream.readUnsignedWord();
						cameraTargetTileY = inStream.readUnsignedWord();
						cameraTargetHeight = inStream.readUnsignedByte();
					}
					if(minimapRotation == 10)
						cameraTargetIndex = inStream.readUnsignedWord();
					pktType = -1;
					return true;
					
				case 248:
					int i5 = inStream.readWordBigA();
					int k12 = inStream.readUnsignedWord();
					if(backDialogID != -1) {
						backDialogID = -1;
						inputTaken = true;
					}
					if(inputDialogState != 0) {
						inputDialogState = 0;
						inputTaken = true;
					}
					openInterfaceID = i5;
					invOverlayInterfaceID = k12;
					needDrawTabArea = true;
					tabAreaAltered = true;
					aBoolean1149 = false;
					pktType = -1;
					return true;
					
				case 79:
					int j5 = inStream.readWordLE();
					int l12 = inStream.readWordBigA();
					RSInterface class9_3 = RSInterface.interfaceCache[j5];
					if(class9_3 != null && class9_3.type == 0) {
						if(l12 < 0)
							l12 = 0;
						if(l12 > class9_3.scrollMax - class9_3.height)
							l12 = class9_3.scrollMax - class9_3.height;
						class9_3.scrollPosition = l12;
					}
					pktType = -1;
					return true;
					
				case 68:
					for(int k5 = 0; k5 < variousSettings.length; k5++)
						if(variousSettings[k5] != tabFlashTimer[k5]) {
							variousSettings[k5] = tabFlashTimer[k5];
							applyVarpSetting(k5);
							needDrawTabArea = true;
						}
					pktType = -1;
					return true;
					
				case 196:
					long l5 = inStream.readQWord();
					int j18 = inStream.readDWord();
					int l21 = inStream.readUnsignedByte();
					boolean flag5 = false;
					for(int i28 = 0; i28 < 100; i28++) {
						if(menuActionCmd4[i28] != j18)
							continue;
						flag5 = true;
						
					}
					if(l21 <= 1) {
						for(int l29 = 0; l29 < ignoreCount; l29++) {
							if(ignoreListAsLongs[l29] != l5)
								continue;
							flag5 = true;
							
						}
					}
					if(!flag5 && anInt1251 == 0)
						try {
							menuActionCmd4[walkPathLength] = j18;
							walkPathLength = (walkPathLength + 1) % 100;
							String s9 = TextInput.decodeText(pktSize - 13, inStream);
							//if(l21 != 3)
								//s9 = Censor.doCensor(s9);
							if(l21 == 2 || l21 == 3)
								pushMessage(s9, 7, "@cr2@" + TextClass.fixName(TextClass.nameForLong(l5)));
							else
							if(l21 == 1)
								pushMessage(s9, 7, "@cr1@" + TextClass.fixName(TextClass.nameForLong(l5)));
							else
								pushMessage(s9, 3, TextClass.fixName(TextClass.nameForLong(l5)));
						} catch(Exception exception1) {
							signlink.reporterror("cde1");
						}
					pktType = -1;
					return true;
					
				case 85:
					hintIconDrawY = inStream.readUnsignedByteNeg();
					hintIconDrawX = inStream.readUnsignedByteNeg();
					pktType = -1;
					return true;
					
				case 24:
					flashingTab = inStream.readUnsignedByteSub();
					if(flashingTab == tabID) {
						if(flashingTab == 3)
							tabID = 1;
						else
							tabID = 3;
						needDrawTabArea = true;
					}
					pktType = -1;
					return true;
					
				case 246:
					int i6 = inStream.readWordLE();
					int i13 = inStream.readUnsignedWord();
					int k18 = inStream.readUnsignedWord();
					if(k18 == 65535) {
						RSInterface.interfaceCache[i6].enabledMediaType = 0;
						pktType = -1;
						return true;
					} else {
						ItemDef itemDef = ItemDef.forID(k18);
						RSInterface.interfaceCache[i6].enabledMediaType = 4;
						RSInterface.interfaceCache[i6].mediaID = k18;
						RSInterface.interfaceCache[i6].modelRotation1 = itemDef.modelRotation1;
						RSInterface.interfaceCache[i6].modelRotation2 = itemDef.modelRotation2;
						RSInterface.interfaceCache[i6].modelZoom = (itemDef.modelZoom * 100) / i13;
						pktType = -1;
						return true;
					}
					
				case 171:
					boolean flag1 = inStream.readUnsignedByte() == 1;
					int j13 = inStream.readUnsignedWord();
					RSInterface.interfaceCache[j13].isMouseoverTriggered = flag1;
					pktType = -1;
					return true;
					
				case 142:
					int j6 = inStream.readWordLE();
					resetInterfaceAnim(j6);
					if(backDialogID != -1) {
						backDialogID = -1;
						inputTaken = true;
					}
					if(inputDialogState != 0) {
						inputDialogState = 0;
						inputTaken = true;
					}
					invOverlayInterfaceID = j6;
					needDrawTabArea = true;
					tabAreaAltered = true;
					openInterfaceID = -1;
					aBoolean1149 = false;
					pktType = -1;
					return true;
					
				case 126:
					String text = inStream.readString();
					int frame = inStream.readWordBigA();
					if (text.startsWith("www.")) {
						launchURL(text);
						pktType = -1;
						return true;
					}
					updateStrings(text, frame);
					sendFrame126(text, frame);
					if (frame >= 18144 && frame <= 18244) {
						clanList[frame - 18144] = text;
					}
					pktType = -1;
					return true;
					
				case 206:
					publicChatMode = inStream.readUnsignedByte();
					privateChatMode = inStream.readUnsignedByte();
					tradeMode = inStream.readUnsignedByte();
					aBoolean1233 = true;
					inputTaken = true;
					pktType = -1;
					return true;
					
				case 240:
					if(tabID == 12)
						needDrawTabArea = true;
					weight = inStream.readSignedWord();
					pktType = -1;
					return true;
					
				case 8:
					int k6 = inStream.readWordLEBigA();
					int l13 = inStream.readUnsignedWord();
					RSInterface.interfaceCache[k6].enabledMediaType = 1;
					RSInterface.interfaceCache[k6].mediaID = l13;
					pktType = -1;
					return true;
					
				case 122:
					int l6 = inStream.readWordLEBigA();
					int i14 = inStream.readWordLEBigA();
					int i19 = i14 >> 10 & 0x1f;
					int i22 = i14 >> 5 & 0x1f;
					int l24 = i14 & 0x1f;
					RSInterface.interfaceCache[l6].textColor = (i19 << 19) + (i22 << 11) + (l24 << 3);
					pktType = -1;
					return true;
					
				case 53:
					needDrawTabArea = true;
					int i7 = inStream.readUnsignedWord();
					RSInterface class9_1 = RSInterface.interfaceCache[i7];
					int j19 = inStream.readUnsignedWord();
					for(int j22 = 0; j22 < j19; j22++) {
						int i25 = inStream.readUnsignedByte();
						if(i25 == 255)
							i25 = inStream.readDWordMixed2();
						class9_1.inv[j22] = inStream.readWordLEBigA();
						class9_1.invStackSizes[j22] = i25;
					}
					for(int j25 = j19; j25 < class9_1.inv.length; j25++) {
						class9_1.inv[j25] = 0;
						class9_1.invStackSizes[j25] = 0;
					}
					pktType = -1;
					return true;
					
				case 230:
					int j7 = inStream.readWordBigA();
					int j14 = inStream.readUnsignedWord();
					int k19 = inStream.readUnsignedWord();
					int k22 = inStream.readWordLEBigA();
					RSInterface.interfaceCache[j14].modelRotation1 = k19;
					RSInterface.interfaceCache[j14].modelRotation2 = k22;
					RSInterface.interfaceCache[j14].modelZoom = j7;
					pktType = -1;
					return true;
					
				case 221:
					mapRegionCount = inStream.readUnsignedByte();
					needDrawTabArea = true;
					pktType = -1;
					return true;
					
				case 177:
					aBoolean1160 = true;
					cameraPosX = inStream.readUnsignedByte();
					cameraPosY = inStream.readUnsignedByte();
					cameraPosHeight = inStream.readUnsignedWord();
					cameraSpeed = inStream.readUnsignedByte();
					cameraAcceleration = inStream.readUnsignedByte();
					if(cameraAcceleration >= 100) {
						int k7 = cameraPosX * 128 + 64;
						int k14 = cameraPosY * 128 + 64;
						int i20 = getTileHeight(plane, k14, k7) - cameraPosHeight;
						int l22 = k7 - xCameraPos;
						int k25 = i20 - zCameraPos;
						int j28 = k14 - yCameraPos;
						int i30 = (int)Math.sqrt(l22 * l22 + j28 * j28);
						yCameraCurve = (int)(Math.atan2(k25, i30) * 325.94900000000001D) & 0x7ff;
						xCameraCurve = (int)(Math.atan2(l22, j28) * -325.94900000000001D) & 0x7ff;
						if(yCameraCurve < 128)
							yCameraCurve = 128;
						if(yCameraCurve > 383)
							yCameraCurve = 383;
					}
					pktType = -1;
					return true;
					
				case 249:
					anInt1046 = inStream.readUnsignedByteAdd();
					unknownInt10 = inStream.readWordLEBigA();
					pktType = -1;
					return true;
					
				case 65:
					updateNPCs(inStream, pktSize);
					pktType = -1;
					return true;
					
				case 27:
					messagePromptRaised = false;
					inputDialogState = 1;
					amountOrNameInput = "";
					inputTaken = true;
					pktType = -1;
					return true;
					
				case 187:
					messagePromptRaised = false;
					inputDialogState = 2;
					amountOrNameInput = "";
					inputTaken = true;
					pktType = -1;
					return true;
					
				case 97:
					int l7 = inStream.readUnsignedWord();
					resetInterfaceAnim(l7);
					if(invOverlayInterfaceID != -1) {
						invOverlayInterfaceID = -1;
						needDrawTabArea = true;
						tabAreaAltered = true;
					}
					if(backDialogID != -1) {
						backDialogID = -1;
						inputTaken = true;
					}
					if(inputDialogState != 0) {
						inputDialogState = 0;
						inputTaken = true;
					}
					openInterfaceID = l7;
					aBoolean1149 = false;
					pktType = -1;
					return true;
					
				case 218:
					int i8 = inStream.readSignedWordLEA();
					dialogID = i8;
					inputTaken = true;
					pktType = -1;
					return true;
					
				case 87:
					int j8 = inStream.readWordLE();
					int l14 = inStream.readDWordMixed1();
					tabFlashTimer[j8] = l14;
					if(variousSettings[j8] != l14) {
						variousSettings[j8] = l14;
						applyVarpSetting(j8);
						needDrawTabArea = true;
						if(dialogID != -1)
							inputTaken = true;
					}
					pktType = -1;
					return true;
					
				case 36:
					int k8 = inStream.readWordLE();
					byte byte0 = inStream.readSignedByte();
					tabFlashTimer[k8] = byte0;
					if(variousSettings[k8] != byte0) {
						variousSettings[k8] = byte0;
						applyVarpSetting(k8);
						needDrawTabArea = true;
						if(dialogID != -1)
							inputTaken = true;
					}
					pktType = -1;
					return true;
					
				case 61:
					flashingSideicon = inStream.readUnsignedByte();
					pktType = -1;
					return true;
					
				case 200:
					int l8 = inStream.readUnsignedWord();
					int i15 = inStream.readSignedWord();
					RSInterface class9_4 = RSInterface.interfaceCache[l8];
					class9_4.enabledAnimation = i15;
					if(i15 == -1) {
						class9_4.enabledSpriteId = 0;
						class9_4.animationId = 0;
					}
					pktType = -1;
					return true;
					
				case 219:
					if(invOverlayInterfaceID != -1) {
						invOverlayInterfaceID = -1;
						needDrawTabArea = true;
						tabAreaAltered = true;
					}
					if(backDialogID != -1) {
						backDialogID = -1;
						inputTaken = true;
					}
					if(inputDialogState != 0) {
						inputDialogState = 0;
						inputTaken = true;
					}
					openInterfaceID = -1;
					aBoolean1149 = false;
					pktType = -1;
					return true;
					
				case 34:
					needDrawTabArea = true;
					int i9 = inStream.readUnsignedWord();
					RSInterface class9_2 = RSInterface.interfaceCache[i9];
					while(inStream.currentOffset < pktSize) {
						int j20 = inStream.readSmart();
						int i23 = inStream.readUnsignedWord();
						int l25 = inStream.readUnsignedByte();
						if(l25 == 255)
							l25 = inStream.readDWord();
						if(j20 >= 0 && j20 < class9_2.inv.length) {
							class9_2.inv[j20] = i23;
							class9_2.invStackSizes[j20] = l25;
						}
					}
					pktType = -1;
					return true;
					
				case 4:
				case 44:
				case 84:
				case 101:
				case 105:
				case 117:
				case 147:
				case 151:
				case 156:
				case 160:
				case 215:
					parseGroupPacket(inStream, pktType);
					pktType = -1;
					return true;
					
				case 106:
					tabID = inStream.readUnsignedByteNeg();
					needDrawTabArea = true;
					tabAreaAltered = true;
					pktType = -1;
					return true;
					
				case 164:
					int j9 = inStream.readWordLE();
					resetInterfaceAnim(j9);
					if(invOverlayInterfaceID != -1) {
						invOverlayInterfaceID = -1;
						needDrawTabArea = true;
						tabAreaAltered = true;
					}
					backDialogID = j9;
					inputTaken = true;
					openInterfaceID = -1;
					aBoolean1149 = false;
					pktType = -1;
					return true;
					
			}
			signlink.reporterror("T1 - " + pktType + "," + pktSize + " - " + entityUpdateIndex + "," + chatScrollMax);
			//resetLogout();
		} catch(IOException _ex) {
			dropClient();
		} catch(Exception exception) {
			String s2 = "T2 - " + pktType + "," + entityUpdateIndex + "," + chatScrollMax + " - " + pktSize + "," + (baseX + myPlayer.smallX[0]) + "," + (baseY + myPlayer.smallY[0]) + " - ";
			for(int j15 = 0; j15 < pktSize && j15 < 50; j15++)
				s2 = s2 + inStream.buffer[j15] + ",";
			signlink.reporterror(s2);
			//resetLogout();
		}
		pktType = -1;
		return true;
	}

	private void processSceneEntities() {
		processMiddleMouseDrag();
		hintIconY++;
		renderPlayersOnScene(true);
		renderNPCsOnScene(true);
		renderPlayersOnScene(false);
		renderNPCsOnScene(false);
		processProjectiles();
		processStationaryGfx();
		if(!aBoolean1160) {
			int i = selectedArea;
			if(moveItemSlotStart / 256 > i)
				i = moveItemSlotStart / 256;
			if(sidebarFlashing[4] && tabAreaX[4] + 128 > i)
				i = tabAreaX[4] + 128;
			int k = minimapInt1 + lastItemSelectedInterface & 0x7ff;
			setCameraPos(600 + i * 3 + cameraZoom, i, cameraSmoothedX, getTileHeight(plane, myPlayer.y, myPlayer.x) - 50, k, cameraSmoothedY);
		}
		int j;
		if(!aBoolean1160)
			j = getCameraPlane();
		else
			j = getCameraRenderPlane();
		int l = xCameraPos;
		int i1 = zCameraPos;
		int j1 = yCameraPos;
		int k1 = yCameraCurve;
		int l1 = xCameraCurve;
		for(int i2 = 0; i2 < 5; i2++)
			if(sidebarFlashing[i2]) {
				int j2 = (int)((Math.random() * (double)(tabAreaFlashCycle[i2] * 2 + 1) - (double)tabAreaFlashCycle[i2]) + Math.sin((double)chatRights[i2] * ((double)walkingQueueX[i2] / 100D)) * (double)tabAreaX[i2]);
				if(i2 == 0)
					xCameraPos += j2;
				if(i2 == 1)
					zCameraPos += j2;
				if(i2 == 2)
					yCameraPos += j2;
				if(i2 == 3)
					xCameraCurve = xCameraCurve + j2 & 0x7ff;
				if(i2 == 4) {
					yCameraCurve += j2;
					if(yCameraCurve < 128)
						yCameraCurve = 128;
					if(yCameraCurve > 383)
						yCameraCurve = 383;
				}
			}
		int k2 = Texture.textureCycleCounter;
		Model.mousePickingEnabled = true;
		Model.mousePickCount = 0;
		Model.mousePickX = super.mouseX - (clientSize == 0 ? 4 : 0);
		Model.mousePickY = super.mouseY - (clientSize == 0 ? 4 : 0);
		DrawingArea.setAllPixelsToZero();
		worldController.renderScene(xCameraPos, yCameraPos, xCameraCurve, zCameraPos, j, yCameraCurve);
		worldController.clearObj5Cache();
		updateEntities();
		drawHeadIcon();
		animateTexture(k2);
		draw3dScreen();
		drawUnfixedGame();
		loginMsgIP.drawGraphics(clientSize == 0 ? 4 : 0, super.graphics, clientSize == 0 ? 4 : 0);
		xCameraPos = l;
		zCameraPos = i1;
		yCameraPos = j1;
		yCameraCurve = k1;
		xCameraCurve = l1;
	}
	public void drawRunOrb() {
		if (!runClicked) {
			if (super.mouseX > 710 && super.mouseX < 742 && super.mouseY > 88 && super.mouseY < 122) {
				hoverorbrun.drawSprite(165, 85);
			} else {
				runorb.drawSprite(165, 85);

			}
		} else {
			if (super.mouseX > 710 && super.mouseX < 742 && super.mouseY > 88 && super.mouseY < 122) {
				hoverorbrun2.drawSprite(165, 85);
			} else {
				runClick.drawSprite(165, 85);
			}
		}
	}
 	public void drawHPOrb() {
		int health;
		String cHP = RSInterface.interfaceCache[4016].message;
			int currentHP = Integer.parseInt(cHP);
		String mHP = RSInterface.interfaceCache[4017].message;
			int maxHP2 = Integer.parseInt(mHP);
		health = (int)(((double)currentHP / (double)maxHP2) * 100D);
		// Draws empty orb 
		emptyOrb.drawSprite(160, 13);
		hitPointsFill.drawSprite(163, 16);
		//Draws current HP text 
		if(health > 100) {
			smallText.drawRightAligned(65280, 176, cHP, 34, true);
		}
		if(health <= 100 && health >= 75) {
			smallText.drawRightAligned(65280, 176, cHP, 34, true);
		}
		else if(health <= 74 && health >= 50) {
			smallText.drawRightAligned(0xffff00, 176, cHP, 34, true);
		}
		else if(health <= 49 && health >= 25) {
			smallText.drawRightAligned(0xfca607, 176, cHP, 34, true);
		}
		else if(health <= 24 && health >= 0) {
			smallText.drawRightAligned(0xf50d0d, 176, cHP, 34, true);
		}
		// Draws inside orb sprites 
	}
 	public void loadOrbs(){
			//drawHPOrb();
			//drawPrayerOrb();
			//drawRunOrb();
	}
 	public void drawPrayerOrb() {
		int prayer;

		String cP = RSInterface.interfaceCache[4012].message;
			int currentPrayer = Integer.parseInt(cP);
		String mP = RSInterface.interfaceCache[4013].message;
			int maxPrayer = Integer.parseInt(mP);
		prayer = (int)(((double)currentPrayer / (double)maxPrayer) * 100D);
		/* Draws empty orb */
		emptyOrb.drawSprite(171, 49);
		prayerFill.drawSprite(174, 52);
		/* Draws current HP text */
		if(prayer <= 100 && prayer >= 75) {
			smallText.drawRightAligned(65280, 187, cP, 71, true);
		}
		else if(prayer <= 74 && prayer >= 50) {
			smallText.drawRightAligned(0xffff00, 187, cP, 71, true);
		}
		else if(prayer <= 49 && prayer >= 25) {
			smallText.drawRightAligned(0xfca607, 187, cP, 71, true);
		}
		else if(prayer <= 24 && prayer >= 0) {
			smallText.drawRightAligned(0xf50d0d, 187, cP, 71, true);
		}
		/* Draws inside orb sprites */
	}


	public void clearTopInterfaces() {
		stream.createFrame(130);
		if (invOverlayInterfaceID != -1) {
			invOverlayInterfaceID = -1;
			needDrawTabArea = true;
			aBoolean1149 = false;
			tabAreaAltered = true;
		}
		if (backDialogID != -1) {
			backDialogID = -1;
			inputTaken = true;
			aBoolean1149 = false;
		}
		openInterfaceID = -1;
		fullscreenInterfaceID = -1;
	}

	public client() {
		fullscreenInterfaceID = -1;
		chatRights = new int[500];
		chatTypeView = 0;
		clanChatMode = 0;
		cButtonHPos = -1;
		cButtonHCPos = -1;
		cButtonCPos = 0;
		if (server == null || server.isEmpty()) server = "127.0.0.1";
		constructMapTiles = new int[104][104];
		friendsNodeIDs = new int[200];
		groundArray = new NodeList[4][104][104];
		midiFading = false;
		loginStream = new Stream(new byte[5000]);
		npcArray = new NPC[16384];
		npcIndices = new int[16384];
		entityUpdateIndices = new int[1000];
		outStream = Stream.create();
		pendingInput = true;
		openInterfaceID = -1;
		currentExp = new int[Skills.skillsCount];
		continuedDialogue = false;
		tabAreaFlashCycle = new int[5];
		tabFlashIndex = -1;
		sidebarFlashing = new boolean[5];
		drawFlames = false;
		reportAbuseInput = "";
		unknownInt10 = -1;
		menuOpen = false;
		inputString = "";
		maxPlayers = 2048;
		myPlayerIndex = 2047;
		playerArray = new Player[maxPlayers];
		playerIndices = new int[maxPlayers];
		entityIndices = new int[maxPlayers];
		playerBuffers = new Stream[maxPlayers];
		lastChatId = 1;
		mapRegions = new int[104][104];
		mapSize = 0x766654;
		terrainData = new byte[16384];
		currentStats = new int[Skills.skillsCount];
		ignoreListAsLongs = new long[100];
		loadingError = false;
		walkingQueueSize = 0x332d25;
		walkingQueueX = new int[5];
		constructRegionData = new int[104][104];
		chatTypes = new int[500];
		chatNames = new String[500];
		chatMessages = new String[500];
		chatButtons = new Sprite[4];
		sideIcons = new Sprite[15];
		redStones = new Sprite[5];
		scrollBarDrag = true;
		friendsListAsLongs = new long[200];
		currentSong = -1;
		drawingFlames = false;
		spriteDrawX = -1;
		spriteDrawY = -1;
		flameLeftX = new int[33];
		flameRightX = new int[256];
		decompressors = new Decompressor[5];
		variousSettings = new int[2000];
		aBoolean972 = false;
		maxOverheadCount = 50;
		overheadX = new int[maxOverheadCount];
		overheadY = new int[maxOverheadCount];
		overheadHeight = new int[maxOverheadCount];
		overheadWidth = new int[maxOverheadCount];
		overheadTextColor = new int[maxOverheadCount];
		overheadTextEffect = new int[maxOverheadCount];
		overheadTextCycle = new int[maxOverheadCount];
		overheadTextStr = new String[maxOverheadCount];
		activeInterfaceId = -1;
		hitMarks = new Sprite[20];
		hitMark = new Sprite[4];
		walkingQueueY = new int[5];
		aBoolean994 = false;
		compassWidth = 0x23201b;
		amountOrNameInput = "";
		projectileList = new NodeList();
		songSwitching = false;
		anInt1018 = -1;
		aBoolean1031 = false;
		mapFunctions = new Sprite[100];
		dialogID = -1;
		maxStats = new int[Skills.skillsCount];
		tabFlashTimer = new int[2000];
		aBoolean1047 = true;
		minimapHintX = new int[151];
		flashingTab = -1;
		spotAnimList = new NodeList();
		minimapHintY = new int[33];
		chatboxInterface = new RSInterface();
		mapScenes = new Background[100];
		barFillColor = 0x4d4233;
		menuActionTypes = new int[7];
		mapFunctionX = new int[1000];
		mapFunctionY = new int[1000];
		aBoolean1080 = false;
		friendsList = new String[200];
		inStream = Stream.create();
		expectedCRCs = new int[9];
		menuActionCmd2 = new int[500];
		menuActionCmd3 = new int[500];
		menuActionID = new int[500];
		menuActionCmd1 = new int[500];
		headIcons = new Sprite[20];
		skullIcons = new Sprite[20];
		headIconsHint = new Sprite[20];
		tabAreaAltered = false;
		inputTitle = "";
		atPlayerActions = new String[5];
		atPlayerArray = new boolean[5];
		tileFlags = new int[4][13][13];
		cameraOscillationSpeed = 2;
		minimapImages = new Sprite[1000];
		aBoolean1141 = false;
		aBoolean1149 = false;
		crosses = new Sprite[8];
		musicEnabled = true;
		needDrawTabArea = false;
		loggedIn = false;
		canMute = false;
		aBoolean1159 = false;
		aBoolean1160 = false;
		minimapZoomDelta = 1;
		myUsername = "";
		myPassword = "";
		genericLoadingError = false;
		reportAbuseInterfaceID = -1;
		spawnObjectList = new NodeList();
		selectedArea = 128;
		invOverlayInterfaceID = -1;
		stream = Stream.create();
		menuActionName = new String[500];
		tabAreaX = new int[5];
		tabAreaY = new int[50];
		minimapRotationDelta = 2;
		chatFilterScrollMax = 78;
		promptInput = "";
		modIcons = new Background[2];
		tabID = 3;
		inputTaken = false;
		songChanging = true;
		chatFilterOffsets = new int[151];
		aCollisionMapArray1230 = new CollisionMap[4];
		aBoolean1233 = false;
		menuActionCmd4 = new int[100];
		menuActionCmd5 = new int[50];
		aBoolean1242 = false;
		mapObjectIds = new int[50];
		rsAlreadyLoaded = false;
		welcomeScreenRaised = false;
		messagePromptRaised = false;
		loginMessage1 = "";
		loginMessage2 = "";
		backDialogID = -1;
		anInt1279 = 2;
		bigX = new int[4000];
		bigY = new int[4000];
		anInt1289 = -1;
	}

	public int rights;
	public String name;
	public String message;
	public String clanname;
	private final int[] chatRights;
	public int chatTypeView;
	public int clanChatMode;
	public int duelMode;
	/* Declare custom sprites */
	private Sprite chatArea;
	private Sprite[] chatButtons;
	private Sprite tabArea;
	private Sprite mapArea;
	private Sprite emptyOrb;
	private Sprite hoverOrb;
	private Sprite hoverorbrun;
	private Sprite hoverorbrun2;
	private Sprite runClick;
	private Sprite runorb;
	private Sprite hitPointsFill;
	private Sprite prayerFill;
 	public boolean runClicked = false;

	/**/
	private RSImageProducer leftFrame;
	private RSImageProducer topFrame;
	private RSImageProducer rightFrame;
	private int ignoreCount;
	private long serverSeed;
	private int[][] constructMapTiles;
	private int[] friendsNodeIDs;
	private NodeList[][][] groundArray;
	private int[] npcUpdateTypes;
	private int[] npcLocalIndices;
	private volatile boolean midiFading;
	private Socket aSocket832;
	private int loginScreenState;
	private Stream loginStream;
	private NPC[] npcArray;
	private int npcCount;
	private int[] npcIndices;
	private int npcUpdateCount;
	private int[] entityUpdateIndices;
	private int entityUpdateCount;
	private int entityUpdateIndex;
	private int chatScrollMax;
	private String clickToContinueString;
	private int privateChatMode;
	private Stream outStream;
	private boolean pendingInput;
	private static int lastKnownPlane;
	private int[] entityUpdateX;
	private int[] entityUpdateY;
	private int[] entityUpdateId;
	private int[] entityUpdateFace;
	private static int cameraAngle;
	private int minimapRotation;
	private int openInterfaceID;
	private int xCameraPos;
	private int zCameraPos;
	private int yCameraPos;
	private int yCameraCurve;
	private int xCameraCurve;
	private int myPrivilege;
	private final int[] currentExp;
	private Sprite[] redStones;
	private Sprite mapFlag;
	private Sprite mapMarker;
	private boolean continuedDialogue;
	private final int[] tabAreaFlashCycle;
	private int tabFlashIndex;
	private final boolean[] sidebarFlashing;
	private int weight;
	private MouseDetection mouseDetection;
	private volatile boolean drawFlames;
	private String reportAbuseInput;
	private int unknownInt10;
	private boolean menuOpen;
	private int lastItemSelectedSlot;
	private String inputString;
	private final int maxPlayers;
	private final int myPlayerIndex;
	private Player[] playerArray;
	private int playerCount;
	private int[] playerIndices;
	private int entityCount;
	private int[] entityIndices;
	private Stream[] playerBuffers;
	private int lastItemSelectedInterface;
	private int lastChatId;
	private int friendsCount;
	private int mapRegionCount;
	private int[][] mapRegions;
	private final int mapSize;
	private byte[] terrainData;
	private int terrainDataIndex;
	private int crossX;
	private int crossY;
	private int crossIndex;
	private int crossType;
	private int plane;
	private final int[] currentStats;
	private static int anInt924_static;
	private final long[] ignoreListAsLongs;
	private boolean loadingError;
	private final int walkingQueueSize;
	private final int[] walkingQueueX;
	private int[][] constructRegionData;
	private Sprite loginBoxSprite;
	private Sprite loginDetailSprite;
	private int cameraTargetIndex;
	private int cameraTargetTileX;
	private int cameraTargetTileY;
	private int cameraTargetHeight;
	private int cameraTargetLocalX;
	private int cameraTargetLocalY;
	private static int anInt940_static;
	private final int[] chatTypes;
	private final String[] chatNames;
	private final String[] chatMessages;
	private int cameraTargetLocalZ;
	private WorldController worldController;
	private Sprite[] sideIcons;
	private int menuScreenArea;
	private int menuOffsetX;
	private int menuOffsetY;
	private int menuWidth;
	private int menuHeight;
	private long lastClickTime;
	private boolean scrollBarDrag;
	private long[] friendsListAsLongs;
	private String[] clanList = new String[100];
	private int currentSong;
	private static int nodeID = 10;
	static int portOff;
	static boolean clientData;
	private static boolean isMembers = true;
	private static boolean lowMem;
	private volatile boolean drawingFlames;
	private int spriteDrawX;
	private int spriteDrawY;
	private final int[] chatColors = {
		0xffff00, 0xff0000, 65280, 65535, 0xff00ff, 0xffffff
	};
	private Background loginFireLeft;
	private Background loginFireRight;
	private final int[] flameLeftX;
	private final int[] flameRightX;
	final Decompressor[] decompressors;
	public int variousSettings[];
	private boolean aBoolean972;
	private final int maxOverheadCount;
	private final int[] overheadX;
	private final int[] overheadY;
	private final int[] overheadHeight;
	private final int[] overheadWidth;
	private final int[] overheadTextColor;
	private final int[] overheadTextEffect;
	private final int[] overheadTextCycle;
	private final String[] overheadTextStr;
	private int moveItemSlotStart;
	private int activeInterfaceId;
	private static int anInt986_static;
	private Sprite[] hitMarks;
	private Sprite[] hitMark;
	private int moveItemSlotEnd;
	private int moveItemInterfaceId;
	private final int[] walkingQueueY;
	private static boolean aBoolean993_static;
	private final boolean aBoolean994;
	private int cameraPosX;
	private int cameraPosY;
	private int cameraPosHeight;
	private int cameraSpeed;
	private int cameraAcceleration;
	private ISAACRandomGen encryption;
	private Sprite mapEdge;
	private Sprite multiOverlay;
	private final int compassWidth;
	static final int[][] anIntArrayArray1003 = {
		{
			6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 
			2983, 54193
		}, {
			8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 
			56621, 4783, 1341, 16578, 35003, 25239
		}, {
			25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 
			10153, 56621, 4783, 1341, 16578, 35003
		}, {
			4626, 11146, 6439, 12, 4758, 10270
		}, {
			4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574
		}
	};
	private String amountOrNameInput;
	private static int anInt1005_static;
	private int daysSinceLastLogin;
	private int pktSize;
	private int pktType;
	private int idleTime;
	private int idleLogout;
	private int hintIconDelay;
	private NodeList projectileList;
	private int cameraSmoothedX;
	private int cameraSmoothedY;
	private int songSwitchDelay;
	private boolean songSwitching;
	private int anInt1018;
	private static final int[] XP_TABLE;
	private int chatAreaScrollPos;
	private int chatAreaScrollMax;
	private int loadingStage;
	private Sprite scrollBar1;
	private Sprite scrollBar2;
	private int tabFlashCycleAlt;
	private Background backBase1;
	private Background backBase2;
	private Background backHmid1;
	private boolean aBoolean1031;
	private Sprite[] mapFunctions;
	private int baseX;
	private int baseY;
	private int regionAbsBaseX;
	private int regionAbsBaseY;
	private int loginFailures;
	private int walkDest;
	private int walkDestX;
	private int walkDestY;
	private int dialogID;
	private final int[] maxStats;
	private final int[] tabFlashTimer;
	private int anInt1046;
	private boolean aBoolean1047;
	private int hintArrowType;
	private String hintText;
	private static int anInt1051_counter;
	private final int[] minimapHintX;
	private StreamLoader titleStreamLoader;
	private int flashingTab;
	private int flashingSideicon;
	private NodeList spotAnimList;
	private final int[] minimapHintY;
	public final RSInterface chatboxInterface;
	private Background[] mapScenes;
	private static int anInt1061_static;
	private int lastMapRegionX;
	private final int barFillColor;
	private int friendsListAction;
	private final int[] menuActionTypes;
	private int mouseInvInterfaceIndex;
	private int lastActiveInvInterface;
	private OnDemandFetcher onDemandFetcher;
	private int mapRegionX;
	private int mapRegionY;
	private int mapFunctionCount;
	private int[] mapFunctionX;
	private int[] mapFunctionY;
	private Sprite mapDotItem;
	private Sprite mapDotNPC;
	private Sprite mapDotPlayer;
	private Sprite mapDotFriend;
	private Sprite mapDotTeam;
	private Sprite mapDotClan;
	private int anInt1079;
	private boolean aBoolean1080;
	private String[] friendsList;
	private Stream inStream;
	private int dragFromSlotInterface;
	private int dragFromSlot;
	private int activeInterfaceType;
	private int dragStartX;
	private int dragStartY;
	public static int chatScrollAmount;
	private final int[] expectedCRCs;
	private int[] menuActionCmd2;
	private int[] menuActionCmd3;
	private int[] menuActionID;
	private int[] menuActionCmd1;
	private Sprite[] headIcons;
	private Sprite[] skullIcons;
	private Sprite[] headIconsHint;
	private static int anInt1097_counter;
	private int cameraLocX;
	private int cameraLocY;
	private int cameraLocHeight;
	private int cameraLocSpeed;
	private int cameraLocAccel;
	private static boolean tabAreaAltered;
	private int anInt1104;
	private RSImageProducer tabImageProducer;
	private RSImageProducer mapAreaIP;
	private RSImageProducer gameScreenIP;
	private RSImageProducer chatAreaIP;
	private RSImageProducer chatSettingIP;
	private RSImageProducer topSideIP1;
	private RSImageProducer topSideIP2;
	private RSImageProducer bottomSideIP1;
	private RSImageProducer bottomSideIP2;
	private static int anInt1117_static;
	private int membersInt;
	private String inputTitle;
	private Sprite compass;
	private RSImageProducer titleIP1;
	private RSImageProducer titleIP2;
	private RSImageProducer titleIP3;
	public static Player myPlayer;
	private final String[] atPlayerActions;
	private final boolean[] atPlayerArray;
	private final int[][][] tileFlags;
	private final int[] tabInterfaceIDs = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
		-1, -1, -1, -1,-1
	};
	private int cameraOscillationH;
	private int cameraOscillationSpeed;
	private int menuActionRow;
	private static int anInt1134_static;
	private int spellSelected;
	private int spellCastOnType;
	private int spellUsableOn;
	private String spellTooltip;
	private Sprite[] minimapImages;
	private boolean aBoolean1141;
	private static int anInt1142_static;
	private int energy;
	private boolean aBoolean1149;
	private Sprite[] crosses;
	private boolean musicEnabled;
	private Background[] loginScreenSprites;
	private static boolean needDrawTabArea;
	private int unreadMessages;
	private static int anInt1155_static;
	private static boolean fpsOn;
	public boolean loggedIn;
	private boolean canMute;
	private boolean aBoolean1159;
	private boolean aBoolean1160;
	static int loopCycle;
	private static final String validUserPassChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
	private RSImageProducer titleMuralIP;
	private RSImageProducer mapEdgeIP;
	private RSImageProducer titleButtonIP;
	private RSImageProducer loginMsgIP;
	private RSImageProducer topCenterIP;
	private int daysSinceRecovChange;
	private RSSocket socketStream;
	private int walkPathLength;
	private int minimapInt3;
	private int minimapZoomDelta;
	private long loginTimer;
	private String myUsername;
	private String myPassword;
	private static int anInt1175_static;
	private boolean genericLoadingError;
	private final int[] mapChunkX = {
		0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
		2, 2, 3
	};
	private int reportAbuseInterfaceID;
	private NodeList spawnObjectList;
	private int[] mapChunkX2;
	private int[] mapChunkY2;
	private int[] mapChunkLandscapeIds;
	private byte[][] mapLandscapeData;
	private int selectedArea;
	private int minimapInt1;
	private int minimapZoomTarget;
	private int minimapZoom;
	private static int anInt1188_static;
	private int invOverlayInterfaceID;
	private int[] chatScrollPositions;
	private int[] chatHighlights;
	private Stream stream;
	private int walkQueueLength;
	private int splitPrivateChat;
	private Background mapBack;
	private String[] menuActionName;
	private Sprite chatAreaBackground;
	private Sprite chatSettingsBackground;
	private final int[] tabAreaX;
	static final int[] anIntArray1204 = {
		9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 
		58654, 5027, 1457, 16565, 34991, 25486
	};
	private static boolean flagged;
	private final int[] tabAreaY;
	private int lastMapRegionY;
	private int minimapInt2;
	private int minimapRotationDelta;
	private int chatFilterScrollMax;
	private String promptInput;
	private int menuActionCounter;
	private int[][][] intGroundArray;
	private long chatLastTyped;
	private int loginScreenCursorPos;
	private final Background[] modIcons;
	private long aLong1220;
	private static int tabID;
	private int hintIconNpcIndex;
	public static boolean inputTaken;
	private int inputDialogState;
	private static int anInt1226_static;
	private int nextSong;
	private boolean songChanging;
	private final int[] chatFilterOffsets;
	private CollisionMap[] aCollisionMapArray1230;
	public static int BIT_MASKS[];
	private boolean aBoolean1233;
	private int[] chatFilterTypes;
	private int[] chatFilterNames;
	private int[] chatFilterMessages;
	private int mapLoadProgress;
	private int mapLoadState;
	public final int maxMenuEntries = 100;
	private final int[] menuActionCmd4;
	private final int[] menuActionCmd5;
	private boolean aBoolean1242;
	private int atInventoryLoopCycle;
	private int atInventoryInterface;
	private int atInventoryIndex;
	private int atInventoryInterfaceType;
	private byte[][] mapObjectData;
	private int tradeMode;
	private int actionType;
	private final int[] mapObjectIds;
	private int anInt1251;
	private final boolean rsAlreadyLoaded;
	private int clickMode;
	private int anInt1254;
	private boolean welcomeScreenRaised;
	private boolean messagePromptRaised;
	private int anInt1257;
	private byte[][][] byteGroundArray;
	private int prevSong;
	private int destX;
	private int destY;
	private Sprite minimapSprite;
	private int hintIconX;
	private int hintIconY;
	private String loginMessage1;
	private String loginMessage2;
	private int hintIconDrawX;
	private int hintIconDrawY;
	private TextDrawingArea smallText;
	private TextDrawingArea boldFont;
	private TextDrawingArea chatTextDrawingArea;
	private int anInt1275;
	private int backDialogID;
	private int anInt1278;
	private int anInt1279;
	private int[] bigX;
	private int[] bigY;
	private int itemSelected;
	private int selectedInventorySlot;
	private int selectedInventoryInterface;
	private int anInt1285;
	private String selectedItemName;
	private int publicChatMode;
	private static int anInt1288_static;
	private int anInt1289;
	public static int anInt1290_public;
	public static String server = "";
	public int drawCount;
	public int fullscreenInterfaceID;
	public int anInt1044;//377
	public int anInt1129;//377
	public int anInt1315;//377
	public int anInt1500;//377
	public int anInt1501;//377
	public int[] fullScreenTextureArray;

	// --- Resize infrastructure (Phase A-2) ---
	public static final int REGULAR_WIDTH = 765;
	public static final int REGULAR_HEIGHT = 503;
	private static final int RESIZABLE_DEFAULT_WIDTH = 900;
	private static final int RESIZABLE_DEFAULT_HEIGHT = 600;
	public static int clientSize = 0; // 0=fixed, 1=resizable, 2=fullscreen
	public static int clientWidth = 765;
	public static int clientHeight = 503;
	public int gameAreaWidth = 512;
	public int gameAreaHeight = 334;
	public static java.awt.Container outerFrame; // set by Jframe.java
	public static client instance;
	public static int cameraZoom = 0;
	private static final int CAMERA_ZOOM_MIN = -300;
	private static final int CAMERA_ZOOM_MAX = 600;
	private static final int CAMERA_ZOOM_STEP = 40;
	public static Sprite[] cacheSprite;
	// --- Resize methods (Phase A-2) ---

	public void checkSize() {
		if (clientSize == 1) {
			// Read actual panel dimensions (not frame — panel fills the content area)
			int w = getGameComponent().getWidth();
			int h = getGameComponent().getHeight();
			if (w > 0 && h > 0 && (w != clientWidth || h != clientHeight)) {
				clientWidth = w;
				clientHeight = h;
				gameAreaWidth = w;
				gameAreaHeight = h;
				super.myWidth = w;
				super.myHeight = h;
				super.graphics = getGameComponent().getGraphics();
				updateGameArea();
			}
		}
	}

	public void updateGameArea() {
		Texture.setViewport(clientSize == 0 ? REGULAR_WIDTH : clientWidth, clientSize == 0 ? REGULAR_HEIGHT : clientHeight);
		fullScreenTextureArray = Texture.scanlineOffset;
		Texture.setViewport(clientSize == 0 ? 519 : clientWidth, clientSize == 0 ? 165 : clientHeight);
		mapChunkX2 = Texture.scanlineOffset;
		Texture.setViewport(clientSize == 0 ? 246 : clientWidth, clientSize == 0 ? 335 : clientHeight);
		mapChunkY2 = Texture.scanlineOffset;
		Texture.setViewport(clientSize == 0 ? 512 : gameAreaWidth, clientSize == 0 ? 334 : gameAreaHeight);
		mapChunkLandscapeIds = Texture.scanlineOffset;

		int ai[] = new int[9];
		for (int i8 = 0; i8 < 9; i8++) {
			int k8 = 128 + i8 * 32 + 15;
			int l8 = 600 + k8 * 3;
			int i9 = Texture.SINE[k8];
			ai[i8] = l8 * i9 >> 16;
		}

		WorldController.drawMinimapTile(500, 800, clientSize == 0 ? 512 : gameAreaWidth, clientSize == 0 ? 334 : gameAreaHeight, ai);
		loginMsgIP = new RSImageProducer(clientSize == 0 ? 512 : gameAreaWidth, clientSize == 0 ? 334 : gameAreaHeight, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		super.fullGameScreen = new RSImageProducer(clientWidth, clientHeight, getGameComponent());
		welcomeScreenRaised = true;
	}

	public void toggleSize(int size) {
		if (clientSize == size) return;
		clientSize = size;
		int width, height;
		if (size == 0) {
			width = REGULAR_WIDTH;
			height = REGULAR_HEIGHT;
			gameAreaWidth = 512;
			gameAreaHeight = 334;
		} else if (size == 1) {
			width = RESIZABLE_DEFAULT_WIDTH;
			height = RESIZABLE_DEFAULT_HEIGHT;
			gameAreaWidth = width;
			gameAreaHeight = height;
		} else {
			java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			width = (int) screen.getWidth();
			height = (int) screen.getHeight();
			gameAreaWidth = width;
			gameAreaHeight = height;
		}
		clientWidth = width;
		clientHeight = height;
		super.myWidth = width;
		super.myHeight = height;

		// Update JFrame and JPanel via Jframe helper
		Jframe.updatePanelSize(size, width, height);

		// Refresh graphics
		super.graphics = getGameComponent().getGraphics();

		updateGameArea();
	}

	/**
	 * Process middle mouse drag for camera rotation.
	 * Called each frame from processSceneEntities.
	 * Horizontal drag = yaw (minimapInt1), Vertical drag = pitch (selectedArea).
	 */
	private void processMiddleMouseDrag() {
		if (super.middleMouseDown) {
			int dx = super.middleMouseDragX - super.middleMouseStartX;
			int dy = super.middleMouseDragY - super.middleMouseStartY;
			// Apply rotation and reset start position for continuous drag
			minimapInt1 = minimapInt1 - dx & 0x7ff;
			selectedArea += dy;
			if (selectedArea < 128)
				selectedArea = 128;
			if (selectedArea > 383)
				selectedArea = 383;
			super.middleMouseStartX = super.middleMouseDragX;
			super.middleMouseStartY = super.middleMouseDragY;
		}
	}

	@Override
	public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
		int rotation = e.getWheelRotation();
		int mx = super.mouseX;
		int my = super.mouseY;

		// Chat area scroll
		boolean overChat;
		if (clientSize == 0) {
			overChat = mx > 0 && mx < 519 && my > 338 && my < 503;
		} else {
			overChat = mx > 0 && mx < 519 && my > clientHeight - 165 && my < clientHeight;
		}
		if (overChat) {
			int scroll = rotation * 30;
			chatScrollAmount += scroll;
			if (chatScrollAmount < 0)
				chatScrollAmount = 0;
			if (chatScrollAmount > chatFilterScrollMax - 110)
				chatScrollAmount = chatFilterScrollMax - 110;
			if (chatScrollAmount < 0)
				chatScrollAmount = 0;
			inputTaken = true;
			return;
		}

		// Minimap area — ignore scroll
		boolean overMinimap;
		if (clientSize == 0) {
			overMinimap = mx > 519 && my < 168;
		} else {
			overMinimap = mx > clientWidth - 246 && my < 168;
		}
		if (overMinimap) {
			return;
		}

		// Tab area — ignore scroll (could add interface scroll later)
		boolean overTab;
		if (clientSize == 0) {
			overTab = mx > 519 && my > 168;
		} else {
			overTab = mx > clientWidth - 246 && my > clientHeight - 335;
		}
		if (overTab) {
			return;
		}

		// Otherwise — camera zoom
		cameraZoom -= rotation * CAMERA_ZOOM_STEP;
		if (cameraZoom < CAMERA_ZOOM_MIN)
			cameraZoom = CAMERA_ZOOM_MIN;
		if (cameraZoom > CAMERA_ZOOM_MAX)
			cameraZoom = CAMERA_ZOOM_MAX;
	}

	/**
	 * Draws the resizable mode UI overlays (chat, tabs, minimap)
	 * on top of the 3D viewport. Called from processSceneEntities.
	 */
	public void drawUnfixedGame() {
		if (clientSize == 0 || cacheSprite == null) return;
		try {
			if (cacheSprite[30] != null)
				cacheSprite[30].drawSprite(0, clientHeight - 166);
			if (cacheSprite[31] != null)
				cacheSprite[31].drawSprite(0, clientHeight - 22);
			if (cacheSprite[33] != null)
				cacheSprite[33].drawSprite(clientWidth - 238, 3);
			boolean wideTabs = clientWidth >= 1000;
			if (wideTabs) {
				if (cacheSprite[27] != null)
					cacheSprite[27].drawSprite(clientWidth - 461, clientHeight - 36);
			} else {
				if (cacheSprite[28] != null)
					cacheSprite[28].drawSprite(clientWidth - 241, clientHeight - 73);
			}
			if (cacheSprite[29] != null) {
				if (wideTabs)
					cacheSprite[29].drawSprite(clientWidth - 204, clientHeight - 310);
				else
					cacheSprite[29].drawSprite(clientWidth - 222, clientHeight - 346);
			}
		} catch (Exception e) { }
	}

	public boolean isFixed() {
		return clientSize == 0;
	}

	// --- End resize methods ---

	public void resetAllImageProducers() {
		if (super.fullGameScreen != null) {
			return;
		}
		topCenterIP = null;
		titleButtonIP = null;
		titleMuralIP = null;
		loginMsgIP = null;
		titleIP1 = null;
		titleIP2 = null;
		titleIP3 = null;
		tabImageProducer = null;
		mapAreaIP = null;
		gameScreenIP = null;
		chatAreaIP = null;
		chatSettingIP = null;
		topSideIP1 = null;
		topSideIP2 = null;
		bottomSideIP1 = null;
		bottomSideIP2 = null;
		super.fullGameScreen = new RSImageProducer(clientWidth, clientHeight, getGameComponent());
		welcomeScreenRaised = true;
	}
	
	
	public void launchURL(String url) { 
		String osName = System.getProperty("os.name"); 
		try { 
			if (osName.startsWith("Mac OS")) { 
				Class fileMgr = Class.forName("com.apple.eio.FileManager"); 
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class}); 
				openURL.invoke(null, new Object[] {url});
			} else if (osName.startsWith("Windows")) 
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url); 
			else { //assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "safari" }; 
			String browser = null; 
			for (int count = 0; count < browsers.length && browser == null; count++) 
				if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
					browser = browsers[count]; 
			if (browser == null) {
				 throw new Exception("Could not find web browser"); 
			} else
				 Runtime.getRuntime().exec(new String[] {browser, url});
			}
		} catch (Exception e) { 
			pushMessage("Failed to open URL.", 0, "");
		}
	}

	static  {
		XP_TABLE = new int[99];
		int i = 0;
		for(int j = 0; j < 99; j++) {
			int l = j + 1;
			int i1 = (int)((double)l + 300D * Math.pow(2D, (double)l / 7D));
			i += i1;
			XP_TABLE[j] = i / 4;
		}
		BIT_MASKS = new int[32];
		i = 2;
		for(int k = 0; k < 32; k++) {
			BIT_MASKS[k] = i - 1;
			i += i;
		}
	}
}
