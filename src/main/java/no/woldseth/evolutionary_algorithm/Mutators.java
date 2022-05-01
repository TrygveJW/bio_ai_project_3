package no.woldseth.evolutionary_algorithm;

import no.woldseth.DebugLogger;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.evolutionary_algorithm.util.PrimsAlgorithm;
import no.woldseth.image.Image;
import no.woldseth.image.Pixel;
import no.woldseth.image.PixelGroup;

import java.util.*;

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

            switch (rng.nextInt(1)) {
                case 0 -> {
                    this.mergeGroupMutation(genotype, image);
                }
                case 1 -> {
                    //                    this.simpleGeneFlipMutation(genotype);
                }
                case 2 -> {
                    //                    this.verticalLineMutation(genotype);
                }
                case 3 -> {
                    //                    this.horisontalLineMutation(genotype);
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
        if (numGroups == 1) {
            return genotype;
        }
        List<Set<Integer>> groupNeighbours = new ArrayList<>();

        for (int i = 0; i < numGroups; i++) {
            groupNeighbours.add(new HashSet<>());
        }

        for (int y = 0; y < image.height - 1; y++) {
            for (int x = 0; x < image.width - 1; x++) {

                int gc     = phenotype.pixelGroupList[image.getPointAsId(x, y)];
                int gDown  = phenotype.pixelGroupList[image.getPointAsId(x, y + 1)];
                int gRight = phenotype.pixelGroupList[image.getPointAsId(x + 1, y)];
                if (gc != gDown) {
                    groupNeighbours.get(gc).add(gDown);
                    groupNeighbours.get(gDown).add(gc);
                } else if (gc != gRight) {
                    groupNeighbours.get(gc).add(gRight);
                    groupNeighbours.get(gRight).add(gc);
                }
            }

        }


        int group1    = rng.nextInt(numGroups);
        var group2Set = groupNeighbours.get(group1);

        //        System.out.println(groupNeighbours.toString());
        //        System.out.println(group2Set.toString());
        int idx2   = (group2Set.size() == 0) ? 0 : rng.nextInt(group2Set.size());
        int group2 = (int) group2Set.toArray()[idx2];

        genotype = new Genotype(mergeGroups(phenotype,
                                            group1,
                                            group2,
                                            phenotype.pixelGroups.get(group1).groupMembers.get(0).getId(),
                                            image
                                           ));

        for (int i = 0; i < genotype.genome.length; i++) {
            if (genotype.genome[i] == null) {

                //                        genotype.genome[i] = PixelConnectionType.RIGHT;
                DebugLogger.dbl().log("\n\nnononnnon\n\n");
                //                DebugLogger.dbl().log(genotype.genome);
            }

        }

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
