import java.io.*;
import java.net.*;
import sign.signlink;
import java.util.zip.*;

public final class Model extends Animable {

	public static void nullLoader() {
		aAnimTransformArray1661 = null;
		faceNearClipped = null;
		faceClippedX = null;
		projectedVertexX = null;
		projectedVertexY = null;
		projectedVertexZ = null;
		depthBuffer = null;
		screenXVertices = null;
		screenYVertices = null;
		depthFaceCount = null;
		depthFaceIndices = null;
		priorityFaceCount = null;
		priorityFaceIndices = null;
		facePriorityDepthSum = null;
		normalFaceDepth = null;
		priorityDepthSum = null;
		SINE = null;
		COSINE = null;
		HSL_TO_RGB = null;
		LIGHT_DECAY = null;
	}

	private static byte[] readModelFile(int Index) {
		try {
			File Model = new File("./Models/"+Index+".dat");
			byte[] aByte = new byte[(int)Model.length()];
			if (aByte != null && aByte.length > 0) {
				FileInputStream Fis = new FileInputStream(Model);
				Fis.read(aByte);
				Fis.close();
				return aByte;
			} else {
				System.out.println("Unable To Find Model "+Index);
				return null;
			}
		} catch(Exception e) {
			System.out.println("Error Reading Model");
			return null;
		}
	}

	public static void initModelStorage(int i, OnDemandFetcherParent onDemandFetcherParent) {
		aAnimTransformArray1661 = new AnimTransform[i + 400000];
		aOnDemandFetcherParent_1662 = onDemandFetcherParent;
	}

	public static void decodeModelHeader(byte[] abyte0, int j) {
        boolean newFormat = abyte0[abyte0.length - 1] == -1 && abyte0[abyte0.length - 2] == -1;
        if (abyte0 == null) {
            AnimTransform animTransform = aAnimTransformArray1661[j] = new AnimTransform();
            animTransform.vertexCount = 0;
            animTransform.faceCount = 0;
            animTransform.texturedFaceCount = 0;
            return;
        }
        Stream class30_sub2_sub2 = new Stream(abyte0);
        class30_sub2_sub2.currentOffset = abyte0.length - (!newFormat ? 18 : 23);
        AnimTransform animTransform_1 = aAnimTransformArray1661[j] = new AnimTransform();
        animTransform_1.rawData = abyte0;
        animTransform_1.vertexCount = class30_sub2_sub2.readUnsignedWord();
        animTransform_1.faceCount = class30_sub2_sub2.readUnsignedWord();
        animTransform_1.texturedFaceCount = class30_sub2_sub2.readUnsignedByte();
        int k = class30_sub2_sub2.readUnsignedByte();
        int l = class30_sub2_sub2.readUnsignedByte();
        int i1 = class30_sub2_sub2.readUnsignedByte();
        int j1 = class30_sub2_sub2.readUnsignedByte();
        int k1 = class30_sub2_sub2.readUnsignedByte();
        if (newFormat) {
            int ignore = class30_sub2_sub2.readUnsignedByte();
        }
        int l1 = class30_sub2_sub2.readUnsignedWord();
        int i2 = class30_sub2_sub2.readUnsignedWord();
        int j2 = class30_sub2_sub2.readUnsignedWord();
        int k2 = class30_sub2_sub2.readUnsignedWord();
        if (newFormat) {
            int ignore = class30_sub2_sub2.readUnsignedWord();
        }
        int l2 = 0;
        animTransform_1.vertexFlagsOffset = l2;
        l2 += animTransform_1.vertexCount;
        animTransform_1.faceTypesOffset = l2;
        l2 += animTransform_1.faceCount;
        animTransform_1.facePrioritiesOffset = l2;
        if(l == 255)
            l2 += animTransform_1.faceCount;
        else
            animTransform_1.facePrioritiesOffset = -l - 1;
        animTransform_1.faceLabelsOffset = l2;
        if(j1 == 1)
            l2 += animTransform_1.faceCount;
        else
            animTransform_1.faceLabelsOffset = -1;
        animTransform_1.faceTexturesOffset = l2;
        if(k == 1)
            l2 += animTransform_1.faceCount;
        else
            animTransform_1.faceTexturesOffset = -1;
        animTransform_1.vertexLabelsOffset = l2;
        if(k1 == 1)
            l2 += animTransform_1.vertexCount;
        else
            animTransform_1.vertexLabelsOffset = -1;
        animTransform_1.faceAlphasOffset = l2;
        if(i1 == 1)
            l2 += animTransform_1.faceCount;
        else
            animTransform_1.faceAlphasOffset = -1;
        animTransform_1.faceVerticesOffset = l2;
        l2 += k2;
        animTransform_1.faceColorsOffset = l2;
        l2 += animTransform_1.faceCount * 2;
        animTransform_1.texCoordOffset = l2;
        l2 += animTransform_1.texturedFaceCount * 6;
        animTransform_1.vertexXOffset = l2;
        l2 += l1;
        animTransform_1.vertexYOffset = l2;
        l2 += i2;
        animTransform_1.vertexZOffset = l2;
        l2 += j2;
    }

	public static void clearModel(int j) {
		aAnimTransformArray1661[j] = null;
	}

	public static Model getModel(int j) {
		if(aAnimTransformArray1661 == null)
			return null;
		AnimTransform animTransform = aAnimTransformArray1661[j];
		if(animTransform == null) {
			aOnDemandFetcherParent_1662.requestModel(j);
			return null;
		} else {
			return new Model(j);
		}
	}

	public static boolean isModelLoaded(int i) {
		if(aAnimTransformArray1661 == null)
			return false;
		AnimTransform animTransform = aAnimTransformArray1661[i];
		if(animTransform == null) {
			aOnDemandFetcherParent_1662.requestModel(i);
			return false;
		} else {
			return true;
		}
	}

	private Model()  {
		singleTile = false;
	}

	private Model(int i) {
		singleTile = false;
		AnimTransform animTransform = aAnimTransformArray1661[i];
		vertexCount = animTransform.vertexCount;
		faceCount = animTransform.faceCount;
		texturedFaceCount = animTransform.texturedFaceCount;
		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		faceVertexA = new int[faceCount];
		faceVertexB = new int[faceCount];
		faceVertexC = new int[faceCount];
		texTriangleA = new int[texturedFaceCount];
		texTriangleB = new int[texturedFaceCount];
		texTriangleC = new int[texturedFaceCount];
		if(animTransform.vertexLabelsOffset >= 0)
			vertexLabels = new int[vertexCount];
		if(animTransform.faceTexturesOffset >= 0)
			faceRenderType = new int[faceCount];
		if(animTransform.facePrioritiesOffset >= 0)
			faceRenderPriorities = new int[faceCount];
		else
			facePriority = -animTransform.facePrioritiesOffset - 1;
		if(animTransform.faceAlphasOffset >= 0)
			faceAlphas = new int[faceCount];
		if(animTransform.faceLabelsOffset >= 0)
			faceLabels = new int[faceCount];
		faceColors = new int[faceCount];
		Stream stream = new Stream(animTransform.rawData);
		stream.currentOffset = animTransform.vertexFlagsOffset;
		Stream stream_1 = new Stream(animTransform.rawData);
		stream_1.currentOffset = animTransform.vertexXOffset;
		Stream stream_2 = new Stream(animTransform.rawData);
		stream_2.currentOffset = animTransform.vertexYOffset;
		Stream stream_3 = new Stream(animTransform.rawData);
		stream_3.currentOffset = animTransform.vertexZOffset;
		Stream stream_4 = new Stream(animTransform.rawData);
		stream_4.currentOffset = animTransform.vertexLabelsOffset;
		int k = 0;
		int l = 0;
		int i1 = 0;
		for(int j1 = 0; j1 < vertexCount; j1++) {
			int k1 = stream.readUnsignedByte();
			int i2 = 0;
			if((k1 & 1) != 0)
			i2 = stream_1.readSmartSigned();
			int k2 = 0;
			if((k1 & 2) != 0)
			k2 = stream_2.readSmartSigned();
			int i3 = 0;
			if((k1 & 4) != 0)
			i3 = stream_3.readSmartSigned();
			vertexX[j1] = k + i2;
			vertexY[j1] = l + k2;
			vertexZ[j1] = i1 + i3;
			k = vertexX[j1];
			l = vertexY[j1];
			i1 = vertexZ[j1];
			if(vertexLabels != null)
			vertexLabels[j1] = stream_4.readUnsignedByte();
		}
		stream.currentOffset = animTransform.faceColorsOffset;
		stream_1.currentOffset = animTransform.faceTexturesOffset;
		stream_2.currentOffset = animTransform.facePrioritiesOffset;
		stream_3.currentOffset = animTransform.faceAlphasOffset;
		stream_4.currentOffset = animTransform.faceLabelsOffset;
		for(int l1 = 0; l1 < faceCount; l1++) {
			faceColors[l1] = stream.readUnsignedWord();
			if(faceRenderType != null)
			faceRenderType[l1] = stream_1.readUnsignedByte();
			if(faceRenderPriorities != null)
			faceRenderPriorities[l1] = stream_2.readUnsignedByte();
			if(faceAlphas != null)
			faceAlphas[l1] = stream_3.readUnsignedByte();
			if(faceLabels != null)
			faceLabels[l1] = stream_4.readUnsignedByte();
		}
		stream.currentOffset = animTransform.faceVerticesOffset;
		stream_1.currentOffset = animTransform.faceTypesOffset;
		int j2 = 0;
		int l2 = 0;
		int j3 = 0;
		int k3 = 0;
		for(int l3 = 0; l3 < faceCount; l3++) {
			int i4 = stream_1.readUnsignedByte();
			if(i4 == 1) {
				j2 = stream.readSmartSigned() + k3;
				k3 = j2;
				l2 = stream.readSmartSigned() + k3;
				k3 = l2;
				j3 = stream.readSmartSigned() + k3;
				k3 = j3;
				faceVertexA[l3] = j2;
				faceVertexB[l3] = l2;
				faceVertexC[l3] = j3;
			}
			if(i4 == 2) {
				j2 = j2;
				l2 = j3;
				j3 = stream.readSmartSigned() + k3;
				k3 = j3;
				faceVertexA[l3] = j2;
				faceVertexB[l3] = l2;
				faceVertexC[l3] = j3;
			}
			if(i4 == 3) {
				j2 = j3;
				l2 = l2;
				j3 = stream.readSmartSigned() + k3;
				k3 = j3;
				faceVertexA[l3] = j2;
				faceVertexB[l3] = l2;
				faceVertexC[l3] = j3;
			}
			if(i4 == 4) {
				int k4 = j2;
				j2 = l2;
				l2 = k4;
				j3 = stream.readSmartSigned() + k3;
				k3 = j3;
				faceVertexA[l3] = j2;
				faceVertexB[l3] = l2;
				faceVertexC[l3] = j3;
			}
		}
		stream.currentOffset = animTransform.texCoordOffset;
		for(int j4 = 0; j4 < texturedFaceCount; j4++) {
			texTriangleA[j4] = stream.readUnsignedWord();
			texTriangleB[j4] = stream.readUnsignedWord();
			texTriangleC[j4] = stream.readUnsignedWord();
		}
	}

