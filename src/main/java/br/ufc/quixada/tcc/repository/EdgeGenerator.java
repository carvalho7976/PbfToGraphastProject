package br.ufc.quixada.tcc.repository;

import java.util.ArrayList;
import java.util.List;

import org.graphast.geometry.Point;
import org.graphast.model.EdgeImpl;
import org.graphast.model.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufc.quixada.tcc.Reader.PbfReaderExample;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import gnu.trove.list.TLongList;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public class EdgeGenerator {
	
	protected static final int EMPTY = -1;
	// pillar node is >= 3
	protected static final int PILLAR_NODE = 1;
	// tower node is <= -3
	protected static final int TOWER_NODE = -2;
	
	
	public Logger logger = LoggerFactory.getLogger(this.getClass());

	Long2LongOpenHashMap baseNodesIds;
	Long2LongOpenHashMap adjNodesIds;

	public EdgeGenerator() {
		baseNodesIds = new Long2LongOpenHashMap();
		adjNodesIds = new Long2LongOpenHashMap();
	}

	//cria nos baseses e adjacentes // impelmentação segue errada pois permite apenas q nós não se repitam
	public void createBaseAndAdjNodes(WayOSM way){

		TLongList nodeList = way.getNodes();
		if(!baseNodesIds.containsKey(nodeList.get(0)) && !adjNodesIds.containsKey(nodeList.get(0)) && !baseNodesIds.containsKey(nodeList.get(nodeList.size()-1)) && !adjNodesIds.containsKey(nodeList.get(nodeList.size()-1)) ){
			baseNodesIds.put(nodeList.get(0), nodeList.get(0));
			adjNodesIds.put(nodeList.get(nodeList.size()-1), nodeList.get(nodeList.size()-1));	

			NodeOSM tempFromNode = (NodeOSM) PbfReaderExample.nodesList.find(nodeList.get(0));
			NodeOSM tempToNode = (NodeOSM) PbfReaderExample.nodesList.find(nodeList.get(nodeList.size()-1));


			NodeImpl fromNode, toNode;
			fromNode = new NodeImpl(tempFromNode.getId(), tempFromNode.getLat(), tempFromNode.getLon());
			toNode = new NodeImpl(tempToNode.getId(), tempToNode.getLat(), tempToNode.getLon());

			PbfReaderExample.graph.addNode(fromNode);
			PbfReaderExample.graph.addNode(toNode);

			List<Point> geometry = new ArrayList<Point>();

			for(int i =0; i < nodeList.size(); i++) {
				NodeOSM tempNode = (NodeOSM) PbfReaderExample.nodesList.find(nodeList.get(i));
				Point p = new Point(tempNode.getLat(),tempNode.getLon());
				geometry.add(p);
			}
			EdgeImpl edge = createEdges(way.getId(),fromNode.getId(), toNode.getId(), geometry);
			PbfReaderExample.graph.addEdge(edge);
			PbfReaderExample.graph.save();

		}		
	}
	public EdgeImpl createEdges(long externalId, long fromNode, long toNode, List<Point> geometry){
		EdgeImpl edge = new EdgeImpl(externalId, fromNode, toNode, 1, "teste", geometry);
		return edge;
	}

	public  Long2LongOpenHashMap getBaseNodesIds() {
		return baseNodesIds;
	}

	public void setBaseNodesIds(Long2LongOpenHashMap baseNodesIds) {
		this.baseNodesIds = baseNodesIds;
	}

	public Long2LongOpenHashMap getAdjNodesIds() {
		return adjNodesIds;
	}

	public void setAdjNodesIds(Long2LongOpenHashMap adjNodesIds) {
		this.adjNodesIds = adjNodesIds;
	}

}
