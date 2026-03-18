// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

final class ObjectManager {

	public ObjectManager(byte abyte0[][][], int ai[][][])
	{
		minimumPlane = 99;
		mapSizeX = 104;
		mapSizeY = 104;
		tileHeights = ai;
		tileSettings = abyte0;
		underlayFloorIds = new byte[4][mapSizeX][mapSizeY];
		overlayFloorIds = new byte[4][mapSizeX][mapSizeY];
		overlayShapes = new byte[4][mapSizeX][mapSizeY];
		overlayOrientations = new byte[4][mapSizeX][mapSizeY];
		tileLightIntensity = new int[4][mapSizeX + 1][mapSizeY + 1];
		tileShadowMap = new byte[4][mapSizeX + 1][mapSizeY + 1];
		lightmap = new int[mapSizeX + 1][mapSizeY + 1];
		blendHue = new int[mapSizeY];
		blendSaturation = new int[mapSizeY];
		blendLightness = new int[mapSizeY];
		blendHueDivisor = new int[mapSizeY];
		blendDirectionCount = new int[mapSizeY];
	}

	private static int noiseHash(int i, int j)
	{
		int k = i + j * 57;
		k = k << 13 ^ k;
		int l = k * (k * k * 15731 + 0xc0ae5) + 0x5208dd0d & 0x7fffffff;
		return l >> 19 & 0xff;
	}

