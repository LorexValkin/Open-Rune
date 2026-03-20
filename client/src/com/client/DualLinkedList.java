package com.client;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class DualLinkedList {

	public DualLinkedList() {
		head = new DualNode();
		head.prevNodeSub = head;
		head.nextNodeSub = head;
	}

	public void insertHead(DualNode nodeSub) {
		if (nodeSub.nextNodeSub != null)
			nodeSub.unlinkSub();
		nodeSub.nextNodeSub = head.nextNodeSub;
		nodeSub.prevNodeSub = head;
		nodeSub.nextNodeSub.prevNodeSub = nodeSub;
		nodeSub.prevNodeSub.nextNodeSub = nodeSub;
	}

	public DualNode popTail() {
		DualNode nodeSub = head.prevNodeSub;
		if (nodeSub == head) {
			return null;
		} else {
			nodeSub.unlinkSub();
			return nodeSub;
		}
	}

	public DualNode reverseGetFirst() {
		DualNode nodeSub = head.prevNodeSub;
		if (nodeSub == head) {
			current = null;
			return null;
		} else {
			current = nodeSub.prevNodeSub;
			return nodeSub;
		}
	}

	public DualNode reverseGetNext() {
		DualNode nodeSub = current;
		if (nodeSub == head) {
			current = null;
			return null;
		} else {
			current = nodeSub.prevNodeSub;
			return nodeSub;
		}
	}

	public int getNodeCount() {
		int i = 0;
		for (DualNode nodeSub = head.prevNodeSub; nodeSub != head; nodeSub = nodeSub.prevNodeSub)
			i++;

		return i;
	}

	private final DualNode head;
	private DualNode current;
}