	public Model(int i, Model aclass30_sub2_sub4_sub6s[]) {
		singleTile = false;
		boolean flag = false;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		facePriority = -1;
		for(int k = 0; k < i; k++) {
			Model model = aclass30_sub2_sub4_sub6s[k];
			if(model != null) {
				vertexCount += model.vertexCount;
				faceCount += model.faceCount;
				texturedFaceCount += model.texturedFaceCount;
				flag |= model.faceRenderType != null;
				if(model.faceRenderPriorities != null) {
					flag1 = true;
				} else {
					if(facePriority == -1)
					facePriority = model.facePriority;
					if(facePriority != model.facePriority)
					flag1 = true;
				}
				flag2 |= model.faceAlphas != null;
				flag3 |= model.faceLabels != null;
			}
		}

		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		vertexLabels = new int[vertexCount];
		faceVertexA = new int[faceCount];
		faceVertexB = new int[faceCount];
		faceVertexC = new int[faceCount];
		texTriangleA = new int[texturedFaceCount];
		texTriangleB = new int[texturedFaceCount];
		texTriangleC = new int[texturedFaceCount];
		if(flag)
			faceRenderType = new int[faceCount];
		if(flag1)
			faceRenderPriorities = new int[faceCount];
		if(flag2)
			faceAlphas = new int[faceCount];
		if(flag3)
			faceLabels = new int[faceCount];
		faceColors = new int[faceCount];
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		int l = 0;
		for(int i1 = 0; i1 < i; i1++) {
			Model model_1 = aclass30_sub2_sub4_sub6s[i1];
			if(model_1 != null) {
				for(int j1 = 0; j1 < model_1.faceCount; j1++) {
					if(flag)
					if(model_1.faceRenderType == null) {
						faceRenderType[faceCount] = 0;
					} else {
						int k1 = model_1.faceRenderType[j1];
						if((k1 & 2) == 2)
						k1 += l << 2;
						faceRenderType[faceCount] = k1;
					}
					if(flag1)
					if(model_1.faceRenderPriorities == null)
						faceRenderPriorities[faceCount] = model_1.facePriority;
					else
						faceRenderPriorities[faceCount] = model_1.faceRenderPriorities[j1];
					if(flag2)
					if(model_1.faceAlphas == null)
						faceAlphas[faceCount] = 0;
					else
						faceAlphas[faceCount] = model_1.faceAlphas[j1];
					if(flag3 && model_1.faceLabels != null)
					faceLabels[faceCount] = model_1.faceLabels[j1];
					faceColors[faceCount] = model_1.faceColors[j1];
					faceVertexA[faceCount] = getDeformedVertex(model_1, model_1.faceVertexA[j1]);
					faceVertexB[faceCount] = getDeformedVertex(model_1, model_1.faceVertexB[j1]);
					faceVertexC[faceCount] = getDeformedVertex(model_1, model_1.faceVertexC[j1]);
					faceCount++;
				}
				for(int l1 = 0; l1 < model_1.texturedFaceCount; l1++) {
					texTriangleA[texturedFaceCount] = getDeformedVertex(model_1, model_1.texTriangleA[l1]);
					texTriangleB[texturedFaceCount] = getDeformedVertex(model_1, model_1.texTriangleB[l1]);
					texTriangleC[texturedFaceCount] = getDeformedVertex(model_1, model_1.texTriangleC[l1]);
					texturedFaceCount++;
				}
				l += model_1.texturedFaceCount;
			}
		}
	}

	public Model(Model aclass30_sub2_sub4_sub6s[]) {
		int i = 2;
		singleTile = false;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		boolean flag4 = false;
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		facePriority = -1;
		for(int k = 0; k < i; k++) {
			Model model = aclass30_sub2_sub4_sub6s[k];
			if(model != null) {
				vertexCount += model.vertexCount;
				faceCount += model.faceCount;
				texturedFaceCount += model.texturedFaceCount;
				flag1 |= model.faceRenderType != null;
				if(model.faceRenderPriorities != null) {
					flag2 = true;
				} else {
					if(facePriority == -1)
					facePriority = model.facePriority;
					if(facePriority != model.facePriority)
					flag2 = true;
				}
				flag3 |= model.faceAlphas != null;
				flag4 |= model.faceColors != null;
			}
		}
		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		faceVertexA = new int[faceCount];
		faceVertexB = new int[faceCount];
		faceVertexC = new int[faceCount];
		faceColorA = new int[faceCount];
		faceColorB = new int[faceCount];
		faceColorC = new int[faceCount];
		texTriangleA = new int[texturedFaceCount];
		texTriangleB = new int[texturedFaceCount];
		texTriangleC = new int[texturedFaceCount];
		if(flag1)
			faceRenderType = new int[faceCount];
		if(flag2)
			faceRenderPriorities = new int[faceCount];
		if(flag3)
			faceAlphas = new int[faceCount];
		if(flag4)
			faceColors = new int[faceCount];
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		int i1 = 0;
		for(int j1 = 0; j1 < i; j1++) {
			Model model_1 = aclass30_sub2_sub4_sub6s[j1];
			if(model_1 != null) {
				int k1 = vertexCount;
				for(int l1 = 0; l1 < model_1.vertexCount; l1++) {
					vertexX[vertexCount] = model_1.vertexX[l1];
					vertexY[vertexCount] = model_1.vertexY[l1];
					vertexZ[vertexCount] = model_1.vertexZ[l1];
					vertexCount++;
				}
				for(int i2 = 0; i2 < model_1.faceCount; i2++) {
					faceVertexA[faceCount] = model_1.faceVertexA[i2] + k1;
					faceVertexB[faceCount] = model_1.faceVertexB[i2] + k1;
					faceVertexC[faceCount] = model_1.faceVertexC[i2] + k1;
					faceColorA[faceCount] = model_1.faceColorA[i2];
					faceColorB[faceCount] = model_1.faceColorB[i2];
					faceColorC[faceCount] = model_1.faceColorC[i2];
					if(flag1)
					if(model_1.faceRenderType == null) {
						faceRenderType[faceCount] = 0;
					} else {
						int j2 = model_1.faceRenderType[i2];
						if((j2 & 2) == 2)
						j2 += i1 << 2;
						faceRenderType[faceCount] = j2;
					}
					if(flag2)
					if(model_1.faceRenderPriorities == null)
						faceRenderPriorities[faceCount] = model_1.facePriority;
					else
						faceRenderPriorities[faceCount] = model_1.faceRenderPriorities[i2];
					if(flag3)
					if(model_1.faceAlphas == null)
						faceAlphas[faceCount] = 0;
					else
						faceAlphas[faceCount] = model_1.faceAlphas[i2];
					if(flag4 && model_1.faceColors != null)
					faceColors[faceCount] = model_1.faceColors[i2];
					faceCount++;
				}
				for(int k2 = 0; k2 < model_1.texturedFaceCount; k2++) {
					texTriangleA[texturedFaceCount] = model_1.texTriangleA[k2] + k1;
					texTriangleB[texturedFaceCount] = model_1.texTriangleB[k2] + k1;
					texTriangleC[texturedFaceCount] = model_1.texTriangleC[k2] + k1;
					texturedFaceCount++;
				}
				i1 += model_1.texturedFaceCount;
			}
		}
		calculateBounds();
	}