	public final void applyTerrainCollision(CollisionMap acollisionMap[], WorldController worldController)
	{
		for(int j = 0; j < 4; j++)
		{
			for(int k = 0; k < 104; k++)
			{
				for(int i1 = 0; i1 < 104; i1++)
					if((tileSettings[j][k][i1] & 1) == 1)
					{
						int k1 = j;
						if((tileSettings[1][k][i1] & 2) == 2)
							k1--;
						if(k1 >= 0)
							acollisionMap[k1].markBlocked(i1, k);
					}

			}

		}
		hueRandomizer += (int)(Math.random() * 5D) - 2;
		if(hueRandomizer < -8)
			hueRandomizer = -8;
		if(hueRandomizer > 8)
			hueRandomizer = 8;
		lightnessRandomizer += (int)(Math.random() * 5D) - 2;
		if(lightnessRandomizer < -16)
			lightnessRandomizer = -16;
		if(lightnessRandomizer > 16)
			lightnessRandomizer = 16;
		for(int l = 0; l < 4; l++)
		{
			byte abyte0[][] = tileShadowMap[l];
			byte byte0 = 96;
			char c = '\u0300';
			byte byte1 = -50;
			byte byte2 = -10;
			byte byte3 = -50;
			int j3 = (int)Math.sqrt(byte1 * byte1 + byte2 * byte2 + byte3 * byte3);
			int l3 = c * j3 >> 8;
			for(int j4 = 1; j4 < mapSizeY - 1; j4++)
			{
				for(int j5 = 1; j5 < mapSizeX - 1; j5++)
				{
					int k6 = tileHeights[l][j5 + 1][j4] - tileHeights[l][j5 - 1][j4];
					int l7 = tileHeights[l][j5][j4 + 1] - tileHeights[l][j5][j4 - 1];
					int j9 = (int)Math.sqrt(k6 * k6 + 0x10000 + l7 * l7);
					int k12 = (k6 << 8) / j9;
					int l13 = 0x10000 / j9;
					int j15 = (l7 << 8) / j9;
					int j16 = byte0 + (byte1 * k12 + byte2 * l13 + byte3 * j15) / l3;
					int j17 = (abyte0[j5 - 1][j4] >> 2) + (abyte0[j5 + 1][j4] >> 3) + (abyte0[j5][j4 - 1] >> 2) + (abyte0[j5][j4 + 1] >> 3) + (abyte0[j5][j4] >> 1);
					lightmap[j5][j4] = j16 - j17;
				}

			}

			for(int k5 = 0; k5 < mapSizeY; k5++)
			{
				blendHue[k5] = 0;
				blendSaturation[k5] = 0;
				blendLightness[k5] = 0;
				blendHueDivisor[k5] = 0;
				blendDirectionCount[k5] = 0;
			}

			for(int l6 = -5; l6 < mapSizeX + 5; l6++)
			{
				for(int i8 = 0; i8 < mapSizeY; i8++)
				{
					int k9 = l6 + 5;
					if(k9 >= 0 && k9 < mapSizeX)
					{
						int l12 = underlayFloorIds[l][k9][i8] & 0xff;
						if(l12 > 0)
						{
							Flo flo = Flo.cache[l12 - 1];
							blendHue[i8] += flo.blendHue;
							blendSaturation[i8] += flo.saturation;
							blendLightness[i8] += flo.lightness;
							blendHueDivisor[i8] += flo.hslWeight;
							blendDirectionCount[i8]++;
						}
					}
					int i13 = l6 - 5;
					if(i13 >= 0 && i13 < mapSizeX)
					{
						int i14 = underlayFloorIds[l][i13][i8] & 0xff;
						if(i14 > 0)
						{
							Flo flo_1 = Flo.cache[i14 - 1];
							blendHue[i8] -= flo_1.blendHue;
							blendSaturation[i8] -= flo_1.saturation;
							blendLightness[i8] -= flo_1.lightness;
							blendHueDivisor[i8] -= flo_1.hslWeight;
							blendDirectionCount[i8]--;
						}
					}
				}

				if(l6 >= 1 && l6 < mapSizeX - 1)
				{
					int l9 = 0;
					int j13 = 0;
					int j14 = 0;
					int k15 = 0;
					int k16 = 0;
					for(int k17 = -5; k17 < mapSizeY + 5; k17++)
					{
						int j18 = k17 + 5;
						if(j18 >= 0 && j18 < mapSizeY)
						{
							l9 += blendHue[j18];
							j13 += blendSaturation[j18];
							j14 += blendLightness[j18];
							k15 += blendHueDivisor[j18];
							k16 += blendDirectionCount[j18];
						}
						int k18 = k17 - 5;
						if(k18 >= 0 && k18 < mapSizeY)
						{
							l9 -= blendHue[k18];
							j13 -= blendSaturation[k18];
							j14 -= blendLightness[k18];
							k15 -= blendHueDivisor[k18];
							k16 -= blendDirectionCount[k18];
						}
						if(k17 >= 1 && k17 < mapSizeY - 1 && (!lowMem || (tileSettings[0][l6][k17] & 2) != 0 || (tileSettings[l][l6][k17] & 0x10) == 0 && getEffectivePlane(k17, l, l6) == currentPlane))
						{
							if(l < minimumPlane)
								minimumPlane = l;
							int l18 = underlayFloorIds[l][l6][k17] & 0xff;
							int i19 = overlayFloorIds[l][l6][k17] & 0xff;
							if(l18 > 0 || i19 > 0)
							{
								int j19 = tileHeights[l][l6][k17];
								int k19 = tileHeights[l][l6 + 1][k17];
								int l19 = tileHeights[l][l6 + 1][k17 + 1];
								int i20 = tileHeights[l][l6][k17 + 1];
								int j20 = lightmap[l6][k17];
								int k20 = lightmap[l6 + 1][k17];
								int l20 = lightmap[l6 + 1][k17 + 1];
								int i21 = lightmap[l6][k17 + 1];
								int j21 = -1;
								int k21 = -1;
								if(l18 > 0)
								{
									int l21 = (l9 * 256) / k15;
									int j22 = j13 / k16;
									int l22 = j14 / k16;
									j21 = packHSL(l21, j22, l22);
									l21 = l21 + hueRandomizer & 0xff;
									l22 += lightnessRandomizer;
									if(l22 < 0)
										l22 = 0;
									else
									if(l22 > 255)
										l22 = 255;
									k21 = packHSL(l21, j22, l22);
								}
								if(l > 0)
								{
									boolean flag = true;
									if(l18 == 0 && overlayShapes[l][l6][k17] != 0)
										flag = false;
									if(i19 > 0 && !Flo.cache[i19 - 1].occluding)
										flag = false;
									if(flag && j19 == k19 && j19 == l19 && j19 == i20)
										tileLightIntensity[l][l6][k17] |= 0x924;
								}
								int i22 = 0;
								if(j21 != -1)
									i22 = Texture.HSL_TO_RGB[adjustUnderlayLight(k21, 96)];
								if(i19 == 0)
								{
									worldController.addTile(l, l6, k17, 0, 0, -1, j19, k19, l19, i20, adjustUnderlayLight(j21, j20), adjustUnderlayLight(j21, k20), adjustUnderlayLight(j21, l20), adjustUnderlayLight(j21, i21), 0, 0, 0, 0, i22, 0);
								} else
								{
									int k22 = overlayShapes[l][l6][k17] + 1;
									byte byte4 = overlayOrientations[l][l6][k17];
									Flo flo_2 = Flo.cache[i19 - 1];
									int i23 = flo_2.textureId;
									int j23;
									int k23;
									if(i23 >= 0)
									{
										k23 = Texture.getAverageTextureColor(i23);
										j23 = -1;
									} else
									if(flo_2.rgb == 0xff00ff)
									{
										k23 = 0;
										j23 = -2;
										i23 = -1;
									} else if(flo_2.rgb == 0x333333) {
										k23 = Texture.HSL_TO_RGB[adjustOverlayLight(flo_2.blendedHSL, 96)];								
										j23 = -2;
										i23 = -1;
									} else {
										j23 = packHSL(flo_2.hue, flo_2.saturation, flo_2.lightness);
										k23 = Texture.HSL_TO_RGB[adjustOverlayLight(flo_2.blendedHSL, 96)];
									}
									worldController.addTile(l, l6, k17, k22, byte4, i23, j19, k19, l19, i20, adjustUnderlayLight(j21, j20), adjustUnderlayLight(j21, k20), adjustUnderlayLight(j21, l20), adjustUnderlayLight(j21, i21), adjustOverlayLight(j23, j20), adjustOverlayLight(j23, k20), adjustOverlayLight(j23, l20), adjustOverlayLight(j23, i21), i22, k23);
								}
							}
						}
					}

				}
			}

			for(int j8 = 1; j8 < mapSizeY - 1; j8++)
			{
				for(int i10 = 1; i10 < mapSizeX - 1; i10++)
					worldController.setTileLogicHeight(l, i10, j8, getEffectivePlane(j8, l, i10));

			}

		}

		worldController.setLighting(-10, -50, -50);
		for(int j1 = 0; j1 < mapSizeX; j1++)
		{
			for(int l1 = 0; l1 < mapSizeY; l1++)
				if((tileSettings[1][j1][l1] & 2) == 2)
					worldController.shiftPlaneDown(l1, j1);

		}

		int i2 = 1;
		int j2 = 2;
		int k2 = 4;
		for(int l2 = 0; l2 < 4; l2++)
		{
			if(l2 > 0)
			{
				i2 <<= 3;
				j2 <<= 3;
				k2 <<= 3;
			}
			for(int i3 = 0; i3 <= l2; i3++)
			{
				for(int k3 = 0; k3 <= mapSizeY; k3++)
				{
					for(int i4 = 0; i4 <= mapSizeX; i4++)
					{
						if((tileLightIntensity[i3][i4][k3] & i2) != 0)
						{
							int k4 = k3;
							int l5 = k3;
							int i7 = i3;
							int k8 = i3;
							for(; k4 > 0 && (tileLightIntensity[i3][i4][k4 - 1] & i2) != 0; k4--);
							for(; l5 < mapSizeY && (tileLightIntensity[i3][i4][l5 + 1] & i2) != 0; l5++);
label0:
							for(; i7 > 0; i7--)
							{
								for(int j10 = k4; j10 <= l5; j10++)
									if((tileLightIntensity[i7 - 1][i4][j10] & i2) == 0)
										break label0;

							}

label1:
							for(; k8 < l2; k8++)
							{
								for(int k10 = k4; k10 <= l5; k10++)
									if((tileLightIntensity[k8 + 1][i4][k10] & i2) == 0)
										break label1;

							}

							int l10 = ((k8 + 1) - i7) * ((l5 - k4) + 1);
							if(l10 >= 8)
							{
								char c1 = '\360';
								int k14 = tileHeights[k8][i4][k4] - c1;
								int l15 = tileHeights[i7][i4][k4];
								WorldController.addOccluder(l2, i4 * 128, l15, i4 * 128, l5 * 128 + 128, k14, k4 * 128, 1);
								for(int l16 = i7; l16 <= k8; l16++)
								{
									for(int l17 = k4; l17 <= l5; l17++)
										tileLightIntensity[l16][i4][l17] &= ~i2;

								}

							}
						}
						if((tileLightIntensity[i3][i4][k3] & j2) != 0)
						{
							int l4 = i4;
							int i6 = i4;
							int j7 = i3;
							int l8 = i3;
							for(; l4 > 0 && (tileLightIntensity[i3][l4 - 1][k3] & j2) != 0; l4--);
							for(; i6 < mapSizeX && (tileLightIntensity[i3][i6 + 1][k3] & j2) != 0; i6++);
label2:
							for(; j7 > 0; j7--)
							{
								for(int i11 = l4; i11 <= i6; i11++)
									if((tileLightIntensity[j7 - 1][i11][k3] & j2) == 0)
										break label2;

							}

label3:
							for(; l8 < l2; l8++)
							{
								for(int j11 = l4; j11 <= i6; j11++)
									if((tileLightIntensity[l8 + 1][j11][k3] & j2) == 0)
										break label3;

							}

							int k11 = ((l8 + 1) - j7) * ((i6 - l4) + 1);
							if(k11 >= 8)
							{
								char c2 = '\360';
								int l14 = tileHeights[l8][l4][k3] - c2;
								int i16 = tileHeights[j7][l4][k3];
								WorldController.addOccluder(l2, l4 * 128, i16, i6 * 128 + 128, k3 * 128, l14, k3 * 128, 2);
								for(int i17 = j7; i17 <= l8; i17++)
								{
									for(int i18 = l4; i18 <= i6; i18++)
										tileLightIntensity[i17][i18][k3] &= ~j2;

								}

							}
						}
						if((tileLightIntensity[i3][i4][k3] & k2) != 0)
						{
							int i5 = i4;
							int j6 = i4;
							int k7 = k3;
							int i9 = k3;
							for(; k7 > 0 && (tileLightIntensity[i3][i4][k7 - 1] & k2) != 0; k7--);
							for(; i9 < mapSizeY && (tileLightIntensity[i3][i4][i9 + 1] & k2) != 0; i9++);
label4:
							for(; i5 > 0; i5--)
							{
								for(int l11 = k7; l11 <= i9; l11++)
									if((tileLightIntensity[i3][i5 - 1][l11] & k2) == 0)
										break label4;

							}

label5:
							for(; j6 < mapSizeX; j6++)
							{
								for(int i12 = k7; i12 <= i9; i12++)
									if((tileLightIntensity[i3][j6 + 1][i12] & k2) == 0)
										break label5;

							}

							if(((j6 - i5) + 1) * ((i9 - k7) + 1) >= 4)
							{
								int j12 = tileHeights[i3][i5][k7];
								WorldController.addOccluder(l2, i5 * 128, j12, j6 * 128 + 128, i9 * 128 + 128, j12, k7 * 128, 4);
								for(int k13 = i5; k13 <= j6; k13++)
								{
									for(int i15 = k7; i15 <= i9; i15++)
										tileLightIntensity[i3][k13][i15] &= ~k2;

								}

							}
						}
					}

				}

			}

		}

	}

