package br.ufc.quixada.tcc.repository;

import java.util.ArrayList;
import java.util.List;

import org.graphast.geometry.Point;
import org.graphast.model.EdgeImpl;
import org.graphast.model.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.pbfReader.PbfReaderExample;
import gnu.trove.list.TLongList;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public class EdgeGenerator {
	
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
			logger.info(" id node " + tempFromNode.getId());
			fromNode = new NodeImpl(tempFromNode.getId(), tempFromNode.getLat(), tempFromNode.getLon());
			toNode = new NodeImpl(tempToNode.getId(), tempToNode.getLat(), tempToNode.getLon());
			
			PbfReaderExample.graph.addNode(fromNode);
			PbfReaderExample.graph.addNode(toNode);
			
			List<Point> geometry = new ArrayList<Point>();
			
			for(int i =0; i < nodeList.size(); i++) {
				NodeOSM tempNode = (NodeOSM) PbfReaderExample.nodesList.find(nodeList.get(i));
				
				double latitute = tempNode.getLat();
				double longitude = tempNode.getLon();
				logger.info("Lat " + latitute + "Long " + longitude);
				
				Point p = new Point(tempNode.getLat(),tempNode.getLon());
				geometry.add(p);
			}
			logger.info(nodeList.get(0) + " " + nodeList.get(nodeList.size()-1));
			EdgeImpl edge = createEdges(nodeList.get(0), nodeList.get(nodeList.size()-1), geometry);
			PbfReaderExample.graph.addEdge(edge);
			PbfReaderExample.graph.save();
			logger.info("edge adicionado");
			
		}		
	}
	public EdgeImpl createEdges(long fromNode, long toNode, List<Point> geometry){
		EdgeImpl edge = new EdgeImpl(PbfReaderExample.idEdgesTest++, fromNode, toNode, 1, "teste", geometry);
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
