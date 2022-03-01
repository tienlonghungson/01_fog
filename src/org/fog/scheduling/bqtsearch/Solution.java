package org.fog.scheduling.bqtsearch;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.utils.Service;

import java.util.Arrays;
import java.util.List;

public class Solution extends Individual {
    public static List<? extends Cloudlet> cloudletsList;
    public static List<FogDevice> fogDevices;
    public static double timeWeight, minTime, minCost;

    private double[] extTimeNodes;
    private double[] costTasks;


    public Solution(int chromosomeLength, int maxValue) {
        super(chromosomeLength, maxValue);
        extTimeNodes = new double[maxValue+1];
        costTasks = new double[chromosomeLength];
        calExecTimeAndCostTaskAndFitness();
    }

    /**
     * update currLoad, Violation and Objection following the information of the move
     *
     * @param moveInfo a 3-elements integer array
     *                 first: index of task
     *                 second: index of the old node assigned to this task
     *                 third: index of the new node assigned to this task
     */
    protected void updateExecTimeAndCostTaskAndFitness(int[] moveInfo) {
        int idxTask = moveInfo[0];
        int oldNode = moveInfo[1];
        int newNode = moveInfo[2];

        extTimeNodes[oldNode] -= ((double) cloudletsList.get(idxTask).getCloudletLength()) /
                fogDevices.get(oldNode).getHostList().get(0).getTotalMips();
        extTimeNodes[newNode] += ((double) cloudletsList.get(idxTask).getCloudletLength()) /
                fogDevices.get(newNode).getHostList().get(0).getTotalMips();

        double time = 0;
        for (double extTime : extTimeNodes) {
            time = Math.max(time, extTime);
        }
        setTime(time);

        double costChanged = 0;
        costChanged -= costTasks[idxTask];
        costTasks[idxTask] = Service.calcCost(cloudletsList.get(idxTask), fogDevices.get(newNode));
        costChanged += costTasks[idxTask];
        setCost(getCost() + costChanged);
        // update objective

        double fitness = timeWeight * minTime / time
                + (1 - timeWeight) * minCost / getCost();
        setFitness(fitness);
    }

    /**
     * update solution by a new move
     *
     * @param moveInfo a 3-elements integer array
     *                 first: index of task
     *                 second: index of the old node assigned to this task
     *                 third: index of the new node assigned to this task
     */
    protected void update(int[] moveInfo) {
        setGene(moveInfo[0], moveInfo[2]);
        updateExecTimeAndCostTaskAndFitness(moveInfo);
    }

    /**
     * 1st time calculate diffLoad
     * , violation, totalDistance and objective
     */

    protected void calExecTimeAndCostTaskAndFitness() {
        Arrays.fill(extTimeNodes,0);
        Arrays.fill(costTasks,0);

        double time=0, cost=0;
        int idxNode;
        for (int idxTask = 0; idxTask < getChromosomeLength(); idxTask++) {
            idxNode = getGene(idxTask);
            extTimeNodes[idxNode]+=cloudletsList.get(idxTask).getCloudletLength();
            costTasks[idxTask]= Service.calcCost(cloudletsList.get(idxTask),fogDevices.get(idxNode));
            cost += costTasks[idxTask];
        }

        for ( idxNode=0;idxNode< extTimeNodes.length;++idxNode){
            extTimeNodes[idxNode]/=fogDevices.get(idxNode).getHostList().get(0).getTotalMips();
            time = Math.max(extTimeNodes[idxNode], time);
        }

        setTime(time);
        setCost(cost);

        double fitness = timeWeight * minTime / time
                + (1 - timeWeight) * minCost / cost;
        setFitness(fitness);
    }


    /**
     * find the best neighbor
     *
     * @param tabu array of tabu
     * @return moveInfo a 3-elements integer array
     *                 first: index of task
     *                 second: index of the old node assigned to this task
     *                 third: index of the new node assigned to this task
     */
    protected int[] findBestNeighbor(int[] tabu) {
        int selectTaskIdx = -1;
        int selectOldNode = -1;
        int selectNewNode = -1;

        double neighBestObj = 0;

        final int maxValue = getMaxValue();
        int oldNode;
        for (int taskIdx = 0; taskIdx < getChromosomeLength(); ++taskIdx) {
            if (tabu[taskIdx] > 0) {
                continue;
            }
            oldNode = getGene(taskIdx);
            for (int newNode = 0; newNode < maxValue; ++newNode) {
                if (newNode != oldNode) {
                    updateExecTimeAndCostTaskAndFitness(new int[]{taskIdx, oldNode, newNode});
                    if (getFitness() > neighBestObj) {
                        neighBestObj = getFitness();
                        selectTaskIdx = taskIdx;
                        selectOldNode = oldNode;
                        selectNewNode = newNode;
                    }
                    updateExecTimeAndCostTaskAndFitness(new int[]{taskIdx, newNode, oldNode});
                }
            }
        }
        return new int[]{selectTaskIdx, selectOldNode, selectNewNode};
    }

    @Override
    public Object clone() {
        Solution cloned;
        cloned = (Solution) super.clone();

        cloned.extTimeNodes = extTimeNodes.clone();
        cloned.costTasks = costTasks.clone();
        return cloned;
    }
}
