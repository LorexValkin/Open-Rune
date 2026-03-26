package com.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

import com.client.sign.Signlink;

public final class OnDemandFetcher extends OnDemandFetcherParent implements Runnable {

	private void readData() {
		try {
			int j = inputStream.available();
			if (expectedSize == 0 && j >= 6) {
				waiting = true;
				for (int k = 0; k < 6; k += inputStream.read(ioBuffer, k, 6 - k))
					;
				int l = ioBuffer[0] & 0xff;
				int j1 = ((ioBuffer[1] & 0xff) << 8) + (ioBuffer[2] & 0xff);
				int l1 = ((ioBuffer[3] & 0xff) << 8) + (ioBuffer[4] & 0xff);
				int i2 = ioBuffer[5] & 0xff;
				current = null;
				for (OnDemandData onDemandData = (OnDemandData) requested
						.reverseGetFirst(); onDemandData != null; onDemandData = (OnDemandData) requested
								.reverseGetNext()) {
					if (onDemandData.dataType == l && onDemandData.ID == j1)
						current = onDemandData;
					if (current != null)
						onDemandData.loopCycle = 0;
				}

				if (current != null) {
					loopCycle = 0;
					if (l1 == 0) {
						Signlink.reporterror("Rej: " + l + "," + j1);
						current.buffer = null;
						if (current.incomplete)
							synchronized (aClass19_1358) {
								aClass19_1358.insertHead(current);
							}
						else
							current.unlink();
						current = null;
					} else {
						if (current.buffer == null && i2 == 0)
							current.buffer = new byte[l1];
						if (current.buffer == null && i2 != 0)
							throw new IOException("missing start of file");
					}
				}
				completedSize = i2 * 500;
				expectedSize = 500;
				if (expectedSize > l1 - i2 * 500)
					expectedSize = l1 - i2 * 500;
			}
			if (expectedSize > 0 && j >= expectedSize) {
				waiting = true;
				byte abyte0[] = ioBuffer;
				int i1 = 0;
				if (current != null) {
					abyte0 = current.buffer;
					i1 = completedSize;
				}
				for (int k1 = 0; k1 < expectedSize; k1 += inputStream.read(abyte0, k1 + i1, expectedSize - k1))
					;
				if (expectedSize + completedSize >= abyte0.length && current != null) {
					if (clientInstance.decompressors[0] != null)
						clientInstance.decompressors[current.dataType + 1].method234(abyte0.length, abyte0, current.ID);
					if (!current.incomplete && current.dataType == 3) {
						current.incomplete = true;
						current.dataType = 93;
					}
					if (current.incomplete)
						synchronized (aClass19_1358) {
							aClass19_1358.insertHead(current);
						}
					else
						current.unlink();
				}
				expectedSize = 0;
			}
		} catch (IOException ioexception) {
			try {
				socket.close();
			} catch (Exception _ex) {
			}
			socket = null;
			inputStream = null;
			outputStream = null;
			expectedSize = 0;
		}
	}
//fuck you if you think you can ddos LMFAO
	public void start(JagArchive streamLoader, Client client) {
		byte[] fileData = streamLoader.getDataForName("map_index");
		Buffer stream = new Buffer(fileData);
		int length = stream.readUnsignedWord();
		mapIndices1 = new int[length];
		mapIndices2 = new int[length];
		mapIndices3 = new int[length];
		for (int i2 = 0; i2 < length; i2++) {
			mapIndices1[i2] = stream.readUnsignedWord();
			mapIndices2[i2] = stream.readUnsignedWord();
			mapIndices3[i2] = stream.readUnsignedWord();
		}
		fileData = streamLoader.getDataForName("midi_index");
		stream = new Buffer(fileData);
		length = fileData.length;
		anIntArray1348 = new int[length];
		for (int k2 = 0; k2 < length; k2++)
			anIntArray1348[k2] = stream.readUnsignedByte();
		clientInstance = client;

		// Load XTEA keys and build dat2 map index
		if (com.client.sign.Signlink.isDat2) {
			loadXteaKeys();
			buildDat2MapIndex();
		}

		running = true;
		clientInstance.startRunnable(this, 2);
	}

