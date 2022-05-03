package no.woldseth.evolutionary_algorithm;

import no.woldseth.DebugLogger;
import no.woldseth.ExtendedRandom;
import no.woldseth.evolutionary_algorithm.representation.EvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.MOOEvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;
import no.woldseth.image.Image;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class NSGA2 {

    private static final ExtendedRandom rng = new ExtendedRandom();

    private static DebugLogger dbl = new DebugLogger(true);
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
        this.numObjectives            = 3;

    }

    private int leak = 5;

    private void leakNew(List<MOOEvaluatedPhenotype> population) {
        ArrayList<Genotype> leakGenomes = new ArrayList<>();
        // leak new
        for (int i = 0; i < leak; i++) {
            leakGenomes.add(Initialization.generateInitialGenome(this.image));
        }
        population.addAll(this.evaluatedGenotypes(leakGenomes));
    }

    private List<MOOEvaluatedPhenotype> sketchyMutate(List<MOOEvaluatedPhenotype> population) {

        ArrayList<MOOEvaluatedPhenotype> ret     = new ArrayList<>();
        var                              genomes = rng.randomChoice(population, 5, false);

        for (MOOEvaluatedPhenotype phenotype : genomes) {
            Genotype newGenome = new Genotype(phenotype.genome.clone());
            mutators.mergeGroupMutation(newGenome, image);
            ret.add(this.evaluatedGenotype(newGenome));
        }
        return ret;

    }

    public List<? extends Phenotype> runGenalg(int numGenerations) {

        List<MOOEvaluatedPhenotype>       population = this.evaluatedGenotypes(genInitialPopulation());
        List<List<MOOEvaluatedPhenotype>> fronts     = null;
        for (int gen = 0; gen < numGenerations; gen++) {

            //            this.leakNew(population);


            var parents = getParentPairs(population);

            // gen children
            var children = genChildren(parents);
            population.addAll(children);


            //            var sketchy = this.sketchyMutate(population);
            //            population.addAll(sketchy);

            fronts = fastNonDominatedSort(population);
            List<MOOEvaluatedPhenotype> next_gen = new ArrayList<>();
            int                         idx      = 0;
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
            int                             roomLeft = populationSize - next_gen.size();
            Iterator<MOOEvaluatedPhenotype> iterator = fronts.get(idx).iterator();
            for (int i = 0; i < roomLeft && iterator.hasNext(); i++) {
                next_gen.add(iterator.next());
            }

            if (gen % 25000 == 0 && gen != 0) {
                population = this.removeDuplicates(next_gen);
            } else {
                population = next_gen;
            }

            //            population = next_gen;
            System.out.println("population size: " + population.size());
            System.out.println("Gen number " + gen);
        }
        //        var paretoFront = fastNonDominatedSort(population).get(0);
        // TODO: 28/04/2022 Fiks resten her Axel
        System.out.println(fronts.get(0).get(0).connectivity);
        frontsToFile(fronts);
        return bruteForceLast(population, 50);
    }

    private List<MOOEvaluatedPhenotype> bruteForceLast(List<MOOEvaluatedPhenotype> population, int rounds) {

        //        ArrayList<MOOEvaluatedPhenotype> ret     = new ArrayList<>();
        //        var                              genomes = rng.randomChoice(population, 3, false);
        var genomes = population;//# rng.randomChoice(population, 5, false);

        //        int c = 0;
        //        for (MOOEvaluatedPhenotype phenotype : genomes) {
        //            c += 1;
        //            var best = phenotype;
        //
        //            image.savePixelGroupEdgeDisplay(best, "./abc/" + c + "_aref");
        //            for (int i = 0; i < rounds; i++) {
        //                Genotype newGenome = new Genotype(best.genome.clone());
        //                newGenome = mutators.mergeGroupMutation(newGenome, image);
        //                var newPheotype = this.evaluatedGenotype(newGenome);
        //
        ////                image.savePixelGroupEdgeDisplay(newPheotype, "./abc/" + c + "_change_" + i);
        //                var better = this.dominates(newPheotype, phenotype);
        //                if (better) {
        //                    best = newPheotype;
        //                }
        //            }
        //            ret.add(best);
        //        }


        var ret = genomes.stream().parallel().map(mooEvaluatedPhenotype -> {
            var best = mooEvaluatedPhenotype;
            //            if (mooEvaluatedPhenotype.pixelGroups.size() == 1) {
            //                return best;
            //            }
            //            for (int i = 0; i < rounds; i++) {
            //                Genotype newGenome = new Genotype(best.genome.clone());
            //                newGenome = mutators.mergeGroupMutation(newGenome, image);
            //                var newPheotype = this.evaluatedGenotype(newGenome);
            //                var better      = this.dominates(newPheotype, best);
            //                if (better) {
            //                    System.out.println("improved");
            //                    best = newPheotype;
            //                }
            //            }
            return best;
        }).collect(Collectors.toList());

        return ret;
    }

    public void frontsToFile(List<List<MOOEvaluatedPhenotype>> m) {
        List<PhenoInterface> ert = new ArrayList<>();
        for (var front : m) {
            crowdingDistanceAssignment(front);
        }
        try {
            File myObj = new File("pareto_fitness.csv");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
            FileWriter myWriter = new FileWriter("pareto_fitness.csv");
            myWriter.write("number, connectivity, edgevalue, deviation, front\n");
            int        frontCounter    = 1;
            for (List<MOOEvaluatedPhenotype> f : m) {
                for (MOOEvaluatedPhenotype f2 : f) {
                    myWriter.write(String.format("%d, %f, %f, %f, %d \n",
                                                 frontCounter,
                                                 f2.connectivity,
                                                 f2.edgeValue,
                                                 f2.overallDeviation,
                                                 f2.getRank()
                                                ));
                    frontCounter += 1;
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
                child = mutators.mutateGenome(child, mutationChance);
                children.add(evaluatedGenotype(child));
            }
        }
        return children;
    }

    private List<MOOEvaluatedPhenotype> removeDuplicates(List<MOOEvaluatedPhenotype> population) {

        int preDropLen = population.size();
        population = population.stream()
                               .map(mooEvaluatedPhenotype -> (Phenotype) mooEvaluatedPhenotype)
                               .distinct().map(phenotype -> (MOOEvaluatedPhenotype) phenotype)
                               .collect(Collectors.toList());
        int postDropLen = population.size();
        int numDropped  = preDropLen - postDropLen;

        dbl.log("pre", preDropLen);
        dbl.log("post", postDropLen);

        ArrayList<Genotype> leakGenomes = new ArrayList<>();
        // leak new
        for (int i = 0; i < numDropped; i++) {
            leakGenomes.add(Initialization.generateInitialGenome(this.image));
        }
        population.addAll(this.evaluatedGenotypes(leakGenomes));
        return population;
    }


    private Genotype genChild(Genotype parent1, Genotype parent2) {
        Genotype child;
        if (rng.nextDouble() < crossChance) {
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

    private List<Selection.ParentPair> getParentPairs(List<MOOEvaluatedPhenotype> population) {

        List<Selection.ParentPair> parents = new ArrayList<>();
        for (int i = 0; i < numParentPairs; i++) {
            parents.add(Selection.tournamentParentSelection(population, false));
        }
        //for (int i = 0; i < 2; i++) {
        //    parents.add(Selection.randomParentSelection(population));
        //}
        return parents;
    }

    public List<List<MOOEvaluatedPhenotype>> fastNonDominatedSort(List<MOOEvaluatedPhenotype> population) {
        /*
         * Implementation based on algorithm presented in "A Fast and Elitist Multiobjective Genetic Algorithm: NSGA-II"
         */
        List<List<MOOEvaluatedPhenotype>> fronts = new ArrayList<>();
        fronts.add(new ArrayList<>());
        for (var t : population) {
            t.resetFrontInfo();
        }
        for (MOOEvaluatedPhenotype moo : population) {
            for (MOOEvaluatedPhenotype moo2 : population) {
                if (moo2.equals(moo)) {
                    continue;
                }
                if (dominates(moo, moo2)) {
                    moo.addDominatedSolution(moo2);
                } else if (dominates(moo2, moo)) {
                    moo.incrementDominationCount();
                }
            }
            if (moo.getDominationCount() == 0) {
                moo.setRank(0);
                fronts.get(0).add(moo);
            }
        }

        int i = 0;
        while (! fronts.get(i).isEmpty()) {
            List<MOOEvaluatedPhenotype> q = new ArrayList<>();
            for (var p : fronts.get(i)) {
                for (var dom : p.getDominatedSolutions()) {
                    dom.decrementDominationCount();
                    if (dom.getDominationCount() == 0) {
                        dom.setRank(i + 1);
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

        for (var m : sol_set) {
            m.setCrowdingDist(0.0);
        }
        // Looper over hver objective
        for (int i = 0; i < this.numObjectives; i++) {
            // Garra en bedre måte å gjøre dette på
            int finalI = i;
            double f_max = sol_set.stream()
                                  .max(Comparator.comparingDouble(fc -> fc.getFitnessValueByIdx(finalI)))
                                  .get().getFitnessValueByIdx(i);

            int finalI1 = i;
            double f_min = sol_set.stream()
                                  .min(Comparator.comparingDouble(fc -> fc.getFitnessValueByIdx(finalI1)))
                                  .get().getFitnessValueByIdx(i);

            double fitDelta        = (f_max - f_min);
            double fitDeltaInverse = fitDelta > 0.0 ? (1 / fitDelta) : Double.POSITIVE_INFINITY;

            int finalI2 = i;
            //TODO: dobbelsjekk om listen skal være i motsatt rekkefølge eller ikke
            //sol_set.sort(Comparator.comparingDouble(o -> -o.getFitnessValueByIdx(finalI2)));
            sol_set.sort(Collections.reverseOrder(Comparator.comparingDouble(o -> o.getFitnessValueByIdx(finalI2))));
            sol_set.get(0).setCrowdingDist(Double.POSITIVE_INFINITY);
            sol_set.get(end).setCrowdingDist(Double.POSITIVE_INFINITY);


            for (int j = 1; j < end - 1; j++) {
                double crowdDist = sol_set.get(j).getCrowdingDist() + (sol_set.get(j + 1)
                                                                              .getFitnessValueByIdx(i) - sol_set.get(j - 1)
                                                                                                                .getFitnessValueByIdx(
                                                                                                                        i)) * fitDeltaInverse;
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

            } else if (! oneBetter && p1.fitnessValues.get(i) > p2.fitnessValues.get(i)) {
                oneBetter = true;
            }
        }
        return oneBetter;
    }

    private MOOEvaluatedPhenotype evaluatedGenotype(Genotype genotype) {
        if (genotype instanceof MOOEvaluatedPhenotype) {
            return (MOOEvaluatedPhenotype) genotype;
        }
        Phenotype phenotype = new Phenotype(genotype, image);
        phenotype.genome = phenotype.getThresholdGroupGenome().genome;
        //        phenotype = new Phenotype(phenotype.getThresholdGroupGenome(), image);
        double deviation           = criterion.phenotypeOverallDeviation(phenotype);
        double edgeVal             = criterion.phenotypeEdgeValue(phenotype);
        double connectivityMeasure = criterion.phenotypeConnectivityMeasure(phenotype);

        //        System.out.println();
        //        dbl.log("dev", diviation);
        //        dbl.log("edg", edgeVal);
        //        dbl.log("con mesh", connectivityMeasure);
        //        dbl.log("fit", diviation + edgeVal + connectivityMeasure);
        return new MOOEvaluatedPhenotype(phenotype, edgeVal, connectivityMeasure, deviation);
        //return new MOOEvaluatedPhenotype(phenotype, rng.nextDouble(), rng.nextDouble(), 0.0);
    }

    //    ExecutorService executorService = Executors.newCachedThreadPool();


    private List<MOOEvaluatedPhenotype> evaluatedGenotypes(List<Genotype> genotypes) {
        //        var ret = genotypes.stream()
        //                           .map(genotype -> executorService.submit(new Callable<MOOEvaluatedPhenotype>() {
        //                               @Override
        //                               public MOOEvaluatedPhenotype call() throws Exception {
        //                                   return evaluatedGenotype(genotype);
        //                               }
        //                           }))
        //                           .map(mooEvaluatedPhenotypeFuture -> {
        //                               try {
        //                                   return mooEvaluatedPhenotypeFuture.get();
        //                               } catch (InterruptedException | ExecutionException e) {
        //                                   throw new RuntimeException(e);
        //                               }
        //                           }).collect(Collectors.toCollection(ArrayList::new));
        //
        //
        //        return ret;
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

    public ArrayList<MOOEvaluatedPhenotype> getMostBest(ArrayList<MOOEvaluatedPhenotype> population) {
        ArrayList<MOOEvaluatedPhenotype> deviation    = new ArrayList<>(population);
        ArrayList<MOOEvaluatedPhenotype> edge         = new ArrayList<>(population);
        ArrayList<MOOEvaluatedPhenotype> connectivity = new ArrayList<>(population);

        deviation.sort(Comparator.comparingDouble(o -> o.overallDeviation));
        edge.sort(Comparator.comparingDouble(o -> o.edgeValue));
        connectivity.sort(Comparator.comparingDouble(o -> o.connectivity));

        ArrayList<MOOEvaluatedPhenotype> best    = new ArrayList<>();
        int                              bestVal = 1000000;
        for (int i = 0; i < population.size(); i++) {
            MOOEvaluatedPhenotype phenotype = population.get(i);
            int indexSum = deviation.indexOf(phenotype) + edge.indexOf(phenotype) + connectivity.indexOf(
                    phenotype);
            if (deviation.indexOf(phenotype) < 3 || edge.indexOf(phenotype) < 3 || connectivity.indexOf(phenotype) < 3) {
                continue;
            }

            dbl.log("image ", i, " sum:", indexSum, "idx'es:",
                    deviation.indexOf(phenotype),
                    edge.indexOf(phenotype),
                    connectivity.indexOf(phenotype)
                   );
            if (indexSum == bestVal) {
                best.add(phenotype);
            } else if (indexSum < bestVal) {
                dbl.log("new most best idx:", i, " sum:", indexSum);

                dbl.log("vals:", phenotype.overallDeviation, phenotype.edgeValue, phenotype.connectivity);
                best.clear();
                best.add(phenotype);
                bestVal = indexSum;
            }

        }
        return best;


    }

}
