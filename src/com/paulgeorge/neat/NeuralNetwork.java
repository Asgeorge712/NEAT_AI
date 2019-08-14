package com.paulgeorge.neat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/************************************************************
 *
 * 
 ************************************************************/
public class NeuralNetwork {

	private Map<Integer, Neuron> neurons; // All neurons in genome, mapped by ID

	private List<Integer> input; // IDs of input neurons
	private List<Integer> output; // IDs of output neurons

	private List<Neuron> unprocessed;

	private Logger log = LogManager.getLogger(NeuralNetwork.class);

	/*********************************************************
	 * 
	 * @param genome
	 *********************************************************/
	public NeuralNetwork(Genome genome) {
		input = new ArrayList<Integer>();
		output = new ArrayList<Integer>();
		neurons = new HashMap<Integer, Neuron>();
		unprocessed = new LinkedList<Neuron>();

		for (Integer nodeID : genome.getNodes().keySet()) {
			NodeGene node = genome.getNodes().get(nodeID);
			Neuron neuron = new Neuron();

			if (node.getType() == NodeGene.TYPE.INPUT) {
				neuron.addInputConnection();
				input.add(nodeID);
			} else if (node.getType() == NodeGene.TYPE.OUTPUT) {
				output.add(nodeID);
			}
			neurons.put(nodeID, neuron);
		}
		log.debug("NeuralNetwork built " + neurons.size() + " neurons.");

		for (Integer connID : genome.getConnections().keySet()) {
			ConnectionGene conn = genome.getConnections().get(connID);
			if (!conn.isExpressed()) {
				continue;
			}

			// the node that leads into the connection, is the inputter to the connection
			Neuron inputter = neurons.get(conn.getInNode());

			// log.debug("Adding output from nodeID=" + conn.getInNode() + "\t to\t nodeID="
			// + conn.getOutNode()
			// + "\tweight=" + conn.getWeight());

			inputter.addOutputConnection(conn.getOutNode(), conn.getWeight());

			Neuron outputReceiver = neurons.get(conn.getOutNode()); // the node that receives from the connection, is
																	// the output receiver of the connection
			// log.debug("Adding input to nodeID="+conn.getOutNode());
			outputReceiver.addInputConnection();
		}
	}

	/******************************************************************
	 * 
	 * @param input_parameter
	 * @return
	 ******************************************************************/
	public float[] calculate(float[] input_parameter) {
		// System.out.println("New round of calculation...");
		if (input_parameter.length != input.size()) {
			throw new IllegalArgumentException("Number of inputs must match number of input neurons in genome");
		}

		for (Integer key : neurons.keySet()) {
			Neuron neuron = neurons.get(key);
			neuron.reset();
		}

		unprocessed.clear();
		unprocessed.addAll(neurons.values());

		// ready the inputs
		for (int i = 0; i < input_parameter.length; i++) { // loop through each input
			Neuron inputNeuron = neurons.get(input.get(i));

			// log.debug("Feeding input value (" + input_parameter[i] + ") into input neuron
			// (id=" + input.get(i) + ")");

			// input neurons only have one input, so we know they're ready for calculation
			inputNeuron.feedInput(input_parameter[i]);
			inputNeuron.calculate();

			// log.debug("Calculating node=" + input.get(i) + " result=" +
			// inputNeuron.getOutput());

			// loop through receivers of this input
			for (int k = 0; k < inputNeuron.getOutputIDs().length; k++) {
				Neuron receiver = neurons.get(inputNeuron.getOutputIDs()[k]);
				// log.debug(" -- Feeding input[" + inputNeuron.getOutput() + "] times weight["
				// + inputNeuron.getOutputWeights()[k] + "] = "
				// + (inputNeuron.getOutput() * inputNeuron.getOutputWeights()[k]) + " into
				// input neuron (id="
				// + inputNeuron.getOutputIDs()[k] + ")");

				// Add the input directly to the next neuron, using the correct weight for the
				// connection
				receiver.feedInput(inputNeuron.getOutput() * inputNeuron.getOutputWeights()[k]);
			}
			unprocessed.remove(inputNeuron);
		}

		int loops = 0;
		while (unprocessed.size() > 0) {
			loops++;
			if (loops > 1000) {
				// System.out.println("Can't solve network... Giving up to return null");
				return null;
			}

			Iterator<Neuron> it = unprocessed.iterator();
			while (it.hasNext()) {
				Neuron n = it.next();
				if (n.isReady()) { // if neuron has all inputs, calculate the neuron
					n.calculate();
					// log.debug("Output value = " + n.getOutput());
					for (int i = 0; i < n.getOutputIDs().length; i++) {
						int receiverID = n.getOutputIDs()[i];
						float receiverValue = n.getOutput() * n.getOutputWeights()[i];
						// log.debug("Feeding input (" + receiverValue + ") into inputneuron (id=" +
						// receiverID + ")");
						neurons.get(receiverID).feedInput(receiverValue);
					}
					it.remove();
				}
			}
		}
		// System.out.println("Solved the network after "+loops+" loops");

		// copy output from output neurons, and copy it into array
		float[] outputs = new float[output.size()];
		for (int i = 0; i < output.size(); i++) {
			outputs[i] = neurons.get(output.get(i)).getOutput();
		}

		return outputs;
	}

}
