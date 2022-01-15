package org.fog.scheduling.gaEntities;

import org.fog.utils.Service;

import java.util.Arrays;

public class Individual implements Cloneable{
	
	protected int[] chromosome;
	private double cost;
	private double time;
	private double fitness = -1;
	private int maxValue;
	
	public Individual(int chromosomeLength, int maxValue) {
		this(chromosomeLength);
		this.maxValue = maxValue;
		for (int gene = 0; gene < chromosomeLength; gene++) {
			this.setGene(gene, Service.rand(0, maxValue));
		}
	}
	
	public Individual(int chromosomeLength, int maxValue, int value) {
		this(chromosomeLength, maxValue);
		for (int gene = 0; gene < chromosomeLength; gene++) {
			this.setGene(gene, value);
		}
	}
	
	public Individual(int chromosomeLength) {
		this.chromosome = new int[chromosomeLength];
	}
	
	public void printGene() {
		for (int gene = 0; gene < this.getChromosomeLength(); gene++) {
			System.out.print(this.getGene(gene) + " ");
		}
	}
	
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getFitness() {
		return fitness;
	}
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * Gets individual's chromosome
	 * 
	 * @return The individual's chromosome
	 */
	public int[] getChromosome() {
		return this.chromosome;
	}

	/**
	 * Gets individual's chromosome length
	 * 
	 * @return The individual's chromosome length
	 */
	public int getChromosomeLength() {
		return this.chromosome.length;
	}

	/**
	 * Set gene at offset
	 * 
	 * @param gene new gene value
	 * @param offset distance from 1st gene
	 */
	public void setGene(int offset, int gene) {
		this.chromosome[offset] = gene;
	}

	/**
	 * Get gene at offset
	 * 
	 * @param offset distance from 1st gene
	 * @return gene
	 */
	public int getGene(int offset) {
		return this.chromosome[offset];
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public String toString() {
		return "Individual{" +
				"chromosome=" + Arrays.toString(chromosome) +
				", cost=" + cost +
				", time=" + time +
				", fitness=" + fitness +
				'}';
	}

	@Override
	public Object clone(){
		try {
			Individual cloned = (Individual) super.clone();
			cloned.chromosome = this.chromosome.clone();
			return cloned;
		}catch (CloneNotSupportedException e){
			throw new InternalError(e);
		}
	}
}
