package org.fog.scheduling.moead;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.AbstractAlgorithm;
import org.fog.scheduling.SchedulingAlgorithm;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.utils.Pair;
import org.fog.utils.Service;

import java.util.*;

public class MOEAD extends AbstractAlgorithm {
    public final int NUM_SUB_PROBLEM;
    public final int NUM_NEIGHBOR;
    private final List<Integer> oneToNArray;
    private final SubProblem[] subProblems;
    private final List<Individual> externalPop = new LinkedList<>();
    Random rd = new Random();

    public MOEAD(int numSubProblem, int numNeighbor,int chromosomeLength, int maxValue){
        NUM_SUB_PROBLEM = numSubProblem;
        NUM_NEIGHBOR = numNeighbor;
        oneToNArray = new ArrayList<>(NUM_NEIGHBOR);
        for (int i=0;i<NUM_NEIGHBOR;++i){
            oneToNArray.add(i);
        }
        subProblems= initSubProblem(chromosomeLength, maxValue);
    }

    /**
     * generate weight vector
     * @return a 2d array
     */
    private double[][] genWeightVector(){
        double[][] weightVector = new double[NUM_SUB_PROBLEM][2];
        Random rd = new Random();
//        final double FACTOR = rd.nextDouble();
        for (int i=0;i<NUM_SUB_PROBLEM-1;++i){
            weightVector[i][0]=((double) (i+1))/(double) NUM_SUB_PROBLEM;
            weightVector[i][1]=1-weightVector[i][0];
        }
        weightVector[NUM_SUB_PROBLEM-1][0]= SchedulingAlgorithm.TIME_WEIGHT;
        weightVector[NUM_SUB_PROBLEM-1][1]=1-weightVector[NUM_SUB_PROBLEM-1][0];
        return weightVector;
    }

    /**
     * determine the neighbor list for each weighted vector
     * @param weightVector list of weighted vector
     * @return list of neighbor
     */
    private int[][] getNeighbor(double[][] weightVector){
        List<List<Pair<Integer,Double>>> distanceList = new ArrayList<>(NUM_SUB_PROBLEM);
        for (int i=0;i<NUM_SUB_PROBLEM;++i){
            distanceList.add(new ArrayList<>(NUM_SUB_PROBLEM));
            distanceList.get(i).add(new Pair<>(i,0d));
        }
        double dis;
        for (int i=0;i<NUM_SUB_PROBLEM-1;++i){
            for (int j=i+1;j<NUM_SUB_PROBLEM;++j){
                dis = Service.euclidDistance(weightVector[i],weightVector[j]);
                distanceList.get(i).add(new Pair<>(j,dis));
                distanceList.get(j).add(new Pair<>(i,dis));
            }
        }
        for (int i=0;i<NUM_SUB_PROBLEM;++i){
            distanceList.get(i).sort((Comparator.comparing(Pair::second)));
        }
        int[][] neighborList = new int[NUM_SUB_PROBLEM][NUM_NEIGHBOR];
        for (int i=0;i<NUM_SUB_PROBLEM;++i){
            for (int j=0;j<NUM_NEIGHBOR;++j){
                neighborList[i][j] = distanceList.get(i).get(j).first();
            }
        }
        return neighborList;
    }

    private SubProblem[] initSubProblem(int chromosomeLength, int maxValue){
        SubProblem[] subProblems = new SubProblem[NUM_SUB_PROBLEM];
        double[][] weightedVector = genWeightVector();
        int[][] neighbors = getNeighbor(weightedVector);
        int lambda;
        for (int i = 0; i < NUM_SUB_PROBLEM; i++) {
            lambda = rd.nextInt(15)+1;
            MOEADIndividual individual = new MOEADIndividual(chromosomeLength,maxValue,lambda);
            subProblems[i] = new SubProblem((float) weightedVector[i][0], individual,NUM_NEIGHBOR,neighbors[i]);
        }
        return subProblems;
    }

