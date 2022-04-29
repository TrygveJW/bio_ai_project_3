package no.woldseth.evolutionary_algorithm.representation;

import no.woldseth.evolutionary_algorithm.PhenoInterface;
import no.woldseth.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MOOEvaluatedPhenotype extends Phenotype implements PhenoInterface {

    public List<Double> fitnessValues;
    public final double edgeValue;
    public final double connectivity;
    public final double overallDeviation;
    public List<MOOEvaluatedPhenotype> dominatedSolutions;
    private int dominationCount = 0;
    private int rank = - 1;
    private double crowdingDist;

    public MOOEvaluatedPhenotype(Phenotype phenotype, double edgeValue, double connectivity, double overallDeviation) {
        super(phenotype.genome, phenotype.image);

        this.edgeValue          = edgeValue;
        this.connectivity       = connectivity;
        this.overallDeviation   = overallDeviation;
        this.fitnessValues      = List.of(edgeValue, connectivity, overallDeviation);
        this.dominatedSolutions = new ArrayList<>();

    }

    public void resetFrontInfo() {
        this.rank               = - 1;
        this.crowdingDist       = 0.0;
        this.dominationCount    = 0;
        this.dominatedSolutions = new ArrayList<>();
    }

    public double getFitnessValueByIdx(int idx) {
        return this.fitnessValues.get(idx);
    }

    public void addDominatedSolution(MOOEvaluatedPhenotype dominated) {
        this.dominatedSolutions.add(dominated);
    }

    public int getDominationCount() {
        return this.dominationCount;
    }

    public void incrementDominationCount() {
        this.dominationCount += 1;
    }

    public void decrementDominationCount() {
        this.dominationCount -= 1;
    }

    public List<MOOEvaluatedPhenotype> getDominatedSolutions() {
        return this.dominatedSolutions;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getCrowdingDist() {
        return crowdingDist;
    }

    public void setCrowdingDist(double crowdingDist) {
        this.crowdingDist = crowdingDist;

    }

    @Override
    public double getFitness() {
        return this.crowdingDist;
    }

    @Override
    public Genotype getGenotype() {
        return (Genotype) this;
    }


}