	public Model(boolean flag, boolean flag1, boolean flag2, Model model)  {
		singleTile = false;
		vertexCount = model.vertexCount;
		faceCount = model.faceCount;
		texturedFaceCount = model.texturedFaceCount;
		if(flag2) {
			vertexX = model.vertexX;
			vertexY = model.vertexY;
			vertexZ = model.vertexZ;
		} else {
			vertexX = new int[vertexCount];
			vertexY = new int[vertexCount];
			vertexZ = new int[vertexCount];
			for(int j = 0; j < vertexCount; j++)  {
				vertexX[j] = model.vertexX[j];
				vertexY[j] = model.vertexY[j];
				vertexZ[j] = model.vertexZ[j];
			}
		}
		if(flag) {
			faceColors = model.faceColors;
		} else {
			faceColors = new int[faceCount];
			System.arraycopy(model.faceColors, 0, faceColors, 0, faceCount);
		}
		if(flag1) 	{
			faceAlphas = model.faceAlphas;
		} else {
			faceAlphas = new int[faceCount];
			if(model.faceAlphas == null) {
				for(int l = 0; l < faceCount; l++)
					faceAlphas[l] = 0;
			} else {
				System.arraycopy(model.faceAlphas, 0, faceAlphas, 0, faceCount);
			}
		}
		vertexLabels = model.vertexLabels;
		faceLabels = model.faceLabels;
		faceRenderType = model.faceRenderType;
		faceVertexA = model.faceVertexA;
		faceVertexB = model.faceVertexB;
		faceVertexC = model.faceVertexC;
		faceRenderPriorities = model.faceRenderPriorities;
		facePriority = model.facePriority;
		texTriangleA = model.texTriangleA;
		texTriangleB = model.texTriangleB;
		texTriangleC = model.texTriangleC;
	}

	public Model(boolean flag, boolean flag1, Model model)  {
		singleTile = false;
		vertexCount = model.vertexCount;
		faceCount = model.faceCount;
		texturedFaceCount = model.texturedFaceCount;
		if(flag) {
			vertexY = new int[vertexCount];
			System.arraycopy(model.vertexY, 0, vertexY, 0, vertexCount);
		} else {
			vertexY = model.vertexY;
		}
		if(flag1) {
			faceColorA = new int[faceCount];
			faceColorB = new int[faceCount];
			faceColorC = new int[faceCount];
			for(int k = 0; k < faceCount; k++) {
				faceColorA[k] = model.faceColorA[k];
				faceColorB[k] = model.faceColorB[k];
				faceColorC[k] = model.faceColorC[k];
			}
			faceRenderType = new int[faceCount];
			if(model.faceRenderType == null) {
				for(int l = 0; l < faceCount; l++)
					faceRenderType[l] = 0;
			} else {
				System.arraycopy(model.faceRenderType, 0, faceRenderType, 0, faceCount);
			}
			super.vertexNormals = new VertexNormal[vertexCount];
			for(int j1 = 0; j1 < vertexCount; j1++) {
				VertexNormal vertexNormal = super.vertexNormals[j1] = new VertexNormal();
				VertexNormal vertexNormal_1 = model.vertexNormals[j1];
				vertexNormal.x = vertexNormal_1.x;
				vertexNormal.y = vertexNormal_1.y;
				vertexNormal.z = vertexNormal_1.z;
				vertexNormal.magnitude = vertexNormal_1.magnitude;
			}
			mergedNormals = model.mergedNormals;
		} else {
			faceColorA = model.faceColorA;
			faceColorB = model.faceColorB;
			faceColorC = model.faceColorC;
			faceRenderType = model.faceRenderType;
		}
		vertexX = model.vertexX;
		vertexZ = model.vertexZ;
		faceColors = model.faceColors;
		faceAlphas = model.faceAlphas;
		faceRenderPriorities = model.faceRenderPriorities;
		facePriority = model.facePriority;
		faceVertexA = model.faceVertexA;
		faceVertexB = model.faceVertexB;
		faceVertexC = model.faceVertexC;
		texTriangleA = model.texTriangleA;
		texTriangleB = model.texTriangleB;
		texTriangleC = model.texTriangleC;
		super.modelHeight = model.modelHeight;
		boundsBottomY = model.boundsBottomY; 
		boundsXZRadius = model.boundsXZRadius;
		boundsNearRadius = model.boundsNearRadius;
		boundsSphereRadius = model.boundsSphereRadius;
		boundsMinX = model.boundsMinX;
		boundsMaxZ = model.boundsMaxZ;
		boundsMinZ = model.boundsMinZ;
		boundsMaxX = model.boundsMaxX;
	}

	public void copyAnimated(Model model, boolean flag) {
		vertexCount = model.vertexCount;
		faceCount = model.faceCount;
		texturedFaceCount = model.texturedFaceCount;
		if(tmpVertexX.length < vertexCount) {
			tmpVertexX = new int[vertexCount + 100];
			tmpVertexY = new int[vertexCount + 100];
			tmpVertexZ = new int[vertexCount + 100];
		}
		vertexX = tmpVertexX;
		vertexY = tmpVertexY;
		vertexZ = tmpVertexZ;
		for(int k = 0; k < vertexCount; k++) {
			vertexX[k] = model.vertexX[k];
			vertexY[k] = model.vertexY[k];
			vertexZ[k] = model.vertexZ[k];
		}
		if(flag) {
			faceAlphas = model.faceAlphas;
		} else {
			if(tmpVertexW.length < faceCount)
				tmpVertexW = new int[faceCount + 100];
			faceAlphas = tmpVertexW;
			if(model.faceAlphas == null) {
				for(int l = 0; l < faceCount; l++)
					faceAlphas[l] = 0;
			} else {
				System.arraycopy(model.faceAlphas, 0, faceAlphas, 0, faceCount);
			}
		}
		faceRenderType = model.faceRenderType;
		faceColors = model.faceColors;
		faceRenderPriorities = model.faceRenderPriorities;
		facePriority = model.facePriority;
		labelGroupsUnused = model.labelGroupsUnused;
		labelGroups = model.labelGroups;
		faceVertexA = model.faceVertexA;
		faceVertexB = model.faceVertexB;
		faceVertexC = model.faceVertexC;
		faceColorA = model.faceColorA;
		faceColorB = model.faceColorB;
		faceColorC = model.faceColorC;
		texTriangleA = model.texTriangleA;
		texTriangleB = model.texTriangleB;
		texTriangleC = model.texTriangleC;
	}

	private int getDeformedVertex(Model model, int i)  {
		int j = -1;
		int k = model.vertexX[i];
		int l = model.vertexY[i];
		int i1 = model.vertexZ[i];
		for(int j1 = 0; j1 < vertexCount; j1++) {
			if(k != vertexX[j1] || l != vertexY[j1] || i1 != vertexZ[j1])
				continue;
			j = j1;
			break;
		}
		if(j == -1) {
			vertexX[vertexCount] = k;
			vertexY[vertexCount] = l;
			vertexZ[vertexCount] = i1;
			if(model.vertexLabels != null)
				vertexLabels[vertexCount] = model.vertexLabels[i];
			j = vertexCount++;
		}
		return j;
	}

	public void calculateBounds() {
		super.modelHeight = 0;
		boundsXZRadius = 0;
		boundsBottomY = 0;
		for(int i = 0; i < vertexCount; i++) {
			int j = vertexX[i];
			int k = vertexY[i];
			int l = vertexZ[i];
			if(-k > super.modelHeight)
				super.modelHeight = -k;
			if(k > boundsBottomY)
				boundsBottomY = k;
			int i1 = j * j + l * l;
			if(i1 > boundsXZRadius)
				boundsXZRadius = i1;
		}
		boundsXZRadius = (int)(Math.sqrt(boundsXZRadius) + 0.98999999999999999D);
		boundsNearRadius = (int)(Math.sqrt(boundsXZRadius * boundsXZRadius + super.modelHeight * super.modelHeight) + 0.98999999999999999D);
		boundsSphereRadius = boundsNearRadius + (int)(Math.sqrt(boundsXZRadius * boundsXZRadius + boundsBottomY * boundsBottomY) + 0.98999999999999999D);
	}

	public void calculateBoundsY() {
		super.modelHeight = 0;
		boundsBottomY = 0;
		for(int i = 0; i < vertexCount; i++) {
			int j = vertexY[i];
			if(-j > super.modelHeight)
				super.modelHeight = -j;
			if(j > boundsBottomY)
				boundsBottomY = j;
		}
		boundsNearRadius = (int)(Math.sqrt(boundsXZRadius * boundsXZRadius + super.modelHeight * super.modelHeight) + 0.98999999999999999D);
		boundsSphereRadius = boundsNearRadius + (int)(Math.sqrt(boundsXZRadius * boundsXZRadius + boundsBottomY * boundsBottomY) + 0.98999999999999999D);
	}

