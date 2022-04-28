package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.EvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.MOOEvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.image.Image;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class NSGA2 {

    private static final Random rng = new Random();

    private final int populationSize;
    private final int numParentPairs;
    private final int numChildrenPerParentPair;
    private final double mutationChance;
    private final double crossChance;
    private final Image image;
    private final Criterion criterion;
    private final Mutators mutators;
    private final Crossover crossover;
    private final int numObjectives;

    public NSGA2(int populationSize,
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
        this.numObjectives = 3;
    }

    public List<? extends Phenotype> runGenalg(int numGenerations) {

        List<MOOEvaluatedPhenotype> population = this.evaluatedGenotypes(genInitialPopulation());
        for (int gen = 0; gen < numGenerations; gen++)
        {
            var parents = getParentPairs(population);

            // gen children
            var children = genChildren(parents);
            population.addAll(children);

            var fronts = fastNonDominatedSort(population);
            List<MOOEvaluatedPhenotype> next_gen = new ArrayList<>();
            int idx = 0;
            if (fronts.size() > 1) {
                System.out.println("Pareto front length: " + fronts.get(0).size());
                System.out.println("Num fronts: " + fronts.size());
            }
            crowdingDistanceAssignment(fronts.get(idx));  // Ensures sorting even when first front too large
            while (next_gen.size() + fronts.get(idx).size() <= this.populationSize) {
                crowdingDistanceAssignment(fronts.get(idx));
                next_gen.addAll(fronts.get(idx));
                idx++;
            }

            fronts.get(idx).sort(Comparator.comparingDouble(MOOEvaluatedPhenotype::getCrowdingDist));
            int roomLeft = populationSize - next_gen.size();
            Iterator<MOOEvaluatedPhenotype> iterator = fronts.get(idx).iterator();
            for (int i = 0; i < roomLeft && iterator.hasNext(); i++)
                next_gen.add(iterator.next());

            population = next_gen;
            System.out.println("Gen number " + gen);
        }
        var paretoFront = fastNonDominatedSort(population).get(0);
        // TODO: 28/04/2022 Fiks resten her Axel
        System.out.println(paretoFront.get(0).connectivity);
        System.out.println("skadoosh");
        frontsToFile(fastNonDominatedSort(population));
        return paretoFront;
    }

    public void frontsToFile(List<List<MOOEvaluatedPhenotype>> m) {
        List<PhenoInterface> ert = new ArrayList<>();
        for (var front : m)
            crowdingDistanceAssignment(front);
        try {
            File myObj = new File("pareto_test.csv");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
            FileWriter myWriter = new FileWriter("pareto_test.csv");
            for (List<MOOEvaluatedPhenotype> f : m) {
                for (MOOEvaluatedPhenotype f2 : f) {
                    myWriter.write(String.format("%f, %f, %f, %d\n", f2.connectivity, f2.edgeValue, f2.overallDeviation, f2.getRank()));
                }
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    // Mye av dette her er fra SimpleGeneticAlgo
    private List<MOOEvaluatedPhenotype> genChildren(List<Selection.ParentPair> parentPairs) {
        List<MOOEvaluatedPhenotype> children = new ArrayList<>();
        for (var parentPair : parentPairs) {
            for (int i = 0; i < numChildrenPerParentPair; i++) {
                Genotype child = genChild(parentPair.parent1, parentPair.parent2);
                child = mutateGenome(child);
                children.add(evaluatedGenotype(child));
            }
        }
        return children;
    }

    private Genotype mutateGenome(Genotype genotype) {
        if (rng.nextDouble() < mutationChance) {
            mutators.simpleGeneFlipMutation(genotype);
        }
        return genotype;
    }

    private Genotype genChild(Genotype parent1, Genotype parent2) {
        Genotype child;
        if (rng.nextDouble() < crossChance) {
            //            child = crossover.uniformCrossover(parent1, parent2, 0.5);
            //            child = crossover.twoPointCross(parent1, parent2);
            child = crossover.twoPointCross2d(parent1, parent2);
        } else {
            if (rng.nextBoolean()) {
                child = new Genotype(parent1.genome);
            } else {
                child = new Genotype(parent2.genome);
            }
        }
        return child;
    }

    private List<Selection.ParentPair> getParentPairs(List<MOOEvaluatedPhenotype> population) {

        List<Selection.ParentPair> parents = new ArrayList<>();
        for (int i = 0; i < numParentPairs; i++) {
            parents.add(Selection.tournamentParentSelection(population, false));
        }
        return parents;
    }

    public List<List<MOOEvaluatedPhenotype>> fastNonDominatedSort(List<MOOEvaluatedPhenotype> population) {
        /*
         * Implementation based on algorithm presented in "A Fast and Elitist Multiobjective Genetic Algorithm: NSGA-II"
         */
        List<List<MOOEvaluatedPhenotype>> fronts = new ArrayList<>();
        fronts.add(new ArrayList<>());
        for (var t : population)
            t.resetFrontInfo();
        for (MOOEvaluatedPhenotype moo : population) {
            for (MOOEvaluatedPhenotype moo2 : population) {
                if(moo2.equals(moo))
                    continue;
                if (dominates(moo, moo2))
                    moo.addDominatedSolution(moo2);
                else if (dominates(moo2, moo))
                    moo.incrementDominationCount();
            }
            if (moo.getDominationCount() == 0) {
                moo.setRank(0);
                fronts.get(0).add(moo);
            }
        }

        int i = 0;
        while (!fronts.get(i).isEmpty()) {
            List<MOOEvaluatedPhenotype> q = new ArrayList<>();
            for(var p : fronts.get(i)) {
                for(var dom : p.getDominatedSolutions()) {
                    dom.decrementDominationCount();
                    if (dom.getDominationCount() == 0) {
                        dom.setRank(i+1);
                        q.add(dom);
                    }
                }
            }
            i++;
            fronts.add(q);
        }
        fronts.remove(fronts.size() - 1);
        return fronts;
    }
    public void crowdingDistanceAssignment(List<MOOEvaluatedPhenotype> sol_set) {
        /*
         * Implementation based on algorithm presented in "A Fast and Elitist Multiobjective Genetic Algorithm: NSGA-II"
         */
        int end = sol_set.size() - 1;

        for (var m : sol_set)
            m.setCrowdingDist(0.0);
        // Looper over hver objective
        for (int i = 0; i < this.numObjectives; i++)
        {
            // Garra en bedre måte å gjøre dette på
            int finalI = i;
            double f_max = sol_set.stream()
                    .max(Comparator.comparingDouble(fc -> fc.getFitnessValueByIdx(finalI)))
                    .get().getFitnessValueByIdx(i);

            int finalI1 = i;
            double f_min = sol_set.stream()
                    .min(Comparator.comparingDouble(fc -> fc.getFitnessValueByIdx(finalI1)))
                    .get().getFitnessValueByIdx(i);

            double fitDelta = (f_max - f_min);
            double fitDeltaInverse = fitDelta > 0.0 ? (1 / fitDelta) : Double.POSITIVE_INFINITY;

            int finalI2 = i;
            //TODO: dobbelsjekk om listen skal være i motsatt rekkefølge eller ikke
            //sol_set.sort(Comparator.comparingDouble(o -> -o.getFitnessValueByIdx(finalI2)));
            sol_set.sort(Collections.reverseOrder(Comparator.comparingDouble(o -> o.getFitnessValueByIdx(finalI2))));
            sol_set.get(0).setCrowdingDist(Double.POSITIVE_INFINITY);
            sol_set.get(end).setCrowdingDist(Double.POSITIVE_INFINITY);


            for (int j = 1; j < end - 1; j++) {
                double crowdDist = sol_set.get(j).getCrowdingDist() + (sol_set.get(j + 1).getFitnessValueByIdx(i) - sol_set.get(j - 1).getFitnessValueByIdx(i)) * fitDeltaInverse;
                sol_set.get(j).setCrowdingDist(crowdDist);
            }
        }


    }
    public boolean dominates(MOOEvaluatedPhenotype p1, MOOEvaluatedPhenotype p2) {
        /*
         * A solution dominates another if:
         * 1. It is no worse in all fitness criteria
         * 2. It is better in at least one fitness criteria
         */
        boolean oneBetter = false;
        for (int i = 0; i < p1.fitnessValues.size(); i++) {
            if (p1.fitnessValues.get(i) < p2.fitnessValues.get(i)) {
                return false;

            } else if (!oneBetter && p1.fitnessValues.get(i) > p2.fitnessValues.get(i)) {
                oneBetter = true;
            }
        }
        return oneBetter;
    }

    private MOOEvaluatedPhenotype evaluatedGenotype(Genotype genotype) {
        if (genotype instanceof MOOEvaluatedPhenotype) {
            return (MOOEvaluatedPhenotype) genotype;
        }
        Phenotype phenotype           = new Phenotype(genotype, image);
        double    deviation           = criterion.phenotypeOverallDeviation(phenotype) * -1;
        double    edgeVal             = criterion.phenotypeEdgeValue(phenotype) * 1;
        double    connectivityMeasure = criterion.phenotypeConnectivityMeasure(phenotype) * -1;

        //        System.out.println();
        //        dbl.log("dev", diviation);
        //        dbl.log("edg", edgeVal);
        //        dbl.log("con mesh", connectivityMeasure);
        //        dbl.log("fit", diviation + edgeVal + connectivityMeasure);
        return new MOOEvaluatedPhenotype(phenotype, edgeVal, connectivityMeasure, deviation);
        //return new MOOEvaluatedPhenotype(phenotype, rng.nextDouble(), rng.nextDouble(), 0.0);
    }

    private List<MOOEvaluatedPhenotype> evaluatedGenotypes(List<Genotype> genotypes) {
        return new ArrayList<MOOEvaluatedPhenotype>(genotypes.stream().map(this::evaluatedGenotype).toList());
    }

    private List<Genotype> genInitialPopulation() {
        List<Genotype> initialPopulation = new ArrayList<>();
        for (int i = 0; i < this.populationSize; i++) {
            initialPopulation.add(Initialization.generateInitialGenome(image));
            //initialPopulation.add(Initialization.generateRandomGenome(image));
        }
        return initialPopulation;
    }
}
