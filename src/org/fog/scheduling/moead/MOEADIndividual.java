package org.fog.scheduling.moead;

import org.fog.scheduling.MOIndividual;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.utils.Service;


public class MOEADIndividual extends MOIndividual{
    /**
     * mutation rate for each mutation operator
     * R1 : reversedMutation
     * R2 : swapHalfMutation
     * R3 : onePointMutation
     */
    private final float R1 = 0.2f, R2 = 0.4f, R3=0.6f;

    public MOEADIndividual(int chromosomeLength,int maxValue){
        super(chromosomeLength);
        setMaxValue(maxValue);
    }
    public MOEADIndividual(int chromosomeLength, int maxValue, int lambda) {
        super(chromosomeLength);
        this.setMaxValue(maxValue);
        for (int i=0;i<chromosomeLength;++i){
            chromosome[i]=Math.min(maxValue,Service.poissonRand(lambda));
        }
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

    public Individual twoPointCrossOver(Individual other){
        final int LEN = getChromosomeLength();
        final int POINT1 = Service.rand(0,(LEN-3))+1;
        final int POINT2 = Service.rand(0,(LEN-POINT1-2))+POINT1+1;

        Individual child = new MOEADIndividual(getChromosomeLength(),getMaxValue());
        for (int i = 0; i < POINT1; i++) {
            child.setGene(i,this.getGene(i));
        }
        for (int i = POINT1; i < POINT2; i++) {
            child.setGene(i,other.getGene(i));
        }
        for (int i = POINT1; i < POINT2; i++) {
            child.setGene(i, this.getGene(i));
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