	private void calculateBoundsXZ()  {
		super.modelHeight = 0;
		boundsXZRadius = 0;
		boundsBottomY = 0;
		boundsMinX = 0xf423f;
		boundsMaxX = 0xfff0bdc1;
		boundsMaxZ = 0xfffe7961;
		boundsMinZ = 0x1869f;
		for(int j = 0; j < vertexCount; j++) {
			int k = vertexX[j];
			int l = vertexY[j];
			int i1 = vertexZ[j];
			if(k < boundsMinX)
				boundsMinX = k;
			if(k > boundsMaxX)
				boundsMaxX = k;
			if(i1 < boundsMinZ)
				boundsMinZ = i1;
			if(i1 > boundsMaxZ)
				boundsMaxZ = i1;
			if(-l > super.modelHeight)
				super.modelHeight = -l;
			if(l > boundsBottomY)
				boundsBottomY = l;
			int j1 = k * k + i1 * i1;
			if(j1 > boundsXZRadius)
				boundsXZRadius = j1;
		}
		boundsXZRadius = (int)Math.sqrt(boundsXZRadius);
		boundsNearRadius = (int)Math.sqrt(boundsXZRadius * boundsXZRadius + super.modelHeight * super.modelHeight);
		boundsSphereRadius = boundsNearRadius + (int)Math.sqrt(boundsXZRadius * boundsXZRadius + boundsBottomY * boundsBottomY);
	}

	public void buildLabelGroups() {
		if(vertexLabels != null) {
			int ai[] = new int[256];
			int j = 0;
			for(int l = 0; l < vertexCount; l++) {
				int j1 = vertexLabels[l];
				ai[j1]++;
				if(j1 > j)
					j = j1;
			}
			labelGroups = new int[j + 1][];
			for(int k1 = 0; k1 <= j; k1++) {
				labelGroups[k1] = new int[ai[k1]];
				ai[k1] = 0;
			}
			for(int j2 = 0; j2 < vertexCount; j2++) {
				int l2 = vertexLabels[j2];
				labelGroups[l2][ai[l2]++] = j2;
			}
			vertexLabels = null;
		}
		if(faceLabels != null) {
			int ai1[] = new int[256];
			int k = 0;
			for(int i1 = 0; i1 < faceCount; i1++) {
				int l1 = faceLabels[i1];
				ai1[l1]++;
				if(l1 > k)
					k = l1;
			}
			labelGroupsUnused = new int[k + 1][];
			for(int i2 = 0; i2 <= k; i2++) {
				labelGroupsUnused[i2] = new int[ai1[i2]];
				ai1[i2] = 0;
			}
			for(int k2 = 0; k2 < faceCount; k2++) {
				int i3 = faceLabels[k2];
				labelGroupsUnused[i3][ai1[i3]++] = k2;
			}
			faceLabels = null;
		}
	}

	public void applyTransform(int i) {
		if(labelGroups == null)
			return;
		if(i == -1)
			return;
		AnimFrame animFrame = AnimFrame.getFrame(i);
		if(animFrame == null)
			return;
		AnimBase animBase = animFrame.base;
		transformTempX = 0;
		transformTempY = 0;
		transformTempZ = 0;
		for(int k = 0; k < animFrame.transformCount; k++) {
			int l = animFrame.transformTypes[k];
			recolorTriangle(animBase.anIntArray342[l], animBase.anIntArrayArray343[l], animFrame.transformX[k], animFrame.transformY[k], animFrame.transformZ[k]);
		}
	}

	public void recolorAll(int ai[], int j, int k) {
		if(k == -1)
			return;
		if(ai == null || j == -1) {
			applyTransform(k);
			return;
		}
		AnimFrame animFrame = AnimFrame.getFrame(k);
		if(animFrame == null)
			return;
		AnimFrame animFrame_1 = AnimFrame.getFrame(j);
		if(animFrame_1 == null) {
			applyTransform(k);
			return;
		}
		AnimBase animBase = animFrame.base;
		transformTempX = 0;
		transformTempY = 0;
		transformTempZ = 0;
		int l = 0;
		int i1 = ai[l++];
		for(int j1 = 0; j1 < animFrame.transformCount; j1++) {
			int k1;
			for(k1 = animFrame.transformTypes[j1]; k1 > i1; i1 = ai[l++]);
				if(k1 != i1 || animBase.anIntArray342[k1] == 0)
					recolorTriangle(animBase.anIntArray342[k1], animBase.anIntArrayArray343[k1], animFrame.transformX[j1], animFrame.transformY[j1], animFrame.transformZ[j1]);
		}
		transformTempX = 0;
		transformTempY = 0;
		transformTempZ = 0;
		l = 0;
		i1 = ai[l++];
		for(int l1 = 0; l1 < animFrame_1.transformCount; l1++) {
			int i2;
			for(i2 = animFrame_1.transformTypes[l1]; i2 > i1; i1 = ai[l++]);
				if(i2 == i1 || animBase.anIntArray342[i2] == 0)
					recolorTriangle(animBase.anIntArray342[i2], animBase.anIntArrayArray343[i2], animFrame_1.transformX[l1], animFrame_1.transformY[l1], animFrame_1.transformZ[l1]);
		}
	}

	private void recolorTriangle(int i, int ai[], int j, int k, int l) {
		int i1 = ai.length;
		if(i == 0) {
			int j1 = 0;
			transformTempX = 0;
			transformTempY = 0;
			transformTempZ = 0;
			for(int k2 = 0; k2 < i1; k2++) {
				int l3 = ai[k2];
				if(l3 < labelGroups.length) {
					int ai5[] = labelGroups[l3];
					for(int i5 = 0; i5 < ai5.length; i5++) {
						int j6 = ai5[i5];
						transformTempX += vertexX[j6];
						transformTempY += vertexY[j6];
						transformTempZ += vertexZ[j6];
						j1++;
					}
				}
			}
			if(j1 > 0) {
				transformTempX = transformTempX / j1 + j;
				transformTempY = transformTempY / j1 + k;
				transformTempZ = transformTempZ / j1 + l;
				return;
			} else {
				transformTempX = j;
				transformTempY = k;
				transformTempZ = l;
				return;
			}
		}
		if(i == 1) {
			for(int k1 = 0; k1 < i1; k1++) {
				int l2 = ai[k1];
				if(l2 < labelGroups.length) {
					int ai1[] = labelGroups[l2];
					for(int i4 = 0; i4 < ai1.length; i4++) {
						int j5 = ai1[i4];
						vertexX[j5] += j;
						vertexY[j5] += k;
						vertexZ[j5] += l;
					}
				}
			}
			return;
		}
		if(i == 2) {
			for(int l1 = 0; l1 < i1; l1++) {
				int i3 = ai[l1];
				if(i3 < labelGroups.length) {
					int ai2[] = labelGroups[i3];
					for(int j4 = 0; j4 < ai2.length; j4++) {
						int k5 = ai2[j4];
						vertexX[k5] -= transformTempX;
						vertexY[k5] -= transformTempY;
						vertexZ[k5] -= transformTempZ;
						int k6 = (j & 0xff) * 8;
						int l6 = (k & 0xff) * 8;
						int i7 = (l & 0xff) * 8;
						if(i7 != 0) {
							int j7 = SINE[i7];
							int i8 = COSINE[i7];
							int l8 = vertexY[k5] * j7 + vertexX[k5] * i8 >> 16;
							vertexY[k5] = vertexY[k5] * i8 - vertexX[k5] * j7 >> 16;
							vertexX[k5] = l8;
						}
						if(k6 != 0) {
							int k7 = SINE[k6];
							int j8 = COSINE[k6];
							int i9 = vertexY[k5] * j8 - vertexZ[k5] * k7 >> 16;
							vertexZ[k5] = vertexY[k5] * k7 + vertexZ[k5] * j8 >> 16;
							vertexY[k5] = i9;
						}
						if(l6 != 0) {
							int l7 = SINE[l6];
							int k8 = COSINE[l6];
							int j9 = vertexZ[k5] * l7 + vertexX[k5] * k8 >> 16;
							vertexZ[k5] = vertexZ[k5] * k8 - vertexX[k5] * l7 >> 16;
							vertexX[k5] = j9;
						}
						vertexX[k5] += transformTempX;
						vertexY[k5] += transformTempY;
						vertexZ[k5] += transformTempZ;
					}
				}
			}
			return;
		}
		if(i == 3) {
			for(int i2 = 0; i2 < i1; i2++) {
				int j3 = ai[i2];
				if(j3 < labelGroups.length) {
					int ai3[] = labelGroups[j3];
					for(int k4 = 0; k4 < ai3.length; k4++) {
						int l5 = ai3[k4];
						vertexX[l5] -= transformTempX;
						vertexY[l5] -= transformTempY;
						vertexZ[l5] -= transformTempZ;
						vertexX[l5] = (vertexX[l5] * j) / 128;
						vertexY[l5] = (vertexY[l5] * k) / 128;
						vertexZ[l5] = (vertexZ[l5] * l) / 128;
						vertexX[l5] += transformTempX;
						vertexY[l5] += transformTempY;
						vertexZ[l5] += transformTempZ;
					}
				}
			}
			return;
		}
		if(i == 5 && labelGroupsUnused != null && faceAlphas != null) {
			for(int j2 = 0; j2 < i1; j2++) {
				int k3 = ai[j2];
				if(k3 < labelGroupsUnused.length) {
					int ai4[] = labelGroupsUnused[k3];
					for(int l4 = 0; l4 < ai4.length; l4++) {
						int i6 = ai4[l4];
						faceAlphas[i6] += j * 8;
						if(faceAlphas[i6] < 0)
							faceAlphas[i6] = 0;
						if(faceAlphas[i6] > 255)
							faceAlphas[i6] = 255;
					}
				}
			}
		}
	}

