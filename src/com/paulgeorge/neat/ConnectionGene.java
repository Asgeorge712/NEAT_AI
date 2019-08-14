package com.paulgeorge.neat;

public class ConnectionGene {
	private int inNode;
	private int outNode;
	private float weight;
	private boolean expressed;
	private int innovation;

	public ConnectionGene(int inNode, int outNode, float weight, boolean expressed, int innovation) {
		this.inNode = inNode;
		this.outNode = outNode;
		this.weight = weight;
		this.expressed = expressed;
		this.innovation = innovation;
	}

	public ConnectionGene(ConnectionGene toBeCopied) {
		this.inNode = toBeCopied.getInNode();
		this.outNode = toBeCopied.getOutNode();
		this.weight = toBeCopied.getWeight();
		this.expressed = toBeCopied.isExpressed();
		this.innovation = toBeCopied.getInnovation();
	}

	public int getInNode() {
		return inNode;
	}

	public int getOutNode() {
		return outNode;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public void disable() {
		expressed = false;
	}

	public boolean isExpressed() {
		return expressed;
	}

	public int getInnovation() {
		return innovation;
	}

	public ConnectionGene copy() {
		return new ConnectionGene(inNode, outNode, weight, expressed, innovation);
	}
}
