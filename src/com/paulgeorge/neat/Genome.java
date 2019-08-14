package com.paulgeorge.neat;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.paulgeorge.neat.NodeGene.TYPE;

public class Genome {

	private Map<Integer, ConnectionGene> connections;
	private Map<Integer, NodeGene> nodes;
	private float fitness;
	private float adjustedFitness;

	private final float PROBABILITY_PERTURBING = 0.9f;

	private static Logger log = LogManager.getLogger(Genome.class);

	/*********************************************************
	 * 
	 *********************************************************/
	public Genome() {
		connections = new TreeMap<>();
		nodes = new TreeMap<>();
		fitness = 0;
	}

	/**********************************************************
	 * 
	 * @param toBeCopied
	 * 
	 **********************************************************/
	public Genome(Genome toBeCopied) {
		nodes = new TreeMap<Integer, NodeGene>();
		connections = new TreeMap<Integer, ConnectionGene>();
		fitness = toBeCopied.getFitness();

		for (Integer index : toBeCopied.getNodes().keySet()) {
			nodes.put(index, new NodeGene(toBeCopied.getNodes().get(index)));
		}

		for (Integer index : toBeCopied.getConnections().keySet()) {
			connections.put(index, new ConnectionGene(toBeCopied.getConnections().get(index)));
		}
	}

	/************************************
	 * 
	 * @return
	 ************************************/
	public float getFitness() {
		return fitness;
	}

	/*****************************************
	 * 
	 * @param fitness
	 *****************************************/
	public void setFitness(float fitness) {
		this.fitness = fitness;
	}

	public float getAdjustedFitness() {
		return adjustedFitness;
	}

	public void setAdjustedFitness(float adjustedFitness) {
		this.adjustedFitness = adjustedFitness;
	}

	public void addNodeGene(NodeGene gene) {
		nodes.put(gene.getId(), gene);
	}

	public void addConnectionGene(ConnectionGene conn) {
		connections.put(conn.getInnovation(), conn);
	}

	public Map<Integer, ConnectionGene> getConnections() {
		return connections;
	}

	public Map<Integer, NodeGene> getNodes() {
		return nodes;
	}

	/**********************************************************
	 * 
	 * @param r
	 **********************************************************/
	public void mutate(Random r) {
		for (ConnectionGene conn : connections.values()) {
			// This will allow up to 100% mutation of the weight (+/- 2 at most)
			float perturbation = r.nextFloat() * 4f - 2f;

			// Maybe we should keep the mutation down to 50%? (+/- 1 at most)
			// float perturbation = r.nextFloat() * 2f - 1f;
			// float perturbation = r.nextFloat() * 3f - 1f;

			if (r.nextFloat() < PROBABILITY_PERTURBING) {
				conn.setWeight(perturbation + conn.getWeight());
				// if (Math.abs(perturbation + conn.getWeight()) > 2) {
				// conn.setWeight(perturbation - conn.getWeight());
				// } else {
				// conn.setWeight(perturbation + conn.getWeight());
				// }
			} else {
				conn.setWeight(r.nextFloat() * 4f - 2f);
			}
		}
	}

