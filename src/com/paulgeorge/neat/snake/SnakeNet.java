package com.paulgeorge.neat.snake;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.paulgeorge.neat.ConnectionGene;
import com.paulgeorge.neat.Evaluator;
import com.paulgeorge.neat.Genome;
import com.paulgeorge.neat.GraphFileUtils;
import com.paulgeorge.neat.InnovationGenerator;
import com.paulgeorge.neat.NeuralNetwork;
import com.paulgeorge.neat.NodeGene;
import com.paulgeorge.neat.NodeGene.TYPE;
import com.paulgeorge.snake.model.GameBoard;

public class SnakeNet {
	private GameBoard board;
	private boolean gameEnded;
	private int numMoves;
	private Genome genome;
	private NeuralNetwork neuralNet;
	private static Logger log = LogManager.getLogger(SnakeNet.class);
	private Random r;

	public static final void main(String args[]) {
		log.debug("Starting SnakeNet main");
		// Create the initial Genome with 12 inputs and 4 outputs

		// Genome genome = createSemiSmartGenome();
		File dir = new File(".");
		log.debug("Current dir is: " + dir.getAbsolutePath());
		Genome genome = getGenomeFromFile(dir.getAbsolutePath() + "\\fittest2.json");

		GenomePrinter printer = new GenomePrinter();
		printer.showGenome(genome, "First Genome");

		log.debug("Just added " + genome.getConnections().size() + " ConnectionGenes!");

		SnakeNet snakeNet = new SnakeNet(genome);

		Evaluator evaluator = new Evaluator(150, genome) {
			@Override
			protected float evaluateGenome(Genome genome) {
				return snakeNet.runGame();
			}
		};

		Genome fittest = null;
		int fittestGeneration = 0;
		for (int x = 0; x < 100; x++) {
			evaluator.evaluate();
			log.debug("************************** ");
			log.debug(" Generation: " + x);
			log.debug("************************** ");
			log.debug("\tHighest Fitness: " + evaluator.getHighestFitness());
			log.debug("\tNumber of Species: " + evaluator.getSpeciesSize());
			log.debug("\tFittest currently: " + (fittest == null ? 0 : fittest.getFitness()));

			if (x == 0 || evaluator.getHighestFitness() > fittest.getFitness()) {
				fittest = evaluator.getFittestGenome();
				fittestGeneration = x;
			}
			log.debug("Fittest Genome score: " + fittest.getFitness() + " is from Gen " + fittestGeneration);
		}

		log.debug("The Fittest genome had a score of: " + fittest.getFitness() + "  Number of Nodes: "
				+ fittest.getNodes().size() + "  Number of Connections: " + fittest.getConnections().size()
				+ "  from Generation: " + fittestGeneration);
		printer.showGenome(fittest, "Fittest Genome");
		// writer.write("fittest1.gexf");
		GraphFileUtils.writeGenomeToFile(fittest, "fittest3.json");

	}

	/*******************************************************************
	 * 
	 * @param g
	 *******************************************************************/
	public SnakeNet(Genome g) {
		this.board = new GameBoard(10, 10);
		this.gameEnded = false;
		// this.r = new Random();
		this.numMoves = 0;
		this.genome = g;
		this.neuralNet = new NeuralNetwork(genome);
		this.r = new Random();
	}

	/************************************************
	 * 
	 * @return
	 ************************************************/
	public float runGame() {
		gameEnded = false;
		numMoves = 0;
		board.newGame();
		Direction d = null;
		while (!gameEnded) {
			d = calculateDirection(genome);
			doAction(d);
			numMoves++;
		}
		return board.score + 0f;
	}

	/******************************************************
	 * 
	 * @param direction
	 ******************************************************/
	public void doAction(Direction direction) {
		board.snake.setDirection(direction.getX(), direction.getY());
		board.snake.move();
		board.detectCollisions();
		if (board.gameOver) {
			gameEnded = true;
		}
	}

