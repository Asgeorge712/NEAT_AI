package com.paulgeorge.neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Evaluator {

	private Random random;
	private Logger log = LogManager.getLogger(Evaluator.class);
	private FitnessComparator fitComp = new FitnessComparator();

	/* Constants for tuning */
	private float C1 = 1.0f;
	private float C2 = 1.0f;
	private float C3 = 0.8f;
	private float DT = 3.0f;
	private float MUTATION_RATE = 0.5f;
	private float ADD_CONNECTION_RATE = 0.1f;
	private float ADD_NODE_RATE = 0.1f;

	private int populationSize;

	private List<Genome> genomes;
	private List<Genome> nextGenGenomes;

	private List<Species> species;

	private Map<Genome, Species> mappedSpecies;
	private Map<Genome, Float> fitnessMap;
	private float highestFitness;
	private Genome fittestGenome;

	public Evaluator(int populationSize, Genome startingGenome) {
		random = new Random();
		log.debug("Creating Evaluator. Population size will be: " + populationSize);
		this.populationSize = populationSize;

		genomes = new ArrayList<Genome>(populationSize);

		// Copy the starting genome up to population size.
		for (int i = 0; i < populationSize; i++) {
			genomes.add(new Genome(startingGenome));
		}

		nextGenGenomes = new ArrayList<Genome>(populationSize);
		mappedSpecies = new HashMap<Genome, Species>();
		fitnessMap = new HashMap<Genome, Float>();
		species = new ArrayList<Species>();
	}

	/**********************************************************
	 * You must create an evaluator based on the thing you are modeling That
	 * evaluator must have a specific evaluator for the fitness of each Genome
	 * working on the model.
	 * 
	 * For example, if your Neural Network is trying to play snake, this method
	 * should play the game once and return the score as the fitness.
	 * 
	 * @param genome
	 * @return
	 **********************************************************/
	protected abstract float evaluateGenome(Genome genome);

	/***********************************************************
	 * Runs one generation
	 * 
	 ***********************************************************/
	public void evaluate() {
		// Reset everything for next generation
		for (Species s : species) {
			s.reset(random);
		}
		fitnessMap.clear();
		mappedSpecies.clear();
		nextGenGenomes.clear();
		highestFitness = Float.MIN_VALUE;
		fittestGenome = null;

		// Place genomes into species
		for (Genome g : genomes) {
			boolean foundSpecies = false;
			// log.debug("Looking for similar Species");
			float distance = 0.0f;
			int speciesCount = 0;
			for (Species s : species) {
				speciesCount++;
				distance = Math.abs(Genome.calculateDistance(g, s.mascot, C1, C2, C3));
				// log.debug("For Species # " + speciesCount + " Distance is: " + distance);
				if (distance < DT) {
					// compatibility distance is less than DT, so genome belongs to this species
					s.members.add(g);
					mappedSpecies.put(g, s);
					foundSpecies = true;
					break;
				}
			}
			if (!foundSpecies) {
				// If there is no appropriate species for this genome, make a new Species!
				log.debug("  NEW SPECIES !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				Species newSpecies = new Species(g);
				species.add(newSpecies);
				mappedSpecies.put(g, newSpecies);
			}
		}

		// for (int i = 0; i < species.size(); i++) {
		// log.debug("Species #" + i + " has " + species.get(i).members.size() + "
		// members.");
		// }

		// Remove unused species
		Iterator<Species> iter = species.iterator();
		while (iter.hasNext()) {
			Species s = iter.next();
			if (s.members.isEmpty()) {
				log.debug("Removing an empty Species.");
				iter.remove();
			}
		}

		// Evaluate genomes and assign fitness
		// int genomeCounter = 0;
		for (Genome g : genomes) {
			Species s = mappedSpecies.get(g); // Get species of the genome
			// genomeCounter++;
			// log.debug("Evaluating Genome: " + genomeCounter);

			// *****************************************************
			// This plays the game and it will return the score.
			// Score is only how many foods it ate.
			// *****************************************************
			float fitness = evaluateGenome(g);
			g.setFitness(fitness);

			// Adjusted fitness is a shared fitness
			float adjustedFitness = fitness / mappedSpecies.get(g).members.size();

			// log.debug("Genome's fitness: " + fitness + " ---- SHARED fitness: " +
			// adjustedFitness);

			s.addAdjustedFitness(adjustedFitness);
			g.setAdjustedFitness(adjustedFitness);

			fitnessMap.put(g, adjustedFitness);
			if (fitness > highestFitness) {
				highestFitness = fitness;
				fittestGenome = new Genome(g);
			}
		}

		// put best genomes from each species into next generation
		float totalAdjustedFitness = 0;
		for (Species s : species) {
			totalAdjustedFitness += s.totalAdjustedFitness;
			Collections.sort(s.members, fitComp);
			Collections.reverse(s.members);
			Genome fittestInSpecies = s.members.get(0);
			nextGenGenomes.add(fittestInSpecies);
		}

		// log.debug("For the " + species.size() + " Species, there is a total adjusted
		// fitness of: " + totalAdjustedFitness);
		Map<Species, Integer> speciesBreedingsLeft = new HashMap<>();
		int speciesCounter = 0;
		int speciesBreedingCounter = 0;

		if (species.size() == 1) {
			speciesBreedingsLeft.put(species.get(0), (populationSize - nextGenGenomes.size()));
			speciesBreedingCounter = populationSize - nextGenGenomes.size();
		} else {
			for (Species s : species) {
				speciesCounter++;
				// log.debug("Species # " + speciesCounter + " has " + s.members.size() + "
				// Members.");
				// if (s.members.size() > 1) {
				float speciesPercent = s.totalAdjustedFitness / totalAdjustedFitness;
				int breedingsLeft = Math.round(speciesPercent * (populationSize - nextGenGenomes.size()));
				speciesBreedingCounter += breedingsLeft;
				// log.debug("Species #" + speciesCounter + " has adjusted fitness of " +
				// s.totalAdjustedFitness + "["
				// + (100 * speciesPercent) + "%] and is alloted " + breedingsLeft
				// + " of the remaining population. ");
				speciesBreedingsLeft.put(s, breedingsLeft);
				// }
			}
		}
		// log.debug("Population size is " + populationSize);
		// log.debug("Champions count: " + nextGenGenomes.size());
		// log.debug("Remaining breeding slots: " + (populationSize -
		// nextGenGenomes.size()));
		// log.debug("Caculated breedings alloted to all species: " +
		// speciesBreedingCounter);

		// Now we know how many children to breed from each species to reach the
		// population limit
		for (Species s : speciesBreedingsLeft.keySet()) {
			int allotedBreeding = speciesBreedingsLeft.get(s);
			// log.debug("There are " + s.members.size() + " Genomes in this Species. It is
			// alloted " + allotedBreeding
			// + " children.");

			// Only get the top 20% of the Genomes in this species for breeding
			int numberToBreed = Math.round(s.members.size() * 0.2f);

			// If a Species only has one member - should it be allowed to breed the number
			// of children its adjustedFitness would allow?
			// For now, yes.
			if (numberToBreed == 0)
				numberToBreed = 1;

			log.debug("This species will breed from the top " + numberToBreed + " Genomes.");

			int randomIndex1;
			int randomIndex2;

			Genome p1;
			Genome p2;
			boolean printedMutation = false;

			for (int i = 0; i < allotedBreeding; i++) {
				// Add .2 to raise .4 to .6 so it rounds up to 1
				int indexUpperRange = Math.round(numberToBreed + 0.2f);
				log.trace("indexUpperRange: " + indexUpperRange);

				randomIndex1 = random.nextInt(indexUpperRange);
				randomIndex2 = random.nextInt(indexUpperRange);
				p1 = s.members.get(randomIndex1);
				p2 = s.members.get(randomIndex2);

				// log.debug("Breeding genome: " + randomIndex1 + " (" + p1.getFitness() + ")
				// with genome: " + randomIndex2
				// + " (" + p2.getFitness() + ")");

				Genome child = Genome.crossover(p1, p2, random);

				// boolean addedMutation = false;
				if (random.nextFloat() < MUTATION_RATE) {
					// log.debug("Adding child mutation ...");
					child.mutate(random);
				}
				if (random.nextFloat() < ADD_CONNECTION_RATE) {
					// log.debug("Adding connection mutation...");
					child.addConnectionMutation(random);
				}
				if (random.nextFloat() < ADD_NODE_RATE) {
					// log.debug("Adding node mutation...");
					child.addNodeMutation(random);
					// addedMutation = true;
				}
				// if (addedMutation && !printedMutation) {
				// printedMutation = true;
				// log.debug("Printing a mutated child");
				// GenomePrinter printer = new GenomePrinter();
				// printer.showGenome(child, "Child Genome");
				// }
				nextGenGenomes.add(child);
			}
		}

		genomes = nextGenGenomes;
		log.debug("The next generation of all species contains " + genomes.size() + " Genomes.");
		nextGenGenomes = new ArrayList<Genome>();
	}

	public int getSpeciesSize() {
		return species.size();
	}

	public float getHighestFitness() {
		return highestFitness;
	}

	public Genome getFittestGenome() {
		return fittestGenome;
	}

}
