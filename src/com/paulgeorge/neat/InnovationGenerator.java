package com.paulgeorge.neat;

public class InnovationGenerator {

	private static volatile InnovationGenerator instance;
	private int innovation = 0;

	private InnovationGenerator() {
		if (instance != null) {
			throw new RuntimeException("Use getInstance() method to get the singleton instance!");
		}
	}

	public static InnovationGenerator getInstance() {
		if (instance == null) {
			synchronized (InnovationGenerator.class) {
				if (instance == null)
					instance = new InnovationGenerator();
			}
		}
		return instance;
	}

	protected InnovationGenerator readResolve() {
		return instance;
	}

	public int getNext() {
		return ++innovation;
	}
}
