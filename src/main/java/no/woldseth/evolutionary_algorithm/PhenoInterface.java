package no.woldseth.evolutionary_algorithm;

import no.woldseth.evolutionary_algorithm.representation.Genotype;

public interface PhenoInterface {
    public double getFitness();
    public Genotype getGenotype();

}
