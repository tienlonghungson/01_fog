package org.fog.scheduling.gaEntities;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.AbstractAlgorithm;
import org.fog.scheduling.SchedulingAlgorithm;

/**
 * The GeneticAlgorithm class is our main abstraction for managing the
 * operations of the genetic algorithm. This class is meant to be
 * problem-specific, meaning that (for instance) the "calcFitness" method may
 * need to change from problem to problem.
 * <p>
 * This class concerns itself mostly with population-level operations, but also
 * problem-specific operations such as calculating fitness, testing for
 * termination criteria, and managing mutation and crossover operations (which
 * generally need to be problem-specific as well).
 * <p>
 * Generally, GeneticAlgorithm might be better suited as an abstract class or an
 * interface, rather than a concrete class as below. A GeneticAlgorithm
 * interface would require implementation of methods such as
 * "isTerminationConditionMet", "calcFitness", "mutatePopulation", etc, and a
 * concrete class would be defined to solve a particular problem domain. For
 * instance, the concrete class "TravelingSalesmanGeneticAlgorithm" would
 * implement the "GeneticAlgorithm" interface. This is not the approach we've
 * chosen, however, so that we can keep each chapter's examples as simple and
 * concrete as possible.
 *
 * @author bkanber
 */
public class GeneticAlgorithm extends AbstractAlgorithm {
    protected final int POPULATION_SIZE;
    /**
     * Mutation rate is the fractional probability than an individual gene will
     * mutate randomly in a given generation. The range is 0.0-1.0, but is
     * generally small (on the order of 0.1 or less).
     */
    protected final double MUTATION_RATE;

    /**
     * Crossover rate is the fractional probability that two individuals will
     * "mate" with each other, sharing genetic information, and creating
     * offspring with traits of each of the parents. Like mutation rate the
     * rance is 0.0-1.0 but small.
     */
    protected final double CROSSOVER_RATE;

    /**
     * Elitism is the concept that the strongest members of the population
     * should be preserved from generation to generation. If an individual is
     * one of the elite, it will not be mutated or crossover.
     */
    protected final int ELITISM_COUNT;


    public GeneticAlgorithm(int populationSize, double mutationRate, double crossoverRate, int elitismCount) {
        this.POPULATION_SIZE = populationSize;
        this.MUTATION_RATE = mutationRate;
        this.CROSSOVER_RATE = crossoverRate;
        this.ELITISM_COUNT = elitismCount;
    }

    /**
     * Initialize population
     *
     * @param chromosomeLength The length of the individuals chromosome
     * @return population The initial population generated
     */
    public Population initPopulation(int chromosomeLength, int maxValue) {
        // Initialize population
        Population population = new Population(this.POPULATION_SIZE, chromosomeLength, maxValue);
        return population;
    }