	/**********************************************************
	 * 
	 * @param g1
	 * @param g2
	 * @return
	 **********************************************************/
	public static float calculateDistance(Genome g1, Genome g2, float c1, float c2, float c3) {
		float distance = 0f;

		int g1MaxInnovation = (Integer) ((TreeMap<?, ?>) g1.getConnections()).lastEntry().getKey();
		int g2MaxInnovation = (Integer) ((TreeMap<?, ?>) g2.getConnections()).lastEntry().getKey();

		int E = 0;
		int D = 0;

		float weightSums = 0f;
		int numMatchingGenes = 0;

		if (g1MaxInnovation >= g2MaxInnovation) { // g1 has excess genes or they are no excess genes
			for (ConnectionGene conn : g1.getConnections().values()) {
				if (g2.getConnections().containsKey(conn.getInnovation())) { // Matching Gene - sum weights
					weightSums += Math
							.abs(conn.getWeight() - g2.getConnections().get(conn.getInnovation()).getWeight());
					numMatchingGenes++;
				} else if (conn.getInnovation() > g2MaxInnovation) { // Excess Gene
					E++;
				} else { // Disjoint Gene
					D++;
				}
			}
		} else {
			for (ConnectionGene conn : g2.getConnections().values()) {
				if (g1.getConnections().containsKey(conn.getInnovation())) { // Matching Gene - sum weights
					weightSums += Math
							.abs(conn.getWeight() - g1.getConnections().get(conn.getInnovation()).getWeight());
					numMatchingGenes++;
				} else if (conn.getInnovation() > g1MaxInnovation) { // Excess Gene
					E++;
				} else { // Disjoint Gene
					D++;
				}
			}

		}

		float W = weightSums / numMatchingGenes;

		// int N = Integer.max(g1.getConnections().size(), g2.getConnections().size());
		int N = Integer.max(g1.getNodes().size(), g2.getNodes().size());
		// if (N < 20) {
		// N = 1;
		// }

		// log.debug("There are " + numMatchingGenes + " matching Genes.");
		// log.debug("Max number of Nodes is " + N);
		distance = ((c1 * E) / N) + ((c2 * D) / N) + (c3 * W);

		// log.debug("Distance: " + distance + " -- Ave weight diff: " + W + " - Excess
		// genes: " + E + ", Disjoint genes: " + D + ", N: " + N);
		return distance;
	}

	/*************************************************************************
	 * 
	 * 
	 * @param parent1
	 * @param parent2
	 * @param r
	 * @return
	 *************************************************************************/
	public static Genome crossover(Genome parent1, Genome parent2, Random r) {
		Genome child = new Genome();

		for (int innovation : parent1.getConnections().keySet()) {
			NodeGene currIn = null;
			NodeGene currOut = null;
			ConnectionGene childConnection = null;

			if (parent2.getConnections().containsKey(innovation)) {
				// Found a matching Gene
				if (r.nextBoolean()) { // Use Parent1
					// log.debug("Using parent 1 for a Matching Gene");
					childConnection = parent1.getConnections().get(innovation).copy();
					currIn = parent1.getNodes().get(childConnection.getInNode()).copy();
					currOut = parent1.getNodes().get(childConnection.getOutNode()).copy();
				} else { // Use Parent2
					// log.debug("Using parent 1 for a Matching Gene");
					childConnection = parent2.getConnections().get(innovation).copy();
					currIn = parent2.getNodes().get(childConnection.getInNode()).copy();
					currOut = parent2.getNodes().get(childConnection.getOutNode()).copy();
				}
			} else {
				// Found a disjoint or excess Gene in parent1
				if (parent1.getFitness() >= parent2.getFitness()) {
					// log.debug("Using parent 1 for a Disjoint or Excess Gene");
					childConnection = parent1.getConnections().get(innovation).copy();
					currIn = parent1.getNodes().get(childConnection.getInNode()).copy();
					currOut = parent1.getNodes().get(childConnection.getOutNode()).copy();
				}
			}

			if (childConnection != null) {
				child.addConnectionGene(childConnection);
				if (!child.getNodes().containsKey(currIn.getId())) {
					child.addNodeGene(currIn);
				}
				if (!child.getNodes().containsKey(currOut.getId())) {
					child.addNodeGene(currOut);
				}
			}
		}

		for (int innovation : parent2.getConnections().keySet()) {
			NodeGene currIn = null;
			NodeGene currOut = null;
			ConnectionGene childConnection = null;
			if (!child.getConnections().containsKey(innovation)) {
				// found a disjoint or excess Gene in Parent2

				if (parent2.getFitness() >= parent1.getFitness()) {
					// log.debug("Using parent 2 for a Disjoint or Excess Gene");
					childConnection = parent2.getConnections().get(innovation).copy();
					currIn = parent2.getNodes().get(childConnection.getInNode()).copy();
					currOut = parent2.getNodes().get(childConnection.getOutNode()).copy();

					child.addConnectionGene(childConnection);
					if (!child.getNodes().containsKey(currIn.getId())) {
						child.addNodeGene(currIn);
					}
					if (!child.getNodes().containsKey(currOut.getId())) {
						child.addNodeGene(currOut);
					}
				}
			}
		}

		// log.debug("Child has " + child.getConnections().size() + " Connections and "
		// + child.getNodes().size()
		// + " Nodes.");
		return child;
	}

