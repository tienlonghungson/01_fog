package org.fog.scheduling.nsgaii;

import org.fog.scheduling.gaEntities.Individual;
import org.fog.scheduling.gaEntities.Population;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NSGAIIPopulation extends Population {
    private final int POPULATION_SIZE;
    private List<List<Individual>> fronts;
    private final Individual bestGlobal;
    /**
     * Initializes population of individuals
     *
     * @param populationSize The number of individuals in the population
     * @param chromosomeLength The size of each individual's chromosome
     */
    protected NSGAIIPopulation(int populationSize, int chromosomeLength, int maxValue) {
        // Initialize the population as an array of individuals
        ArrayList<Individual> population = new ArrayList<>(populationSize<<1);
        this.POPULATION_SIZE = populationSize;

        // Create each individual in turn
        for (int individualCount = 0; individualCount < (populationSize<<1); individualCount++) {
            // Create an individual, initializing its chromosome to the give length
            NSGAIIIndividual individual = new NSGAIIIndividual(chromosomeLength, maxValue);
            // Add individual to population
            population.add(individual);
        }
        setPopulation(population);
        bestGlobal = new NSGAIIIndividual(chromosomeLength,maxValue);
    }

    protected void fastNonDominatedSorting(){
        fronts = new ArrayList<>();
        fronts.add(new ArrayList<>());
        int sortedNum = 0; // number of sorted individual

//        System.out.println(POPULATION_SIZE<<1);
        for (int i=0;i<(POPULATION_SIZE<<1);++i){
            NSGAIIIndividual individual = (NSGAIIIndividual) getPopulation().get(i);
            individual.getDominatedList().clear(); // set to empty
            individual.updateNumDominated(-individual.getNumDominated()); // set to 0
            for (int j=0;j<(POPULATION_SIZE<<1);++j){
                if (j!=i) {
//                    System.out.print(j);
                    NSGAIIIndividual other = (NSGAIIIndividual) getPopulation().get(j);
                    if (individual.isDominating(other)) {
                        individual.getDominatedList().add(other);
                    } else if (other.isDominating(individual)){
                        individual.updateNumDominated(1);
                    }
                }
            }
//            System.out.println();
            if (individual.getNumDominated()==0){
//                System.out.println("Added to Pareto");
                fronts.get(0).add(individual);
                individual.setRank(0);
                sortedNum++;
            }
        }

        int i=-1;
        while(!fronts.get(++i).isEmpty()&&(sortedNum<POPULATION_SIZE)){
            ArrayList<Individual> q  = new ArrayList<>();
            for (Individual individual: fronts.get(i)) {
                for (NSGAIIIndividual other : ((NSGAIIIndividual) individual).getDominatedList()){
                    other.updateNumDominated(-1);
                    if (other.getNumDominated()==0){
                        q.add(other);
                        other.setRank(i+1);
                        sortedNum++;
                    }
                }
            }
            fronts.add(q);
        }
//        System.out.println("Stopped Searching. Sorted Num= "+sortedNum);
//        System.out.println("There are "+fronts.size()+" fronts");
//        for (List<Individual> front : fronts){
//            System.out.println("Front have "+front.size());
//        }
    }

    /**
     * assign crowding-distance to each individual of a front
     *
     * @param front list of individuals need to be assigned crowding-distance
     */
    private void crowdingDistanceAssignment(List<Individual> front){
        int lastIdx = front.size()-1;
        double fMax,fMin;
        Individual individual, preInd, postInd;
        for (Individual ind : front){
            ind.setFitness(0);
        }

        assert (!front.isEmpty());
//        System.out.println("front:"); System.out.println(front);
        // distance in time
        front.sort(((o1, o2) -> {
            if (o1.getTime()<=o2.getTime()){
                return 0;
            } else {
                return 1;
            }
        }));
        front.get(0).setFitness(Double.POSITIVE_INFINITY); front.get(lastIdx).setFitness(Double.POSITIVE_INFINITY);
        fMin = front.get(0).getTime(); fMax = front.get(lastIdx).getTime();

        for (int i=1;i<lastIdx;++i){
            individual=front.get(i); preInd = front.get(i-1); postInd = front.get(i+1);
            individual.setFitness(individual.getFitness()+ (postInd.getTime()- preInd.getTime())/(fMax-fMin));
        }

        // distance in cost
        front.sort((o1,o2)->{
            if (o1.getCost()<=o2.getCost()){
                return 0;
            } else {
                return 1;
            }
        });
        front.get(0).setFitness(Double.POSITIVE_INFINITY); front.get(lastIdx).setFitness(Double.POSITIVE_INFINITY);
        fMin = front.get(0).getCost(); fMax = front.get(lastIdx).getCost();

        for (int i = 1; i < lastIdx; i++) {
            individual=front.get(i); preInd = front.get(i-1); postInd = front.get(i+1);
            individual.setFitness(individual.getFitness()+ (postInd.getCost()-preInd.getCost())/(fMax-fMin));
        }
    }

    /**
     * assign crowding-distance to each individual of a front determined by its index
     *
     * @param idx index of the front
     */
    private void crowdingDistanceAssignment(int idx){
        crowdingDistanceAssignment(fronts.get(idx));
    }

    /**
     * select top N {@code POPULATION_SIZE} individual from this Population currently in size of 2*N
     */
    protected void select(){
        fastNonDominatedSorting();
        saveBest();
//        System.out.println("Sorted");
        assert (fronts!=null);
        List<Individual> selectedPop = new ArrayList<>(POPULATION_SIZE<<1);
        int currSize=0;
        for (List<Individual> front: this.fronts){
            currSize+=front.size();
//            System.out.println("currSize: "+currSize);
//            assert (!front.isEmpty());
//            System.out.println("front: "); System.out.println(front);
            if (currSize<POPULATION_SIZE){
                crowdingDistanceAssignment(front);
                selectedPop.addAll(front);
            } else {
                int supplementNum = POPULATION_SIZE-currSize+front.size();
                crowdingDistanceAssignment(front);
                selectedPop.addAll(front.subList(0,supplementNum));
                break;
            }
        }
        setPopulation(selectedPop);
    }

    @Override
    public void sortPopulation() {
//        assert (fronts!=null);
        if (fronts!=null) {
            fronts.get(0).sort(((o1, o2) -> {
                if (o1.getFitness() > o2.getFitness()) {
                    return 1;
                } else {
                    return 0;
                }
            }));
        }
    }

    public void saveBest(){
        if (bestGlobal.getFitness()<getFittest(0).getFitness()){
            Individual tmp = getFittest(0);
            final int LEN = bestGlobal.getChromosomeLength();
            for (int i=0;i<LEN;++i){
                bestGlobal.setGene(i, tmp.getGene(i));
            }
            bestGlobal.setTime(tmp.getTime());
            bestGlobal.setCost(tmp.getCost());
            bestGlobal.setFitness(tmp.getFitness());
        }

    }

    public void addBestToPopulation(){
        final int LEN = bestGlobal.getChromosomeLength();
        Individual lastInd = this.getPopulation().get(new Random().nextInt(this.getPopulation().size()));
        for (int i=0;i<LEN;++i){
            lastInd.setGene(i, bestGlobal.getGene(i));
        }
        lastInd.setTime(bestGlobal.getTime());
        lastInd.setCost(bestGlobal.getCost());
        lastInd.setFitness(bestGlobal.getFitness());
    }

    public Individual getBestGlobal(){
        return bestGlobal;
    }

    @Override
    public Individual getFittest(int offset) {
        assert (fronts!=null);
        return fronts.get(0).get(offset);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Individual individual: getPopulation()){
            stringBuilder.append(individual.toString());
        }
        return stringBuilder.toString();
    }
}
