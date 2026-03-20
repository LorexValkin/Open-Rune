package com.client;

public final class SceneObject {

    public SceneObject() {
    }

    int sceneLevel;
    int worldY;
    int worldX;
    int worldZ;
    public Renderable renderable;
    public int rotation;
    int tileStartX;
    int tileEndX;
    int tileStartY;
    int tileEndY;
    int renderHeight;
    int renderCycle;
    public int uid;
    byte flags;
}