	public void rotateY90()  {
		for(int j = 0; j < vertexCount; j++) {
			int k = vertexX[j];
			vertexX[j] = vertexZ[j];
			vertexZ[j] = -k;
		}
	}

	public void rotateX(int i) {
		int k = SINE[i];
		int l = COSINE[i];
		for(int i1 = 0; i1 < vertexCount; i1++) {
			int j1 = vertexY[i1] * l - vertexZ[i1] * k >> 16;
			vertexZ[i1] = vertexY[i1] * k + vertexZ[i1] * l >> 16;
			vertexY[i1] = j1;
		}
	}

	public void translate(int i, int j, int l) {
		for(int i1 = 0; i1 < vertexCount; i1++) {
			vertexX[i1] += i;
			vertexY[i1] += j;
			vertexZ[i1] += l;
		}
	}

	public void replaceColor(int i, int j) {
		for(int k = 0; k < faceCount; k++)
			if(faceColors[k] == i)
				faceColors[k] = j;
	}

	public void mirrorZ() {
		for(int j = 0; j < vertexCount; j++)
			vertexZ[j] = -vertexZ[j];
		for(int k = 0; k < faceCount; k++) {
			int l = faceVertexA[k];
			faceVertexA[k] = faceVertexC[k];
			faceVertexC[k] = l;
		}
	}

	public void scale(int i, int j, int l) {
		for(int i1 = 0; i1 < vertexCount; i1++) {
			vertexX[i1] = (vertexX[i1] * i) / 128;
			vertexY[i1] = (vertexY[i1] * l) / 128;
			vertexZ[i1] = (vertexZ[i1] * j) / 128;
		}
	}

	public void calculateLighting(int i, int j, int k, int l, int i1, boolean flag) {
		int j1 = (int)Math.sqrt(k * k + l * l + i1 * i1);
		int k1 = j * j1 >> 8;
		if(faceColorA == null) {
			faceColorA = new int[faceCount];
			faceColorB = new int[faceCount];
			faceColorC = new int[faceCount];
		}
		if(super.vertexNormals == null) {
			super.vertexNormals = new VertexNormal[vertexCount];
			for(int l1 = 0; l1 < vertexCount; l1++)
				super.vertexNormals[l1] = new VertexNormal();
		}
		for(int i2 = 0; i2 < faceCount; i2++) {
			int j2 = faceVertexA[i2];
			int l2 = faceVertexB[i2];
			int i3 = faceVertexC[i2];
			int j3 = vertexX[l2] - vertexX[j2];
			int k3 = vertexY[l2] - vertexY[j2];
			int l3 = vertexZ[l2] - vertexZ[j2];
			int i4 = vertexX[i3] - vertexX[j2];
			int j4 = vertexY[i3] - vertexY[j2];
			int k4 = vertexZ[i3] - vertexZ[j2];
			int l4 = k3 * k4 - j4 * l3;
			int i5 = l3 * i4 - k4 * j3;
			int j5;
			for(j5 = j3 * j4 - i4 * k3; l4 > 8192 || i5 > 8192 || j5 > 8192 || l4 < -8192 || i5 < -8192 || j5 < -8192; j5 >>= 1) {
				l4 >>= 1;
				i5 >>= 1;
			}
			int k5 = (int)Math.sqrt(l4 * l4 + i5 * i5 + j5 * j5);
			if(k5 <= 0)
				k5 = 1;
			l4 = (l4 * 256) / k5;
			i5 = (i5 * 256) / k5;
			j5 = (j5 * 256) / k5;
			if(faceRenderType == null || (faceRenderType[i2] & 1) == 0) {
				VertexNormal vertexNormal_2 = super.vertexNormals[j2];
				vertexNormal_2.x += l4;
				vertexNormal_2.y += i5;
				vertexNormal_2.z += j5;
				vertexNormal_2.magnitude++;
				vertexNormal_2 = super.vertexNormals[l2];
				vertexNormal_2.x += l4;
				vertexNormal_2.y += i5;
				vertexNormal_2.z += j5;
				vertexNormal_2.magnitude++;
				vertexNormal_2 = super.vertexNormals[i3];
				vertexNormal_2.x += l4;
				vertexNormal_2.y += i5;
				vertexNormal_2.z += j5;
				vertexNormal_2.magnitude++;
			} else {
				int l5 = i + (k * l4 + l * i5 + i1 * j5) / (k1 + k1 / 2);
				faceColorA[i2] = hslToRgb(faceColors[i2], l5, faceRenderType[i2]);
			}
		}
		if(flag) {
			calculateLightingMerged(i, k1, k, l, i1);
		} else {
			mergedNormals = new VertexNormal[vertexCount];
			for(int k2 = 0; k2 < vertexCount; k2++) {
				VertexNormal vertexNormal = super.vertexNormals[k2];
				VertexNormal vertexNormal_1 = mergedNormals[k2] = new VertexNormal();
				vertexNormal_1.x = vertexNormal.x;
				vertexNormal_1.y = vertexNormal.y;
				vertexNormal_1.z = vertexNormal.z;
				vertexNormal_1.magnitude = vertexNormal.magnitude;
			}
		}
		if(flag) {
			calculateBounds();
		} else {
			calculateBoundsXZ();
		}
	}

	public void calculateLightingMerged(int i, int j, int k, int l, int i1) {
		for(int j1 = 0; j1 < faceCount; j1++) {
			int k1 = faceVertexA[j1];
			int i2 = faceVertexB[j1];
			int j2 = faceVertexC[j1];
			if(faceRenderType == null) {
				int i3 = faceColors[j1];
				VertexNormal vertexNormal = super.vertexNormals[k1];
				int k2 = i + (k * vertexNormal.x + l * vertexNormal.y + i1 * vertexNormal.z) / (j * vertexNormal.magnitude);
				faceColorA[j1] = hslToRgb(i3, k2, 0);
				vertexNormal = super.vertexNormals[i2];
				k2 = i + (k * vertexNormal.x + l * vertexNormal.y + i1 * vertexNormal.z) / (j * vertexNormal.magnitude);
				faceColorB[j1] = hslToRgb(i3, k2, 0);
				vertexNormal = super.vertexNormals[j2];
				k2 = i + (k * vertexNormal.x + l * vertexNormal.y + i1 * vertexNormal.z) / (j * vertexNormal.magnitude);
				faceColorC[j1] = hslToRgb(i3, k2, 0);
			} else if((faceRenderType[j1] & 1) == 0) {
				int j3 = faceColors[j1];
				int k3 = faceRenderType[j1];
				VertexNormal vertexNormal_1 = super.vertexNormals[k1];
				int l2 = i + (k * vertexNormal_1.x + l * vertexNormal_1.y + i1 * vertexNormal_1.z) / (j * vertexNormal_1.magnitude);
				faceColorA[j1] = hslToRgb(j3, l2, k3);
				vertexNormal_1 = super.vertexNormals[i2];
				l2 = i + (k * vertexNormal_1.x + l * vertexNormal_1.y + i1 * vertexNormal_1.z) / (j * vertexNormal_1.magnitude);
				faceColorB[j1] = hslToRgb(j3, l2, k3);
				vertexNormal_1 = super.vertexNormals[j2];
				l2 = i + (k * vertexNormal_1.x + l * vertexNormal_1.y + i1 * vertexNormal_1.z) / (j * vertexNormal_1.magnitude);
				faceColorC[j1] = hslToRgb(j3, l2, k3);
			}
		}
		super.vertexNormals = null;
		mergedNormals = null;
		vertexLabels = null;
		faceLabels = null;
		if(faceRenderType != null) {
			for(int l1 = 0; l1 < faceCount; l1++)
				if((faceRenderType[l1] & 2) == 2)
					return;
		}
		faceColors = null;
	}

	private static int hslToRgb(int i, int j, int k) {
		if((k & 2) == 2) {
			if(j < 0)
				j = 0;
			else if(j > 127)
				j = 127;
			j = 127 - j;
			return j;
		}
		j = j * (i & 0x7f) >> 7;
		if(j < 2)
			j = 2;
		else if(j > 126)
			j = 126;
		return (i & 0xff80) + j;
	}

