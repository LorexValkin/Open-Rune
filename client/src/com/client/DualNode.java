package com.client;

public class DualNode extends CacheableNode {

    public final void unlinkSub() {
        if (nextNodeSub == null) {
        } else {
            nextNodeSub.prevNodeSub = prevNodeSub;
            prevNodeSub.nextNodeSub = nextNodeSub;
            prevNodeSub = null;
            nextNodeSub = null;
        }
    }

    public DualNode() {
    }

    public DualNode prevNodeSub;
    DualNode nextNodeSub;
}
