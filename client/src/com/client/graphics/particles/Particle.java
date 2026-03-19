package com.client.graphics.particles;

import com.client.graphics.particles.ParticleDefinition;
import com.client.graphics.particles.ParticleVector;

public class Particle {
   private int anInt17;
   private int color;
   private float size;
   private float alpha;
   private boolean dead;
   private ParticleDefinition aParticleDefinition18;
   private ParticleVector velocity;
   private ParticleVector position;
   private int particleDepth;
   private float oldAlpha;

   public final void tick() {
      if(this.aParticleDefinition18 != null) {
         ++this.anInt17;
         if(this.anInt17 >= this.aParticleDefinition18.getLifeSpan()) {
            this.dead = true;
         } else {
            this.color += this.aParticleDefinition18.getColorStep();
            this.size += this.aParticleDefinition18.getSizeStep();
            this.position.addLocal(this.velocity);
            this.velocity.addLocal(this.aParticleDefinition18.getVelocityStep());
            this.alpha += this.aParticleDefinition18.getAlphaStep();
            if(this.aParticleDefinition18.getGravity() != null) {
               this.position.addLocal(this.aParticleDefinition18.getGravity());
            }

            if(this.alpha <= 0.0F) {
               this.alpha = 0.025F;
            }
         }
      }

   }

   public Particle(ParticleDefinition def, ParticleVector position, int particleDepth, int definitionID) {
      this(def.getStartColor(), def.getStartSize(), def.getStartVelocity(definitionID).clone(), def.getSpawnedShape().divide(ParticleDefinition.RANDOM).addLocal(position), def.getStartAlpha(), particleDepth);
      this.aParticleDefinition18 = def;
   }

   public Particle(int color, float size, ParticleVector velocity, ParticleVector position, float alpha, int particleDepth) {
      this.anInt17 = 0;
      this.dead = false;
      this.aParticleDefinition18 = null;
      this.color = color;
      this.size = size;
      this.velocity = velocity;
      this.position = position;
      this.alpha = alpha;
      this.particleDepth = particleDepth;
      this.oldAlpha = alpha;
   }

   public final int getAge() {
      return this.anInt17;
   }

   public void setAge(int age) {
      this.anInt17 = age;
   }

   public final float getOldAlpha() {
      return this.oldAlpha;
   }

   public final int getDepth() {
      return this.particleDepth;
   }

   public void setDepth(int particleDepth) {
      this.particleDepth = particleDepth;
   }

   public final int getColor() {
      return this.color;
   }

   public Particle setColor(int color) {
      this.color = color;
      return this;
   }

   public final float getAlpha() {
      return this.alpha;
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   public final float getSize() {
      return this.size;
   }

   public void setSize(float size) {
      this.size = size;
   }

   public void setDead(boolean dead) {
      this.dead = dead;
   }

   public final boolean isDead() {
      return this.dead;
   }

   public ParticleVector getVelocity() {
      return this.velocity;
   }

   public final ParticleDefinition getDefinition() {
      return this.aParticleDefinition18;
   }

   public final ParticleVector getPosition() {
      return this.position;
   }
}
