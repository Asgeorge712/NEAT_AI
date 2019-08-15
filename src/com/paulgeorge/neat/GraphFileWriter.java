package com.paulgeorge.neat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Map;

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.NodeShape;

public class GraphFileWriter {

	Genome genome;

	public GraphFileWriter(Genome g) {
		this.genome = g;
	}

	public void write(String fileName) {
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();

		gexf.getMetadata().setLastModified(date.getTime()).setCreator("Paul George")
				.setDescription("A Snake AI Neural Network");
		gexf.setVisualization(true);

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);

		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);

		Attribute attWeight = attrList.createAttribute("0", AttributeType.FLOAT, "weight");
		Attribute attInnovation = attrList.createAttribute("1", AttributeType.STRING, "innovation");

		Map<Integer, ConnectionGene> connections = genome.getConnections();

		for (ConnectionGene connection : connections.values()) {
			// GEFX Stuff
			Node fromNode;
			Node toNode;

			// NEAT Stuff
			NodeGene inNode = genome.getNodes().get(connection.getInNode());
			NodeGene outNode = genome.getNodes().get(connection.getOutNode());
			if (graph.getNode(inNode.getId() + "") == null) {
				fromNode = graph.createNode(inNode.getId() + "");
				fromNode.getShapeEntity().setNodeShape(NodeShape.DISC);
			} else {
				fromNode = graph.getNode(String.valueOf(inNode.getId()));
			}

			if (graph.getNode(outNode.getId() + "") == null) {
				toNode = graph.createNode(String.valueOf(outNode.getId()));
				fromNode.getShapeEntity().setNodeShape(NodeShape.DISC);
			} else {
				toNode = graph.getNode(String.valueOf(outNode.getId()));
			}

			Edge edge = fromNode.connectTo(toNode).setEdgeType(EdgeType.DIRECTED);
			edge.getAttributeValues().addValue(attWeight, String.valueOf(connection.getWeight()));
			edge.getAttributeValues().addValue(attInnovation, String.valueOf(connection.getInnovation()));

		}

		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File(fileName);
		Writer out;
		try {
			out = new FileWriter(f, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			System.out.println(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
