package io.eclipse.arcana.scratch;

import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageAnalyzer {
    public static void main(String[] args) {
        String[] files = {
            "assets/choose/WANDS_features.png",
            "assets/choose/SWORDS_features.png",
            "assets/choose/PENTACLES_features.png",
            "assets/choose/CUPS_features.png"
        };
        for (String f : files) {
            File file = new File(f);
            if (!file.exists()) {
                System.out.println(f + " not found");
                continue;
            }
            try {
                BufferedImage img = ImageIO.read(file);
                int w = img.getWidth();
                int h = img.getHeight();
                
                int minX = w, maxX = 0;
                int minY = h, maxY = 0;
                int count = 0;
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int rgb = img.getRGB(x, y);
                        int alpha = (rgb >> 24) & 0xff;
                        if (alpha > 10) { // non-transparent
                            if (x < minX) minX = x;
                            if (x > maxX) maxX = x;
                            if (y < minY) minY = y;
                            if (y > maxY) maxY = y;
                            count++;
                        }
                    }
                }
                if (count > 0) {
                    System.out.println(f + ": active pixels = " + count);
                    System.out.println("  X bounds: " + minX + " to " + maxX + " (center=" + (minX + maxX)/2 + ", width=" + (maxX - minX + 1) + ")");
                    System.out.println("  Y bounds: " + minY + " to " + maxY + " (center=" + (minY + maxY)/2 + ", height=" + (maxY - minY + 1) + ")");
                } else {
                    System.out.println(f + ": Completely transparent!");
                }
            } catch (Exception e) {
                System.out.println(f + ": Error - " + e.getMessage());
            }
        }
    }
}
