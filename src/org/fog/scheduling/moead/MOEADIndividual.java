package org.fog.scheduling.moead;

import org.fog.scheduling.MOIndividual;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.utils.Service;

import java.util.Arrays;

public class MOEADIndividual extends MOIndividual{
    /**
     * mutation rate for each mutation operator
     * R1 : reversedMutation
     * R2 : swapHalfMutation
     * R3 : onePointMutation
     */
    private final float R1 = 0.03f, R2 = 0.06f, R3=0.1f;
    public MOEADIndividual(int chromosomeLength, int maxValue) {
        super(chromosomeLength,maxValue);
//        this.setMaxValue(maxValue);
    }

    public MOEADIndividual(int chromosomeLength, int maxValue, int value) {
        super(chromosomeLength, maxValue, value);
    }

    public MOEADIndividual(int chromosomeLength) {
        super(chromosomeLength);
    }

    public Individual onePointCrossOver(Individual other){
        final int POINT = Service.rand(1,getChromosomeLength()-1);
        final int LEN = getChromosomeLength();
        Individual child = new MOEADIndividual(getChromosomeLength(),getMaxValue());
        for (int i=0;i<POINT;++i){
            child.setGene(i,this.getGene(i));
        }
        for (int i=POINT;i<LEN;++i){
            child.setGene(i, other.getGene(i));
        }
        return child;
    }

    public void mutate(float rate){
        if (rate<R1){
            reversedMutation();
        } else if (rate<R2){
            swapHalfMutation();
        } else if (rate<=R3){
            onePointMutation();
        }
    }
}