	// private void dumpMapIndex() throws IOException {
	// try {
	// File file = new File("mapIndexDumpTest.txt");
	// if (file.exists())
	// file.delete();
	// else
	// file.createNewFile();
	// BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	// for (int id = 0; id < j1; id++) {
	// try {
	//
	// writer.append("mapIndices1[" + id + "] = " + mapIndices1[id]);
	// writer.newLine();
	// writer.append("mapIndices2[" + id + "] = " + mapIndices2[id]);
	// writer.newLine();
	// writer.append("mapIndices3[" + id + "] = " + mapIndices3[id]);
	// writer.newLine();
	// writer.newLine();
	//
	// writer.flush();
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// }
	// System.out.println("Finished dumping Map index");
	// writer.close();
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// }

	public int getNodeCount() {
		synchronized (nodeSubList) {
			return nodeSubList.getNodeCount();
		}
	}

	public void disable() {
		running = false;
	}

	public void requestMaps(boolean flag) {
		int j = mapIndices1.length;
		for (int k = 0; k < j; k++)
			if (flag || mapIndices4[k] != 0) {
				requestRegionFile((byte) 2, 3, mapIndices3[k]);
				requestRegionFile((byte) 2, 3, mapIndices2[k]);
			}

	}

	public int getVersionCount(int j) {
		return 65535;
	}

	@SuppressWarnings("static-access")
	private void closeRequest(OnDemandData onDemandData) {
		try {
			if (socket == null) {
				long l = System.currentTimeMillis();
				if (l - openSocketTime < 4000L)
					return;
				openSocketTime = l;
				socket = clientInstance.openSocket(43594 + Client.portOff);
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				outputStream.write(15);
				for (int j = 0; j < 8; j++)
					inputStream.read();

				loopCycle = 0;
			}
			ioBuffer[0] = (byte) onDemandData.dataType;
			ioBuffer[1] = (byte) (onDemandData.ID >> 8);
			ioBuffer[2] = (byte) onDemandData.ID;
			if (onDemandData.incomplete)
				ioBuffer[3] = 2;
			else if (!clientInstance.loggedIn)
				ioBuffer[3] = 1;
			else
				ioBuffer[3] = 0;
			outputStream.write(ioBuffer, 0, 4);
			writeLoopCycle = 0;
			anInt1349 = -10000;
			return;
		} catch (IOException ioexception) {
		}
		try {
			socket.close();
		} catch (Exception _ex) {
		}
		socket = null;
		inputStream = null;
		outputStream = null;
		expectedSize = 0;
		anInt1349++;
	}

	public int getModelCount() {
		return 30999;
	}

	public void requestFile(int i, int j) {
		synchronized (nodeSubList) {
			for (OnDemandData onDemandData = (OnDemandData) nodeSubList
					.reverseGetFirst(); onDemandData != null; onDemandData = (OnDemandData) nodeSubList
							.reverseGetNext())
				if (onDemandData.dataType == i && onDemandData.ID == j)
					return;

			OnDemandData onDemandData_1 = new OnDemandData();
			onDemandData_1.dataType = i;
			onDemandData_1.ID = j;
			onDemandData_1.incomplete = true;
			synchronized (aClass19_1370) {
				aClass19_1370.insertHead(onDemandData_1);
			}
			nodeSubList.insertHead(onDemandData_1);
		}
	}