	/*- **************************************************************
	 * Inputs are as follows: 
	 * 		Format First Letter: N = North, E = East, S = South, W = West 
	 * 		Second Letter: S = Snake, F = Food, W = Wall 
	 * 0  - N S 
	 * 1  - N F 
	 * 2  - N W 
	 * 3  - E S 
	 * 4  - E F 
	 * 5  - E W 
	 * 6  - W S 
	 * 7  - W F 
	 * 8  - W W 
	 * 9  - S S 
	 * 10 - S F 
	 * 11 - S W
	 * 
	 * @return
	 **********************************************************/
	private float[] getSnakeInput() {
		float[] inputs = { 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f };

		Point snake = board.snake.head.location;

		// Starting from Point snake, we want to look left, right up and down and report
		// the first thing we find going out (snake, food, wall, nothing)

		/*-
		+--------------------+
		|                    |
		|   f                |
		|                    |
		|       b            |   North 3 - Food 
		|   s   b            |   East  4 - Snake   
		|   b   b            |   South 1 - Snake
		|   bbbbb            |   West  4 - Wall
		|                    |
		|                    |
		|                    |
		+--------------------+
		*/

		// System.out.println(board.toString());
		// * * * * * * * * * Look North * * * * * * * * *
		// log.debug("Snake is at: " + board.snake.head.location);
		// log.debug("Looking North.");
		if (board.containsSnake(new Point(snake.x, snake.y - 1))) { // Snake head is just below a body part
			inputs[0] = 1;
		}
		for (int y = snake.y - 1; y >= -1; y--) { // Look for food
			Point newPoint = new Point(snake.x, y);
			if (board.containsSnack(newPoint)) {
				inputs[1] = (board.size - (snake.y - y)) / (float) board.size;
				// log.debug("Found Food North. Setting input to " + inputs[1]);
				break;
			}
		}
		if (snake.y == 0) { // Snake at the wall
			inputs[2] = 1;
		}

		// * * * * * * * * * Look East * * * * * * * * *
		// log.debug("Looking East.");
		if (board.containsSnake(new Point(snake.x + 1, snake.y))) { // Snake head is just left of a body part
			inputs[3] = 1;
		}
		if (snake.x == board.size - 1) { // Snake at the wall
			inputs[5] = 1;
		}
		for (int x = snake.x + 1; x <= board.size; x++) {
			Point newPoint = new Point(x, snake.y);
			if (board.containsSnack(newPoint)) {
				inputs[4] = (board.size - (x - snake.x)) / (float) board.size;
				// log.debug("Found Food East. Setting input to " + inputs[4]);
				break;
			}
		}

		// * * * * * * * * * Look South * * * * * * * * *
		// log.debug("Looking South.");
		if (board.containsSnake(new Point(snake.x, snake.y + 1))) { // Snake head is just above a body part
			inputs[6] = 1;
		}
		if (snake.y == board.size - 1) { // Snake at the wall
			inputs[8] = 1;
		}
		for (int y = snake.y + 1; y <= board.size; y++) {
			Point newPoint = new Point(snake.x, y);
			if (board.containsSnack(newPoint)) {
				inputs[7] = (board.size - (y - snake.y)) / (float) board.size;
				// log.debug("Found Food South. Setting input to " + inputs[7]);
				break;
			}
		}

		// * * * * * * * * * Look West * * * * * * * * *
		// log.debug("Looking West.");
		if (board.containsSnake(new Point(snake.x - 1, snake.y))) { // Snake head is just right of a body part
			inputs[9] = 1;
		}
		if (snake.x == 0) { // Snake at the wall
			inputs[11] = 1;
		}
		for (int x = snake.x - 1; x >= -1; x--) {
			Point newPoint = new Point(x, snake.y);
			if (board.containsSnack(newPoint)) {
				inputs[10] = (board.size - (snake.x - x)) / (float) board.size;
				// log.debug("Found Food West. Setting input to " + inputs[10]);
				break;
			}
		}

		return inputs;
	}

	/***********************************************************
	 * 
	 * @param g
	 * @return
	 ***********************************************************/
	private Direction calculateDirection(Genome g) {
		Direction d = Direction.EAST;
		float[] input_params = getSnakeInput();
		// log.debug("input_params: N S N F N W E S E F E W S S S F S W W S W F W W");
		// log.debug("input_params: " + Arrays.toString(input_params));

		float[] outputs = neuralNet.calculate(input_params);
		// log.debug("outputs: N E S W");
		// log.debug("outputs: " + Arrays.toString(outputs));

		// output index:
		// - 0 = North
		// - 1 = East
		// - 2 = South
		// - 3 = West
		float maxValue = Float.MIN_NORMAL;
		int maxIndex = Integer.MIN_VALUE;
		float EPSILON = 0.0000000001f;

		List<Integer> largestIndexes = new ArrayList<>();
		maxValue = outputs[0];
		largestIndexes.add(0);
		// log.debug("Right now index 0 is max with: " + outputs[0]);

		for (int i = 1; i < outputs.length; i++) {
			if (Math.abs(outputs[i] - maxValue) < EPSILON) {
				// log.debug("Found another index: " + i + " with the same value: " +
				// outputs[i]);
				largestIndexes.add(i);
			} else if (outputs[i] > maxValue) {
				// New maxValue
				// log.debug("Found a new Max Value at index: " + i + " with value: " +
				// outputs[i]);
				maxValue = outputs[i];
				largestIndexes = new ArrayList<>();
				largestIndexes.add(i);
			}
		}

		// Now pick a random index from largestIndexes
		maxIndex = largestIndexes.get(r.nextInt(largestIndexes.size()));
		// log.debug("I choose index: " + maxIndex + " out of a list of " +
		// largestIndexes.size() + " equal indexes.");

		if (maxIndex == 0)
			d = Direction.NORTH;
		else if (maxIndex == 1)
			d = Direction.EAST;
		else if (maxIndex == 2)
			d = Direction.SOUTH;
		else if (maxIndex == 3)
			d = Direction.WEST;
		else {
			System.err.println("ERROR Finding direction!!!!!");
		}
		return d;
	}

