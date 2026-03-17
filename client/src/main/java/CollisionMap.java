// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class CollisionMap
{

	public CollisionMap()
	{
		offsetX = 0;
		offsetY = 0;
		width = 104;
		height = 104;
		flags = new int[width][height];
		reset();
	}

	public void reset()
	{
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
				if(i == 0 || j == 0 || i == width - 1 || j == height - 1)
					flags[i][j] = 0xffffff;
				else
					flags[i][j] = 0x1000000;

		}

	}

	public void addWallFlags(int i, int j, int k, int l, boolean flag)
	{
		k -= offsetX;
		i -= offsetY;
		if(l == 0)
		{
			if(j == 0)
			{
				setFlag(k, i, 128);
				setFlag(k - 1, i, 8);
			}
			if(j == 1)
			{
				setFlag(k, i, 2);
				setFlag(k, i + 1, 32);
			}
			if(j == 2)
			{
				setFlag(k, i, 8);
				setFlag(k + 1, i, 128);
			}
			if(j == 3)
			{
				setFlag(k, i, 32);
				setFlag(k, i - 1, 2);
			}
		}
		if(l == 1 || l == 3)
		{
			if(j == 0)
			{
				setFlag(k, i, 1);
				setFlag(k - 1, i + 1, 16);
			}
			if(j == 1)
			{
				setFlag(k, i, 4);
				setFlag(k + 1, i + 1, 64);
			}
			if(j == 2)
			{
				setFlag(k, i, 16);
				setFlag(k + 1, i - 1, 1);
			}
			if(j == 3)
			{
				setFlag(k, i, 64);
				setFlag(k - 1, i - 1, 4);
			}
		}
		if(l == 2)
		{
			if(j == 0)
			{
				setFlag(k, i, 130);
				setFlag(k - 1, i, 8);
				setFlag(k, i + 1, 32);
			}
			if(j == 1)
			{
				setFlag(k, i, 10);
				setFlag(k, i + 1, 32);
				setFlag(k + 1, i, 128);
			}
			if(j == 2)
			{
				setFlag(k, i, 40);
				setFlag(k + 1, i, 128);
				setFlag(k, i - 1, 2);
			}
			if(j == 3)
			{
				setFlag(k, i, 160);
				setFlag(k, i - 1, 2);
				setFlag(k - 1, i, 8);
			}
		}
		if(flag)
		{
			if(l == 0)
			{
				if(j == 0)
				{
					setFlag(k, i, 0x10000);
					setFlag(k - 1, i, 4096);
				}
				if(j == 1)
				{
					setFlag(k, i, 1024);
					setFlag(k, i + 1, 16384);
				}
				if(j == 2)
				{
					setFlag(k, i, 4096);
					setFlag(k + 1, i, 0x10000);
				}
				if(j == 3)
				{
					setFlag(k, i, 16384);
					setFlag(k, i - 1, 1024);
				}
			}
			if(l == 1 || l == 3)
			{
				if(j == 0)
				{
					setFlag(k, i, 512);
					setFlag(k - 1, i + 1, 8192);
				}
				if(j == 1)
				{
					setFlag(k, i, 2048);
					setFlag(k + 1, i + 1, 32768);
				}
				if(j == 2)
				{
					setFlag(k, i, 8192);
					setFlag(k + 1, i - 1, 512);
				}
				if(j == 3)
				{
					setFlag(k, i, 32768);
					setFlag(k - 1, i - 1, 2048);
				}
			}
			if(l == 2)
			{
				if(j == 0)
				{
					setFlag(k, i, 0x10400);
					setFlag(k - 1, i, 4096);
					setFlag(k, i + 1, 16384);
				}
				if(j == 1)
				{
					setFlag(k, i, 5120);
					setFlag(k, i + 1, 16384);
					setFlag(k + 1, i, 0x10000);
				}
				if(j == 2)
				{
					setFlag(k, i, 20480);
					setFlag(k + 1, i, 0x10000);
					setFlag(k, i - 1, 1024);
				}
				if(j == 3)
				{
					setFlag(k, i, 0x14000);
					setFlag(k, i - 1, 1024);
					setFlag(k - 1, i, 4096);
				}
			}
		}
	}

	public void addObjectFlags(boolean flag, int j, int k, int l, int i1, int j1)
	{
		int k1 = 256;
		if(flag)
			k1 += 0x20000;
		l -= offsetX;
		i1 -= offsetY;
		if(j1 == 1 || j1 == 3)
		{
			int l1 = j;
			j = k;
			k = l1;
		}
		for(int i2 = l; i2 < l + j; i2++)
			if(i2 >= 0 && i2 < width)
			{
				for(int j2 = i1; j2 < i1 + k; j2++)
					if(j2 >= 0 && j2 < height)
						setFlag(i2, j2, k1);

			}

	}

	public void markBlocked(int i, int k)
	{
		k -= offsetX;
		i -= offsetY;
		flags[k][i] |= 0x200000;
	}

	private void setFlag(int i, int j, int k)
	{
		flags[i][j] |= k;
	}

	public void removeWallFlags(int i, int j, boolean flag, int k, int l)
	{
		k -= offsetX;
		l -= offsetY;
		if(j == 0)
		{
			if(i == 0)
			{
				clearFlag(128, k, l);
				clearFlag(8, k - 1, l);
			}
			if(i == 1)
			{
				clearFlag(2, k, l);
				clearFlag(32, k, l + 1);
			}
			if(i == 2)
			{
				clearFlag(8, k, l);
				clearFlag(128, k + 1, l);
			}
			if(i == 3)
			{
				clearFlag(32, k, l);
				clearFlag(2, k, l - 1);
			}
		}
		if(j == 1 || j == 3)
		{
			if(i == 0)
			{
				clearFlag(1, k, l);
				clearFlag(16, k - 1, l + 1);
			}
			if(i == 1)
			{
				clearFlag(4, k, l);
				clearFlag(64, k + 1, l + 1);
			}
			if(i == 2)
			{
				clearFlag(16, k, l);
				clearFlag(1, k + 1, l - 1);
			}
			if(i == 3)
			{
				clearFlag(64, k, l);
				clearFlag(4, k - 1, l - 1);
			}
		}
		if(j == 2)
		{
			if(i == 0)
			{
				clearFlag(130, k, l);
				clearFlag(8, k - 1, l);
				clearFlag(32, k, l + 1);
			}
			if(i == 1)
			{
				clearFlag(10, k, l);
				clearFlag(32, k, l + 1);
				clearFlag(128, k + 1, l);
			}
			if(i == 2)
			{
				clearFlag(40, k, l);
				clearFlag(128, k + 1, l);
				clearFlag(2, k, l - 1);
			}
			if(i == 3)
			{
				clearFlag(160, k, l);
				clearFlag(2, k, l - 1);
				clearFlag(8, k - 1, l);
			}
		}
		if(flag)
		{
			if(j == 0)
			{
				if(i == 0)
				{
					clearFlag(0x10000, k, l);
					clearFlag(4096, k - 1, l);
				}
				if(i == 1)
				{
					clearFlag(1024, k, l);
					clearFlag(16384, k, l + 1);
				}
				if(i == 2)
				{
					clearFlag(4096, k, l);
					clearFlag(0x10000, k + 1, l);
				}
				if(i == 3)
				{
					clearFlag(16384, k, l);
					clearFlag(1024, k, l - 1);
				}
			}
			if(j == 1 || j == 3)
			{
				if(i == 0)
				{
					clearFlag(512, k, l);
					clearFlag(8192, k - 1, l + 1);
				}
				if(i == 1)
				{
					clearFlag(2048, k, l);
					clearFlag(32768, k + 1, l + 1);
				}
				if(i == 2)
				{
					clearFlag(8192, k, l);
					clearFlag(512, k + 1, l - 1);
				}
				if(i == 3)
				{
					clearFlag(32768, k, l);
					clearFlag(2048, k - 1, l - 1);
				}
			}
			if(j == 2)
			{
				if(i == 0)
				{
					clearFlag(0x10400, k, l);
					clearFlag(4096, k - 1, l);
					clearFlag(16384, k, l + 1);
				}
				if(i == 1)
				{
					clearFlag(5120, k, l);
					clearFlag(16384, k, l + 1);
					clearFlag(0x10000, k + 1, l);
				}
				if(i == 2)
				{
					clearFlag(20480, k, l);
					clearFlag(0x10000, k + 1, l);
					clearFlag(1024, k, l - 1);
				}
				if(i == 3)
				{
					clearFlag(0x14000, k, l);
					clearFlag(1024, k, l - 1);
					clearFlag(4096, k - 1, l);
				}
			}
		}
	}

	public void removeObjectFlags(int i, int j, int k, int l, int i1, boolean flag)
	{
		int j1 = 256;
		if(flag)
			j1 += 0x20000;
		k -= offsetX;
		l -= offsetY;
		if(i == 1 || i == 3)
		{
			int k1 = j;
			j = i1;
			i1 = k1;
		}
		for(int l1 = k; l1 < k + j; l1++)
			if(l1 >= 0 && l1 < width)
			{
				for(int i2 = l; i2 < l + i1; i2++)
					if(i2 >= 0 && i2 < height)
						clearFlag(j1, l1, i2);

			}

	}

	private void clearFlag(int i, int j, int k)
	{
		flags[j][k] &= 0xffffff - i;
	}

	public void unmarkBlocked(int j, int k)
	{
		k -= offsetX;
		j -= offsetY;
		flags[k][j] &= 0xdfffff;
	}

	public boolean canReachWall(int i, int j, int k, int i1, int j1, int k1)
	{
		if(j == i && k == k1)
			return true;
		j -= offsetX;
		k -= offsetY;
		i -= offsetX;
		k1 -= offsetY;
		if(j1 == 0)
			if(i1 == 0)
			{
				if(j == i - 1 && k == k1)
					return true;
				if(j == i && k == k1 + 1 && (flags[j][k] & 0x1280120) == 0)
					return true;
				if(j == i && k == k1 - 1 && (flags[j][k] & 0x1280102) == 0)
					return true;
			} else
			if(i1 == 1)
			{
				if(j == i && k == k1 + 1)
					return true;
				if(j == i - 1 && k == k1 && (flags[j][k] & 0x1280108) == 0)
					return true;
				if(j == i + 1 && k == k1 && (flags[j][k] & 0x1280180) == 0)
					return true;
			} else
			if(i1 == 2)
			{
				if(j == i + 1 && k == k1)
					return true;
				if(j == i && k == k1 + 1 && (flags[j][k] & 0x1280120) == 0)
					return true;
				if(j == i && k == k1 - 1 && (flags[j][k] & 0x1280102) == 0)
					return true;
			} else
			if(i1 == 3)
			{
				if(j == i && k == k1 - 1)
					return true;
				if(j == i - 1 && k == k1 && (flags[j][k] & 0x1280108) == 0)
					return true;
				if(j == i + 1 && k == k1 && (flags[j][k] & 0x1280180) == 0)
					return true;
			}
		if(j1 == 2)
			if(i1 == 0)
			{
				if(j == i - 1 && k == k1)
					return true;
				if(j == i && k == k1 + 1)
					return true;
				if(j == i + 1 && k == k1 && (flags[j][k] & 0x1280180) == 0)
					return true;
				if(j == i && k == k1 - 1 && (flags[j][k] & 0x1280102) == 0)
					return true;
			} else
			if(i1 == 1)
			{
				if(j == i - 1 && k == k1 && (flags[j][k] & 0x1280108) == 0)
					return true;
				if(j == i && k == k1 + 1)
					return true;
				if(j == i + 1 && k == k1)
					return true;
				if(j == i && k == k1 - 1 && (flags[j][k] & 0x1280102) == 0)
					return true;
			} else
			if(i1 == 2)
			{
				if(j == i - 1 && k == k1 && (flags[j][k] & 0x1280108) == 0)
					return true;
				if(j == i && k == k1 + 1 && (flags[j][k] & 0x1280120) == 0)
					return true;
				if(j == i + 1 && k == k1)
					return true;
				if(j == i && k == k1 - 1)
					return true;
			} else
			if(i1 == 3)
			{
				if(j == i - 1 && k == k1)
					return true;
				if(j == i && k == k1 + 1 && (flags[j][k] & 0x1280120) == 0)
					return true;
				if(j == i + 1 && k == k1 && (flags[j][k] & 0x1280180) == 0)
					return true;
				if(j == i && k == k1 - 1)
					return true;
			}
		if(j1 == 9)
		{
			if(j == i && k == k1 + 1 && (flags[j][k] & 0x20) == 0)
				return true;
			if(j == i && k == k1 - 1 && (flags[j][k] & 2) == 0)
				return true;
			if(j == i - 1 && k == k1 && (flags[j][k] & 8) == 0)
				return true;
			if(j == i + 1 && k == k1 && (flags[j][k] & 0x80) == 0)
				return true;
		}
		return false;
	}

	public boolean canReachDeco(int i, int j, int k, int l, int i1, int j1)
	{
		if(j1 == i && k == j)
			return true;
		j1 -= offsetX;
		k -= offsetY;
		i -= offsetX;
		j -= offsetY;
		if(l == 6 || l == 7)
		{
			if(l == 7)
				i1 = i1 + 2 & 3;
			if(i1 == 0)
			{
				if(j1 == i + 1 && k == j && (flags[j1][k] & 0x80) == 0)
					return true;
				if(j1 == i && k == j - 1 && (flags[j1][k] & 2) == 0)
					return true;
			} else
			if(i1 == 1)
			{
				if(j1 == i - 1 && k == j && (flags[j1][k] & 8) == 0)
					return true;
				if(j1 == i && k == j - 1 && (flags[j1][k] & 2) == 0)
					return true;
			} else
			if(i1 == 2)
			{
				if(j1 == i - 1 && k == j && (flags[j1][k] & 8) == 0)
					return true;
				if(j1 == i && k == j + 1 && (flags[j1][k] & 0x20) == 0)
					return true;
			} else
			if(i1 == 3)
			{
				if(j1 == i + 1 && k == j && (flags[j1][k] & 0x80) == 0)
					return true;
				if(j1 == i && k == j + 1 && (flags[j1][k] & 0x20) == 0)
					return true;
			}
		}
		if(l == 8)
		{
			if(j1 == i && k == j + 1 && (flags[j1][k] & 0x20) == 0)
				return true;
			if(j1 == i && k == j - 1 && (flags[j1][k] & 2) == 0)
				return true;
			if(j1 == i - 1 && k == j && (flags[j1][k] & 8) == 0)
				return true;
			if(j1 == i + 1 && k == j && (flags[j1][k] & 0x80) == 0)
				return true;
		}
		return false;
	}

	public boolean canReachObject(int i, int j, int k, int l, int i1, int j1,
							 int k1)
	{
		int l1 = (j + j1) - 1;
		int i2 = (i + l) - 1;
		if(k >= j && k <= l1 && k1 >= i && k1 <= i2)
			return true;
		if(k == j - 1 && k1 >= i && k1 <= i2 && (flags[k - offsetX][k1 - offsetY] & 8) == 0 && (i1 & 8) == 0)
			return true;
		if(k == l1 + 1 && k1 >= i && k1 <= i2 && (flags[k - offsetX][k1 - offsetY] & 0x80) == 0 && (i1 & 2) == 0)
			return true;
		return k1 == i - 1 && k >= j && k <= l1 && (flags[k - offsetX][k1 - offsetY] & 2) == 0 && (i1 & 4) == 0 || k1 == i2 + 1 && k >= j && k <= l1 && (flags[k - offsetX][k1 - offsetY] & 0x20) == 0 && (i1 & 1) == 0;
	}

	private final int offsetX;
	private final int offsetY;
	private final int width;
	private final int height;
	public final int[][] flags;
}
