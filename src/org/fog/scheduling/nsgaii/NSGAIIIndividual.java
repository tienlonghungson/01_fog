package org.fog.scheduling.nsgaii;

import org.fog.scheduling.gaEntities.Individual;

import java.util.ArrayList;
import java.util.List;

public class NSGAIIIndividual extends Individual {

    /**
     * numDominated : the number of individual dominating this individual
     * dominatedList : the list of individual dominated by this individual
     * rank : front index
     * fitness: first used for crowding distance, in the end used for fitness
     */
    private int numDominated=0;
    private List<NSGAIIIndividual> dominatedList = new ArrayList<>();
    private int rank;

    public NSGAIIIndividual(int chromosomeLength, int maxValue) {
        super(chromosomeLength, maxValue);
    }

    public NSGAIIIndividual(int chromosomeLength){
        super(chromosomeLength);
    }

    protected int getNumDominated() {
        return numDominated;
    }
    protected void updateNumDominated(int add){
        numDominated+=add;
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

    protected boolean isDominating(NSGAIIIndividual other){
        return (this.getTime()<other.getTime() && this.getCost()<other.getCost());
    }

    /**
     * crowding comparison
     * @param other compared individual
     * @return {$code true} if this individual > compared individual
     */
    protected boolean isCrowding(NSGAIIIndividual other){
        return (this.getRank()<other.getRank()) ||
                (this.getRank()==other.getRank() && this.getFitness()>other.getFitness());
    }

    protected void reversedMutation(){
        final int LEN = getChromosomeLength();
        for (int i=0;i<LEN;++i){
            chromosome[i]=chromosome[i]^chromosome[LEN-1-i];
            chromosome[LEN-1-i]=chromosome[i]^chromosome[LEN-1-i];
            chromosome[i]=chromosome[i]^chromosome[LEN-1-i];
        }
    }

    protected void swapHalfMutation(){
        final int LEN = getChromosomeLength();
        final int DIS = (LEN-1)>>1;
        for (int i=0;i<DIS;++i){
            chromosome[i]=chromosome[i]^chromosome[i+DIS];
            chromosome[LEN-1-i]=chromosome[i]^chromosome[i+DIS];
            chromosome[i]=chromosome[i]^chromosome[i+DIS];
        }
    }

    @Override
    public String toString() {
        return "NSGAIIIndividual{" +
                "numDominated=" + numDominated +
                ", dominatedList=" + dominatedList.size() +
                ", rank=" + rank +
                ", time=" + getTime()+
                ", cost=" + getCost()+
                "}\n";
    }
}