    /**
     * Evaluate the whole population
     * <p>
     * Essentially, loop over the individuals in the population, calculate the
     * fitness for each, and then calculate the entire population's fitness. The
     * population's fitness may or may not be important, but what is important
     * here is making sure that each individual gets evaluated.
     *
     * @param population the population to evaluate
     */
    public Population evalPopulation(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {

        double populationFitness = 0;

        // Loop over population evaluating individuals and summing population fitness
        for (Individual individual : population.getPopulation()) {
            populationFitness += calcFitness(individual, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT);
        }

        //sort population with increasing fitness value
        population.sortPopulation();

        population.setPopulationFitness(populationFitness);
        return population;
    }


    /**
     * Check if population has met termination condition
     * <p>
     * For this simple problem, we know what a perfect solution looks like, so
     * we can simply stop evolving once we've reached a fitness of one.
     *
     * @param population
     * @return boolean True if termination condition met, otherwise, false
     */
    public boolean isTerminationConditionMet(Population population) {
        for (Individual individual : population.getPopulation()) {
            if (individual.getFitness() == 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Select parent for crossover
     *
     * @param population The population to select parent from
     * @return The individual selected as a parent
     */
    public Individual selectIndividual(Population population) {
        // Get individuals
        List<Individual> individuals = population.getPopulation();

        // Spin roulette wheel
        double populationFitness = population.getPopulationFitness();
        double rouletteWheelPosition = Math.random() * populationFitness;

        // Find parent
        double spinWheel = 0;
        for (Individual individual : individuals) {
            spinWheel += individual.getFitness();
            if (spinWheel >= rouletteWheelPosition) {
                return individual;
            }
        }
        return individuals.get(population.size() - 1);
    }

    /**
     * Apply crossover to population
     * <p>
     * Crossover, more colloquially considered "mating", takes the population
     * and blends individuals to create new offspring. It is hoped that when two
     * individuals crossover that their offspring will have the strongest
     * qualities of each of the parents. Of course, it's possible that an
     * offspring will end up with the weakest qualities of each parent.
     * <p>
     * This method considers both the GeneticAlgorithm instance's crossoverRate
     * and the elitismCount.
     * <p>
     * The type of crossover we perform depends on the problem domain. We don't
     * want to create invalid solutions with crossover, so this method will need
     * to be changed for different types of problems.
     * <p>
     * This particular crossover method selects random genes from each parent.
     *
     * @param population The population to apply crossover to
     * @return The new population
     */
    public Population crossoverPopulation(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
        // Create new population
        List<Individual> newPopulation = new ArrayList<>();

        // Loop over current population by fitness
        for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
            Individual parent1 = population.getFittest(populationIndex);

            // Apply crossover to this individual?
            if (this.CROSSOVER_RATE > Math.random()) {
                // Initialize offspring
                Individual offspring = new Individual(parent1.getChromosomeLength());

                // Find second parent
                Individual parent2 = selectIndividual(population);
                offspring = crossover2Point(parent1, parent2);

                if (parent1.getFitness() <= calcFitness(offspring, fogDevices, cloudletList,SchedulingAlgorithm.TIME_WEIGHT)
                        && !doesPopupationIncludeIndividual(population, offspring)) {
                    newPopulation.add(offspring);
                } else {
                    newPopulation.add(parent1);
                }
            } else {
                newPopulation.add(population.getFittest(populationIndex));
            }
        }
        population.getPopulation().clear();
        population.setPopulation(newPopulation);

        return population;
    }

    // crossover 2 points between 2 parents and create an offspring
    public Individual crossover2Point(Individual parent1, Individual parent2) {
        Individual offspring = new Individual(parent1.getChromosomeLength());
        int crossoverPoint1 = Service.rand(0, parent1.getChromosomeLength() - 1);
        int crossoverPoint2 = Service.rand(crossoverPoint1 + 1, crossoverPoint1 + parent1.getChromosomeLength());

        for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
            if (crossoverPoint2 >= parent1.getChromosomeLength()) {
                if (geneIndex >= crossoverPoint1 || geneIndex < (crossoverPoint2 - parent1.getChromosomeLength())) {
                    offspring.setGene(geneIndex, parent2.getGene(geneIndex));
                } else {
                    offspring.setGene(geneIndex, parent1.getGene(geneIndex));
                }
            } else {
                if (geneIndex >= crossoverPoint1 && geneIndex < crossoverPoint2) {
                    offspring.setGene(geneIndex, parent2.getGene(geneIndex));
                } else {
                    offspring.setGene(geneIndex, parent1.getGene(geneIndex));
                }
            }
        }
        return offspring;
    }

    // crossover 2 points between 2 parents and create an offspring
    public List<Individual> crossover2Point2(Individual parent1, Individual parent2) {
        List<Individual> listOffsprings = new ArrayList<Individual>();
        Individual offspring1 = new Individual(parent1.getChromosomeLength());
        Individual offspring2 = new Individual(parent1.getChromosomeLength());
        int crossoverPoint1 = Service.rand(0, parent1.getChromosomeLength() - 1);
        int crossoverPoint2 = Service.rand(crossoverPoint1 + 1, crossoverPoint1 + parent1.getChromosomeLength() - 1);

        for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
            if (crossoverPoint2 >= parent1.getChromosomeLength()) {
                if (geneIndex >= crossoverPoint1 || geneIndex < (crossoverPoint2 - parent1.getChromosomeLength())) {
                    offspring1.setGene(geneIndex, parent2.getGene(geneIndex));
                    offspring2.setGene(geneIndex, parent1.getGene(geneIndex));
                } else {
                    offspring1.setGene(geneIndex, parent1.getGene(geneIndex));
                    offspring2.setGene(geneIndex, parent2.getGene(geneIndex));
                }
            } else {
                if (geneIndex >= crossoverPoint1 && geneIndex < crossoverPoint2) {
                    offspring1.setGene(geneIndex, parent2.getGene(geneIndex));
                    offspring2.setGene(geneIndex, parent1.getGene(geneIndex));
                } else {
                    offspring1.setGene(geneIndex, parent1.getGene(geneIndex));
                    offspring2.setGene(geneIndex, parent2.getGene(geneIndex));
                }
            }
        }
        listOffsprings.add(offspring1);
        listOffsprings.add(offspring2);
        return listOffsprings;
    }

    // crossover 1 points between 2 parents and create an offspring
    public Individual crossover1Point(Individual parent1, Individual parent2) {
        Individual offspring = new Individual(parent1.getChromosomeLength());
        int crossoverPoint = Service.rand(0, parent1.getChromosomeLength());
        for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
            // Use half of parent1's genes and half of parent2's genes
            if (crossoverPoint > geneIndex) {
                offspring.setGene(geneIndex, parent1.getGene(geneIndex));
            } else {
                offspring.setGene(geneIndex, parent2.getGene(geneIndex));
            }
        }
        return offspring;
    }


