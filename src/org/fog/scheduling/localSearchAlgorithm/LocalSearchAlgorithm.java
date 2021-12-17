package org.fog.scheduling.localSearchAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.AbstractAlgorithm;
import org.fog.scheduling.SchedulingAlgorithm;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.scheduling.gaEntities.Service;

public class LocalSearchAlgorithm extends AbstractAlgorithm {

    public LocalSearchAlgorithm() {

    }

    public Individual hillClimbing(Individual individual, List<FogDevice> fogDevices,
                                   List<? extends Cloudlet> cloudletList) {

        // listChange contains which gene change makes the individual better
        List<Pair> listChange = new ArrayList<Pair>();

        int numberRound = 0;

        // Start local search loop
        do {
            numberRound++;
            System.out.println("\n--------------------------------------");
            System.out.println("Round " + numberRound + ": ");
            listChange.clear();
            // fitness stores the fitness value of current individual
            double fitness = calcFitness(individual, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT);

            // consider which gene changed makes individual better
            for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {

                    // create newIndividual similar to individual
                    Individual newIndividual = new Individual(individual.getChromosomeLength());
                    for (int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
                        newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
                    }

                    // change a gene of individual to form newIndividual
                    newIndividual.setGene(cloudletId, fogId);
                    double newFitness = calcFitness(newIndividual, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT);
                    // if newIndividual is better then individual, store change
                    // in listChange
                    if (newFitness > fitness) {
                        listChange.add(new Pair(cloudletId, fogId));
                    }
                }
            }

            // if exist any gene make individual better, select randomly a gene
            // change to have newIndividual
            System.out.println("Number of changelist: " + listChange.size());
            if (!listChange.isEmpty()) {
                int change = Service.rand(0, listChange.size() - 1);
                individual.setGene(listChange.get(change).getCloudletId(), listChange.get(change).getFogId());
                System.out.println("change possition: " + listChange.get(change).getCloudletId() + " "
                        + listChange.get(change).getFogId());
            }
            individual.printGene();

            System.out.println("\nFitness value: " + individual.getFitness());
            System.out.println("Min Time: " + this.getMinTime() + "/// Makespan: " + individual.getTime());
            System.out.println("Min Cost: " + this.getMinCost() + "/// TotalCost: " + individual.getCost());

        } while (!listChange.isEmpty());
        return individual;
    }

    public void restart(Individual individual, int tabu[][]) {
        individual = new Individual(individual.getChromosomeLength(), individual.getMaxValue());


        for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
            for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                tabu[cloudletId][fogId] = -1;
            }
        }

    }

    public Individual tabuSearch(Individual individual, List<FogDevice> fogDevices,
                                 List<? extends Cloudlet> cloudletList, int maxStable, int maxInteration, int maxTime, int tabuLength) {

        // initiate Tabu metric, the value of each element is -1
        int[][] tabuMetric = new int[individual.getChromosomeLength()][individual.getMaxValue() + 1];
        for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
            for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                tabuMetric[cloudletId][fogId] = -1;
            }
        }

        Individual bestSolution = new Individual(cloudletList.size(), fogDevices.size() - 1);
        double bestValue = calcFitness(bestSolution, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT);

        // listChange contains which gene change makes the individual better
        List<Pair> listChange = new ArrayList<Pair>();

        double start = System.currentTimeMillis();
        int count = 0;
        maxTime = maxTime * 1000;
        Random R = new Random();
        int nic = 0;

        while (System.currentTimeMillis() - start < maxTime && count < maxInteration) {
            int sel_i = -1;
            int sel_v = -1;
            listChange.clear();
            double min = -10000;
            double valueIndividual = calcFitness(individual, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT);
            // consider which gene changed makes individual better
            for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {

                    if (tabuMetric[cloudletId][fogId] <= count) {
                        // create newIndividual similar to individual
                        Individual newIndividual = new Individual(individual.getChromosomeLength());
                        for (int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
                            newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
                        }

                        // change a gene of individual to form newIndividual
                        newIndividual.setGene(cloudletId, fogId);
                        double newFitness = calcFitness(newIndividual, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT);
                        double deltaF = newFitness - valueIndividual;
                        // if newIndividual is better then individual, store change
                        // in listChange
                        if (deltaF > min) {
                            min = deltaF;
                            sel_i = cloudletId;
                            sel_v = fogId;
                            listChange.clear();
                            listChange.add(new Pair(sel_i, sel_v));
                        } else if (deltaF == min) {
                            listChange.add(new Pair(cloudletId, fogId));
                        }
                    }

                }
            }
            if (listChange.size() > 0) {
                int k = R.nextInt(listChange.size());
                Pair p = listChange.get(k);
                sel_i = p.getCloudletId();
                sel_v = p.getFogId();
                individual.setGene(sel_i, sel_v);
                tabuMetric[sel_i][sel_v] = count + tabuLength;
                valueIndividual = calcFitness(individual, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT);
                System.out.println("Step: " + count + "----Current value: " + valueIndividual + "----Best value: " + bestValue + "----Delta: " + min + "----Nic: " + nic);

                if (valueIndividual > bestValue) {
                    bestValue = valueIndividual;
                    for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                        bestSolution.setGene(geneIndex, individual.getGene(geneIndex));
                    }
                }
                if (valueIndividual <= bestValue) {
                    nic++;
                    if (nic > maxStable) {
                        nic = 0;
                        System.out.println("Tabu restart:");
//                                              restart(individual, tabuMetric);
                        individual = new Individual(individual.getChromosomeLength(), individual.getMaxValue());
                        for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                            for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                                tabuMetric[cloudletId][fogId] = -1;
                            }
                        }
                    }
                }
            } else {
                nic = 0;
                System.out.println("Tabu restart:");
//                              restart(individual, tabuMetric);
                individual = new Individual(individual.getChromosomeLength(), individual.getMaxValue());
                for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                    for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                        tabuMetric[cloudletId][fogId] = -1;
                    }
                }
            }
            count++;
        }
        return bestSolution;

    }

}
