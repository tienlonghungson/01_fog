package org.fog.scheduling;

import org.fog.scheduling.gaEntities.Individual;
import org.fog.scheduling.nsgaii.NSGAIIIndividual;
import org.fog.utils.Service;

public abstract class MOIndividual extends Individual{
    public MOIndividual(int chromosomeLength, int maxValue) {
        super(chromosomeLength, maxValue);
    }

    public MOIndividual(int chromosomeLength, int maxValue, int value) {
        super(chromosomeLength, maxValue, value);
    }

    public MOIndividual(int chromosomeLength) {
        super(chromosomeLength);
    }

    public void reversedMutation(){
        final int LEN = getChromosomeLength();
        for (int i=0;i<LEN;++i){
            chromosome[i]=chromosome[i]^chromosome[LEN-1-i];
            chromosome[LEN-1-i]=chromosome[i]^chromosome[LEN-1-i];
            chromosome[i]=chromosome[i]^chromosome[LEN-1-i];
        }
    }

    public void swapHalfMutation(){
        final int LEN = getChromosomeLength();
        final int DIS = (LEN-1)>>1;
        for (int i=0;i<DIS;++i){
            chromosome[i]=chromosome[i]^chromosome[i+DIS];
            chromosome[LEN-1-i]=chromosome[i]^chromosome[i+DIS];
            chromosome[i]=chromosome[i]^chromosome[i+DIS];
        }
    }

    public void onePointMutation(){
        final int POINT = Service.rand(0,getChromosomeLength()-1);
        chromosome[POINT] = Service.rand(0,getMaxValue());
    }

    public boolean isDominating(Individual other){
        return (this.getTime()<other.getTime() && this.getCost()<=other.getCost())
                || (this.getTime()<=other.getTime()&&this.getCost()<other.getCost());
    }

}
