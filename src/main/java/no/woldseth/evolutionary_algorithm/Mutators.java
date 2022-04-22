package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.image.Image;

import java.util.Random;

public class Mutators {

    private Image image;

    private static Random rng = new Random();

    public Mutators(Image image) {
        this.image = image;
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

}
