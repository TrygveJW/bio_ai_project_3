package no.woldseth.evolutionary_algorithm.representation;

import no.woldseth.evolutionary_algorithm.PhenoInterface;

public class EvaluatedPhenotype extends Phenotype implements Comparable<EvaluatedPhenotype>, PhenoInterface {

    public double fitness;

    public EvaluatedPhenotype(Phenotype phenotype, double fitness) {
        super(phenotype.genome, phenotype.image);
        this.fitness = fitness;
    }


    @Override
    public int compareTo(EvaluatedPhenotype o) {
        return Double.compare(fitness, o.fitness);
    }

    @Override
    public double getFitness() {
        return this.fitness;
    }

    @Override
    public Genotype getGenotype() {
        return new Genotype(this.genome);
    }
}