    public void evalPopulation(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList){
        for (int i=0;i<NUM_SUB_PROBLEM;++i){
            calcFitness(subProblems[i].getIndividual(),fogDevices,cloudletList,subProblems[i].TIME_WEIGHT);
        }
    }

    public void update(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList){
        double auxFitness; // store fitness for a new solution in a sub-problem
        SubProblem auxSubProblem; // store a neighbor sub-problem
        boolean isAdded; // whether a new offspring is added to EP
        MOEADIndividual auxInd; // store a temporary individual in EP
        for (int i = 0; i < NUM_SUB_PROBLEM; i++) {
            int idxSubPro1, idxSubPro2;
            Collections.shuffle(oneToNArray);
            idxSubPro1 = subProblems[i].getIdxNeighbor(oneToNArray.get(0));
            idxSubPro2 = subProblems[i].getIdxNeighbor(oneToNArray.get(1));

            MOEADIndividual offspringInd = (MOEADIndividual) subProblems[idxSubPro1].getIndividual().
                                            twoPointCrossOver(subProblems[idxSubPro2].getIndividual());
            float mutationRate = rd.nextFloat();
            offspringInd.mutate(mutationRate);

            calcFitness(offspringInd,fogDevices,cloudletList,subProblems[i].TIME_WEIGHT);
            if (subProblems[i].getIndividual().getFitness()<offspringInd.getFitness()){
                subProblems[i].setIndividual((MOEADIndividual) offspringInd.clone());
            }

            for (int j=1;j<NUM_NEIGHBOR;++j){
                auxSubProblem = subProblems[subProblems[i].getIdxNeighbor(j)];
                auxFitness = updateFitnessWithoutSaving(offspringInd,auxSubProblem.TIME_WEIGHT);
                if (auxSubProblem.getIndividual().getFitness()<auxFitness){
                    auxSubProblem.setIndividual((MOEADIndividual) offspringInd.clone());
                    auxSubProblem.getIndividual().setFitness(auxFitness);
                }
            }

            isAdded = true;
            Iterator<Individual> iterator = externalPop.iterator();
            while (iterator.hasNext()){
                auxInd = (MOEADIndividual) iterator.next();
                if (auxInd.isDominating(offspringInd)){
                    isAdded = false;
                }
                if (offspringInd.isDominating(auxInd)){
                    iterator.remove();
                }
            }
            if (isAdded){
                externalPop.add(offspringInd);
            }

        }

//        System.out.println("Current EP");
//        Iterator<Individual> iterator = externalPop.iterator();
//        while (iterator.hasNext()){
//            System.out.println(iterator.next());
//        }
    }

    private double updateFitnessWithoutSaving(Individual individual, double timeWeight){
        return timeWeight*getMinTime()/individual.getTime() + (1-timeWeight)*getMinCost()/individual.getCost();
    }

    private double updateFitnessAndSave(Individual individual, double timeWeight){
        individual.setFitness(updateFitnessWithoutSaving(individual,timeWeight));
        return individual.getFitness();
    }

    private ArrayList<Pair<Double,Individual>> sortEP(double timeWeight){
        Iterator<Individual> iterator = externalPop.iterator();
        MOEADIndividual auxInd;
        ArrayList<Pair<Double,Individual>> externalPopArray = new ArrayList<>();
        while(iterator.hasNext()){
            auxInd = (MOEADIndividual) iterator.next();
            externalPopArray.add(new Pair<>(updateFitnessWithoutSaving(auxInd,timeWeight),auxInd));
        }
        externalPopArray.sort(Comparator.comparingDouble(Pair<Double,Individual>::first).reversed());
        return externalPopArray;
    }

    public Pair<Double,Individual> getBestFitness(double timeWeight){
        ArrayList<Pair<Double,Individual>> externalPopArray=sortEP(timeWeight);
        return externalPopArray.get(0);
    }
}
