package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.image.Image;

import java.awt.*;
import java.util.Random;

public class Crossover {

    private Image image;
    private static Random rng = new Random();

    public Crossover(Image image) {
        this.image = image;
    }


    public Genotype uniformCrossover(Genotype parent1, Genotype parent2, double fromParent1Prob) {
        Genotype child = new Genotype(new PixelConnectionType[parent1.genome.length]);

        for (int i = 0; i < parent1.genome.length; i++) {
            if (rng.nextDouble() < fromParent1Prob) {
                child.genome[i] = parent1.genome[i];
            } else {
                child.genome[i] = parent2.genome[i];
            }
        }
        return child;
    }


    public Genotype twoPointCross(Genotype parent1, Genotype parent2) {
        Genotype child = new Genotype(new PixelConnectionType[parent1.genome.length]);


        int a    = rng.nextInt(child.genome.length);
        int b    = rng.nextInt(child.genome.length);
        int from = Math.min(a, b);
        int to   = Math.max(a, b);

        for (int i = 0; i < child.genome.length; i++) {
            if (i >= from && i < to) {
                child.genome[i] = parent1.genome[i];
            } else {
                child.genome[i] = parent2.genome[i];
            }
        }

        return child;
    }


    public Genotype twoPointCross2d(Genotype parent1, Genotype parent2) {
        Genotype child = new Genotype(new PixelConnectionType[parent1.genome.length]);


        int a     = rng.nextInt(image.width);
        int b     = rng.nextInt(image.width);
        int fromX = Math.min(a, b);
        int toX   = Math.max(a, b);


        int c     = rng.nextInt(image.height);
        int d     = rng.nextInt(image.height);
        int fromY = Math.min(c, d);
        int toY   = Math.max(c, d);

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int i = image.getPointAsId(x, y);
                if (y > fromY && y < toY && x > fromX && x < toX) {
                    child.genome[i] = parent1.genome[i];
                } else {
                    child.genome[i] = parent2.genome[i];
                }

            }
        }

        return child;
    }
}