	private static int calculateNoise(int i, int j)
	{
		int k = (interpolatedNoise(i + 45365, j + 0x16713, 4) - 128) + (interpolatedNoise(i + 10294, j + 37821, 2) - 128 >> 1) + (interpolatedNoise(i, j, 1) - 128 >> 2);
		k = (int)((double)k * 0.29999999999999999D) + 35;
		if(k < 10)
			k = 10;
		else
		if(k > 60)
			k = 60;
		return k;
	}

	public static void preloadObjectModels(Stream stream, OnDemandFetcher class42_sub1)
	{
label0:
		{
			int i = -1;
			do
			{
				int j = stream.readSmart();
				if(j == 0)
					break label0;
				i += j;
				ObjectDef class46 = ObjectDef.forID(i);
				class46.preloadModels(class42_sub1);
				do
				{
					int k = stream.readSmart();
					if(k == 0)
						break;
					stream.readUnsignedByte();
				} while(true);
			} while(true);
		}
	}

	public final void flattenTerrain(int i, int j, int l, int i1)
	{
		for(int j1 = i; j1 <= i + j; j1++)
		{
			for(int k1 = i1; k1 <= i1 + l; k1++)
				if(k1 >= 0 && k1 < mapSizeX && j1 >= 0 && j1 < mapSizeY)
				{
					tileShadowMap[0][k1][j1] = 127;
					if(k1 == i1 && k1 > 0)
						tileHeights[0][k1][j1] = tileHeights[0][k1 - 1][j1];
					if(k1 == i1 + l && k1 < mapSizeX - 1)
						tileHeights[0][k1][j1] = tileHeights[0][k1 + 1][j1];
					if(j1 == i && j1 > 0)
						tileHeights[0][k1][j1] = tileHeights[0][k1][j1 - 1];
					if(j1 == i + j && j1 < mapSizeY - 1)
						tileHeights[0][k1][j1] = tileHeights[0][k1][j1 + 1];
				}

		}
	}

