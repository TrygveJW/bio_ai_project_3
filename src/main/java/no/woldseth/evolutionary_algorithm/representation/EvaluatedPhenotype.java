package no.woldseth.evolutionary_algorithm.representation;

public class EvaluatedPhenotype extends Phenotype implements Comparable<EvaluatedPhenotype> {

    public final double fitness;

    public EvaluatedPhenotype(Phenotype phenotype, double fitness) {
        super(phenotype.genome, phenotype.image);
        this.fitness = fitness;
    }


    @Override
    public int compareTo(EvaluatedPhenotype o) {
        return Double.compare(fitness, o.fitness);
    }
}
