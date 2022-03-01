package org.fog.scheduling.bqtsearch;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.AbstractAlgorithm;
import org.fog.scheduling.SchedulingAlgorithm;
import org.fog.scheduling.gaEntities.Individual;

import java.util.Arrays;
import java.util.List;

public class TabuSearch extends AbstractAlgorithm {
    private static int NUM_GENERATION;

    private static void setNumGeneration(int nCloudlets, int nFogs) {
        NUM_GENERATION = Math.max(50, Math.min(1500, 100 * Math.max(nCloudlets / nFogs, nFogs / nCloudlets)));
    }

    private static void setNumGeneration() {
        NUM_GENERATION = 2000;
    }

    private int tbl = 5;

    Solution currSol, bestSol, lastImprovedSol;

    private final int N_CLOUDLETS;
    private final int N_FOGS;

    public TabuSearch(int N_CLOUDLETS, int N_FOGS) {
        this.N_CLOUDLETS = N_CLOUDLETS;
        this.N_FOGS = N_FOGS;
    }

    /**
     * generate a random solution
     *
     * @param chromosome length of solution array
     * @param maxValue   upperbound of element in solution array
     * @return random solution
     */
    private Solution genSolution(int chromosome, int maxValue) {
        return new Solution(chromosome, maxValue);
    }

    public Individual search(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletsList) {
        Solution.fogDevices = fogDevices;
        Solution.cloudletsList = cloudletsList;
        Solution.minTime = getMinTime();
        Solution.minCost = getMinCost();
        Solution.timeWeight = SchedulingAlgorithm.TIME_WEIGHT;

        int[] tabu = new int[N_CLOUDLETS];

        currSol = genSolution(N_CLOUDLETS, N_FOGS - 1);
        double bestFitness = 0;
        double oldFitness;

        int it = 0;
        final int TB_MIN = 2, TB_MAX = 5;
        int stable = 0, stableLimit = 50;
        int restartFreq = 200;

        setNumGeneration();
        System.out.println("#Generations=" + NUM_GENERATION);
        while (it < NUM_GENERATION) {
            it++;
            if (currSol.getFitness() > bestFitness) {
                bestFitness = currSol.getFitness();
                bestSol = (Solution) currSol.clone();
                stable = 0;
            } else if (stable == stableLimit) {
                currSol = (Solution) lastImprovedSol.clone();
                stable = 0;
            } else {
                stable++;
                if (it % restartFreq == 0) {
                    currSol = genSolution(N_CLOUDLETS, N_FOGS - 1);
                    Arrays.fill(tabu, 0);
                }
            }

            oldFitness = currSol.getFitness();
            int[] moveToNext = currSol.findBestNeighbor(tabu);
            if (moveToNext[0] == -1 ||
                    moveToNext[1] == -1 ||
                    moveToNext[2] == -1) {
                currSol = genSolution(N_CLOUDLETS, N_FOGS - 1);
                continue;
            }
            currSol.update(moveToNext);
            for (int i = 0; i < tabu.length; ++i) {
                if (tabu[i] > 0) {
                    tabu[i]--;
                }
            }

            tabu[moveToNext[0]] = tbl;
            if (currSol.getFitness() > oldFitness) {
                if (tbl > TB_MIN) {
                    tbl--;
                }

                lastImprovedSol = (Solution) currSol.clone();
                stable = 0;
            } else {
                if (tbl < TB_MAX) {
                    tbl++;
                }
            }
            System.out.println("\nSolution of generation " + it + ": " + currSol.getFitness());
            System.out.println("Makespan: (" + getMinTime() + ")--" + currSol.getTime());
            System.out.println("TotalCost: (" + getMinCost() + ")--" + currSol.getCost());
        }

        System.out.println(">>>>>>>>>>>>>>>>>>>RESULTS<<<<<<<<<<<<<<<<<<<<<");
        System.out.println("Found solution in " + it + " generations");
        bestSol.printGene();
        System.out.println("\nBest solution: " + bestSol.getFitness());
        System.out.println("Double Check Fitness\nAbstract's Fitness :" +
                calcFitness(bestSol, fogDevices, cloudletsList, SchedulingAlgorithm.TIME_WEIGHT));
        return bestSol;
    }
}