	public void renderModel2D(int j, int k, int l, int i1, int j1, int k1) {
		int i = 0; //was a parameter
		int l1 = Texture.textureInt1;
		int i2 = Texture.textureInt2;
		int j2 = SINE[i];
		int k2 = COSINE[i];
		int l2 = SINE[j];
		int i3 = COSINE[j];
		int j3 = SINE[k];
		int k3 = COSINE[k];
		int l3 = SINE[l];
		int i4 = COSINE[l];
		int j4 = j1 * l3 + k1 * i4 >> 16;
		for(int k4 = 0; k4 < vertexCount; k4++) {
			int l4 = vertexX[k4];
			int i5 = vertexY[k4];
			int j5 = vertexZ[k4];
			if(k != 0) {
				int k5 = i5 * j3 + l4 * k3 >> 16;
				i5 = i5 * k3 - l4 * j3 >> 16;
				l4 = k5;
			}
			if(i != 0) {
				int l5 = i5 * k2 - j5 * j2 >> 16;
				j5 = i5 * j2 + j5 * k2 >> 16;
				i5 = l5;
			}
			if(j != 0) {
				int i6 = j5 * l2 + l4 * i3 >> 16;
				j5 = j5 * i3 - l4 * l2 >> 16;
				l4 = i6;
			}
			l4 += i1;
			i5 += j1;
			j5 += k1;
			int j6 = i5 * i4 - j5 * l3 >> 16;
			j5 = i5 * l3 + j5 * i4 >> 16;
			i5 = j6;
			projectedVertexZ[k4] = j5 - j4;
			projectedVertexX[k4] = l1 + (l4 << 9) / j5;
			projectedVertexY[k4] = i2 + (i5 << 9) / j5;
			if(texturedFaceCount > 0) {
				depthBuffer[k4] = l4;
				screenXVertices[k4] = i5;
				screenYVertices[k4] = j5;
			}
		}
		try {
			drawTriangle(false, false, 0);
		} catch(Exception _ex) {
		}
	}

	public void renderAtPoint(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
		int j2 = l1 * i1 - j1 * l >> 16;
		int k2 = k1 * j + j2 * k >> 16;
		int l2 = boundsXZRadius * k >> 16;
		int i3 = k2 + l2;
		if(i3 <= 50 || k2 >= 3500)
			return;
		int j3 = l1 * l + j1 * i1 >> 16;
		int k3 = j3 - boundsXZRadius << 9;
		if(k3 / i3 >= DrawingArea.centerY)
			return;
		int l3 = j3 + boundsXZRadius << 9;
		if(l3 / i3 <= -DrawingArea.centerY)
			return;
		int i4 = k1 * k - j2 * j >> 16;
		int j4 = boundsXZRadius * j >> 16;
		int k4 = i4 + j4 << 9;
		if(k4 / i3 <= -DrawingArea.bottomY)
			return;
		int l4 = j4 + (super.modelHeight * k >> 16);
		int i5 = i4 - l4 << 9;
		if(i5 / i3 >= DrawingArea.bottomY)
			return;
		int j5 = l2 + (super.modelHeight * j >> 16);
		boolean flag = false;
		if(k2 - j5 <= 50)
			flag = true;
		boolean flag1 = false;
		if(i2 > 0 && mousePickingEnabled) {
			int k5 = k2 - l2;
			if(k5 <= 50)
				k5 = 50;
			if(j3 > 0) {
				k3 /= i3;
				l3 /= k5;
			} else {
				l3 /= i3;
				k3 /= k5;
			}
			if(i4 > 0) {
				i5 /= i3;
				k4 /= k5;
			} else {
				k4 /= i3;
				i5 /= k5;
			}
			int i6 = mousePickX - Texture.textureInt1;
			int k6 = mousePickY - Texture.textureInt2;
			if(i6 > k3 && i6 < l3 && k6 > i5 && k6 < k4)
				if(singleTile)
					mousePickResults[mousePickCount++] = i2;
				else
					flag1 = true;
		}
		int l5 = Texture.textureInt1;
		int j6 = Texture.textureInt2;
		int l6 = 0;
		int i7 = 0;
		if(i != 0) {
			l6 = SINE[i];
			i7 = COSINE[i];
		}
		for(int j7 = 0; j7 < vertexCount; j7++) {
			int k7 = vertexX[j7];
			int l7 = vertexY[j7];
			int i8 = vertexZ[j7];
			if(i != 0) {
				int j8 = i8 * l6 + k7 * i7 >> 16;
				i8 = i8 * i7 - k7 * l6 >> 16;
				k7 = j8;
			}
			k7 += j1;
			l7 += k1;
			i8 += l1;
			int k8 = i8 * l + k7 * i1 >> 16;
			i8 = i8 * i1 - k7 * l >> 16;
			k7 = k8;
			k8 = l7 * k - i8 * j >> 16;
			i8 = l7 * j + i8 * k >> 16;
			l7 = k8;
			projectedVertexZ[j7] = i8 - k2;
			if(i8 >= 50) {
				projectedVertexX[j7] = l5 + (k7 << 9) / i8;
				projectedVertexY[j7] = j6 + (l7 << 9) / i8;
			} else {
				projectedVertexX[j7] = -5000;
				flag = true;
			}
			if(flag || texturedFaceCount > 0) {
				depthBuffer[j7] = k7;
				screenXVertices[j7] = l7;
				screenYVertices[j7] = i8;
			}
		}
		try {
			drawTriangle(flag, flag1, i2);
		} catch(Exception _ex) {
		}
	}

	private void drawTriangle(boolean flag, boolean flag1, int i) {
		for(int j = 0; j < boundsSphereRadius; j++)
			depthFaceCount[j] = 0;
		for(int k = 0; k < faceCount; k++)
			if(faceRenderType == null || faceRenderType[k] != -1) {
				int l = faceVertexA[k];
				int k1 = faceVertexB[k];
				int j2 = faceVertexC[k];
				int i3 = projectedVertexX[l];
				int l3 = projectedVertexX[k1];
				int k4 = projectedVertexX[j2];
				if(flag && (i3 == -5000 || l3 == -5000 || k4 == -5000)) {
					faceClippedX[k] = true;
					int j5 = (projectedVertexZ[l] + projectedVertexZ[k1] + projectedVertexZ[j2]) / 3 + boundsNearRadius;
					depthFaceIndices[j5][depthFaceCount[j5]++] = k;
				} else {
					if(flag1 && isTriangleVisible(mousePickX, mousePickY, projectedVertexY[l], projectedVertexY[k1], projectedVertexY[j2], i3, l3, k4)) {
						mousePickResults[mousePickCount++] = i;
						flag1 = false;
					}
					if((i3 - l3) * (projectedVertexY[j2] - projectedVertexY[k1]) - (projectedVertexY[l] - projectedVertexY[k1]) * (k4 - l3) > 0) {
						faceClippedX[k] = false;
						faceNearClipped[k] = i3 < 0 || l3 < 0 || k4 < 0 || i3 > DrawingArea.centerX || l3 > DrawingArea.centerX || k4 > DrawingArea.centerX;
						int k5 = (projectedVertexZ[l] + projectedVertexZ[k1] + projectedVertexZ[j2]) / 3 + boundsNearRadius;
						depthFaceIndices[k5][depthFaceCount[k5]++] = k;
					}
				}
			}
		if(faceRenderPriorities == null) {
			for(int i1 = boundsSphereRadius - 1; i1 >= 0; i1--) {
				int l1 = depthFaceCount[i1];
				if(l1 > 0) {
					int ai[] = depthFaceIndices[i1];
					for(int j3 = 0; j3 < l1; j3++)
					drawFlatTriangle(ai[j3]);
				}
			}
			return;
		}
		for(int j1 = 0; j1 < 12; j1++) {
			priorityFaceCount[j1] = 0;
			priorityDepthSum[j1] = 0;
		}
		
		for(int i2 = boundsSphereRadius - 1; i2 >= 0; i2--) {
			int k2 = depthFaceCount[i2];
			if(k2 > 0) {
				int ai1[] = depthFaceIndices[i2];
				for(int i4 = 0; i4 < k2; i4++) {
					int l4 = ai1[i4];
					int l5 = faceRenderPriorities[l4];
					int j6 = priorityFaceCount[l5]++;
					priorityFaceIndices[l5][j6] = l4;
					if(l5 < 10)
						priorityDepthSum[l5] += i2;
					else if(l5 == 10)
						facePriorityDepthSum[j6] = i2;
					else
						normalFaceDepth[j6] = i2;
				}
			}
		}

		int l2 = 0;
		if(priorityFaceCount[1] > 0 || priorityFaceCount[2] > 0)
			l2 = (priorityDepthSum[1] + priorityDepthSum[2]) / (priorityFaceCount[1] + priorityFaceCount[2]);
		int k3 = 0;
		if(priorityFaceCount[3] > 0 || priorityFaceCount[4] > 0)
			k3 = (priorityDepthSum[3] + priorityDepthSum[4]) / (priorityFaceCount[3] + priorityFaceCount[4]);
		int j4 = 0;
		if(priorityFaceCount[6] > 0 || priorityFaceCount[8] > 0)
			j4 = (priorityDepthSum[6] + priorityDepthSum[8]) / (priorityFaceCount[6] + priorityFaceCount[8]);
		int i6 = 0;
		int k6 = priorityFaceCount[10];
		int ai2[] = priorityFaceIndices[10];
		int ai3[] = facePriorityDepthSum;
		if(i6 == k6) {
			i6 = 0;
			k6 = priorityFaceCount[11];
			ai2 = priorityFaceIndices[11];
			ai3 = normalFaceDepth;
		}
		int i5;
		if(i6 < k6)
			i5 = ai3[i6];
		else
			i5 = -1000;
		for(int l6 = 0; l6 < 10; l6++) {
			while(l6 == 0 && i5 > l2) {
				drawFlatTriangle(ai2[i6++]);
			if(i6 == k6 && ai2 != priorityFaceIndices[11]) {
				i6 = 0;
				k6 = priorityFaceCount[11];
				ai2 = priorityFaceIndices[11];
				ai3 = normalFaceDepth;
			}
			if(i6 < k6)
				i5 = ai3[i6];
			else
				i5 = -1000;
			}
			while(l6 == 3 && i5 > k3) {
			drawFlatTriangle(ai2[i6++]);
			if(i6 == k6 && ai2 != priorityFaceIndices[11]) {
				i6 = 0;
				k6 = priorityFaceCount[11];
				ai2 = priorityFaceIndices[11];
				ai3 = normalFaceDepth;
			}
			if(i6 < k6)
				i5 = ai3[i6];
			else
				i5 = -1000;
			}
			while(l6 == 5 && i5 > j4)  {
			drawFlatTriangle(ai2[i6++]);
			if(i6 == k6 && ai2 != priorityFaceIndices[11]) {
				i6 = 0;
				k6 = priorityFaceCount[11];
				ai2 = priorityFaceIndices[11];
				ai3 = normalFaceDepth;
			}
			if(i6 < k6)
				i5 = ai3[i6];
			else
				i5 = -1000;
			}
			int i7 = priorityFaceCount[l6];
			int ai4[] = priorityFaceIndices[l6];
			for(int j7 = 0; j7 < i7; j7++)
				drawFlatTriangle(ai4[j7]);
		}
		while(i5 != -1000) {
			drawFlatTriangle(ai2[i6++]);
			if(i6 == k6 && ai2 != priorityFaceIndices[11]) {
				i6 = 0;
				ai2 = priorityFaceIndices[11];
				k6 = priorityFaceCount[11];
				ai3 = normalFaceDepth;
			}
			if(i6 < k6)
				i5 = ai3[i6];
			else
				i5 = -1000;
		}
	}

