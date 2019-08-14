package com.paulgeorge.neat;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Species {

	public Genome mascot;
	public List<Genome> members;
	public float totalAdjustedFitness = 0f;

	public Species(Genome mascot) {
		this.mascot = mascot;
		this.members = new LinkedList<Genome>();
		this.members.add(mascot);
	}

	public void addAdjustedFitness(float adjustedFitness) {
		this.totalAdjustedFitness += adjustedFitness;
	}

	/*******************************************************************************
	 * 
	 * Selects new random mascot + clear members + set totaladjustedfitness to 0f
	 * 
	 *******************************************************************************/
	public void reset(Random r) {
		int newMascotIndex = r.nextInt(members.size());
		this.mascot = members.get(newMascotIndex);
		members.clear();
		totalAdjustedFitness = 0f;
	}
}