    /**
     * Apply mutation to population
     * <p>
     * Mutation affects individuals rather than the population. We look at each
     * individual in the population, and if they're lucky enough (or unlucky, as
     * it were), apply some randomness to their chromosome. Like crossover, the
     * type of mutation applied depends on the specific problem we're solving.
     * In this case, we simply randomly flip 0s to 1s and vice versa.
     * <p>
     * This method will consider the GeneticAlgorithm instance's mutationRate
     * and elitismCount
     *
     * @param population The population to apply mutation to
     * @return The mutated population
     */
    public Population mutatePopulation(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {

        // Loop over current population by fitness
        for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
            // if the current individual is selected to mutation phase
            if (this.MUTATION_RATE > Math.random() && populationIndex >= this.ELITISM_COUNT) {
                Individual individual = population.getFittest(populationIndex);
                individual.setGene(Service.rand(0, individual.getChromosomeLength() - 1), Service.rand(0, individual.getMaxValue()));

            }
        }
        // Return mutated population
        return population;
    }

    public boolean doesPopupationIncludeIndividual(Population population, Individual individual) {
        boolean include = false;
        for (int index = 0; index < population.size(); index++) {
            boolean similar = true;
            if (individual.getFitness() == population.getIndividual(index).getFitness()) {
                for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                    if (individual.getGene(geneIndex) != population.getIndividual(index).getGene(geneIndex)) {
                        similar = false;
                    }
                }
                if (similar) {
                    include = true;
                    break;
                }
            }
            if (include) break;
        }
        return include;
    }

    public void selectPopulation(Population population) {
        population.sortPopulation();

        System.out.println("Before Selection: ");
        population.printPopulation();

        while (population.size() > SchedulingAlgorithm.NUMBER_INDIVIDUAL) {
            population.getPopulation().remove(SchedulingAlgorithm.NUMBER_INDIVIDUAL);
        }
        System.out.println("After Selection: ");
        population.printPopulation();

    }

    public boolean isSameIndividual(Individual individual1, Individual individual2) {
        boolean same = true;
        for (int geneIndex = 0; geneIndex < individual1.getChromosomeLength(); geneIndex++) {
            if (individual1.getGene(geneIndex) != individual2.getGene(geneIndex)) {
                same = false;
                break;
            }
        }
        return same;
    }

    public static void main(String[] args) {

    }

    public Population crossoverPopulation2(Population population, List<FogDevice> fogDevices,
                                           List<? extends Cloudlet> cloudletList) {
        Population newPopulation = new Population();

        // Copy some individuals to population of next generation
        int numberOfParentPairs = (int) (population.size() * this.CROSSOVER_RATE / 2);
        int numberOfCopyIndividuals = population.size() - 2 * numberOfParentPairs;

        for (int index = 0; index < numberOfCopyIndividuals; index++) {
            if (index < this.ELITISM_COUNT) {
                newPopulation.getPopulation().add(population.getFittest(index));
            } else {
                Individual individual = this.selectIndividual(population);
                newPopulation.getPopulation().add(individual);
            }
        }

        // Loop over current population by fitness
        for (int loopIndex = 0; loopIndex < numberOfParentPairs; loopIndex++) {
            // Initialize offspring
            Individual parent1 = this.selectIndividual(population);
            Individual parent2 = new Individual(parent1.getChromosomeLength());
            do {
                // Find second parent
                parent2 = this.selectIndividual(population);
            } while (isSameIndividual(parent1, parent2));

            List<Individual> listOffsprings = crossover2Point2(parent1, parent2);

            newPopulation.getPopulation().add(listOffsprings.get(0));
            newPopulation.getPopulation().add(listOffsprings.get(1));
        }
        return newPopulation;
    }

    public Population mutatePopulation2(Population newPopulation, List<FogDevice> fogDevices,
                                        List<? extends Cloudlet> cloudletList) {
        // Loop over current population by fitness
        for (int populationIndex = 0; populationIndex < newPopulation.size(); populationIndex++) {
            // if the current individual is selected to mutation phase
            if (this.MUTATION_RATE > Math.random() && populationIndex >= this.ELITISM_COUNT) {
                Individual individual = newPopulation.getFittest(populationIndex);
                individual.setGene(Service.rand(0, individual.getChromosomeLength() - 1),
                        Service.rand(0, individual.getMaxValue()));
            }
        }
        return newPopulation;
    }

    public Population selectPopulation2(Population population, Population newPopulation, List<FogDevice> fogDevices,
                                        List<? extends Cloudlet> cloudletList) {

        for (Individual individual : newPopulation.getPopulation()) {
            population.getPopulation().add(individual);
        }
        newPopulation.getPopulation().clear();
        population = this.evalPopulation(population, fogDevices, cloudletList);


        System.out.println("Before Selection: ");
        population.printPopulation();

        while (population.size() > SchedulingAlgorithm.NUMBER_INDIVIDUAL) {
            population.getPopulation().remove(SchedulingAlgorithm.NUMBER_INDIVIDUAL);
        }
        return population;
    }

}
