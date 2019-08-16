package com.paulgeorge.neat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.paulgeorge.neat.NodeGene.TYPE;

public class GraphFileUtils {

	private static Logger log = LogManager.getLogger(GraphFileUtils.class);

	public GraphFileUtils() {
	}

	/**************************************************************************
	 * 
	 * @param genome
	 * @param fileName
	 **************************************************************************/
	public static void writeGenomeToFile(Genome genome, String fileName) {
		JsonObject json = convertToJson(genome);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String prettyJson = gson.toJson(json);
		// log.debug("Pretty json is: " + prettyJson);
		try {
			File file = new File(fileName);
			FileWriter out = new FileWriter(file);
			out.write(prettyJson);
			out.flush();
			out.close();
			log.debug("Written to file: " + file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/***********************************************************************
	 * 
	 * @param fileName
	 * @return
	 ***********************************************************************/
	public static Genome readGenomeFromFile(String fileName) {
		String input = readFile(fileName);
		Gson gson = new Gson();
		JsonObject genomeJson = gson.fromJson(input, JsonObject.class);
		Genome genome = new Genome();

		float fitness = genomeJson.get("fitness").getAsFloat();
		genome.setFitness(fitness);

		JsonArray nodesArray = genomeJson.get("nodes").getAsJsonArray();
		for (JsonElement nodeElement : nodesArray) {
			JsonObject nodeObject = nodeElement.getAsJsonObject();
			int id = nodeObject.get("id").getAsInt();
			String type = nodeObject.get("type").getAsString();
			NodeGene node = new NodeGene(TYPE.valueOf(type), id);
			genome.addNodeGene(node);
		}

		JsonArray connectionsArray = genomeJson.get("connections").getAsJsonArray();
		for (JsonElement connectionElement : connectionsArray) {
			JsonObject connectionObject = connectionElement.getAsJsonObject();
			int innovation = connectionObject.get("innovation").getAsInt();
			float weight = connectionObject.get("weight").getAsFloat();
			boolean expressed = connectionObject.get("isExpressed").getAsBoolean();
			int nodeIn = connectionObject.get("in").getAsInt();
			int nodeOut = connectionObject.get("out").getAsInt();
			ConnectionGene connection = new ConnectionGene(nodeIn, nodeOut, weight, expressed, innovation);
			genome.addConnectionGene(connection);
		}
		return genome;
	}

	/************************************************************************
	 * 
	 * @param genome
	 * @return
	 ************************************************************************/
	private static JsonObject convertToJson(Genome genome) {
		JsonObject genomeJson = new JsonObject();
		float fitness = genome.getFitness();
		genomeJson.add("fitness", new JsonPrimitive(fitness));
		JsonArray nodesArray = new JsonArray();
		for (NodeGene node : genome.getNodes().values()) {
			JsonObject nodeJson = new JsonObject();
			nodeJson.add("id", new JsonPrimitive(node.getId()));
			nodeJson.add("type", new JsonPrimitive(node.getType().name()));
			nodesArray.add(nodeJson);
		}
		genomeJson.add("nodes", nodesArray);

		JsonArray connectionsArray = new JsonArray();
		for (ConnectionGene connection : genome.getConnections().values()) {
			JsonObject connectionJson = new JsonObject();
			connectionJson.add("innovation", new JsonPrimitive(connection.getInnovation()));
			connectionJson.add("in", new JsonPrimitive(connection.getInNode()));
			connectionJson.add("out", new JsonPrimitive(connection.getOutNode()));
			connectionJson.add("weight", new JsonPrimitive(connection.getWeight()));
			connectionJson.add("isExpressed", new JsonPrimitive(connection.isExpressed()));
			connectionsArray.add(connectionJson);
		}
		genomeJson.add("connections", connectionsArray);

		return genomeJson;
	}

	/**********************************************************************
	 * 
	 * @param filePath
	 * @return
	 **********************************************************************/
	private static String readFile(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}

}
