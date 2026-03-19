package com.client.graphics.particles;

import com.client.Sprite;
import com.client.graphics.particles.ParticleVector;
import com.client.graphics.particles.PointSpawnShape;
import com.client.graphics.particles.SpawnShape;
import java.util.Random;

public class ParticleDefinition {
   public int particleDepth;
   public static Random RANDOM = new Random(System.currentTimeMillis());
   public static ParticleDefinition[] cache = new ParticleDefinition[]{new ParticleDefinition() {
      {
         this.setStartVelocity(new ParticleVector(0, -1, 0));
         this.setEndVelocity(new ParticleVector(0, -1, 0));
         this.setGravity(new ParticleVector(0, 0, 0));
         this.setLifeSpan(19);
         this.setStartColor(16777215);
         this.setSpawnRate(4);
         this.setStartSize(1.1F);
         this.setEndSize(0.0F);
         this.setStartAlpha(0.095F);
         this.updateSteps();
         this.setColorStep(0);
      }
   }, new ParticleDefinition() {
      {
         this.setStartVelocity(new ParticleVector(0, 2, 0));
         this.setEndVelocity(new ParticleVector(0, 2, 0));
         this.setGravity(new ParticleVector(0, 1, 0));
         this.setLifeSpan(15);
         this.setStartColor(16713728);
         this.setSpawnRate(3);
         this.setStartSize(2.0F);
         this.setEndSize(0.5F);
         this.setStartAlpha(0.0F);
         this.setEndAlpha(0.095F);
         this.updateSteps();
         this.setColorStep(2304);
      }
   }, new ParticleDefinition() {
      {
         this.setStartVelocity(new ParticleVector(0, 0, 0));
         this.setEndVelocity(new ParticleVector(0, 0, 0));
         this.setGravity(new ParticleVector(0, 0, 0));
         this.setLifeSpan(50);
         this.setStartColor(16777113);
         this.setSpawnRate(30);
         this.setStartSize(1.0F);
         this.setEndSize(0.05F);
         this.setStartAlpha(0.03F);
         this.setEndAlpha(0.03F);
         this.updateSteps();
         this.setColorStep(16777113);
      }
   }, new ParticleDefinition() {
      {
         this.setStartVelocity(new ParticleVector(0, -1, 0));
         this.setEndVelocity(new ParticleVector(0, -1, 0));
         this.setGravity(new ParticleVector(0, 0, 0));
         this.setLifeSpan(19);
         this.setStartColor(0);
         this.setSpawnRate(4);
         this.setStartSize(1.1F);
         this.setEndSize(0.0F);
         this.setStartAlpha(0.095F);
         this.updateSteps();
         this.setColorStep(0);
      }
   }};
   private float startSize = 1.0F;
   private float endSize = 1.0F;
   private int startColor = -1;
   private int endColor = -1;
   private ParticleVector startVelocity;
   private ParticleVector endVelocity;
   private SpawnShape spawnShape;
   private float startAlpha;
   private float endAlpha;
   private int lifeSpan;
   private int spawnRate;
   private Sprite sprite;
   private ParticleVector gravity;
   private int colorStep;
   private float sizeStep;
   private ParticleVector velocityStep;
   private float alphaStep;

   public ParticleDefinition() {
      this.startVelocity = ParticleVector.ZERO;
      this.endVelocity = ParticleVector.ZERO;
      this.spawnShape = new PointSpawnShape(ParticleVector.ZERO);
      this.startAlpha = 1.0F;
      this.endAlpha = 0.05F;
      this.lifeSpan = 1;
      this.spawnRate = 1;
   }

   public final SpawnShape getSpawnedShape() {
      return this.spawnShape;
   }

   public final float getStartAlpha() {
      return this.startAlpha;
   }

   public final void setStartAlpha(float startAlpha) {
      this.startAlpha = startAlpha;
   }

   public final float getAlphaStep() {
      return this.alphaStep;
   }

   public final Sprite getSprite() {
      return this.sprite;
   }

   public final void setSprite(Sprite sprite) {
      this.sprite = sprite;
   }

   public final int getSpawnRate() {
      return this.spawnRate;
   }

   public final void setSpawnRate(int spawnRate) {
      this.spawnRate = spawnRate;
   }

   public final void setStartSize(float startSize) {
      this.startSize = startSize;
   }

   public final float getStartSize() {
      return this.startSize;
   }

   public float getEndSize() {
      return this.endSize;
   }

   public int getEndColor() {
      return this.endColor;
   }

   public final void setEndSize(float endSize) {
      this.endSize = endSize;
   }

   public final int getStartColor() {
      return this.startColor;
   }

   public final void setStartColor(int startColor) {
      this.startColor = startColor;
   }

   public int randomWithRange(int min, int max) {
      int range = max - min + 1;
      return (int)(Math.random() * (double)range) + min;
   }

   public final ParticleVector getStartVelocity(int id) {
      return new ParticleVector(this.startVelocity.getX() + this.randomWithRange(-1, 1), this.startVelocity.getY() + this.randomWithRange(0, 0), this.startVelocity.getZ() + this.randomWithRange(-1, 1));
   }

   public void setEndAlpha(float endAlpha) {
      this.endAlpha = endAlpha;
   }

   public ParticleVector getGravity() {
      return this.gravity;
   }

   public void setGravity(ParticleVector gravity) {
      this.gravity = gravity;
   }

   public final void setStartVelocity(ParticleVector startVelocity) {
      this.startVelocity = startVelocity;
   }

   public ParticleVector getEndVelocity() {
      return this.endVelocity;
   }

   public final void setEndVelocity(ParticleVector endVelocity) {
      this.endVelocity = endVelocity;
   }

   public final int getLifeSpan() {
      return this.lifeSpan;
   }

   public final void setLifeSpan(int lifeSpan) {
      this.lifeSpan = lifeSpan;
   }

   public final void setColorStep(int colorStep) {
      this.colorStep = colorStep;
   }

   public final float getSizeStep() {
      return this.sizeStep;
   }

   public final ParticleVector getVelocityStep() {
      return this.velocityStep;
   }

   public final int getColorStep() {
      return this.colorStep;
   }

   public final void updateSteps() {
      this.sizeStep = (this.endSize - this.startSize) / ((float)this.lifeSpan * 1.0F);
      this.colorStep = (this.endColor - this.startColor) / this.lifeSpan;
      this.velocityStep = this.endVelocity.subtract(this.startVelocity).divide((float)this.lifeSpan);
      this.alphaStep = (this.endAlpha - this.startAlpha) / (float)this.lifeSpan;
   }
}
