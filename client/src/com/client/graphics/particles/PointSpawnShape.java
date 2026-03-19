package com.client.graphics.particles;

import com.client.graphics.particles.ParticleVector;
import com.client.graphics.particles.SpawnShape;
import java.util.Random;

public class PointSpawnShape implements SpawnShape {
   private ParticleVector vector;

   public PointSpawnShape(ParticleVector vector) {
      this.vector = vector;
   }

   public final ParticleVector divide(Random random) {
      return this.vector.clone();
   }
}
