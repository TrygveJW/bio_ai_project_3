package no.woldseth.evolutionary_algorithm;

import no.woldseth.DebugLogger;
import no.woldseth.evolutionary_algorithm.representation.EvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.image.Image;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SimpleGenteticAlgorithm {

    private static DebugLogger dbl = new DebugLogger(true);
    private static Random rng = new Random();

    private int populationSize;
    private int numParentPairs;
    private int numChildrenPerParentPair;

    private double mutationChance;
    private double crossChance;

    private Image image;

    private Criterion criterion;
    private Mutators mutators;
    private Crossover crossover;

    public SimpleGenteticAlgorithm(int populationSize,
                                   int numParentPairs,
                                   int numChildrenPerParentPair,
                                   double mutationChance,
                                   double crossChance,
                                   Image image) {
        this.populationSize           = populationSize;
        this.numParentPairs           = numParentPairs;
        this.numChildrenPerParentPair = numChildrenPerParentPair;
        this.mutationChance           = mutationChance;
        this.crossChance              = crossChance;
        this.image                    = image;
        this.criterion                = new Criterion(image);
        this.mutators                 = new Mutators(image);
        this.crossover                = new Crossover(image);
    }

    private void printPopInfo(List<EvaluatedPhenotype> pop, int generation) {
        EvaluatedPhenotype best      = null;
        double             bestScore = - Double.MAX_VALUE;
        for (var evp : pop) {
            if (evp.fitness > bestScore) {
                best      = evp;
                bestScore = evp.fitness;
            }

        }

        System.out.printf("Best fitness generation %s is %s\n", generation, bestScore);

    }

    public Phenotype runGenalg(int numGenerations) {
        // gen inital pop
        List<EvaluatedPhenotype> population = this.evaluatedGenotypes(genInitialPopulation());


        // loop
        for (int i = 0; i < numGenerations; i++) {
            printPopInfo(population, i);

            // select parents
            var parents = getParentPairs(population);

            // gen children
            var children = genChildren(parents);

            population.addAll(children);

            // surivior selection
            //            population = Selection.killAllParentsSelection(population, parents);
            population = Selection.elitismSelection(population, this.populationSize, true);
        }
        population.sort(Comparator.naturalOrder());
        int lastIdx = population.size() - 1;
        this.displayEval(population.get(lastIdx));
        return population.get(lastIdx);
        //        image.savePixelGroupEdgeDisplay(population.get(0));
        //
        //        for (int i = 0; i < image.height; i++) {
        //            System.out.println(Arrays.toString(Arrays.copyOfRange(population.get(0).pixelGroupList,
        //                                                                  i * image.width,
        //                                                                  (i + 1) * image.width
        //                                                                 )));
        //
        //
        //        }
    }

    private List<Selection.ParentPair> getParentPairs(List<EvaluatedPhenotype> population) {

        List<Selection.ParentPair> parents = new ArrayList<>();
        for (int i = 0; i < numParentPairs; i++) {
            parents.add(Selection.tournamentParentSelection(population, true));
        }

        for (int i = 0; i < 2; i++) {
            parents.add(Selection.randomParentSelection(population));
        }
        return parents;

    }

    private Genotype genChild(Genotype parent1, Genotype parent2) {
        Genotype child;
        if (rng.nextDouble() < crossChance) {
            //            child = crossover.uniformCrossover(parent1, parent2, 0.5);
            //            child = crossover.twoPointCross(parent1, parent2);
            child = crossover.crossoverParents(parent1, parent2);
        } else {
            if (rng.nextBoolean()) {
                child = new Genotype(parent1.genome);
            } else {
                child = new Genotype(parent2.genome);
            }
        }
        return child;
    }


    private List<EvaluatedPhenotype> genChildren(List<Selection.ParentPair> parentPairs) {
        List<EvaluatedPhenotype> children = new ArrayList<>();
        for (var parentPair : parentPairs) {
            for (int i = 0; i < numChildrenPerParentPair; i++) {
                Genotype child = genChild(parentPair.parent1, parentPair.parent2);
                child = mutators.mutateGenome(child, mutationChance);
                children.add(evaluatedGenotype(child));
            }
        }
        return children;
    }


    private double deviationWeight = 0.0002;
    private double edgeValWeight = 8;
    private double connectivityWeight = 1;

    private EvaluatedPhenotype evaluatedGenotype(Genotype genotype) {
        if (genotype instanceof EvaluatedPhenotype) {
            return (EvaluatedPhenotype) genotype;
        }
        Phenotype phenotype           = new Phenotype(genotype, image);
        double    diviation           = criterion.phenotypeOverallDeviation(phenotype) * deviationWeight;
        double    edgeVal             = criterion.phenotypeEdgeValue(phenotype) * edgeValWeight;
        double    connectivityMeasure = criterion.phenotypeConnectivityMeasure(phenotype) * connectivityWeight;

        //        System.out.println();
        //        dbl.log("dev", diviation);
        //        dbl.log("edg", edgeVal);
        //        dbl.log("con mesh", connectivityMeasure);
        //        dbl.log("fit", diviation + edgeVal + connectivityMeasure);

        return new EvaluatedPhenotype(phenotype, diviation + edgeVal + connectivityMeasure);
    }

    private void displayEval(Genotype genotype) {

        Phenotype phenotype           = new Phenotype(genotype, image);
        double    diviation           = criterion.phenotypeOverallDeviation(phenotype) * deviationWeight;
        double    edgeVal             = criterion.phenotypeEdgeValue(phenotype) * edgeValWeight;
        double    connectivityMeasure = criterion.phenotypeConnectivityMeasure(phenotype) * connectivityWeight;

        System.out.println();
        dbl.log("dev", diviation);
        dbl.log("edg", edgeVal);
        dbl.log("con mesh", connectivityMeasure);
        dbl.log("fit", diviation + edgeVal + connectivityMeasure);

    }

    private List<EvaluatedPhenotype> evaluatedGenotypes(List<Genotype> genotypes) {
        return new ArrayList<EvaluatedPhenotype>(genotypes.stream().map(this::evaluatedGenotype).toList());
    }

    private List<Genotype> genInitialPopulation() {
        List<Genotype> initialPopulation = new ArrayList<>();
        for (int i = 0; i < this.populationSize; i++) {
            var newGenome = Initialization.generateInitialGenome(image);
            //            newGenome = mutators.simpleGeneFlipMutation(newGenome);
            initialPopulation.add(newGenome);
        }
        return initialPopulation;
    }
}