	private void placeObject(int i, WorldController worldController, CollisionMap collisionMap, int j, int k, int l, int i1, int j1)
	{
		/*int mX = clientInstance.MapX - 6;
		int mY = clientInstance.MapY - 6;
		int actualX = mX * 8 + l;
		int actualY = mY * 8 + i;
		int actualH = k;
		if(!(actualX >= 2630 && actualX <= 2639 && actualY >= 4680 && actualY <= 4690)//castle at starting point
		&& !(actualX >= 2640 && actualX <= 2643 && actualY >= 4680 && actualY <= 4689)//castle at starting point
		&& !(actualX >= 2630 && actualX <= 2636 && actualY >= 4676 && actualY <= 4689)//castle at starting point
		&& !(actualX >= 2629 && actualX <= 2637 && actualY >= 4679 && actualY <= 4685)//castle at starting point
		&& !(actualX >= 2637 && actualX <= 2638 && actualY >= 4691 && actualY <= 4692)//castle at starting point
		&& !(actualX == 2635 && actualY == 4693) && !(actualX == 2634 && actualY == 4693)
		) {*/
			if(lowMem && (tileSettings[0][l][i] & 2) == 0)
			{
				if((tileSettings[k][l][i] & 0x10) != 0)
					return;
				if(getEffectivePlane(i, k, l) != currentPlane)
					return;
			}
			if(k < minimumPlane)
				minimumPlane = k;
			int k1 = tileHeights[k][l][i];
			int l1 = tileHeights[k][l + 1][i];
			int i2 = tileHeights[k][l + 1][i + 1];
			int j2 = tileHeights[k][l][i + 1];
			int k2 = k1 + l1 + i2 + j2 >> 2;
			ObjectDef class46 = ObjectDef.forID(i1);
			int l2 = l + (i << 7) + (i1 << 14) + 0x40000000;
			if(!class46.hasActions)
				l2 += 0x80000000;
			byte byte0 = (byte)((j1 << 6) + j);
			if(j == 22)
			{
				if(lowMem && !class46.hasActions && !class46.obstructsGround)
					return;
				Object obj;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj = class46.getObjectModel(22, j1, k1, l1, i2, j2, -1);
				else
					obj = new Animable_Sub5(i1, j1, 22, l1, i2, k1, j2, class46.animationId, true);
				worldController.addGroundDecoration(k, k2, i, ((Animable) (obj)), byte0, l2, l);
				if(class46.blocksProjectile && class46.hasActions && collisionMap != null)
					collisionMap.markBlocked(i, l);
				return;
			}
			if(j == 10 || j == 11)
			{
				Object obj1;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj1 = class46.getObjectModel(10, j1, k1, l1, i2, j2, -1);
				else
					obj1 = new Animable_Sub5(i1, j1, 10, l1, i2, k1, j2, class46.animationId, true);
				if(obj1 != null)
				{
					int i5 = 0;
					if(j == 11)
						i5 += 256;
					int j4;
					int l4;
					if(j1 == 1 || j1 == 3)
					{
						j4 = class46.sizeY;
						l4 = class46.sizeX;
					} else
					{
						j4 = class46.sizeX;
						l4 = class46.sizeY;
					}
					if(worldController.addInteractiveObject(l2, byte0, k2, l4, ((Animable) (obj1)), j4, k, i5, i, l) && class46.castsShadow)
					{
						Model model;
						if(obj1 instanceof Model)
							model = (Model)obj1;
						else
							model = class46.getObjectModel(10, j1, k1, l1, i2, j2, -1);
						if(model != null)
						{
							for(int j5 = 0; j5 <= j4; j5++)
							{
								for(int k5 = 0; k5 <= l4; k5++)
								{
									int l5 = model.boundsXZRadius / 4;
									if(l5 > 30)
										l5 = 30;
									if(l5 > tileShadowMap[k][l + j5][i + k5])
										tileShadowMap[k][l + j5][i + k5] = (byte)l5;
								}

							}

						}
					}
				}
				if(class46.blocksProjectile && collisionMap != null)
					collisionMap.addObjectFlags(class46.impenetrable, class46.sizeX, class46.sizeY, l, i, j1);
				return;
			}
			if(j >= 12)
			{
				Object obj2;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj2 = class46.getObjectModel(j, j1, k1, l1, i2, j2, -1);
				else
					obj2 = new Animable_Sub5(i1, j1, j, l1, i2, k1, j2, class46.animationId, true);
				worldController.addInteractiveObject(l2, byte0, k2, 1, ((Animable) (obj2)), 1, k, 0, i, l);
				if(j >= 12 && j <= 17 && j != 13 && k > 0)
					tileLightIntensity[k][l][i] |= 0x924;
				if(class46.blocksProjectile && collisionMap != null)
					collisionMap.addObjectFlags(class46.impenetrable, class46.sizeX, class46.sizeY, l, i, j1);
				return;
			}
			if(j == 0)
			{
				Object obj3;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj3 = class46.getObjectModel(0, j1, k1, l1, i2, j2, -1);
				else
					obj3 = new Animable_Sub5(i1, j1, 0, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallObject(WALL_ROTATION_FLAGS[j1], ((Animable) (obj3)), l2, i, byte0, l, null, k2, 0, k);
				if(j1 == 0)
				{
					if(class46.castsShadow)
					{
						tileShadowMap[k][l][i] = 50;
						tileShadowMap[k][l][i + 1] = 50;
					}
					if(class46.occlude)
						tileLightIntensity[k][l][i] |= 0x249;
				} else
				if(j1 == 1)
				{
					if(class46.castsShadow)
					{
						tileShadowMap[k][l][i + 1] = 50;
						tileShadowMap[k][l + 1][i + 1] = 50;
					}
					if(class46.occlude)
						tileLightIntensity[k][l][i + 1] |= 0x492;
				} else
				if(j1 == 2)
				{
					if(class46.castsShadow)
					{
						tileShadowMap[k][l + 1][i] = 50;
						tileShadowMap[k][l + 1][i + 1] = 50;
					}
					if(class46.occlude)
						tileLightIntensity[k][l + 1][i] |= 0x249;
				} else
				if(j1 == 3)
				{
					if(class46.castsShadow)
					{
						tileShadowMap[k][l][i] = 50;
						tileShadowMap[k][l + 1][i] = 50;
					}
					if(class46.occlude)
						tileLightIntensity[k][l][i] |= 0x492;
				}
				if(class46.blocksProjectile && collisionMap != null)
					collisionMap.addWallFlags(i, j1, l, j, class46.impenetrable);
				if(class46.decorDisplacement != 16)
					worldController.scaleWallDecoration(i, class46.decorDisplacement, l, k);
				return;
			}
			if(j == 1)
			{
				Object obj4;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj4 = class46.getObjectModel(1, j1, k1, l1, i2, j2, -1);
				else
					obj4 = new Animable_Sub5(i1, j1, 1, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallObject(WALL_TYPE_FLAGS[j1], ((Animable) (obj4)), l2, i, byte0, l, null, k2, 0, k);
				if(class46.castsShadow)
					if(j1 == 0)
						tileShadowMap[k][l][i + 1] = 50;
					else
					if(j1 == 1)
						tileShadowMap[k][l + 1][i + 1] = 50;
					else
					if(j1 == 2)
						tileShadowMap[k][l + 1][i] = 50;
					else
					if(j1 == 3)
						tileShadowMap[k][l][i] = 50;
				if(class46.blocksProjectile && collisionMap != null)
					collisionMap.addWallFlags(i, j1, l, j, class46.impenetrable);
				return;
			}
			if(j == 2)
			{
				int i3 = j1 + 1 & 3;
				Object obj11;
				Object obj12;
				if(class46.animationId == -1 && class46.childrenIDs == null)
				{
					obj11 = class46.getObjectModel(2, 4 + j1, k1, l1, i2, j2, -1);
					obj12 = class46.getObjectModel(2, i3, k1, l1, i2, j2, -1);
				} else
				{
					obj11 = new Animable_Sub5(i1, 4 + j1, 2, l1, i2, k1, j2, class46.animationId, true);
					obj12 = new Animable_Sub5(i1, i3, 2, l1, i2, k1, j2, class46.animationId, true);
				}
				worldController.addWallObject(WALL_ROTATION_FLAGS[j1], ((Animable) (obj11)), l2, i, byte0, l, ((Animable) (obj12)), k2, WALL_ROTATION_FLAGS[i3], k);
				if(class46.occlude)
					if(j1 == 0)
					{
						tileLightIntensity[k][l][i] |= 0x249;
						tileLightIntensity[k][l][i + 1] |= 0x492;
					} else
					if(j1 == 1)
					{
						tileLightIntensity[k][l][i + 1] |= 0x492;
						tileLightIntensity[k][l + 1][i] |= 0x249;
					} else
					if(j1 == 2)
					{
						tileLightIntensity[k][l + 1][i] |= 0x249;
						tileLightIntensity[k][l][i] |= 0x492;
					} else
					if(j1 == 3)
					{
						tileLightIntensity[k][l][i] |= 0x492;
						tileLightIntensity[k][l][i] |= 0x249;
					}
				if(class46.blocksProjectile && collisionMap != null)
					collisionMap.addWallFlags(i, j1, l, j, class46.impenetrable);
				if(class46.decorDisplacement != 16)
					worldController.scaleWallDecoration(i, class46.decorDisplacement, l, k);
				return;
			}
			if(j == 3)
			{
				Object obj5;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj5 = class46.getObjectModel(3, j1, k1, l1, i2, j2, -1);
				else
					obj5 = new Animable_Sub5(i1, j1, 3, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallObject(WALL_TYPE_FLAGS[j1], ((Animable) (obj5)), l2, i, byte0, l, null, k2, 0, k);
				if(class46.castsShadow)
					if(j1 == 0)
						tileShadowMap[k][l][i + 1] = 50;
					else
					if(j1 == 1)
						tileShadowMap[k][l + 1][i + 1] = 50;
					else
					if(j1 == 2)
						tileShadowMap[k][l + 1][i] = 50;
					else
					if(j1 == 3)
						tileShadowMap[k][l][i] = 50;
				if(class46.blocksProjectile && collisionMap != null)
					collisionMap.addWallFlags(i, j1, l, j, class46.impenetrable);
				return;
			}
			if(j == 9)
			{
				Object obj6;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj6 = class46.getObjectModel(j, j1, k1, l1, i2, j2, -1);
				else
					obj6 = new Animable_Sub5(i1, j1, j, l1, i2, k1, j2, class46.animationId, true);
				worldController.addInteractiveObject(l2, byte0, k2, 1, ((Animable) (obj6)), 1, k, 0, i, l);
				if(class46.blocksProjectile && collisionMap != null)
					collisionMap.addObjectFlags(class46.impenetrable, class46.sizeX, class46.sizeY, l, i, j1);
				return;
			}
			if(class46.contouredGround)
				if(j1 == 1)
				{
					int j3 = j2;
					j2 = i2;
					i2 = l1;
					l1 = k1;
					k1 = j3;
				} else
				if(j1 == 2)
				{
					int k3 = j2;
					j2 = l1;
					l1 = k3;
					k3 = i2;
					i2 = k1;
					k1 = k3;
				} else
				if(j1 == 3)
				{
					int l3 = j2;
					j2 = k1;
					k1 = l1;
					l1 = i2;
					i2 = l3;
				}
			if(j == 4)
			{
				Object obj7;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj7 = class46.getObjectModel(4, 0, k1, l1, i2, j2, -1);
				else
					obj7 = new Animable_Sub5(i1, 0, 4, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallDecoration(l2, i, j1 * 512, k, 0, k2, ((Animable) (obj7)), l, byte0, 0, WALL_ROTATION_FLAGS[j1]);
				return;
			}
			if(j == 5)
			{
				int i4 = 16;
				int k4 = worldController.getWallObjectUID(k, l, i);
				if(k4 > 0)
					i4 = ObjectDef.forID(k4 >> 14 & 0x7fff).decorDisplacement;
				Object obj13;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj13 = class46.getObjectModel(4, 0, k1, l1, i2, j2, -1);
				else
					obj13 = new Animable_Sub5(i1, 0, 4, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallDecoration(l2, i, j1 * 512, k, DIRECTION_OFFSET_X[j1] * i4, k2, ((Animable) (obj13)), l, byte0, DIRECTION_OFFSET_Y[j1] * i4, WALL_ROTATION_FLAGS[j1]);
				return;
			}
			if(j == 6)
			{
				Object obj8;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj8 = class46.getObjectModel(4, 0, k1, l1, i2, j2, -1);
				else
					obj8 = new Animable_Sub5(i1, 0, 4, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallDecoration(l2, i, j1, k, 0, k2, ((Animable) (obj8)), l, byte0, 0, 256);
				return;
			}
			if(j == 7)
			{
				Object obj9;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj9 = class46.getObjectModel(4, 0, k1, l1, i2, j2, -1);
				else
					obj9 = new Animable_Sub5(i1, 0, 4, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallDecoration(l2, i, j1, k, 0, k2, ((Animable) (obj9)), l, byte0, 0, 512);
				return;
			}
			if(j == 8)
			{
				Object obj10;
				if(class46.animationId == -1 && class46.childrenIDs == null)
					obj10 = class46.getObjectModel(4, 0, k1, l1, i2, j2, -1);
				else
					obj10 = new Animable_Sub5(i1, 0, 4, l1, i2, k1, j2, class46.animationId, true);
				worldController.addWallDecoration(l2, i, j1, k, 0, k2, ((Animable) (obj10)), l, byte0, 0, 768);
			}
		//}
	}

	public static client clientInstance;

	private static int interpolatedNoise(int i, int j, int k)
	{
		int l = i / k;
		int i1 = i & k - 1;
		int j1 = j / k;
		int k1 = j & k - 1;
		int l1 = smoothNoise(l, j1);
		int i2 = smoothNoise(l + 1, j1);
		int j2 = smoothNoise(l, j1 + 1);
		int k2 = smoothNoise(l + 1, j1 + 1);
		int l2 = interpolate(l1, i2, i1, k);
		int i3 = interpolate(j2, k2, i1, k);
		return interpolate(l2, i3, k1, k);
	}

	private int packHSL(int i, int j, int k)
	{
		if(k > 179)
			j /= 2;
		if(k > 192)
			j /= 2;
		if(k > 217)
			j /= 2;
		if(k > 243)
			j /= 2;
		return (i / 4 << 10) + (j / 32 << 7) + k / 2;
	}

	public static boolean objectHasActions(int i, int j)
	{
		ObjectDef class46 = ObjectDef.forID(i);
		if(j == 11)
			j = 10;
		if(j >= 5 && j <= 8)
			j = 4;
		return class46.hasModelType(j);
	}

	public final void parseInstancedLandscape(int i, int j, CollisionMap acollisionMap[], int l, int i1, byte abyte0[],
								int j1, int k1, int l1)
	{
		for(int i2 = 0; i2 < 8; i2++)
		{
			for(int j2 = 0; j2 < 8; j2++)
				if(l + i2 > 0 && l + i2 < 103 && l1 + j2 > 0 && l1 + j2 < 103)
					acollisionMap[k1].flags[l + i2][l1 + j2] &= 0xfeffffff;

		}
		Stream stream = new Stream(abyte0);
		for(int l2 = 0; l2 < 4; l2++)
		{
			for(int i3 = 0; i3 < 64; i3++)
			{
				for(int j3 = 0; j3 < 64; j3++)
					if(l2 == i && i3 >= i1 && i3 < i1 + 8 && j3 >= j1 && j3 < j1 + 8)
						parseTileData(l1 + RotationUtil.rotateLengthCoord(j3 & 7, j, i3 & 7), 0, stream, l + RotationUtil.rotateWidthCoord(j, j3 & 7, i3 & 7), k1, j, 0);
					else
						parseTileData(-1, 0, stream, -1, 0, 0, 0);

			}

		}

	}

	public final void parseLandscape(byte abyte0[], int i, int j, int k, int l, CollisionMap acollisionMap[])
	{
		for(int i1 = 0; i1 < 4; i1++)
		{
			for(int j1 = 0; j1 < 64; j1++)
			{
				for(int k1 = 0; k1 < 64; k1++)
					if(j + j1 > 0 && j + j1 < 103 && i + k1 > 0 && i + k1 < 103)
						acollisionMap[i1].flags[j + j1][i + k1] &= 0xfeffffff;

			}

		}

		Stream stream = new Stream(abyte0);
		for(int l1 = 0; l1 < 4; l1++)
		{
			for(int i2 = 0; i2 < 64; i2++)
			{
				for(int j2 = 0; j2 < 64; j2++)
					parseTileData(j2 + i, l, stream, i2 + j, l1, 0, k);

			}

		}
	}

	private void parseTileData(int i, int j, Stream stream, int k, int l, int i1,
								 int k1)
	{
		if(k >= 0 && k < 104 && i >= 0 && i < 104)
		{
			tileSettings[l][k][i] = 0;
			do
			{
				int l1 = stream.readUnsignedByte();
				if(l1 == 0)
					if(l == 0)
					{
						tileHeights[0][k][i] = -calculateNoise(0xe3b7b + k + k1, 0x87cce + i + j) * 8;
						return;
					} else
					{
						tileHeights[l][k][i] = tileHeights[l - 1][k][i] - 240;
						return;
					}
				if(l1 == 1)
				{
					int j2 = stream.readUnsignedByte();
					if(j2 == 1)
						j2 = 0;
					if(l == 0)
					{
						tileHeights[0][k][i] = -j2 * 8;
						return;
					} else
					{
						tileHeights[l][k][i] = tileHeights[l - 1][k][i] - j2 * 8;
						return;
					}
				}
				if(l1 <= 49)
				{
					overlayFloorIds[l][k][i] = stream.readSignedByte();
					overlayShapes[l][k][i] = (byte)((l1 - 2) / 4);
					overlayOrientations[l][k][i] = (byte)((l1 - 2) + i1 & 3);
				} else
				if(l1 <= 81)
					tileSettings[l][k][i] = (byte)(l1 - 49);
				else
					underlayFloorIds[l][k][i] = (byte)(l1 - 81);
			} while(true);
		}
		do
		{
			int i2 = stream.readUnsignedByte();
			if(i2 == 0)
				break;
			if(i2 == 1)
			{
				stream.readUnsignedByte();
				return;
			}
			if(i2 <= 49)
				stream.readUnsignedByte();
		} while(true);
	}

	private int getEffectivePlane(int i, int j, int k)
	{
		if((tileSettings[j][k][i] & 8) != 0)
			return 0;
		if(j > 0 && (tileSettings[1][k][i] & 2) != 0)
			return j - 1;
		else
			return j;
	}

	public final void parseObjectLocations(CollisionMap acollisionMap[], WorldController worldController, int i, int j, int k, int l,
								byte abyte0[], int i1, int j1, int k1)
	{
label0:
		{
			Stream stream = new Stream(abyte0);
			int l1 = -1;
			do
			{
				int i2 = stream.readSmart();
				if(i2 == 0)
					break label0;
				l1 += i2;
				int j2 = 0;
				do
				{
					int k2 = stream.readSmart();
					if(k2 == 0)
						break;
					j2 += k2 - 1;
					int l2 = j2 & 0x3f;
					int i3 = j2 >> 6 & 0x3f;
					int j3 = j2 >> 12;
					int k3 = stream.readUnsignedByte();
					int l3 = k3 >> 2;
					int i4 = k3 & 3;
					if(j3 == i && i3 >= i1 && i3 < i1 + 8 && l2 >= k && l2 < k + 8)
					{
						ObjectDef class46 = ObjectDef.forID(l1);
						int j4 = j + RotationUtil.rotateObjectX(j1, class46.sizeY, i3 & 7, l2 & 7, class46.sizeX);
						int k4 = k1 + RotationUtil.rotateObjectY(l2 & 7, class46.sizeY, j1, class46.sizeX, i3 & 7);
						if(j4 > 0 && k4 > 0 && j4 < 103 && k4 < 103)
						{
							int l4 = j3;
							if((tileSettings[1][j4][k4] & 2) == 2)
								l4--;
							CollisionMap collisionMap = null;
							if(l4 >= 0)
								collisionMap = acollisionMap[l4];
							placeObject(k4, worldController, collisionMap, l3, l, j4, l1, i4 + j1 & 3);
						}
					}
				} while(true);
			} while(true);
		}
	}

	private static int interpolate(int i, int j, int k, int l)
	{
		int i1 = 0x10000 - Texture.COSINE[(k * 1024) / l] >> 1;
		return (i * (0x10000 - i1) >> 16) + (j * i1 >> 16);
	}

	private int adjustOverlayLight(int i, int j)
	{
		if(i == -2)
			return 0xbc614e;
		if(i == -1)
		{
			if(j < 0)
				j = 0;
			else
			if(j > 127)
				j = 127;
			j = 127 - j;
			return j;
		}
		j = (j * (i & 0x7f)) / 128;
		if(j < 2)
			j = 2;
		else
		if(j > 126)
			j = 126;
		return (i & 0xff80) + j;
	}

	private static int smoothNoise(int i, int j)
	{
		int k = noiseHash(i - 1, j - 1) + noiseHash(i + 1, j - 1) + noiseHash(i - 1, j + 1) + noiseHash(i + 1, j + 1);
		int l = noiseHash(i - 1, j) + noiseHash(i + 1, j) + noiseHash(i, j - 1) + noiseHash(i, j + 1);
		int i1 = noiseHash(i, j);
		return k / 16 + l / 8 + i1 / 4;
	}

	private static int adjustUnderlayLight(int i, int j)
	{
		if(i == -1)
			return 0xbc614e;
		j = (j * (i & 0x7f)) / 128;
		if(j < 2)
			j = 2;
		else
		if(j > 126)
			j = 126;
		return (i & 0xff80) + j;
	}

	public static void placeObjectStatic(WorldController worldController, int i, int j, int k, int l, CollisionMap collisionMap, int ai[][][], int i1,
								 int j1, int k1)
	{
		int l1 = ai[l][i1][j];
		int i2 = ai[l][i1 + 1][j];
		int j2 = ai[l][i1 + 1][j + 1];
		int k2 = ai[l][i1][j + 1];
		int l2 = l1 + i2 + j2 + k2 >> 2;
		ObjectDef class46 = ObjectDef.forID(j1);
		int i3 = i1 + (j << 7) + (j1 << 14) + 0x40000000;
		if(!class46.hasActions)
			i3 += 0x80000000;
		byte byte1 = (byte)((i << 6) + k);
		if(k == 22)
		{
			Object obj;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj = class46.getObjectModel(22, i, l1, i2, j2, k2, -1);
			else
				obj = new Animable_Sub5(j1, i, 22, i2, j2, l1, k2, class46.animationId, true);
			worldController.addGroundDecoration(k1, l2, j, ((Animable) (obj)), byte1, i3, i1);
			if(class46.blocksProjectile && class46.hasActions)
				collisionMap.markBlocked(j, i1);
			return;
		}
		if(k == 10 || k == 11)
		{
			Object obj1;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj1 = class46.getObjectModel(10, i, l1, i2, j2, k2, -1);
			else
				obj1 = new Animable_Sub5(j1, i, 10, i2, j2, l1, k2, class46.animationId, true);
			if(obj1 != null)
			{
				int j5 = 0;
				if(k == 11)
					j5 += 256;
				int k4;
				int i5;
				if(i == 1 || i == 3)
				{
					k4 = class46.sizeY;
					i5 = class46.sizeX;
				} else
				{
					k4 = class46.sizeX;
					i5 = class46.sizeY;
				}
				worldController.addInteractiveObject(i3, byte1, l2, i5, ((Animable) (obj1)), k4, k1, j5, j, i1);
			}
			if(class46.blocksProjectile)
				collisionMap.addObjectFlags(class46.impenetrable, class46.sizeX, class46.sizeY, i1, j, i);
			return;
		}
		if(k >= 12)
		{
			Object obj2;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj2 = class46.getObjectModel(k, i, l1, i2, j2, k2, -1);
			else
				obj2 = new Animable_Sub5(j1, i, k, i2, j2, l1, k2, class46.animationId, true);
			worldController.addInteractiveObject(i3, byte1, l2, 1, ((Animable) (obj2)), 1, k1, 0, j, i1);
			if(class46.blocksProjectile)
				collisionMap.addObjectFlags(class46.impenetrable, class46.sizeX, class46.sizeY, i1, j, i);
			return;
		}
		if(k == 0)
		{
			Object obj3;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj3 = class46.getObjectModel(0, i, l1, i2, j2, k2, -1);
			else
				obj3 = new Animable_Sub5(j1, i, 0, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallObject(WALL_ROTATION_FLAGS[i], ((Animable) (obj3)), i3, j, byte1, i1, null, l2, 0, k1);
			if(class46.blocksProjectile)
				collisionMap.addWallFlags(j, i, i1, k, class46.impenetrable);
			return;
		}
		if(k == 1)
		{
			Object obj4;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj4 = class46.getObjectModel(1, i, l1, i2, j2, k2, -1);
			else
				obj4 = new Animable_Sub5(j1, i, 1, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallObject(WALL_TYPE_FLAGS[i], ((Animable) (obj4)), i3, j, byte1, i1, null, l2, 0, k1);
			if(class46.blocksProjectile)
				collisionMap.addWallFlags(j, i, i1, k, class46.impenetrable);
			return;
		}
		if(k == 2)
		{
			int j3 = i + 1 & 3;
			Object obj11;
			Object obj12;
			if(class46.animationId == -1 && class46.childrenIDs == null)
			{
				obj11 = class46.getObjectModel(2, 4 + i, l1, i2, j2, k2, -1);
				obj12 = class46.getObjectModel(2, j3, l1, i2, j2, k2, -1);
			} else
			{
				obj11 = new Animable_Sub5(j1, 4 + i, 2, i2, j2, l1, k2, class46.animationId, true);
				obj12 = new Animable_Sub5(j1, j3, 2, i2, j2, l1, k2, class46.animationId, true);
			}
			worldController.addWallObject(WALL_ROTATION_FLAGS[i], ((Animable) (obj11)), i3, j, byte1, i1, ((Animable) (obj12)), l2, WALL_ROTATION_FLAGS[j3], k1);
			if(class46.blocksProjectile)
				collisionMap.addWallFlags(j, i, i1, k, class46.impenetrable);
			return;
		}
		if(k == 3)
		{
			Object obj5;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj5 = class46.getObjectModel(3, i, l1, i2, j2, k2, -1);
			else
				obj5 = new Animable_Sub5(j1, i, 3, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallObject(WALL_TYPE_FLAGS[i], ((Animable) (obj5)), i3, j, byte1, i1, null, l2, 0, k1);
			if(class46.blocksProjectile)
				collisionMap.addWallFlags(j, i, i1, k, class46.impenetrable);
			return;
		}
		if(k == 9)
		{
			Object obj6;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj6 = class46.getObjectModel(k, i, l1, i2, j2, k2, -1);
			else
				obj6 = new Animable_Sub5(j1, i, k, i2, j2, l1, k2, class46.animationId, true);
			worldController.addInteractiveObject(i3, byte1, l2, 1, ((Animable) (obj6)), 1, k1, 0, j, i1);
			if(class46.blocksProjectile)
				collisionMap.addObjectFlags(class46.impenetrable, class46.sizeX, class46.sizeY, i1, j, i);
			return;
		}
		if(class46.contouredGround)
			if(i == 1)
			{
				int k3 = k2;
				k2 = j2;
				j2 = i2;
				i2 = l1;
				l1 = k3;
			} else
			if(i == 2)
			{
				int l3 = k2;
				k2 = i2;
				i2 = l3;
				l3 = j2;
				j2 = l1;
				l1 = l3;
			} else
			if(i == 3)
			{
				int i4 = k2;
				k2 = l1;
				l1 = i2;
				i2 = j2;
				j2 = i4;
			}
		if(k == 4)
		{
			Object obj7;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj7 = class46.getObjectModel(4, 0, l1, i2, j2, k2, -1);
			else
				obj7 = new Animable_Sub5(j1, 0, 4, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallDecoration(i3, j, i * 512, k1, 0, l2, ((Animable) (obj7)), i1, byte1, 0, WALL_ROTATION_FLAGS[i]);
			return;
		}
		if(k == 5)
		{
			int j4 = 16;
			int l4 = worldController.getWallObjectUID(k1, i1, j);
			if(l4 > 0)
				j4 = ObjectDef.forID(l4 >> 14 & 0x7fff).decorDisplacement;
			Object obj13;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj13 = class46.getObjectModel(4, 0, l1, i2, j2, k2, -1);
			else
				obj13 = new Animable_Sub5(j1, 0, 4, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallDecoration(i3, j, i * 512, k1, DIRECTION_OFFSET_X[i] * j4, l2, ((Animable) (obj13)), i1, byte1, DIRECTION_OFFSET_Y[i] * j4, WALL_ROTATION_FLAGS[i]);
			return;
		}
		if(k == 6)
		{
			Object obj8;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj8 = class46.getObjectModel(4, 0, l1, i2, j2, k2, -1);
			else
				obj8 = new Animable_Sub5(j1, 0, 4, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallDecoration(i3, j, i, k1, 0, l2, ((Animable) (obj8)), i1, byte1, 0, 256);
			return;
		}
		if(k == 7)
		{
			Object obj9;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj9 = class46.getObjectModel(4, 0, l1, i2, j2, k2, -1);
			else
				obj9 = new Animable_Sub5(j1, 0, 4, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallDecoration(i3, j, i, k1, 0, l2, ((Animable) (obj9)), i1, byte1, 0, 512);
			return;
		}
		if(k == 8)
		{
			Object obj10;
			if(class46.animationId == -1 && class46.childrenIDs == null)
				obj10 = class46.getObjectModel(4, 0, l1, i2, j2, k2, -1);
			else
				obj10 = new Animable_Sub5(j1, 0, 4, i2, j2, l1, k2, class46.animationId, true);
			worldController.addWallDecoration(i3, j, i, k1, 0, l2, ((Animable) (obj10)), i1, byte1, 0, 768);
		}
	}

  public static boolean checkObjectData(int i, byte[] is, int i_250_
  ) //xxx bad method, decompiled with JODE
  {
	boolean bool = true;
	Stream stream = new Stream(is);
	int i_252_ = -1;
	for (;;)
	  {
	int i_253_ = stream.readSmart ();
	if (i_253_ == 0)
	  break;
	i_252_ += i_253_;
	int i_254_ = 0;
	boolean bool_255_ = false;
	for (;;)
	  {
		if (bool_255_)
		  {
		int i_256_ = stream.readSmart ();
		if (i_256_ == 0)
		  break;
		stream.readUnsignedByte();
		  }
		else
		  {
		int i_257_ = stream.readSmart ();
		if (i_257_ == 0)
		  break;
		i_254_ += i_257_ - 1;
		int i_258_ = i_254_ & 0x3f;
		int i_259_ = i_254_ >> 6 & 0x3f;
		int i_260_ = stream.readUnsignedByte() >> 2;
		int i_261_ = i_259_ + i;
		int i_262_ = i_258_ + i_250_;
		if (i_261_ > 0 && i_262_ > 0 && i_261_ < 103 && i_262_ < 103)
		  {
			ObjectDef class46 = ObjectDef.forID (i_252_);
			if (i_260_ != 22 || !lowMem || class46.hasActions
					|| class46.obstructsGround)
			  {
			bool &= class46.modelsReady ();
			bool_255_ = true;
			  }
		  }
		  }
	  }
	  }
	return bool;
  }

	public final void parseLocalObjectLocations(int i, CollisionMap acollisionMap[], int j, WorldController worldController, byte abyte0[])
	{
label0:
		{
			Stream stream = new Stream(abyte0);
			int l = -1;
			do
			{
				int i1 = stream.readSmart();
				if(i1 == 0)
					break label0;
				l += i1;
				int j1 = 0;
				do
				{
					int k1 = stream.readSmart();
					if(k1 == 0)
						break;
					j1 += k1 - 1;
					int l1 = j1 & 0x3f;
					int i2 = j1 >> 6 & 0x3f;
					int j2 = j1 >> 12;
					int k2 = stream.readUnsignedByte();
					int l2 = k2 >> 2;
					int i3 = k2 & 3;
					int j3 = i2 + i;
					int k3 = l1 + j;
					if(j3 > 0 && k3 > 0 && j3 < 103 && k3 < 103)
					{
						int l3 = j2;
						if((tileSettings[1][j3][k3] & 2) == 2)
							l3--;
						CollisionMap collisionMap = null;
						if(l3 >= 0)
							collisionMap = acollisionMap[l3];
						placeObject(k3, worldController, collisionMap, l2, j2, j3, l, i3);
					}
				} while(true);
			} while(true);
		}
	}

	private static int hueRandomizer = (int)(Math.random() * 17D) - 8;
	private final int[] blendHue;
	private final int[] blendSaturation;
	private final int[] blendLightness;
	private final int[] blendHueDivisor;
	private final int[] blendDirectionCount;
	private final int[][][] tileHeights;
	private final byte[][][] overlayFloorIds;
	static int currentPlane;
	private static int lightnessRandomizer = (int)(Math.random() * 33D) - 16;
	private final byte[][][] tileShadowMap;
	private final int[][][] tileLightIntensity;
	private final byte[][][] overlayShapes;
	private static final int DIRECTION_OFFSET_X[] = {
		1, 0, -1, 0
	};
	private static final int WALL_DECO_ROT_OFFSET = 323;
	private final int[][] lightmap;
	private static final int WALL_TYPE_FLAGS[] = {
		16, 32, 64, 128
	};
	private final byte[][][] underlayFloorIds;
	private static final int DIRECTION_OFFSET_Y[] = {
		0, -1, 0, 1
	};
	static int minimumPlane = 99;
	private final int mapSizeX;
	private final int mapSizeY;
	private final byte[][][] overlayOrientations;
	private final byte[][][] tileSettings;
	static boolean lowMem = true;
	private static final int WALL_ROTATION_FLAGS[] = {
		1, 2, 4, 8
	};

}
