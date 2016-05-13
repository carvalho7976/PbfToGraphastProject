package br.ufc.quixada.tcc.pbfReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.graphast.config.Configuration;
import org.graphast.geometry.Point;
import org.graphast.model.Edge;
import org.graphast.model.EdgeImpl;
import org.graphast.model.GraphBounds;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;


import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.RelationOSM;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.PbfReader;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.Sink;
import gnu.trove.list.TLongList;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

public class PbfReaderExample implements Sink, Closeable{
	private static long countNos = 0;
	private static long countWays = 0;
	private static long countRel = 0;
	
	private static ObjectBigArrayBigList<Node> todosNos = new ObjectBigArrayBigList<Node>();
	private static ObjectBigArrayBigList<WayOSM> todosWay = new ObjectBigArrayBigList<WayOSM>();
	

		   
	public static void main(String[] args) throws Exception {
		
		Thread pbfReaderThread;

		InputStream input = PbfReaderExample.class.getResourceAsStream("/luxembourg-latest.osm.pbf");

		PbfReader reader = new PbfReader(input, new PbfReaderExample(), 8);
		pbfReaderThread = new Thread(reader, "PBF Reader");
		pbfReaderThread.start();
		
	
    }  

    
	public void close() throws IOException {
		System.out.println("fechou garoto");
		
	}
	
	public void complete() {
		System.out.println("numero de nós " + countNos);
		System.out.println("numero de ways " + countWays);
		System.out.println("numero de relações " + countRel);	
		System.out.println("array de nós " + todosNos.size64());
		
		String graphastTmpDir = Configuration.USER_HOME + "/graphast/tmp/osmimporter";
		
		graphastTmpDir = Configuration.USER_HOME + "/graphast/tmp/osmimporter";
		GraphBounds graph = new GraphImpl(graphastTmpDir);
		
		graph = addNodesToGraph(graph);
		System.out.println("adicionou nos");
		
		graph = criarEdges(graph);
		
		System.out.println("grafo edges" + graph.getNumberOfEdges());
		System.out.println("grafo nodes" + graph.getNumberOfNodes());
	}

	private GraphBounds addNodesToGraph(GraphBounds graph) {
		for(int i = 0; i < todosNos.size64(); i++){
			 graph.addNode(todosNos.get(i));
		}
		return graph;
		
	}


	private GraphBounds criarEdges(GraphBounds graph) {
		
		for(int i = 0; todosWay.size64() > i; i++){
			WayOSM way = todosWay.get(i);
			
			System.out.println(way.toString());
			
			Edge edge = createEdgeFromWay(way, i);
			
			System.out.println("edge " + i);
			
			if(edge != null)
				graph.addEdge(edge);
		}
		
		return graph;
	}
	private Edge createEdgeFromWay(WayOSM way, long id){
		
		TLongList nodes = way.getNodes();
		if(nodes.size() > 2){
			List<Point> geometry = new ArrayList<Point>();
			
			if(nodes.size() > 3){
				for(int i= 0; i < nodes.size() - 1 ; i++){
					Node node = getNodeByID(nodes.get(i));
					if(node != null){
						Point p = new Point(node.getLatitude(), node.getLongitude());
						geometry.add(p);
					}
				}
				return  new EdgeImpl(id, nodes.get(0),nodes.get(nodes.size()-1), 1, "teste", geometry);
			}			
			
		}
		System.out.println("edge nulo");
		
		return null;
	}
	private Node getNodeByID(long nodeId){
		
		for(int i = 0; i < todosNos.size64(); i++){
			
			if(todosNos.get(i).getId() == nodeId){
				//System.out.println(todosNos.get(i).toString());
				return todosNos.get(i);
			}
				
		}
		return null;
	}


	public void process(GenericOsmElement item) {
		
		if(item != null){
			
			switch (item.getType()) {
				case  GenericOsmElement.NODE:
					NodeOSM node = (NodeOSM) item;
					NodeImpl n = new NodeImpl();
					n.setId(node.getId());
					n.setLatitude(node.getLat());
					n.setLongitude(node.getLon());
					
					todosNos.add(n);
					
					countNos++;
					//if(node.toString().length() > 2)
						//System.out.println("Nó: " + node.toString());					
					break;
				case GenericOsmElement.WAY:
					WayOSM way = (WayOSM) item;
					todosWay.add(way);
					countWays++;
					TLongList nos = way.getNodes();
					//for (int i = 0; i < nos.size(); i++) {
					//}
					
					//TODO
					break;
				case GenericOsmElement.RELATION:
					RelationOSM rel = (RelationOSM) item;
					countRel++;
					//System.out.println("relation: " + rel.toString());
					break;
			default:
				break;
			}
		}
		
	}

}
