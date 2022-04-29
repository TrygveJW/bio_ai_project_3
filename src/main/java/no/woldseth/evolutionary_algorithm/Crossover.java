package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.image.Image;
import no.woldseth.image.Pixel;
import no.woldseth.image.PixelGroup;

import java.awt.*;
import java.util.Random;

public class Crossover {

    private Image image;
    private static Random rng = new Random();

    public Crossover(Image image) {
        this.image = image;
    }


    public Genotype crossoverParents(Genotype parent1, Genotype parent2) {
        Genotype child;
        switch (rng.nextInt(2)) {
            case 0 -> {
                child = this.twoPointCross2d(parent1, parent2);
            }
            case 1 -> {
                child = this.zoneCrossover(parent1, parent2);
            }
            case 2 -> {
                child = this.twoPointCross(parent1, parent2);
            }
            default -> {
                throw new RuntimeException();
            }
        }
        return child;
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

    private int maxSize1 = 300;
    private int maxSize = 100;

    public Genotype twoPointCross(Genotype parent1, Genotype parent2) {
        Genotype child = new Genotype(new PixelConnectionType[parent1.genome.length]);

        //        int size  = rng.nextInt(maxSize1);
        //        int start = rng.nextInt(child.genome.length - size);
        //        int a     = start;
        //        int b     = start + size;


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


    public Genotype zoneCrossover(Genotype parent1, Genotype parent2) {

        Genotype  child     = new Genotype(parent1.genome.clone());
        Phenotype phenotype = null;
        if (parent2 instanceof Phenotype) {
            phenotype = (Phenotype) parent2;
        } else {
            phenotype = new Phenotype(parent2, image);
        }


        int        numGroups = phenotype.pixelGroups.size();
        PixelGroup copyGroup = phenotype.pixelGroups.get(rng.nextInt(numGroups));

        copyGroup.groupMembers.forEach(pixel -> {
            int pid = pixel.getId();
            child.genome[pid] = parent2.genome[pid];

        });

        return child;

    }

    public Genotype twoPointCross2d(Genotype parent1, Genotype parent2) {
        Genotype child = new Genotype(new PixelConnectionType[parent1.genome.length]);


        //        int size  = rng.nextInt(maxSize);
        //        int start = rng.nextInt(image.width - size);
        //        int a     = start;
        //        int b     = start + size;


        int a     = rng.nextInt(image.width);
        int b     = rng.nextInt(image.width);
        int fromX = Math.min(a, b);
        int toX   = Math.max(a, b);

        //        size  = rng.nextInt(maxSize);
        //        start = rng.nextInt(image.height - size);
        //        int c = start;
        //        int d = start + size;


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
