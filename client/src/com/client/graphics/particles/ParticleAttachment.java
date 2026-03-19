package com.client.graphics.particles;

import java.util.HashMap;
import java.util.Map;

public class ParticleAttachment {
   private static Map attachments = new HashMap();

   static {
      attachments.put(Integer.valueOf(29616), new int[][]{{272, 0}, {49, 0}, {37, 0}, {17, 0}, {41, 0}, {283, 0}, {315, 0}});
      attachments.put(Integer.valueOf(29624), new int[][]{{249, 0}, {49, 0}, {37, 0}, {17, 0}, {41, 0}, {283, 0}, {313, 0}});
      attachments.put(Integer.valueOf(31896), new int[][]{{33, 1}, {32, 1}});
      attachments.put(Integer.valueOf(13333), new int[][]{{18, 1}, {19, 1}, {31, 1}, {46, 1}, {29, 1}, {17, 1}});
      attachments.put(Integer.valueOf(13341), new int[][]{{18, 1}, {19, 1}, {31, 1}, {46, 1}, {29, 1}, {17, 1}});
      attachments.put(Integer.valueOf(31756), new int[][]{{89, 2}});
      attachments.put(Integer.valueOf(31830), new int[][]{{100, 2}});
      attachments.put(Integer.valueOf('\u814e'), new int[][]{{257, 3}, {40, 3}, {55, 3}, {19, 3}, {17, 3}, {5, 3}, {18, 3}, {204, 3}, {206, 3}, {268, 3}, {271, 3}});
   }

   public static int[][] getAttachments(int model) {
      return (int[][])attachments.get(Integer.valueOf(model));
   }
}
