package no.woldseth.evolutionary_algorithm;

import lombok.AllArgsConstructor;
import no.woldseth.DebugLogger;
import no.woldseth.ExtendedRandom;
import no.woldseth.evolutionary_algorithm.representation.EvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Genotype;
import no.woldseth.evolutionary_algorithm.representation.MOOEvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Phenotype;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Selection {


    private static ExtendedRandom rng = new ExtendedRandom();

    @AllArgsConstructor
    public static class ParentPair {
        public Genotype parent1;
        public Genotype parent2;
    }

    private static final int tournamentSize = 4;

    public static ParentPair tournamentParentSelection(List<? extends PhenoInterface> parentCandidates,
                                                       boolean maximize) {
        List<? extends PhenoInterface> tournamentPool = rng.randomChoice(parentCandidates, tournamentSize, true);

        int flip = maximize ? - 1 : 1;
        tournamentPool.sort(Comparator.comparingDouble(o -> flip * o.getFitness()));

        if (maximize) {
            if (tournamentPool.get(0).getFitness() < tournamentPool.get(1).getFitness()) {
                throw new RuntimeException();
            }
        }
        return new ParentPair(tournamentPool.get(0).getGenotype(), tournamentPool.get(1).getGenotype());
    }


    public static ParentPair randomParentSelection(List<? extends PhenoInterface> parentCandidates) {
        List<? extends PhenoInterface> tournamentPool = rng.randomChoice(parentCandidates, 2, false);
        return new ParentPair(tournamentPool.get(0).getGenotype(), tournamentPool.get(1).getGenotype());
    }

    public static List<EvaluatedPhenotype> killAllParentsSelection(List<EvaluatedPhenotype> population,
                                                                   List<ParentPair> parentPairs) {
        parentPairs.forEach(parentPair -> {
            population.remove(parentPair.parent1);
            population.remove(parentPair.parent2);
        });
        return population;
    }


    public static List<EvaluatedPhenotype> elitismSelection(List<EvaluatedPhenotype> population,
                                                            int goalSize,
                                                            boolean maximize) {
        List<EvaluatedPhenotype> ret  = new ArrayList<>();
        int                      flip = maximize ? - 1 : 1;
        population.sort((o1, o2) -> flip * o1.compareTo(o2));

        //        System.out.println();
        //        System.out.println();
        //        System.out.println();
        //        for (int i = 0; i < population.size(); i++) {
        //            System.out.println(population.get(i).fitness);
        //        }

        for (int i = 0; i < goalSize; i++) {
            ret.add(population.get(i));
        }

        //        System.out.println();
        //        for (int i = 0; i < ret.size(); i++) {
        //            System.out.println(ret.get(i).fitness);
        //        }

        return ret;
    }
}
