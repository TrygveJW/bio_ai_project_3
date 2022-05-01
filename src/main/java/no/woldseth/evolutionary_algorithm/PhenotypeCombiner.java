package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PhenotypeCombiner {


    public static void saveAggregatedPhenotype(List<? extends Phenotype> phenotypes,
                                               Image image,
                                               File imageFile,
                                               String outFilePath) {
        int[][] agrMapX = new int[image.height][image.width];
        int[][] agrMapY = new int[image.height][image.width];

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                agrMapX[y][x] = 0;
                agrMapY[y][x] = 0;

            }
        }

        phenotypes.forEach(phenotype -> {
            IntStream.range(0, image.height).forEach(value -> {
                for (int i = 0; i < image.width - 1; i++) {
                    int g1 = phenotype.pixelGroupList[image.getPointAsId(i, value)];
                    int g2 = phenotype.pixelGroupList[image.getPointAsId(i + 1, value)];

                    if (g1 != g2) {
                        agrMapX[value][i] += 1;
                    }
                }
            });

            IntStream.range(0, image.width).forEach(value -> {
                for (int i = 0; i < image.height - 1; i++) {
                    int g1 = phenotype.pixelGroupList[image.getPointAsId(value, i)];
                    int g2 = phenotype.pixelGroupList[image.getPointAsId(value, i + 1)];

                    if (g1 != g2) {
                        agrMapX[i][value] += 1;
                    }
                }
            });

        });


        try {
            Color borderColor = Color.magenta;

            BufferedImage bufferedImage = ImageIO.read(imageFile);

            var max_val = Arrays.stream(agrMapX).flatMapToInt(Arrays::stream).max().getAsInt();
            int lim     = 10;
            IntStream.range(0, image.height).forEach(value -> {
                for (int i = 0; i < image.width - 1; i++) {


                    if (agrMapX[value][i] >= lim) {
                        bufferedImage.setRGB(i, value, borderColor.getRGB());
                    }

                }
            });

            IntStream.range(0, image.width).forEach(value -> {
                for (int i = 0; i < image.height - 1; i++) {
                    if (agrMapX[i][value] >= lim) {
                        bufferedImage.setRGB(value, i, borderColor.getRGB());
                    }
                }
            });

            File outFp = new File(outFilePath + ".png");
            ImageIO.write(bufferedImage, "png", outFp);

        } catch (Exception e) {

        }
    }
}
