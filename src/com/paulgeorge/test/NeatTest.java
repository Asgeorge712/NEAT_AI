package com.paulgeorge.test;

import java.util.Random;

import com.paulgeorge.neat.ConnectionGene;
import com.paulgeorge.neat.Genome;
import com.paulgeorge.neat.NodeGene;
import com.paulgeorge.neat.NodeGene.TYPE;

public class NeatTest {

	public static void main(String[] args) {
		testCrossover();
	}

	private static void testAddNodeMutation() {
		Genome parent1 = new Genome();

		for (int i = 0; i < 3; i++) {
			NodeGene node = new NodeGene(TYPE.INPUT, i + 1);
			parent1.addNodeGene(node);
		}
		parent1.addNodeGene(new NodeGene(TYPE.OUTPUT, 4));
		parent1.addNodeGene(new NodeGene(TYPE.HIDDEN, 5));

		parent1.addConnectionGene(new ConnectionGene(1, 4, 2f, true, 1));
		parent1.addConnectionGene(new ConnectionGene(2, 4, 1f, false, 2));
		parent1.addConnectionGene(new ConnectionGene(3, 4, .5f, true, 3));
		parent1.addConnectionGene(new ConnectionGene(2, 5, -1.5f, true, 4));
		parent1.addConnectionGene(new ConnectionGene(5, 4, .1f, true, 5));
		parent1.addConnectionGene(new ConnectionGene(1, 5, -1f, true, 6));

		System.out.println(parent1.toString());

		parent1.addNodeMutation(new Random());
		System.out.println(parent1.toString());

	}

	private static void testCrossover() {
		Genome parent1 = new Genome();

		for (int i = 0; i < 3; i++) {
			NodeGene node = new NodeGene(TYPE.INPUT, i + 1);
			parent1.addNodeGene(node);
		}
		parent1.addNodeGene(new NodeGene(TYPE.OUTPUT, 4));
		parent1.addNodeGene(new NodeGene(TYPE.HIDDEN, 5));

		parent1.addConnectionGene(new ConnectionGene(1, 4, 1.2f, true, 1));
		parent1.addConnectionGene(new ConnectionGene(2, 4, -1.5f, false, 2));
		parent1.addConnectionGene(new ConnectionGene(3, 4, .7f, true, 3));
		parent1.addConnectionGene(new ConnectionGene(2, 5, 1.3f, true, 4));
		parent1.addConnectionGene(new ConnectionGene(5, 4, .2f, true, 5));
		parent1.addConnectionGene(new ConnectionGene(1, 5, -1.2f, true, 8));

		parent1.setFitness(200);

		Genome parent2 = new Genome();
		for (int i = 0; i < 3; i++) {
			NodeGene node = new NodeGene(TYPE.INPUT, i + 1);
			parent2.addNodeGene(node);
		}
		parent2.addNodeGene(new NodeGene(TYPE.OUTPUT, 4));
		parent2.addNodeGene(new NodeGene(TYPE.HIDDEN, 5));
		parent2.addNodeGene(new NodeGene(TYPE.HIDDEN, 6));

		parent2.addConnectionGene(new ConnectionGene(1, 4, 1.5f, true, 1));
		parent2.addConnectionGene(new ConnectionGene(2, 4, -2f, false, 2));
		parent2.addConnectionGene(new ConnectionGene(3, 4, 1.1f, true, 3));
		parent2.addConnectionGene(new ConnectionGene(2, 5, 1.7f, true, 4));
		parent2.addConnectionGene(new ConnectionGene(5, 4, .5f, false, 5));
		parent2.addConnectionGene(new ConnectionGene(5, 6, 1.5f, true, 6));
		parent2.addConnectionGene(new ConnectionGene(6, 4, -1f, true, 7));
		parent2.addConnectionGene(new ConnectionGene(3, 5, 1.8f, true, 9));
		parent2.addConnectionGene(new ConnectionGene(1, 6, .9f, true, 10));
		parent2.setFitness(200);

		System.out.println(parent1.toString());
		System.out.println(parent2.toString());

		Genome child = Genome.crossover(parent1, parent2, new Random());
		System.out.println(child.toString());
		float C1 = 1.0f;
		float C2 = 1.0f;
		float C3 = 0.4f;

		System.out.println("Difference from Parent1 and 2 : " + Genome.calculateDistance(parent1, parent2, C1, C2, C3));
		System.out
				.println("Difference from Parent1 and child : " + Genome.calculateDistance(parent1, child, C1, C2, C3));
		System.out
				.println("Difference from Parent2 and child : " + Genome.calculateDistance(parent2, child, C1, C2, C3));

	}
}
