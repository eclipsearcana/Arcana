package io.eclipse.arcana.scratch;

import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageChecker {
    public static void main(String[] args) {
        String[] files = {
            "assets/choose/WANDS_features.png",
            "assets/choose/SWORDS_features.png",
            "assets/choose/PENTACLES_features.png",
            "assets/choose/CUPS_features.png"
        };
        for (String f : files) {
            File file = new File(f);
            if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    System.out.println(f + ": " + img.getWidth() + "x" + img.getHeight());
                } catch (Exception e) {
                    System.out.println(f + ": Error - " + e.getMessage());
                }
            } else {
                System.out.println(f + ": Not found");
            }
        }
    }
}
