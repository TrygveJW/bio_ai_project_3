package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.image.Image;
import no.woldseth.image.Pixel;
import no.woldseth.image.PixelGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Criterion {


    private static List<String> connectionTypes = List.of("up",
                                                          "up-left",
                                                          "left",
                                                          "down-left",
                                                          "down",
                                                          "down-right",
                                                          "right",
                                                          "up-right"
                                                         );
    private Image image;

    public Criterion(Image image) {
        this.image = image;
    }

    private double pixelGroupRgbDist(PixelGroup pixelGroup) {
        ArrayList<Pixel> pixels = pixelGroup.groupMembers;

        var avgRed   = pixels.stream().map(Pixel::getRed).mapToInt(Integer::intValue).average().getAsDouble();
        var avgGreen = pixels.stream().map(Pixel::getGreen).mapToInt(Integer::intValue).average().getAsDouble();
        var avgBlue  = pixels.stream().map(Pixel::getBlue).mapToInt(Integer::intValue).average().getAsDouble();

        return pixels.stream()
                     .map(pixel -> Math.sqrt(Math.pow(pixel.getRed() - avgRed,
                                                      2
                                                     ) + Math.pow(pixel.getGreen() - avgGreen, 2) + Math.pow(
                             pixel.getBlue() - avgBlue, 2)))
                     .mapToDouble(Double::doubleValue)
                     .average()
                     .getAsDouble();
    }

    public double phenotypeOverallDeviation(Phenotype phenotype) {
        return phenotype.pixelGroups.stream()
                                    .map(this::pixelGroupRgbDist)
                                    .mapToDouble(Double::doubleValue)
                                    .average()
                                    .getAsDouble();
    }

    public double phenotypeEdgeValue(Phenotype phenotype) {
        double edgeVal = IntStream.range(0, image.height).mapToDouble(value -> {
            double roll = 0;
            for (int i = 0; i < image.width - 1; i++) {
                int g1 = phenotype.pixelGroupList[image.getPointAsId(i, value)];
                int g2 = phenotype.pixelGroupList[image.getPointAsId(i + 1, value)];

                if (g1 != g2) {
                    roll += image.getRgbDelta(i, value, i + 1, value);
                }
            }
            return roll;
        }).sum();

        edgeVal += IntStream.range(0, image.width).mapToDouble(value -> {
            double roll = 0;
            for (int i = 0; i < image.height - 1; i++) {
                int g1 = phenotype.pixelGroupList[image.getPointAsId(value, i)];
                int g2 = phenotype.pixelGroupList[image.getPointAsId(value, i + 1)];

                if (g1 != g2) {
                    roll += image.getRgbDelta(value, i, value, i + 1);
                }
            }
            return roll;
        }).sum();

        return edgeVal;
    }

    public double phenotypeConnectivityMeasure(Phenotype phenotype) {

        return IntStream.range(0, image.height).mapToDouble(value -> {
            double fullroll = 0;
            for (int i = 0; i < image.width - 1; i++) {
                double roll = 0;
                for (var conn : connectionTypes) {
                    int newX   = i;
                    int newY   = value;
                    int weight = 0;
                    switch (conn) {
                        case "up" -> {
                            newY -= 1;
                            weight = 1;
                        }
                        case "up-left" -> {
                            newY -= 1;
                            newX -= 1;
                            weight = 1;
                        }
                        case "left" -> {
                            newX -= 1;
                            weight = 1;
                        }
                        case "down-left" -> {
                            newX -= 1;
                            newY += 1;
                            weight = 1;
                        }
                        case "down" -> {
                            newY += 1;
                            weight = 1;
                        }

                        case "down-right" -> {
                            newX += 1;
                            newY += 1;
                            weight = 1;
                        }
                        case "right" -> {
                            newX += 1;
                            weight = 1;
                        }
                        case "up-right" -> {
                            newX += 1;
                            newY -= 1;
                            weight = 1;
                        }
                    }

                    if ((newX >= image.width || newX < 0) || (newY >= image.height || newY < 0)) {
                        continue;
                    }

                    int g1 = phenotype.pixelGroupList[image.getPointAsId(i, value)];
                    int g2 = phenotype.pixelGroupList[image.getPointAsId(newX, newY)];
                    if (g1 != g2) {
                        roll += weight;
                    }

                }
                fullroll += roll / 8;

            }
            return fullroll;
        }).sum();
    }
}