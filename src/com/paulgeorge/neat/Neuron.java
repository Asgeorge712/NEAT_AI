package com.paulgeorge.neat;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Neuron {

	private float output;
	private Float[] inputs;

	private int[] outputIDs;
	private float[] outputWeights;

	private Logger log = LogManager.getLogger(Neuron.class);

	public Neuron() {
		inputs = new Float[0];
		outputIDs = new int[0];
		outputWeights = new float[0];
	}

	public void setOutput(float output) {
		this.output = output;
	}

	public int[] getOutputIDs() {
		return outputIDs;
	}

	public void setOutputIDs(int[] outputIDs) {
		this.outputIDs = outputIDs;
	}

	public float[] getOutputWeights() {
		return outputWeights;
	}

	public void setOutputWeights(float[] outputWeights) {
		this.outputWeights = outputWeights;
	}

	/**
	 * Adds a connection from this neuron to another neuron
	 * 
	 * @param outputID ID of the target neuron
	 * @param weight   Weight on this connection
	 */
	public void addOutputConnection(int outputID, float weight) {
		// log.debug("Adding output id: " + outputID + " With weight: " + weight);
		int[] nOutputIDs = new int[outputIDs.length + 1]; // make a new array, slightly larger, to make room
		for (int i = 0; i < outputIDs.length; i++) { // copy over old array
			nOutputIDs[i] = outputIDs[i];
		}
		nOutputIDs[outputIDs.length] = outputID; // add new value at the end
		outputIDs = nOutputIDs;

		float[] nOutputWeights = new float[outputWeights.length + 1]; // make a new array, slightly larger, to make room
		for (int i = 0; i < outputWeights.length; i++) {
			nOutputWeights[i] = outputWeights[i];
		}
		nOutputWeights[outputWeights.length] = weight;
		outputWeights = nOutputWeights;
	}

	/**
	 * Adds a connection to this neuron from another neuron
	 */
	public void addInputConnection() {
		// System.out.println("Adding input connection, current length is
		// "+inputs.length);
		Float[] nInputs = new Float[inputs.length + 1];
		for (int i = 0; i < nInputs.length; i++) {
			nInputs[i] = null;
		}
		this.inputs = nInputs;
		// System.out.println("Finished adding input connection, current length is
		// "+inputs.length);
	}

	/**
	 * Takes all the inputs, and calculates them into an output This can only happen
	 * if the neuron {@link #isReady() isReady}
	 */
	public float calculate() {
		float sum = 0f;
		for (Float f : inputs) {
			sum += f;
		}
		output = sigmoidActivationFunction(sum);
		return output;
	}

	/**
	 * If a neuron is ready, it has all the needed inputs to do calculation
	 * 
	 * @return true if ready
	 */
	public boolean isReady() {
		for (Float f : inputs) {
			if (f == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds an input to the neuron in the first slot available
	 */
	public void feedInput(float input) {
		// System.out.println("Feeding input\tInput slots total: "+inputs.length);
		boolean foundSlot = false;
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i] == null) {
				inputs[i] = input;
				foundSlot = true;
				break;
			}
		}
		if (!foundSlot) {
			throw new RuntimeException("No input slot ready for input. Input array: " + Arrays.toString(inputs));
		}
	}

	/**
	 * @return Output of this neuron
	 */
	public float getOutput() {
		return output;
	}

	/**
	 * Resets the inputs on this neuron, as well as the calculation
	 */
	public void reset() {
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = null;
		}
		output = 0f;
	}

	/* Takes any float, and returns a value between 0 and 1. 0f returns 0.5f */
	private float sigmoidActivationFunction(float in) {
		float answer = (float) (1f / (1f + Math.exp(-4.9d * in)));
		// log.debug("Feeding " + in + " into sigmoidActivation Function returns a value
		// of " + answer);
		return answer;
	}
}