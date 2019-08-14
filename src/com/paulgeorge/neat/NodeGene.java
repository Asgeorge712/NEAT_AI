package com.paulgeorge.neat;

public class NodeGene {
	public enum TYPE {
		INPUT, OUTPUT, HIDDEN
	};

	private TYPE type;
	private int id;

	public NodeGene(TYPE type, int id) {
		super();
		this.type = type;
		this.id = id;
	}

	public NodeGene(NodeGene toBeCopied) {
		this.type = toBeCopied.getType();
		this.id = toBeCopied.getId();
	}

	public TYPE getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public NodeGene copy() {
		return new NodeGene(this.type, this.id);
	}
}
