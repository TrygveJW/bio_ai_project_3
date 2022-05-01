package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.evolutionary_algorithm.util.PrimsAlgorithm;
import no.woldseth.image.Image;
import no.woldseth.image.Pixel;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class Initialization {
    private static Random rng = new Random();

    public static Genotype generateInitialGenome(Image image) {
        //        PixelConnectionType[] genome = Arrays.stream(image.pixleArray)
        //                                             .map(pixel -> getPixelBestConnection(pixel, image))
        //                                             .toArray(PixelConnectionType[]::new);

        PixelConnectionType[] genome = PrimsAlgorithm.getSpanningTree(image);
        Random                rng    = new Random();
        for (int i = 0; i < 100; i++) {
            //            genome[rng.nextInt(genome.length)] = PixelConnectionType.values()[rng.nextInt(PixelConnectionType.values().length)];
            genome[rng.nextInt(genome.length)] = PixelConnectionType.SELF;

        }
        var g = new Genotype(genome);
        var p = new Phenotype(genome, image);

        return p.getThresholdGroupGenome();

    }


    public static Genotype generateRandomGenome(Image image) {
        PixelConnectionType[] genome = Arrays.stream(image.pixleArray)
                                             .map(pixel -> getPixelBestConnection(pixel, image))
                                             .toArray(PixelConnectionType[]::new);


        //Random                rng    = new Random();
        //for (int i = 0; i < 10; i++) {
        //    genome[rng.nextInt(genome.length)] = PixelConnectionType.SELF;

        //}
        return new Genotype(genome);
    }

    private static List<PixelConnectionType> connectionTypes = List.of(PixelConnectionType.UP,
                                                                       PixelConnectionType.DOWN,
                                                                       PixelConnectionType.LEFT,
                                                                       PixelConnectionType.RIGHT
                                                                      );

    private static PixelConnectionType traverse(Pixel pixel, Image image) {
        int curX = pixel.getX();
        int curY = pixel.getX();

        PixelConnectionType bestConType = PixelConnectionType.SELF;
        double              lowestDelta = 1 << 10;

        for (var conn : connectionTypes) {
            int newX = curX;
            int newY = curY;
            switch (conn) {
                case UP -> {
                    newY -= 1;
                }
                case LEFT -> {
                    newX -= 1;
                }
                case DOWN -> {
                    newY += 1;
                }
                case RIGHT -> {
                    newX += 1;
                }
                case SELF -> {
                    System.out.println("ISSUES");
                }
            }


            if ((newX >= image.width || newX < 0) || (newY >= image.height || newY < 0)) {
                continue;
            }
            double delta = image.getRgbDelta(curX, curY, newX, newY);
            if (delta < lowestDelta) {
                bestConType = conn;
            }
        }

        return bestConType;

    }

    private static PixelConnectionType getPixelBestConnection(Pixel pixel, Image image) {
        int curX = pixel.getX();
        int curY = pixel.getX();

        PixelConnectionType bestConType = PixelConnectionType.SELF;
        double              lowestDelta = 1 << 10;

        for (var conn : connectionTypes) {
            int newX = curX;
            int newY = curY;
            switch (conn) {
                case UP -> {
                    newY -= 1;
                }
                case LEFT -> {
                    newX -= 1;
                }
                case DOWN -> {
                    newY += 1;
                }
                case RIGHT -> {
                    newX += 1;
                }
                case SELF -> {
                    System.out.println("ISSUES");
                }
            }


            if ((newX >= image.width || newX < 0) || (newY >= image.height || newY < 0)) {
                continue;
            }
            double delta = image.getRgbDelta(curX, curY, newX, newY);
            if (delta < lowestDelta) {
                bestConType = conn;
            }
        }

        return bestConType;

    }


}
