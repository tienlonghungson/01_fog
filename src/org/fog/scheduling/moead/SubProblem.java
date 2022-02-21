package org.fog.scheduling.moead;

import java.util.List;

public class SubProblem {
    public final float TIME_WEIGHT, COST_WEIGHT;
    private MOEADIndividual individual;
    private final int[] neighborProblems;

    public SubProblem(float timeWeight, MOEADIndividual individual, int numNeighbors,int[] idxNeighbor){
        this(timeWeight, numNeighbors, idxNeighbor);
        setIndividual(individual);
    }

    public SubProblem(float timeWeight, int numNeighbors,int[] idxNeighbor){
        assert (numNeighbors==idxNeighbor.length);
        assert (timeWeight<1&&timeWeight>0);
        TIME_WEIGHT = timeWeight;
        COST_WEIGHT = 1 - TIME_WEIGHT;
        neighborProblems = new int[numNeighbors];
        System.arraycopy(idxNeighbor, 0, neighborProblems, 0, numNeighbors);
    }

    public int getIdxNeighbor(int i){
        return neighborProblems[i];
    }

    public MOEADIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(MOEADIndividual individual) {
        this.individual = individual;
    }
}