	/********************************************************
	 * 
	 * @param r
	 ********************************************************/
	public void addConnectionMutation(Random r) {
		NodeGene node1 = null;
		NodeGene node2 = null;

		int numTries = 0;
		boolean validPair = false;
		while (!validPair) {
			numTries++;
			if (numTries > 100) {
				System.out.println("Tried 100 times to find a connection that doesn't exist.  Giving up");
				return;
			}

			node1 = nodes.get(r.nextInt(nodes.size()));
			node2 = nodes.get(r.nextInt(nodes.size()));
			boolean badPair = false;
			if (connectionExists(node1.getId(), node2.getId())) {
				// Bad
				badPair = true;
			}

			if (node1.getType() == TYPE.INPUT && node2.getType() == TYPE.INPUT) {
				// bad
				badPair = true;
			}

			if (node1.getType() == TYPE.OUTPUT && node2.getType() == TYPE.OUTPUT) {
				// bad
				badPair = true;
			}

			if (node2.getId() == node1.getId()) {
				// bad
				badPair = true;
			}

			if (!badPair) {
				validPair = true;
			}
		}

		boolean reversed = false;
		if (node1.getType() == TYPE.HIDDEN && node2.getType() == TYPE.INPUT) {
			reversed = true;
		} else if (node1.getType() == TYPE.OUTPUT
				&& (node2.getType() == TYPE.HIDDEN || node2.getType() == TYPE.INPUT)) {
			reversed = true;
		}

		if (reversed) {
			NodeGene tempNode = node1;
			node1 = node2;
			node2 = tempNode;
		}

		float weight = r.nextFloat() * 2f - 1f;
		ConnectionGene newConnection = new ConnectionGene(node1.getId(), node2.getId(), weight, true,
				InnovationGenerator.getInstance().getNext());

		connections.put(newConnection.getInnovation(), newConnection);

	}

	/********************************************************
	 * 
	 * @param r
	 ********************************************************/
	public void addNodeMutation(Random r) {
		// Get the keys from the Connection HashMap (these are the connectionIds)
		Set<Integer> keys = connections.keySet();

		// Second, convert the set to an array using toArray:
		Integer[] array = keys.toArray(new Integer[keys.size()]);

		// Finally, get a random entry from the array.
		int randomId = array[r.nextInt(array.length)];

		// log.debug("******************** Adding a node mutation. Connections size is:
		// " + connections.size()
		// + ", using random connection ID: " + randomId);

		ConnectionGene existingConnection = connections.get(randomId);
		existingConnection.disable();

		int inNode = existingConnection.getInNode();
		int outNode = existingConnection.getOutNode();

		NodeGene newNode = new NodeGene(TYPE.HIDDEN, nodes.size());

		ConnectionGene newConnection1 = new ConnectionGene(inNode, newNode.getId(), 1, true,
				InnovationGenerator.getInstance().getNext());
		ConnectionGene newConnection2 = new ConnectionGene(newNode.getId(), outNode, existingConnection.getWeight(),
				true, InnovationGenerator.getInstance().getNext());

		nodes.put(newNode.getId(), newNode);
		connections.put(newConnection1.getInnovation(), newConnection1);
		connections.put(newConnection2.getInnovation(), newConnection2);

	}

	/*********************************************************
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 *********************************************************/
	private boolean connectionExists(int node1, int node2) {
		for (Integer innovation : connections.keySet()) {
			ConnectionGene conn = connections.get(innovation);
			if ((conn.getInNode() == node1 && conn.getOutNode() == node2)
					|| (conn.getInNode() == node2 && conn.getOutNode() == node1)) {
				return true;
			}
		}
		return false;
	}

	/*******************************************
	 * 
	 *******************************************/
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int innovationCounter = 1;
		for (ConnectionGene conn : connections.values()) {
			while (conn.getInnovation() > innovationCounter) {
				sb.append("                      ");
				innovationCounter++;
			}
			sb.append(conn.getInnovation()).append(" : ").append(conn.getInNode()).append(" --> ")
					.append(conn.getOutNode());
			if (!conn.isExpressed()) {
				sb.append(":DISABLED  ");
			} else {
				sb.append("           ");
			}
			innovationCounter++;
		}

		return sb.toString();
	}
}
