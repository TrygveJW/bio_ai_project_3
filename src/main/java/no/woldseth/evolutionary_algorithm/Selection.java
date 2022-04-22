package no.woldseth.evolutionary_algorithm;

import lombok.AllArgsConstructor;
import no.woldseth.DebugLogger;
import no.woldseth.ExtendedRandom;
import no.woldseth.evolutionary_algorithm.representation.EvaluatedPhenotype;
import no.woldseth.evolutionary_algorithm.representation.Genotype;

import java.util.ArrayList;
import java.util.List;

public class Selection {


    private static ExtendedRandom rng = new ExtendedRandom();

    @AllArgsConstructor
    public static class ParentPair {
        public Genotype parent1;
        public Genotype parent2;
    }

    private static final int tournamentSize = 5;

    public static ParentPair tournamentParentSelection(List<EvaluatedPhenotype> parentCandidates, boolean maximize) {
        List<EvaluatedPhenotype> tournamentPool = rng.randomChoice(parentCandidates, tournamentSize, true);

        int flip = maximize ? - 1 : 1;
        tournamentPool.sort((o1, o2) -> flip * o1.compareTo(o2));

        if (maximize) {
            if (tournamentPool.get(0).fitness < tournamentPool.get(1).fitness) {
                throw new RuntimeException();
            }
        }
        return new ParentPair(tournamentPool.get(0), tournamentPool.get(1));

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

        for (int i = 0; i < goalSize; i++) {
            ret.add(population.get(i));
        }

        return population;
    }
}
