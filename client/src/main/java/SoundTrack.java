// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SoundTrack
{

	public static void initNoise()
	{
		oscillatorPhase = new int[32768];
		for(int i = 0; i < 32768; i++)
			if(Math.random() > 0.5D)
				oscillatorPhase[i] = 1;
			else
				oscillatorPhase[i] = -1;

		oscillatorAmplitude = new int[32768];
		for(int j = 0; j < 32768; j++)
			oscillatorAmplitude[j] = (int)(Math.sin((double)j / 5215.1903000000002D) * 16384D);

		oscillatorFrequency = new int[0x35d54];
	}

	public int[] synthesize(int i, int j)
	{
		for(int k = 0; k < i; k++)
			oscillatorFrequency[k] = 0;

		if(j < 10)
			return oscillatorFrequency;
		double d = (double)i / ((double)j + 0.0D);
		pitchEnvelope.resetValues();
		volumeEnvelope.resetValues();
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		if(pitchModEnvelope != null)
		{
			pitchModEnvelope.resetValues();
			pitchModRangeEnvelope.resetValues();
			l = (int)(((double)(pitchModEnvelope.formStart - pitchModEnvelope.formDuration) * 32.768000000000001D) / d);
			i1 = (int)(((double)pitchModEnvelope.formDuration * 32.768000000000001D) / d);
		}
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		if(gatingEnvelope != null)
		{
			gatingEnvelope.resetValues();
			gatingFreqEnvelope.resetValues();
			k1 = (int)(((double)(gatingEnvelope.formStart - gatingEnvelope.formDuration) * 32.768000000000001D) / d);
			l1 = (int)(((double)gatingEnvelope.formDuration * 32.768000000000001D) / d);
		}
		for(int j2 = 0; j2 < 5; j2++)
			if(oscillatorVolume[j2] != 0)
			{
				oscillatorSemitone[j2] = 0;
				oscillatorStart[j2] = (int)((double)oscillatorDelay[j2] * d);
				oscillatorVolumeDelta[j2] = (oscillatorVolume[j2] << 14) / 100;
				oscillatorPitchDelta[j2] = (int)(((double)(pitchEnvelope.formStart - pitchEnvelope.formDuration) * 32.768000000000001D * Math.pow(1.0057929410678534D, oscillatorPitch[j2])) / d);
				oscillatorMinDelay[j2] = (int)(((double)pitchEnvelope.formDuration * 32.768000000000001D) / d);
			}

		for(int k2 = 0; k2 < i; k2++)
		{
			int l2 = pitchEnvelope.evaluateSE(i);
			int j4 = volumeEnvelope.evaluateSE(i);
			if(pitchModEnvelope != null)
			{
				int j5 = pitchModEnvelope.evaluateSE(i);
				int j6 = pitchModRangeEnvelope.evaluateSE(i);
				l2 += evaluateWave(j6, j1, pitchModEnvelope.formEnd) >> 1;
				j1 += (j5 * l >> 16) + i1;
			}
			if(gatingEnvelope != null)
			{
				int k5 = gatingEnvelope.evaluateSE(i);
				int k6 = gatingFreqEnvelope.evaluateSE(i);
				j4 = j4 * ((evaluateWave(k6, i2, gatingEnvelope.formEnd) >> 1) + 32768) >> 15;
				i2 += (k5 * k1 >> 16) + l1;
			}
			for(int l5 = 0; l5 < 5; l5++)
				if(oscillatorVolume[l5] != 0)
				{
					int l6 = k2 + oscillatorStart[l5];
					if(l6 < i)
					{
						oscillatorFrequency[l6] += evaluateWave(j4 * oscillatorVolumeDelta[l5] >> 15, oscillatorSemitone[l5], pitchEnvelope.formEnd);
						oscillatorSemitone[l5] += (l2 * oscillatorPitchDelta[l5] >> 16) + oscillatorMinDelay[l5];
					}
				}

		}

		if(filterEnvelope != null)
		{
			filterEnvelope.resetValues();
			filterRangeEnvelope.resetValues();
			int i3 = 0;
			boolean flag = false;
			boolean flag1 = true;
			for(int i7 = 0; i7 < i; i7++)
			{
				int k7 = filterEnvelope.evaluateSE(i);
				int i8 = filterRangeEnvelope.evaluateSE(i);
				int k4;
				if(flag1)
					k4 = filterEnvelope.formDuration + ((filterEnvelope.formStart - filterEnvelope.formDuration) * k7 >> 8);
				else
					k4 = filterEnvelope.formDuration + ((filterEnvelope.formStart - filterEnvelope.formDuration) * i8 >> 8);
				if((i3 += 256) >= k4)
				{
					i3 = 0;
					flag1 = !flag1;
				}
				if(flag1)
					oscillatorFrequency[i7] = 0;
			}

		}
		if(delayTime > 0 && delayDecay > 0)
		{
			int j3 = (int)((double)delayTime * d);
			for(int l4 = j3; l4 < i; l4++)
				oscillatorFrequency[l4] += (oscillatorFrequency[l4 - j3] * delayDecay) / 100;

		}
		if(aAudioFilter_111.outputBuffer[0] > 0 || aAudioFilter_111.outputBuffer[1] > 0)
		{
			releaseEnvelope.resetValues();
			int k3 = releaseEnvelope.evaluateSE(i + 1);
			int i5 = aAudioFilter_111.computeCoefficients(0, (float)k3 / 65536F);
			int i6 = aAudioFilter_111.computeCoefficients(1, (float)k3 / 65536F);
			if(i >= i5 + i6)
			{
				int j7 = 0;
				int l7 = i6;
				if(l7 > i - i5)
					l7 = i - i5;
				for(; j7 < l7; j7++)
				{
					int j8 = (int)((long)oscillatorFrequency[j7 + i5] * (long)AudioFilter.outputSize >> 16);
					for(int k8 = 0; k8 < i5; k8++)
						j8 += (int)((long)oscillatorFrequency[(j7 + i5) - 1 - k8] * (long)AudioFilter.mixBuffer[0][k8] >> 16);

					for(int j9 = 0; j9 < j7; j9++)
						j8 -= (int)((long)oscillatorFrequency[j7 - 1 - j9] * (long)AudioFilter.mixBuffer[1][j9] >> 16);

					oscillatorFrequency[j7] = j8;
					k3 = releaseEnvelope.evaluateSE(i + 1);
				}

				char c = '\200';
				l7 = c;
				do
				{
					if(l7 > i - i5)
						l7 = i - i5;
					for(; j7 < l7; j7++)
					{
						int l8 = (int)((long)oscillatorFrequency[j7 + i5] * (long)AudioFilter.outputSize >> 16);
						for(int k9 = 0; k9 < i5; k9++)
							l8 += (int)((long)oscillatorFrequency[(j7 + i5) - 1 - k9] * (long)AudioFilter.mixBuffer[0][k9] >> 16);

						for(int i10 = 0; i10 < i6; i10++)
							l8 -= (int)((long)oscillatorFrequency[j7 - 1 - i10] * (long)AudioFilter.mixBuffer[1][i10] >> 16);

						oscillatorFrequency[j7] = l8;
						k3 = releaseEnvelope.evaluateSE(i + 1);
					}

					if(j7 >= i - i5)
						break;
					i5 = aAudioFilter_111.computeCoefficients(0, (float)k3 / 65536F);
					i6 = aAudioFilter_111.computeCoefficients(1, (float)k3 / 65536F);
					l7 += c;
				} while(true);
				for(; j7 < i; j7++)
				{
					int i9 = 0;
					for(int l9 = (j7 + i5) - i; l9 < i5; l9++)
						i9 += (int)((long)oscillatorFrequency[(j7 + i5) - 1 - l9] * (long)AudioFilter.mixBuffer[0][l9] >> 16);

					for(int j10 = 0; j10 < i6; j10++)
						i9 -= (int)((long)oscillatorFrequency[j7 - 1 - j10] * (long)AudioFilter.mixBuffer[1][j10] >> 16);

					oscillatorFrequency[j7] = i9;
					int l3 = releaseEnvelope.evaluateSE(i + 1);
				}

			}
		}
		for(int i4 = 0; i4 < i; i4++)
		{
			if(oscillatorFrequency[i4] < -32768)
				oscillatorFrequency[i4] = -32768;
			if(oscillatorFrequency[i4] > 32767)
				oscillatorFrequency[i4] = 32767;
		}

		return oscillatorFrequency;
	}

	private int evaluateWave(int i, int k, int l)
	{
		if(l == 1)
			if((k & 0x7fff) < 16384)
				return i;
			else
				return -i;
		if(l == 2)
			return oscillatorAmplitude[k & 0x7fff] * i >> 14;
		if(l == 3)
			return ((k & 0x7fff) * i >> 14) - i;
		if(l == 4)
			return oscillatorPhase[k / 2607 & 0x7fff] * i;
		else
			return 0;
	}

	public void decode(Stream stream)
	{
		pitchEnvelope = new SoundEnvelope();
		pitchEnvelope.decodeSE(stream);
		volumeEnvelope = new SoundEnvelope();
		volumeEnvelope.decodeSE(stream);
		int i = stream.readUnsignedByte();
		if(i != 0)
		{
			stream.currentOffset--;
			pitchModEnvelope = new SoundEnvelope();
			pitchModEnvelope.decodeSE(stream);
			pitchModRangeEnvelope = new SoundEnvelope();
			pitchModRangeEnvelope.decodeSE(stream);
		}
		i = stream.readUnsignedByte();
		if(i != 0)
		{
			stream.currentOffset--;
			gatingEnvelope = new SoundEnvelope();
			gatingEnvelope.decodeSE(stream);
			gatingFreqEnvelope = new SoundEnvelope();
			gatingFreqEnvelope.decodeSE(stream);
		}
		i = stream.readUnsignedByte();
		if(i != 0)
		{
			stream.currentOffset--;
			filterEnvelope = new SoundEnvelope();
			filterEnvelope.decodeSE(stream);
			filterRangeEnvelope = new SoundEnvelope();
			filterRangeEnvelope.decodeSE(stream);
		}
		for(int j = 0; j < 10; j++)
		{
			int k = stream.readSmart();
			if(k == 0)
				break;
			oscillatorVolume[j] = k;
			oscillatorPitch[j] = stream.readSmartSigned();
			oscillatorDelay[j] = stream.readSmart();
		}

		delayTime = stream.readSmart();
		delayDecay = stream.readSmart();
		duration = stream.readUnsignedWord();
		offset = stream.readUnsignedWord();
		aAudioFilter_111 = new AudioFilter();
		releaseEnvelope = new SoundEnvelope();
		aAudioFilter_111.decodeFilter(stream, releaseEnvelope);
	}

	public SoundTrack()
	{
		oscillatorVolume = new int[5];
		oscillatorPitch = new int[5];
		oscillatorDelay = new int[5];
		delayDecay = 100;
		duration = 500;
	}

	private SoundEnvelope pitchEnvelope;
	private SoundEnvelope volumeEnvelope;
	private SoundEnvelope pitchModEnvelope;
	private SoundEnvelope pitchModRangeEnvelope;
	private SoundEnvelope gatingEnvelope;
	private SoundEnvelope gatingFreqEnvelope;
	private SoundEnvelope filterEnvelope;
	private SoundEnvelope filterRangeEnvelope;
	private final int[] oscillatorVolume;
	private final int[] oscillatorPitch;
	private final int[] oscillatorDelay;
	private int delayTime;
	private int delayDecay;
	private AudioFilter aAudioFilter_111;
	private SoundEnvelope releaseEnvelope;
	int duration;
	int offset;
	private static int[] oscillatorFrequency;
	private static int[] oscillatorPhase;
	private static int[] oscillatorAmplitude;
	private static final int[] oscillatorSemitone = new int[5];
	private static final int[] oscillatorStart = new int[5];
	private static final int[] oscillatorVolumeDelta = new int[5];
	private static final int[] oscillatorPitchDelta = new int[5];
	private static final int[] oscillatorMinDelay = new int[5];

}
