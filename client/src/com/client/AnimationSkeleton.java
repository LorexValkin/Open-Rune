package com.client;

public final class AnimationSkeleton {

    public AnimationSkeleton(Buffer stream) {
        int anInt341 = stream.readUnsignedWord();
        boneTypes = new int[anInt341];
        boneGroups = new int[anInt341][];
        for(int j = 0; j < anInt341; j++)
        	boneTypes[j] = stream.readUnsignedWord();
		for(int j = 0; j < anInt341; j++)
			boneGroups[j] = new int[stream.readUnsignedWord()];
        for(int j = 0; j < anInt341; j++)
			for(int l = 0; l < boneGroups[j].length; l++)
				boneGroups[j][l] = stream.readUnsignedWord();
    }

    public final int[] boneTypes;//boneTypes
    public final int[][] boneGroups;//boneGroups
}
