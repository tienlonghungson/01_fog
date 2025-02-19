package org.fog.scheduling.nsgaii;

import org.fog.scheduling.gaEntities.GeneticAlgorithm;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.utils.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NSGAIIAlgorithms extends GeneticAlgorithm {
    /**
     * tournament selection parameter
     */
    private int kWay = 2;
    private final Random rd = new Random();

    public NSGAIIAlgorithms(int populationSize, double mutationRate, double crossoverRate, int elitismCount) {
        super(populationSize, mutationRate, crossoverRate, elitismCount);
        assert (populationSize % 2 == 0); // populationSize %2==0 is for convenient
    }

    public NSGAIIAlgorithms(int populationSize, double mutationRate, double crossoverRate, int elitismCount, int kWay) {
        this(populationSize, mutationRate, crossoverRate, elitismCount);
        this.kWay = kWay;
    }

    @Override
    public NSGAIIPopulation initPopulation(int chromosomeLength, int maxValue) {
        return new NSGAIIPopulation(this.POPULATION_SIZE, chromosomeLength, maxValue);
    }

    public void selectPopulation(NSGAIIPopulation nsgaiiPopulation) {
        nsgaiiPopulation.select();
    }

    public void crossOverPopulation(NSGAIIPopulation nsgaiiPopulation) {
        int iter = POPULATION_SIZE >> 1;
        List<Individual> pop = nsgaiiPopulation.getPopulation();
        float mutateDecision;
        for (int i = 0; i < iter; ++i) {
            NSGAIIIndividual parent1 = tournamentSelection(selectRandomKTour(pop));
            NSGAIIIndividual parent2 = tournamentSelection(selectRandomKTour(pop));
            NSGAIIIndividual children1;
            NSGAIIIndividual children2;

            final float CLONE_RATE = 0.5f;
            final float R1 = (float) (MUTATION_RATE / 3), R2 = 2 * R1, R3 = (float) MUTATION_RATE;

//            twoPointCrossover(parent1,parent2,children1,children2);
            children1 = new NSGAIIIndividual(parent1.getChromosomeLength());
            children2 = new NSGAIIIndividual(parent1.getChromosomeLength());
//            onePointCrossover(parent1, parent2, children1, children2);
            binaryCrossOver(parent1, parent2, children1, children2);
            children1.setMaxValue(parent1.getMaxValue());
            children2.setMaxValue(parent2.getMaxValue());


            mutateDecision = rd.nextFloat();
            if (mutateDecision < R1) {
                children1.setGene(rd.nextInt(children1.getChromosomeLength()), rd.nextInt(children1.getMaxValue()));
                children2.reversedMutation();
            } else if (mutateDecision < R2) {
                children2.setGene(rd.nextInt(children2.getChromosomeLength()), rd.nextInt(children2.getMaxValue()));
                children1.swapHalfMutation();
            } else if (mutateDecision < R3) {
                children1.reversedMutation();
                children2.swapHalfMutation();
            }
            pop.add(children1);
            pop.add(children2);
        }
    }

    private NSGAIIIndividual[] selectRandomKTour(List<Individual> nsgaiiIndividualList) {
        NSGAIIIndividual[] nsgaiiIndividuals = new NSGAIIIndividual[kWay];
        ArrayList<Integer> idxes = new ArrayList<>(POPULATION_SIZE);
        for (int i = 0; i < POPULATION_SIZE; i++) {
            idxes.add(i);
        }
        Collections.shuffle(idxes);
        for (int i = 0; i < kWay; i++) {
            nsgaiiIndividuals[i] = (NSGAIIIndividual) nsgaiiIndividualList.get(idxes.get(i));
        }
        return nsgaiiIndividuals;
    }

    private NSGAIIIndividual tournamentSelection(NSGAIIIndividual[] kTour) {
        NSGAIIIndividual bestInd = kTour[0];
        for (int i = 1; i < kTour.length; ++i) {
            if (kTour[i].isCrowding(bestInd)) {
                bestInd = kTour[i];
            }
        }
        return bestInd;
    }

    private void twoPointCrossover(Individual parent1, Individual parent2, Individual children1, Individual children2) {
        int chromosomeLength = parent1.getChromosomeLength();
        int firstPoint = rd.nextInt((chromosomeLength - 2)) + 1;
        int secondPoint = rd.nextInt((chromosomeLength - firstPoint - 1)) + firstPoint + 1;

        for (int i = 0; i < firstPoint; i++) {
            children1.setGene(i, parent1.getGene(i));
            children2.setGene(i, parent2.getGene(i));
        }
        for (int i = firstPoint; i < secondPoint; i++) {
            children1.setGene(i, parent2.getGene(i));
            children2.setGene(i, parent1.getGene(i));
        }
        for (int i = secondPoint; i < chromosomeLength; i++) {
            children1.setGene(i, parent1.getGene(i));
            children2.setGene(i, parent2.getGene(i));
        }
    }

    private void onePointCrossover(Individual parent1, Individual parent2, Individual children1, Individual children2) {
        int chromosomeLength = parent1.getChromosomeLength();
        int point = rd.nextInt((chromosomeLength - 1)) + 1;

        for (int i = 0; i < point; i++) {
            children1.setGene(i, parent1.getGene(i));
            children2.setGene(i, parent2.getGene(i));
        }
        for (int i = point; i < chromosomeLength; i++) {
            children1.setGene(i, parent2.getGene(i));
            children2.setGene(i, parent1.getGene(i));
        }
    }

    private void binaryCrossOver(Individual parent1, Individual parent2, Individual children1, Individual children2) {
        int chromosomeLength = parent1.getChromosomeLength();

        double binaryParameter;
        int gene1, gene2;
        for (int i = 0; i < chromosomeLength; i++) {
            binaryParameter = Service.binaryParameter(2);
            gene1 = (int) Math.max(0,
                    Math.min(parent1.getMaxValue(),
                            0.5 * ((1 - binaryParameter) * parent1.getGene(i) + (1 + binaryParameter) * parent2.getGene(i)
                            )
                    )
            );
            gene2 = (int) Math.max(0,
                    Math.min(parent1.getMaxValue(),
                            0.5 * ((1 + binaryParameter) * parent1.getGene(i) + (1 - binaryParameter) * parent2.getGene(i))
                    )
            );
            children1.setGene(i, gene1);
            children2.setGene(i, gene2);
        }
    }

}
