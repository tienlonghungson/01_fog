package org.fog.scheduling.nsgaii;

import org.fog.scheduling.MOIndividual;
import org.fog.scheduling.gaEntities.Individual;

import java.util.ArrayList;
import java.util.List;

public class NSGAIIIndividual extends MOIndividual {

    /**
     * numDominated : the number of individual dominating this individual
     * dominatedList : the list of individual dominated by this individual
     * rank : front index
     * fitness: first used for crowding distance, in the end used for fitness
     */
    private int numDominated = 0;
    private final List<NSGAIIIndividual> dominatedList = new ArrayList<>();
    private int rank;

    public NSGAIIIndividual(int chromosomeLength, int maxValue) {
        super(chromosomeLength, maxValue);
    }

    public NSGAIIIndividual(int chromosomeLength) {
        super(chromosomeLength);
    }

    protected int getNumDominated() {
        return numDominated;
    }

    protected void updateNumDominated(int add) {
        numDominated += add;
    }

    protected List<NSGAIIIndividual> getDominatedList() {
        return dominatedList;
    }

    protected int getRank() {
        return rank;
    }

    protected void setRank(int rank) {
        this.rank = rank;
    }


    /**
     * crowding comparison
     *
     * @param other compared individual
     * @return {$code true} if this individual > compared individual
     */
    protected boolean isCrowding(NSGAIIIndividual other) {
        return (this.getRank() < other.getRank()) ||
                (this.getRank() == other.getRank() && this.getFitness() > other.getFitness());
    }

    @Override
    public String toString() {
        return "NSGAIIIndividual{" +
                "numDominated=" + numDominated +
                ", dominatedList=" + dominatedList.size() +
                ", rank=" + rank +
                ", time=" + getTime() +
                ", cost=" + getCost() +
                "}\n";
    }
}
