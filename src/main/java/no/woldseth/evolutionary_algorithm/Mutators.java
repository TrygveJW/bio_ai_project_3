package no.woldseth.evolutionary_algorithm;

import no.woldseth.DebugLogger;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.evolutionary_algorithm.util.PrimsAlgorithm;
import no.woldseth.image.Image;
import no.woldseth.image.Pixel;
import no.woldseth.image.PixelGroup;

import java.util.Arrays;
import java.util.Random;

public class Mutators {

    private Image image;

    private static Random rng = new Random();

    public Mutators(Image image) {
        this.image = image;
    }


    public Genotype mutateGenome(Genotype genotype, double mutationChance) {
        if (rng.nextDouble() < mutationChance) {
            //            mutators.simpleGeneFlipMutation(genotype);

            //            mutators.mergeGroupMutation(genotype, image);

            switch (rng.nextInt(2)) {
                case 0 -> {
                    this.simpleGeneFlipMutation(genotype);
                }
                case 1 -> {
                    this.horisontalLineMutation(genotype);
                }
                case 2 -> {
                    this.verticalLineMutation(genotype);
                }
                case 3 -> {
                    this.mergeGroupMutation(genotype, image);
                }
                default -> {
                    throw new RuntimeException();
                }
            }

        }
        return genotype;
    }


    private PixelConnectionType getRandomConnType() {
        return PixelConnectionType.values()[rng.nextInt(PixelConnectionType.values().length)];
    }

    public Genotype simpleGeneFlipMutation(Genotype genotype) {
        int                 flipIndex   = rng.nextInt(genotype.genome.length);
        PixelConnectionType newConnType = getRandomConnType();
        genotype.genome[flipIndex] = newConnType;
        return genotype;
    }


    private int maxHorisontalFlip = 5;

    public Genotype horisontalLineMutation(Genotype genotype) {
        int flipRow = rng.nextInt(image.height);

        int pointInRow = rng.nextInt(image.width);
        int numToFlip  = rng.nextInt(image.width - pointInRow);


        numToFlip = Math.min(maxHorisontalFlip, numToFlip);

        int shiftBase = (flipRow * image.width) + pointInRow;
        for (int i = 0; i < numToFlip; i++) {
            genotype.genome[shiftBase + i] = PixelConnectionType.RIGHT;
        }
        return genotype;
    }

    private int maxVerticalFlip = 5;

    public Genotype verticalLineMutation(Genotype genotype) {
        int col = rng.nextInt(image.width);

        int pointInCol = rng.nextInt(image.height);
        int numToFlip  = rng.nextInt(image.height - pointInCol);

        numToFlip = Math.min(maxVerticalFlip, numToFlip);

        for (int i = pointInCol; i < (pointInCol + numToFlip); i++) {
            int idx = (i * image.width) + col;
            genotype.genome[idx] = PixelConnectionType.DOWN;
        }
        return genotype;
    }

    public Genotype mergeGroupMutation(Genotype genotype, Image image) {
        Phenotype phenotype = null;
        if (genotype instanceof Phenotype) {
            phenotype = (Phenotype) genotype;
        } else {
            phenotype = new Phenotype(genotype, image);
        }

        int numGroups = phenotype.pixelGroups.size();

        //        DebugLogger.dbl().log("num G before", phenotype.pixelGroups.size());
        //        var preSize = numGroups;


        PixelGroup groupToPop = phenotype.pixelGroups.get(rng.nextInt(numGroups));
        Pixel      startPoint = groupToPop.groupMembers.get(rng.nextInt(groupToPop.groupMembers.size()));


        boolean popDown = rng.nextBoolean();
        int     merge1  = 0;
        int     merge2  = 0;

        int startId = 0;

        if (popDown) {
            for (int y = startPoint.getY(); y < image.height - 1; y++) {
                var g1 = phenotype.pixelGroupList[(y * image.width) + startPoint.getX()];
                var g2 = phenotype.pixelGroupList[((y + 1) * image.width) + startPoint.getX()];

                if (g1 != g2) {
                    merge1  = g1;
                    merge2  = g2;
                    startId = (y * image.width) + startPoint.getX();
                    break;
                }
            }
        } else {
            for (int x = startPoint.getX(); x < image.width - 1; x++) {
                var g1 = phenotype.pixelGroupList[(startPoint.getY() * image.width) + startPoint.getX()];
                var g2 = phenotype.pixelGroupList[(startPoint.getY() * image.width) + (startPoint.getX() + 1)];

                if (g1 != g2) {
                    merge1  = g1;
                    merge2  = g2;
                    startId = (startPoint.getY() * image.width) + x;
                    startId = (startPoint.getY() * image.width) + x;
                    break;
                }
            }
        }
        //        System.out.println(Arrays.deepToString(genotype.genome));
        genotype = new Genotype(mergeGroups(phenotype, merge1, merge2, startId, image));

        for (int i = 0; i < genotype.genome.length; i++) {
            if (genotype.genome[i] == null) {

                genotype.genome[i] = PixelConnectionType.RIGHT;
                //                DebugLogger.dbl().log("\n\nnononnnon\n\n");
                //                DebugLogger.dbl().log(genotype.genome);
            }

        }

        //        System.out.println(Arrays.deepToString(genotype.genome));
        //        System.exit(0);
        //        var ph_test  = new Phenotype(new Genotype(genotype.genome), image);
        //        var postSize = ph_test.pixelGroups.size();
        //
        //        if (preSize != postSize) {
        //            System.out.println("\n\n\nCHANGE\n\n\n\n");
        //        }
        //        DebugLogger.dbl().log("num G after", ph_test.pixelGroups.size());

        return genotype;
    }

    private static void zeroGroup(PixelConnectionType[] genotype, PixelGroup group) {
        group.groupMembers.forEach(pixel -> genotype[pixel.getId()] = null);
    }

    private static PixelConnectionType[] mergeGroups(Phenotype phenotype,
                                                     int group1,
                                                     int group2,
                                                     int startId,
                                                     Image image) {
        PixelConnectionType[] genotype = phenotype.genome;

        zeroGroup(genotype, phenotype.pixelGroups.get(group1));
        zeroGroup(genotype, phenotype.pixelGroups.get(group2));

        return PrimsAlgorithm.connectGroups(image, genotype, startId);
    }

}
