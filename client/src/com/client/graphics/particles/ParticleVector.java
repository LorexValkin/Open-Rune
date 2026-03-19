package com.client.graphics.particles;

public class ParticleVector {
   public static ParticleVector ZERO = new ParticleVector(0, 0, 0);
   private int anInt46;
   private int anInt47;
   private int anInt48;

   public ParticleVector(int x, int y, int z) {
      this.anInt46 = x;
      this.anInt47 = y;
      this.anInt48 = z;
   }

   public final int getX() {
      return this.anInt46;
   }

   public final int getY() {
      return this.anInt47;
   }

   public final int getZ() {
      return this.anInt48;
   }

   public final ParticleVector subtract(ParticleVector other) {
      return new ParticleVector(this.anInt46 - other.anInt46, this.anInt47 - other.anInt47, this.anInt48 - other.anInt48);
   }

   public final ParticleVector divide(float scalar) {
      return new ParticleVector((int)((float)this.anInt46 / scalar), (int)((float)this.anInt47 / scalar), (int)((float)this.anInt48 / scalar));
   }

   public final ParticleVector addLocal(ParticleVector other) {
      this.anInt46 += other.anInt46;
      this.anInt47 += other.anInt47;
      this.anInt48 += other.anInt48;
      return this;
   }

   public final ParticleVector clone() {
      return new ParticleVector(this.anInt46, this.anInt47, this.anInt48);
   }

   public final String toString() {
      return "Vector{x=" + this.anInt46 + ", y=" + this.anInt47 + ", z=" + this.anInt48 + '\u007d';
   }
}
