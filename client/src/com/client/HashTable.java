package com.client;

import com.client.sign.Signlink;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 


final class HashTable {

	public HashTable() {
		int i = 1024;// was parameter
		size = i;
		cache = new CacheableNode[i];
		for (int k = 0; k < i; k++) {
			CacheableNode node = cache[k] = new CacheableNode();
			node.prev = node;
			node.next = node;
		}

	}

	public CacheableNode findNodeByID(long l) {
		CacheableNode node = cache[(int) (l & size - 1)];
		for (CacheableNode node_1 = node.prev; node_1 != node; node_1 = node_1.prev)
			if (node_1.id == l)
				return node_1;

		return null;
	}

	public void removeFromCache(CacheableNode node, long l) {
		try {
			if (node.next != null)
				node.unlink();
			CacheableNode node_1 = cache[(int) (l & size - 1)];
			node.next = node_1.next;
			node.prev = node_1;
			node.next.prev = node;
			node.prev.next = node;
			node.id = l;
			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("91499, " + node + ", " + l + ", " + (byte) 7
					+ ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	private final int size;
	private final CacheableNode[] cache;
}