	/***************************************************************************************************
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws XMLStreamException
	 ***************************************************************************************************/
	private static Genome getGenomeFromFile(String fileName) {
		return GraphFileUtils.readGenomeFromFile(fileName);
	}

	/****************************************************************************
	 * This Genome is given weights such that it is discouraged from moving into
	 * walls or itself and is encouraged to move towards food
	 * 
	 * @return
	 *****************************************************************************/
	private static Genome createSemiSmartGenome() {
		Genome genome = new Genome();
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 0)); // North Snake
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 1)); // North Food
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 2)); // North Wall
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 3)); // East Snake
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 4)); // East Food
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 5)); // East Wall
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 6)); // South Snake
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 7)); // South Food
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 8)); // South Wall
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 9)); // West Snake
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 10));// West Food
		genome.addNodeGene(new NodeGene(TYPE.INPUT, 11)); // West Wall
		genome.addNodeGene(new NodeGene(TYPE.OUTPUT, 12)); // Go North
		genome.addNodeGene(new NodeGene(TYPE.OUTPUT, 13)); // Go East
		genome.addNodeGene(new NodeGene(TYPE.OUTPUT, 14)); // Go South
		genome.addNodeGene(new NodeGene(TYPE.OUTPUT, 15)); // Go West

		InnovationGenerator i = InnovationGenerator.getInstance();

		// North Stuff
		genome.addConnectionGene(new ConnectionGene(0, 12, -1, true, i.getNext())); // North Snake - don't go North
		genome.addConnectionGene(new ConnectionGene(1, 12, 1, true, i.getNext())); // North Food - go North
		genome.addConnectionGene(new ConnectionGene(2, 12, -1, true, i.getNext())); // East Wall - don't go east
		// Don't care about the rest
		for (int x = 3; x <= 11; x++) {
			genome.addConnectionGene(new ConnectionGene(x, 12, 0, true, i.getNext()));
		}

		// East Stuff
		genome.addConnectionGene(new ConnectionGene(3, 13, -1, true, i.getNext())); // East Snake - don't go East
		genome.addConnectionGene(new ConnectionGene(4, 13, 1, true, i.getNext())); // East Food - go East
		genome.addConnectionGene(new ConnectionGene(5, 13, -1, true, i.getNext())); // East Wall - Don't go East
		// Don't care about the rest
		for (int x = 0; x <= 11; x++) {
			if (x != 3 & x != 4 && x != 5) {
				genome.addConnectionGene(new ConnectionGene(x, 13, 0, true, i.getNext()));
			}
		}

		// South Stuff
		genome.addConnectionGene(new ConnectionGene(6, 14, -1, true, i.getNext())); // South Snake - don't go South
		genome.addConnectionGene(new ConnectionGene(7, 14, 1, true, i.getNext())); // South Food - go South
		genome.addConnectionGene(new ConnectionGene(8, 14, -1, true, i.getNext())); // South Wall - Don't go South
		// Don't care about the rest
		for (int x = 0; x <= 11; x++) {
			if (x != 6 & x != 7 && x != 8) {
				genome.addConnectionGene(new ConnectionGene(x, 14, 0, true, i.getNext()));
			}
		}

		// West Stuff
		genome.addConnectionGene(new ConnectionGene(9, 15, -1, true, i.getNext())); // West Snake - don't go West
		genome.addConnectionGene(new ConnectionGene(10, 15, 1, true, i.getNext())); // West Food - go West
		genome.addConnectionGene(new ConnectionGene(11, 15, -1, true, i.getNext())); // West Wall - Don't go West
		// Don't care about the rest
		for (int x = 0; x <= 11; x++) {
			if (x != 9 & x != 10 && x != 11) {
				genome.addConnectionGene(new ConnectionGene(x, 15, 0, true, i.getNext()));
			}
		}
		return genome;
	}
}