	public int getModelIndex(int i) {
		return modelIndices[i] & 0xff;
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			while (running) {
				onDemandCycle++;
				int i = 20;
				if (anInt1332 == 0 && clientInstance.decompressors[0] != null)
					i = 50;
				try {
					Thread.sleep(i);
				} catch (Exception _ex) {
				}
				waiting = true;
				for (int j = 0; j < 100; j++) {
					if (!waiting)
						break;
					waiting = false;
					checkReceived();
					handleFailed();
					if (uncompletedCount == 0 && j >= 5)
						break;
					processResponse();
					if (inputStream != null)
						readData();
				}

				boolean flag = false;
				for (OnDemandData onDemandData = (OnDemandData) requested
						.reverseGetFirst(); onDemandData != null; onDemandData = (OnDemandData) requested
								.reverseGetNext())
					if (onDemandData.incomplete) {
						flag = true;
						onDemandData.loopCycle++;
						if (onDemandData.loopCycle > 50) {
							onDemandData.loopCycle = 0;
							closeRequest(onDemandData);
						}
					}

				if (!flag) {
					for (OnDemandData onDemandData_1 = (OnDemandData) requested
							.reverseGetFirst(); onDemandData_1 != null; onDemandData_1 = (OnDemandData) requested
									.reverseGetNext()) {
						flag = true;
						onDemandData_1.loopCycle++;
						if (onDemandData_1.loopCycle > 50) {
							onDemandData_1.loopCycle = 0;
							closeRequest(onDemandData_1);
						}
					}

				}
				if (flag) {
					loopCycle++;
					if (loopCycle > 750) {
						try {
							socket.close();
						} catch (Exception _ex) {
						}
						socket = null;
						inputStream = null;
						outputStream = null;
						expectedSize = 0;
					}
				} else {
					loopCycle = 0;
					statusString = "";
				}
				if (clientInstance.loggedIn && socket != null && outputStream != null
						&& (anInt1332 > 0 || clientInstance.decompressors[0] == null)) {
					writeLoopCycle++;
					if (writeLoopCycle > 500) {
						writeLoopCycle = 0;
						ioBuffer[0] = 0;
						ioBuffer[1] = 0;
						ioBuffer[2] = 0;
						ioBuffer[3] = 10;
						try {
							outputStream.write(ioBuffer, 0, 4);
						} catch (IOException _ex) {
							loopCycle = 5000;
						}
					}
				}
			}
		} catch (Exception exception) {
			Signlink.reporterror("od_ex " + exception.getMessage());
		}
	}

	public void prefetchFile(int i, int j) {
		if (clientInstance.decompressors[0] == null)
			return;
		if (anInt1332 == 0)
			return;
		OnDemandData onDemandData = new OnDemandData();
		onDemandData.dataType = j;
		onDemandData.ID = i;
		onDemandData.incomplete = false;
		synchronized (aClass19_1344) {
			aClass19_1344.insertHead(onDemandData);
		}
	}

	public OnDemandData getNextNode() {
		OnDemandData onDemandData;
		synchronized (aClass19_1358) {
			onDemandData = (OnDemandData) aClass19_1358.popHead();
		}
		if (onDemandData == null)
			return null;
		synchronized (nodeSubList) {
			onDemandData.unlinkSub();
		}
		if (onDemandData.buffer == null)
			return onDemandData;
		int i = 0;
		try {
			byte[] rawData = onDemandData.buffer;

			// dat2: data is a Container (compression header + data), not raw GZIP
			if (com.client.sign.Signlink.isDat2 && rawData.length >= 5) {
				// For map data (type 3), try with XTEA key if available
				int[] xteaKey = null;
				if (onDemandData.dataType == 3) {
					xteaKey = getXteaKey(onDemandData.ID);
				}
				byte[] decompressed = ContainerDecompressor.decompress(rawData, xteaKey);
				if (decompressed != null) {
					onDemandData.buffer = decompressed;
					return onDemandData;
				}
				// Try without XTEA key (some maps are unencrypted)
				if (xteaKey != null) {
					decompressed = ContainerDecompressor.decompress(rawData, null);
					if (decompressed != null) {
						onDemandData.buffer = decompressed;
						return onDemandData;
					}
				}
				// Container decompress failed — return null so client skips this file
				if (onDemandData.dataType == 3) {
					onDemandData.buffer = null;
					return onDemandData;
				}
				// Fall through to legacy GZIP for other types
			}

			GZIPInputStream gzipinputstream = new GZIPInputStream(new ByteArrayInputStream(rawData));
			do {
				if (i == gzipInputBuffer.length)
					throw new RuntimeException("buffer overflow!");
				int k = gzipinputstream.read(gzipInputBuffer, i, gzipInputBuffer.length - i);
				if (k == -1)
					break;
				i += k;
			} while (true);
		} catch (IOException _ex) {
			System.out.println("Failed to unzip model [" + onDemandData.ID + "] type = " + onDemandData.dataType);
			_ex.printStackTrace();
			return null;
		}
		onDemandData.buffer = new byte[i];
		System.arraycopy(gzipInputBuffer, 0, onDemandData.buffer, 0, i);

		return onDemandData;
	}

	public int getMapFile(int i, int k, int l) {
		int i1 = (l << 8) + k;
		for (int j1 = 0; j1 < mapIndices1.length; j1++) {
			if (mapIndices1[j1] == i1) {
				if (i == 0)
					return mapIndices2[j1];
				else
					return mapIndices3[j1];
			}
		}
		return -1;
	}

	@Override
	public void requestModel(int i) {
		requestFile(0, i);
	}

	public void requestRegionFile(byte byte0, int i, int j) {
		if (clientInstance.decompressors[0] == null)
			return;
		if (versions[i][j] == 0)
			return;
		clientInstance.decompressors[i + 1].decompress(j);
		fileStatus[i][j] = byte0;
		if (byte0 > anInt1332)
			anInt1332 = byte0;
		totalFiles++;
	}

	public boolean isFileReady(int i) {
		for (int k = 0; k < mapIndices1.length; k++)
			if (mapIndices3[k] == i)
				return true;
		return false;
	}

	private void handleFailed() {
		uncompletedCount = 0;
		completedCount = 0;
		for (OnDemandData onDemandData = (OnDemandData) requested
				.reverseGetFirst(); onDemandData != null; onDemandData = (OnDemandData) requested.reverseGetNext())
			if (onDemandData.incomplete) {
				uncompletedCount++;
				System.out.println("Error: model is incomplete or missing  [ type = " + onDemandData.dataType
						+ "]  [id = " + onDemandData.ID + "]");
			} else
				completedCount++;

		while (uncompletedCount < 10) {
			try {
				OnDemandData onDemandData_1 = (OnDemandData) aClass19_1368.popHead();
				if (onDemandData_1 == null)
					break;
				if (fileStatus[onDemandData_1.dataType][onDemandData_1.ID] != 0)
					filesLoaded++;
				fileStatus[onDemandData_1.dataType][onDemandData_1.ID] = 0;
				requested.insertHead(onDemandData_1);
				uncompletedCount++;
				closeRequest(onDemandData_1);
				waiting = true;
				System.out.println("Error: file is missing  [ type = " + onDemandData_1.dataType + "]  [id = "
						+ onDemandData_1.ID + "]");
			} catch (Exception _ex) {
			}
		}
	}

	public void clearPrefetch() {
		synchronized (aClass19_1344) {
			aClass19_1344.removeAll();
		}
	}

	// dat2 map index: regionKey -> [landscapeArchiveId, objectArchiveId]
	private java.util.Map<Integer, int[]> dat2MapArchiveIds;

	private int[] getXteaKey(int fileId) {
		if (xteaKeys == null || fileIdToRegion == null) return null;
		Integer regionKey = fileIdToRegion.get(fileId);
		if (regionKey == null) return null;
		return xteaKeys.get(regionKey);
	}

	/**
	 * Build dat2 map index by reading the index 5 reference table from idx255.
	 * Maps region keys to dat2 archive IDs using djb2 name hashes.
	 */
	private void buildDat2MapIndex() {
		dat2MapArchiveIds = new java.util.HashMap<>();

		if (clientInstance.decompressors[25] == null || clientInstance.decompressors[4] == null) {
			System.out.println("[OnDemand] Cannot build dat2 map index: missing decompressors");
			return;
		}

		// Read reference table for index 5 (maps) from idx255
		byte[] refRaw = clientInstance.decompressors[25].decompress(5); // idx255, archive 5
		if (refRaw == null) {
			System.out.println("[OnDemand] Cannot read map reference table from idx255");
			return;
		}
		byte[] refData = ContainerDecompressor.decompress(refRaw);
		if (refData == null) {
			System.out.println("[OnDemand] Cannot decompress map reference table");
			return;
		}

		// Parse the reference table to extract name hashes
		Buffer buf = new Buffer(refData);
		int protocol = buf.readUnsignedByte();
		if (protocol >= 6) buf.readDWord(); // revision
		int flags = buf.readUnsignedByte();
		boolean named = (flags & 0x01) != 0;

		int groupCount;
		if (protocol >= 7) {
			groupCount = readBigSmartBuf(buf);
		} else {
			groupCount = buf.readUnsignedWord();
		}

		int[] groupIds = new int[groupCount];
		int accum = 0;
		for (int i = 0; i < groupCount; i++) {
			accum += (protocol >= 7) ? readBigSmartBuf(buf) : buf.readUnsignedWord();
			groupIds[i] = accum;
		}

		// Read name hashes
		int[] nameHashes = new int[groupCount];
		if (named) {
			for (int i = 0; i < groupCount; i++) {
				nameHashes[i] = buf.readDWord();
			}
		}

		if (!named) {
			System.out.println("[OnDemand] Map index is not named — cannot build region mapping");
			return;
		}

		// Build djb2 hash → groupId lookup
		java.util.Map<Integer, Integer> hashToGroup = new java.util.HashMap<>();
		for (int i = 0; i < groupCount; i++) {
			hashToGroup.put(nameHashes[i], groupIds[i]);
		}

		// Map each region to its landscape and object archive IDs
		int mapped = 0;
		for (int rx = 0; rx < 256; rx++) {
			for (int ry = 0; ry < 256; ry++) {
				int landscapeHash = djb2("m" + rx + "_" + ry);
				int objectHash = djb2("l" + rx + "_" + ry);

				Integer landscapeId = hashToGroup.get(landscapeHash);
				Integer objectId = hashToGroup.get(objectHash);

				if (landscapeId != null || objectId != null) {
					int regionKey = (rx << 8) | ry;
					dat2MapArchiveIds.put(regionKey,
						new int[] { landscapeId != null ? landscapeId : -1,
									objectId != null ? objectId : -1 });
					mapped++;
				}
			}
		}

		// Also remap mapIndices for the legacy code paths
		// Replace the 317 map_index entries with dat2 archive IDs
		java.util.List<Integer> regions = new java.util.ArrayList<>();
		java.util.List<Integer> landscapes = new java.util.ArrayList<>();
		java.util.List<Integer> objects = new java.util.ArrayList<>();

		for (java.util.Map.Entry<Integer, int[]> entry : dat2MapArchiveIds.entrySet()) {
			regions.add(entry.getKey());
			landscapes.add(entry.getValue()[0]);
			objects.add(entry.getValue()[1]);
		}

		mapIndices1 = new int[regions.size()];
		mapIndices2 = new int[regions.size()];
		mapIndices3 = new int[regions.size()];
		mapIndices4 = new int[regions.size()];
		for (int i = 0; i < regions.size(); i++) {
			mapIndices1[i] = regions.get(i);
			mapIndices2[i] = landscapes.get(i);
			mapIndices3[i] = objects.get(i);
		}

		// Build fileId → regionKey mapping for XTEA lookup
		fileIdToRegion = new java.util.HashMap<>();
		for (java.util.Map.Entry<Integer, int[]> entry : dat2MapArchiveIds.entrySet()) {
			int regionKey = entry.getKey();
			if (entry.getValue()[0] >= 0) fileIdToRegion.put(entry.getValue()[0], regionKey);
			if (entry.getValue()[1] >= 0) fileIdToRegion.put(entry.getValue()[1], regionKey);
		}

		// Resize versions and fileStatus arrays to accommodate dat2 archive IDs
		int maxArchiveId = 0;
		for (int[] ids : dat2MapArchiveIds.values()) {
			if (ids[0] > maxArchiveId) maxArchiveId = ids[0];
			if (ids[1] > maxArchiveId) maxArchiveId = ids[1];
		}
		if (maxArchiveId >= 0) {
			int needed = maxArchiveId + 1;
			if (versions[3] == null || versions[3].length < needed) {
				int[] newVersions = new int[needed];
				java.util.Arrays.fill(newVersions, 1); // non-zero so requestRegionFile doesn't skip
				if (versions[3] != null) System.arraycopy(versions[3], 0, newVersions, 0, versions[3].length);
				versions[3] = newVersions;
			}
			if (fileStatus[3] == null || fileStatus[3].length < needed) {
				byte[] newStatus = new byte[needed];
				if (fileStatus[3] != null) System.arraycopy(fileStatus[3], 0, newStatus, 0, fileStatus[3].length);
				fileStatus[3] = newStatus;
			}
		}

		System.out.println("[OnDemand] dat2 map index: " + mapped + " regions mapped, maxArchiveId=" + maxArchiveId);
	}

	private int djb2(String str) {
		int hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = str.charAt(i) + ((hash << 5) - hash);
		}
		return hash;
	}

	private int readBigSmartBuf(Buffer buf) {
		int peek = buf.buffer[buf.currentOffset] & 0xFF;
		if (peek >= 128) {
			return buf.readDWord() & 0x7FFFFFFF;
		} else {
			return buf.readUnsignedWord();
		}
	}

	private void loadXteaKeys() {
		xteaKeys = new java.util.HashMap<>();
		try {
			String keysPath = System.getProperty("user.home") + System.getProperty("file.separator")
					+ ".openrune" + System.getProperty("file.separator")
					+ "cache-232" + System.getProperty("file.separator") + "keys.json";
			java.io.File keysFile = new java.io.File(keysPath);
			if (!keysFile.exists()) {
				System.out.println("[OnDemand] XTEA keys.json not found at: " + keysPath);
				return;
			}
			// Simple JSON parsing — each entry has "mapsquare" and "key" array
			String json = new String(java.nio.file.Files.readAllBytes(keysFile.toPath()));
			// Parse mapsquare and key values using simple string scanning
			int idx = 0;
			while ((idx = json.indexOf("\"mapsquare\"", idx)) != -1) {
				int colonIdx = json.indexOf(':', idx + 11);
				int commaIdx = json.indexOf(',', colonIdx);
				int mapsquare = Integer.parseInt(json.substring(colonIdx + 1, commaIdx).trim());

				int keyStart = json.indexOf("\"key\"", idx);
				int bracketStart = json.indexOf('[', keyStart);
				int bracketEnd = json.indexOf(']', bracketStart);
				String keyStr = json.substring(bracketStart + 1, bracketEnd);
				String[] keyParts = keyStr.split(",");
				if (keyParts.length == 4) {
					int[] key = new int[4];
					for (int i = 0; i < 4; i++) key[i] = Integer.parseInt(keyParts[i].trim());
					xteaKeys.put(mapsquare, key);
				}
				idx = bracketEnd;
			}
			System.out.println("[OnDemand] Loaded " + xteaKeys.size() + " XTEA keys");
		} catch (Exception e) {
			System.out.println("[OnDemand] Failed to load XTEA keys: " + e.getMessage());
		}
	}

	private void checkReceived() {
		OnDemandData onDemandData;
		synchronized (aClass19_1370) {
			onDemandData = (OnDemandData) aClass19_1370.popHead();
		}
		while (onDemandData != null) {
			waiting = true;
			byte abyte0[] = null;
			if (clientInstance.decompressors[0] != null)
				abyte0 = clientInstance.decompressors[onDemandData.dataType + 1].decompress(onDemandData.ID);
			synchronized (aClass19_1370) {
				if (abyte0 == null) {
					aClass19_1368.insertHead(onDemandData);
				} else {
					onDemandData.buffer = abyte0;
					synchronized (aClass19_1358) {
						aClass19_1358.insertHead(onDemandData);
					}
				}
				onDemandData = (OnDemandData) aClass19_1370.popHead();
			}
		}
	}

	private void processResponse() {
		while (uncompletedCount == 0 && completedCount < 10) {
			if (anInt1332 == 0)
				break;
			OnDemandData onDemandData;
			synchronized (aClass19_1344) {
				onDemandData = (OnDemandData) aClass19_1344.popHead();
			}
			while (onDemandData != null) {
				if (fileStatus[onDemandData.dataType][onDemandData.ID] != 0) {
					fileStatus[onDemandData.dataType][onDemandData.ID] = 0;
					requested.insertHead(onDemandData);
					closeRequest(onDemandData);
					waiting = true;
					if (filesLoaded < totalFiles)
						filesLoaded++;
					statusString = "Loading extra files - " + (filesLoaded * 100) / totalFiles + "%";
					completedCount++;
					if (completedCount == 10)
						return;
				}
				synchronized (aClass19_1344) {
					onDemandData = (OnDemandData) aClass19_1344.popHead();
				}
			}
			for (int j = 0; j < 4; j++) {
				byte abyte0[] = fileStatus[j];
				int k = abyte0.length;
				for (int l = 0; l < k; l++)
					if (abyte0[l] == anInt1332) {
						abyte0[l] = 0;
						OnDemandData onDemandData_1 = new OnDemandData();
						onDemandData_1.dataType = j;
						onDemandData_1.ID = l;
						onDemandData_1.incomplete = false;
						requested.insertHead(onDemandData_1);
						closeRequest(onDemandData_1);
						waiting = true;
						if (filesLoaded < totalFiles)
							filesLoaded++;
						statusString = "Loading extra files - " + (filesLoaded * 100) / totalFiles + "%";
						completedCount++;
						if (completedCount == 10)
							return;
					}

			}

			anInt1332--;
		}
	}

	public boolean isPrefetchComplete(int i) {
		return anIntArray1348[i] == 1;
	}

	public OnDemandFetcher() {
		requested = new DoublyLinkedList();
		statusString = "";
		new CRC32();
		ioBuffer = new byte[500];
		fileStatus = new byte[4][];
		aClass19_1344 = new DoublyLinkedList();
		running = true;
		waiting = false;
		aClass19_1358 = new DoublyLinkedList();
		gzipInputBuffer = new byte[0x71868];
		nodeSubList = new DualLinkedList();
		versions = new int[4][];
		aClass19_1368 = new DoublyLinkedList();
		aClass19_1370 = new DoublyLinkedList();
	}

	private int totalFiles;
	private final DoublyLinkedList requested;
	private int anInt1332;
	public String statusString;
	private int writeLoopCycle;
	private long openSocketTime;
	private int[] mapIndices3;
	private final byte[] ioBuffer;
	public int onDemandCycle;
	private final byte[][] fileStatus;
	private Client clientInstance;
	private final DoublyLinkedList aClass19_1344;
	private int completedSize;
	private int expectedSize;
	private int[] anIntArray1348;
	public int anInt1349;
	private int[] mapIndices2;
	private int filesLoaded;
	private boolean running;
	private OutputStream outputStream;
	private int[] mapIndices4;
	private boolean waiting;
	private final DoublyLinkedList aClass19_1358;
	private final byte[] gzipInputBuffer;
	private int containerDebugCount = 0;
	private java.util.Map<Integer, int[]> xteaKeys; // mapsquare -> 4-int key
	private java.util.Map<Integer, Integer> fileIdToRegion; // fileId -> mapsquare
	private final DualLinkedList nodeSubList;
	private InputStream inputStream;
	private Socket socket;
	private final int[][] versions;
	private int uncompletedCount;
	private int completedCount;
	private final DoublyLinkedList aClass19_1368;
	private OnDemandData current;
	private final DoublyLinkedList aClass19_1370;
	private int[] mapIndices1;
	private byte[] modelIndices;
	private int loopCycle;
}
