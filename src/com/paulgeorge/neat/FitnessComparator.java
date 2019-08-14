package com.paulgeorge.neat;

import java.util.Comparator;

public class FitnessComparator implements Comparator<Genome> {
	@Override
	public int compare(Genome one, Genome two) {
		if (one.getFitness() > two.getFitness()) {
			return 1;
		} else if (one.getFitness() < two.getFitness()) {
			return -1;
		}
		return 0;
	}
}