	private void drawFlatTriangle(int i) {
		if(faceClippedX[i]) {
			drawTexturedTriangle(i);
			return;
		}
		int j = faceVertexA[i];
		int k = faceVertexB[i];
		int l = faceVertexC[i];
		Texture.opaque = faceNearClipped[i];
		if(faceAlphas == null)
			Texture.textureCycle = 0;
		else
			Texture.textureCycle = faceAlphas[i];
		int i1;
		if(faceRenderType == null)
			i1 = 0;
		else
			i1 = faceRenderType[i] & 3;
		if(i1 == 0) {
			Texture.drawGouraudTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], faceColorA[i], faceColorB[i], faceColorC[i]);
			return;
		}
		if(i1 == 1) {
			Texture.drawFlatShadedTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], HSL_TO_RGB[faceColorA[i]]);
			return;
		}
		if(i1 == 2) {
			int j1 = faceRenderType[i] >> 2;
			int l1 = texTriangleA[j1];
			int j2 = texTriangleB[j1];
			int l2 = texTriangleC[j1];
			Texture.drawTexturedTriangleFull(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], faceColorA[i], faceColorB[i], faceColorC[i], depthBuffer[l1], depthBuffer[j2], depthBuffer[l2], screenXVertices[l1], screenXVertices[j2], screenXVertices[l2], screenYVertices[l1], screenYVertices[j2], screenYVertices[l2], faceColors[i]);
			return;
		}
		if(i1 == 3) {
			int k1 = faceRenderType[i] >> 2;
			int i2 = texTriangleA[k1];
			int k2 = texTriangleB[k1];
			int i3 = texTriangleC[k1];
			Texture.drawTexturedTriangleFull(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], faceColorA[i], faceColorA[i], faceColorA[i], depthBuffer[i2], depthBuffer[k2], depthBuffer[i3], screenXVertices[i2], screenXVertices[k2], screenXVertices[i3], screenYVertices[i2], screenYVertices[k2], screenYVertices[i3], faceColors[i]);
		}
	}

	private void drawTexturedTriangle(int i) {
		int j = Texture.textureInt1;
		int k = Texture.textureInt2;
		int l = 0;
		int i1 = faceVertexA[i];
		int j1 = faceVertexB[i];
		int k1 = faceVertexC[i];
		int l1 = screenYVertices[i1];
		int i2 = screenYVertices[j1];
		int j2 = screenYVertices[k1];
		if(l1 >= 50) {
			tmpFaceA[l] = projectedVertexX[i1];
			tmpFaceB[l] = projectedVertexY[i1];
			tmpFaceC[l++] = faceColorA[i];
		} else {
			int k2 = depthBuffer[i1];
			int k3 = screenXVertices[i1];
			int k4 = faceColorA[i];
			if(j2 >= 50) {
				int k5 = (50 - l1) * LIGHT_DECAY[j2 - l1];
				tmpFaceA[l] = j + (k2 + ((depthBuffer[k1] - k2) * k5 >> 16) << 9) / 50;
				tmpFaceB[l] = k + (k3 + ((screenXVertices[k1] - k3) * k5 >> 16) << 9) / 50;
				tmpFaceC[l++] = k4 + ((faceColorC[i] - k4) * k5 >> 16);
			}
			if(i2 >= 50) {
				int l5 = (50 - l1) * LIGHT_DECAY[i2 - l1];
				tmpFaceA[l] = j + (k2 + ((depthBuffer[j1] - k2) * l5 >> 16) << 9) / 50;
				tmpFaceB[l] = k + (k3 + ((screenXVertices[j1] - k3) * l5 >> 16) << 9) / 50;
				tmpFaceC[l++] = k4 + ((faceColorB[i] - k4) * l5 >> 16);
			}
		}
		if(i2 >= 50) {
			tmpFaceA[l] = projectedVertexX[j1];
			tmpFaceB[l] = projectedVertexY[j1];
			tmpFaceC[l++] = faceColorB[i];
		} else {
			int l2 = depthBuffer[j1];
			int l3 = screenXVertices[j1];
			int l4 = faceColorB[i];
			if(l1 >= 50) {
				int i6 = (50 - i2) * LIGHT_DECAY[l1 - i2];
				tmpFaceA[l] = j + (l2 + ((depthBuffer[i1] - l2) * i6 >> 16) << 9) / 50;
				tmpFaceB[l] = k + (l3 + ((screenXVertices[i1] - l3) * i6 >> 16) << 9) / 50;
				tmpFaceC[l++] = l4 + ((faceColorA[i] - l4) * i6 >> 16);
			}
			if(j2 >= 50) {
				int j6 = (50 - i2) * LIGHT_DECAY[j2 - i2];
				tmpFaceA[l] = j + (l2 + ((depthBuffer[k1] - l2) * j6 >> 16) << 9) / 50;
				tmpFaceB[l] = k + (l3 + ((screenXVertices[k1] - l3) * j6 >> 16) << 9) / 50;
				tmpFaceC[l++] = l4 + ((faceColorC[i] - l4) * j6 >> 16);
			}
		}
		if(j2 >= 50) {
			tmpFaceA[l] = projectedVertexX[k1];
			tmpFaceB[l] = projectedVertexY[k1];
			tmpFaceC[l++] = faceColorC[i];
		} else {
			int i3 = depthBuffer[k1];
			int i4 = screenXVertices[k1];
			int i5 = faceColorC[i];
			if(i2 >= 50) {
				int k6 = (50 - j2) * LIGHT_DECAY[i2 - j2];
				tmpFaceA[l] = j + (i3 + ((depthBuffer[j1] - i3) * k6 >> 16) << 9) / 50;
				tmpFaceB[l] = k + (i4 + ((screenXVertices[j1] - i4) * k6 >> 16) << 9) / 50;
				tmpFaceC[l++] = i5 + ((faceColorB[i] - i5) * k6 >> 16);
			}
			if(l1 >= 50) {
				int l6 = (50 - j2) * LIGHT_DECAY[l1 - j2];
				tmpFaceA[l] = j + (i3 + ((depthBuffer[i1] - i3) * l6 >> 16) << 9) / 50;
				tmpFaceB[l] = k + (i4 + ((screenXVertices[i1] - i4) * l6 >> 16) << 9) / 50;
				tmpFaceC[l++] = i5 + ((faceColorA[i] - i5) * l6 >> 16);
			}
		}
		int j3 = tmpFaceA[0];
		int j4 = tmpFaceA[1];
		int j5 = tmpFaceA[2];
		int i7 = tmpFaceB[0];
		int j7 = tmpFaceB[1];
		int k7 = tmpFaceB[2];
		if((j3 - j4) * (k7 - j7) - (i7 - j7) * (j5 - j4) > 0) {
			Texture.opaque = false;
			if(l == 3) {
				if(j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.centerX || j4 > DrawingArea.centerX || j5 > DrawingArea.centerX)
					Texture.opaque = true;
				int l7;
				if(faceRenderType == null)
					l7 = 0;
				else
					l7 = faceRenderType[i] & 3;
				if(l7 == 0)
					Texture.drawGouraudTriangle(i7, j7, k7, j3, j4, j5, tmpFaceC[0], tmpFaceC[1], tmpFaceC[2]);
				else if(l7 == 1)
					Texture.drawFlatShadedTriangle(i7, j7, k7, j3, j4, j5, HSL_TO_RGB[faceColorA[i]]);
				else if(l7 == 2) {
					int j8 = faceRenderType[i] >> 2;
					int k9 = texTriangleA[j8];
					int k10 = texTriangleB[j8];
					int k11 = texTriangleC[j8];
					Texture.drawTexturedTriangleFull(i7, j7, k7, j3, j4, j5, tmpFaceC[0], tmpFaceC[1], tmpFaceC[2], depthBuffer[k9], depthBuffer[k10], depthBuffer[k11], screenXVertices[k9], screenXVertices[k10], screenXVertices[k11], screenYVertices[k9], screenYVertices[k10], screenYVertices[k11], faceColors[i]);
				} else if(l7 == 3) {
					int k8 = faceRenderType[i] >> 2;
					int l9 = texTriangleA[k8];
					int l10 = texTriangleB[k8];
					int l11 = texTriangleC[k8];
					Texture.drawTexturedTriangleFull(i7, j7, k7, j3, j4, j5, faceColorA[i], faceColorA[i], faceColorA[i], depthBuffer[l9], depthBuffer[l10], depthBuffer[l11], screenXVertices[l9], screenXVertices[l10], screenXVertices[l11], screenYVertices[l9], screenYVertices[l10], screenYVertices[l11], faceColors[i]);
				}
			}
			if(l == 4) {
				if(j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.centerX || j4 > DrawingArea.centerX || j5 > DrawingArea.centerX || tmpFaceA[3] < 0 || tmpFaceA[3] > DrawingArea.centerX)
					Texture.opaque = true;
				int i8;
				if(faceRenderType == null)
					i8 = 0;
				else
					i8 = faceRenderType[i] & 3;
				if(i8 == 0) {
					Texture.drawGouraudTriangle(i7, j7, k7, j3, j4, j5, tmpFaceC[0], tmpFaceC[1], tmpFaceC[2]);
					Texture.drawGouraudTriangle(i7, k7, tmpFaceB[3], j3, j5, tmpFaceA[3], tmpFaceC[0], tmpFaceC[2], tmpFaceC[3]);
					return;
				}
				if(i8 == 1) {
					int l8 = HSL_TO_RGB[faceColorA[i]];
					Texture.drawFlatShadedTriangle(i7, j7, k7, j3, j4, j5, l8);
					Texture.drawFlatShadedTriangle(i7, k7, tmpFaceB[3], j3, j5, tmpFaceA[3], l8);
					return;
				}
				if(i8 == 2) {
					int i9 = faceRenderType[i] >> 2;
					int i10 = texTriangleA[i9];
					int i11 = texTriangleB[i9];
					int i12 = texTriangleC[i9];
					Texture.drawTexturedTriangleFull(i7, j7, k7, j3, j4, j5, tmpFaceC[0], tmpFaceC[1], tmpFaceC[2], depthBuffer[i10], depthBuffer[i11], depthBuffer[i12], screenXVertices[i10], screenXVertices[i11], screenXVertices[i12], screenYVertices[i10], screenYVertices[i11], screenYVertices[i12], faceColors[i]);
					Texture.drawTexturedTriangleFull(i7, k7, tmpFaceB[3], j3, j5, tmpFaceA[3], tmpFaceC[0], tmpFaceC[2], tmpFaceC[3], depthBuffer[i10], depthBuffer[i11], depthBuffer[i12], screenXVertices[i10], screenXVertices[i11], screenXVertices[i12], screenYVertices[i10], screenYVertices[i11], screenYVertices[i12], faceColors[i]);
					return;
				}
				if(i8 == 3) {
					int j9 = faceRenderType[i] >> 2;
					int j10 = texTriangleA[j9];
					int j11 = texTriangleB[j9];
					int j12 = texTriangleC[j9];
					Texture.drawTexturedTriangleFull(i7, j7, k7, j3, j4, j5, faceColorA[i], faceColorA[i], faceColorA[i], depthBuffer[j10], depthBuffer[j11], depthBuffer[j12], screenXVertices[j10], screenXVertices[j11], screenXVertices[j12], screenYVertices[j10], screenYVertices[j11], screenYVertices[j12], faceColors[i]);
					Texture.drawTexturedTriangleFull(i7, k7, tmpFaceB[3], j3, j5, tmpFaceA[3], faceColorA[i], faceColorA[i], faceColorA[i], depthBuffer[j10], depthBuffer[j11], depthBuffer[j12], screenXVertices[j10], screenXVertices[j11], screenXVertices[j12], screenYVertices[j10], screenYVertices[j11], screenYVertices[j12], faceColors[i]);
				}
			}
		}
	}

	private boolean isTriangleVisible(int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
		if(j < k && j < l && j < i1)
			return false;
		if(j > k && j > l && j > i1)
			return false;
		return !(i < j1 && i < k1 && i < l1) && (i <= j1 || i <= k1 || i <= l1);
	}

	public static final Model sharedModel = new Model();
	private static int[] tmpVertexX = new int[2000];
	private static int[] tmpVertexY = new int[2000];
	private static int[] tmpVertexZ = new int[2000];
	private static int[] tmpVertexW = new int[2000];
	public int vertexCount;
	public int vertexX[];
	public int vertexY[];
	public int vertexZ[];
	public int faceCount;
	public int faceVertexA[];
	public int faceVertexB[];
	public int faceVertexC[];
	private int[] faceColorA;
	private int[] faceColorB;
	private int[] faceColorC;
	public int faceRenderType[];
	private int[] faceRenderPriorities;
	private int[] faceAlphas;
	public int faceColors[];
	private int facePriority;
	private int texturedFaceCount;
	private int[] texTriangleA;
	private int[] texTriangleB;
	private int[] texTriangleC;
	public int boundsMinX;
	public int boundsMaxX;
	public int boundsMaxZ;
	public int boundsMinZ;
	public int boundsXZRadius;
	public int boundsBottomY;
	private int boundsSphereRadius;
	private int boundsNearRadius;
	public int objectHeight;
	private int[] vertexLabels;
	private int[] faceLabels;
	public int labelGroups[][];
	public int labelGroupsUnused[][];
	public boolean singleTile;
	VertexNormal mergedNormals[];
	private static AnimTransform[] aAnimTransformArray1661;
	private static OnDemandFetcherParent aOnDemandFetcherParent_1662;
	private static boolean[] faceNearClipped = new boolean[4096];
	private static boolean[] faceClippedX = new boolean[4096];
	private static int[] projectedVertexX = new int[4096];
	private static int[] projectedVertexY = new int[4096];
	private static int[] projectedVertexZ = new int[4096];
	private static int[] depthBuffer = new int[4096];
	private static int[] screenXVertices = new int[4096];
	private static int[] screenYVertices = new int[4096];
	private static int[] depthFaceCount = new int[1500];
	private static int[][] depthFaceIndices = new int[1500][512];
	private static int[] priorityFaceCount = new int[12];
	private static int[][] priorityFaceIndices = new int[12][2000];
	private static int[] facePriorityDepthSum = new int[2000];
	private static int[] normalFaceDepth = new int[2000];
	private static int[] priorityDepthSum = new int[12];
	private static final int[] tmpFaceA = new int[10];
	private static final int[] tmpFaceB = new int[10];
	private static final int[] tmpFaceC = new int[10];
	private static int transformTempX;
	private static int transformTempY;
	private static int transformTempZ;
	public static boolean mousePickingEnabled;
	public static int mousePickX;
	public static int mousePickY;
	public static int mousePickCount;
	public static final int[] mousePickResults = new int[1000];
	public static int SINE[];
	public static int COSINE[];
	private static int[] HSL_TO_RGB;
	private static int[] LIGHT_DECAY;

	static {
		SINE = Texture.SINE;
		COSINE = Texture.COSINE;
		HSL_TO_RGB = Texture.HSL_TO_RGB;
		LIGHT_DECAY = Texture.lightDecay;
	}
}