package br.ufc.quixada.tcc.Reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.graphast.geometry.Point;
import org.graphast.model.EdgeImpl;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.OSMInputFile;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.repository.NodeRepository;
import br.ufc.quixada.tcc.repository.Repository;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongLongHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;

public class Reader {
	private static int workers = -1;

	protected static final int EMPTY = -1;
	// pillar node is >= 3
	protected static final int PILLAR_NODE = 1;
	// tower node is <= -3
	protected static final int TOWER_NODE = 2;
	
	protected static int countWay = 0;
	
	public static Repository nodesList = new NodeRepository();
	public  Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Long, Integer> osmNodeIdToInternalNodeMap;
	
	private Map<Long,NodeOSM > osmNodes;
	private TLongLongHashMap nosNoGrafo;
	private File osmFile;
	private GraphImpl graph;
	private String graphastTmpDir;

	public Reader(File osmFile, String graphHastTmpDir) {
		this.graphastTmpDir = graphHastTmpDir;
		this.osmFile = osmFile;
		osmNodeIdToInternalNodeMap = Collections.synchronizedMap( new ConcurrentHashMap<Long, Integer>());
		osmNodes = new ConcurrentHashMap<Long, NodeOSM>();
		nosNoGrafo = new TLongLongHashMap();
		 this.graph = new GraphImpl(graphastTmpDir);
	}
	
	public GraphImpl execute() throws IOException{
		
		logger.info("processing... highways");
		double initialTime = System.currentTimeMillis();
		
		processHighWays(osmFile);
		
		double finalTime = System.currentTimeMillis();
		double total = finalTime - initialTime;
		
		logger.info("time: " + total);
		
		logger.info("creating graph");
		createGraph(osmFile);
		
		
		graph.save();
	
		logger.info("nodes " + graph.getNumberOfNodes());
		logger.info("edges " + graph.getNumberOfEdges());
						
		return graph;
	}
	void processHighWays(File osmFile) throws IOException{

		OSMInputFile in = null;
		ExecutorService executor =  Executors.newFixedThreadPool(8);

		try	{
			in = new OSMInputFile(osmFile).setWorkerThreads(workers).open();

			
			GenericOsmElement item;
			int i = 0;
			System.out.println("antes laço");
			while ((item = in.getNext()) != null){
				final GenericOsmElement item2 = item;
				
						if(item2.isType(GenericOsmElement.NODE)){						
							 					
							executor.execute(new Runnable() {
								
								public void run() {
									addToNodeList((NodeOSM) item2);
								}
							});
						}
						if (item2.isType(GenericOsmElement.WAY)){
							
							final WayOSM way = (WayOSM) item2;
							       	//ignore broken or complex geometry
										if (way.getNodes().size() >= 2 && acceptWay(way)){
											
											TLongList wayNodes = way.getNodes();
											int s = wayNodes.size();
											for (int index = 0; index < s; index++){
												prepareHighwayNode(wayNodes.get(index));
											}

										}
						         }
			}
			executor.shutdown();
			System.out.println("esperado terminar....");
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);		
			executor.shutdownNow();
			if(!executor.isTerminated()){
				System.out.println("não terminou");;
			}
			System.out.println("depois laço");
			System.out.println("numero nós " + osmNodes.size());

		}catch(Exception e){
			e.printStackTrace();
		}finally {			 
			 try
		        {
		            if (in != null)
		                in.close();
		        } catch (IOException ex)
		        {
		            throw new RuntimeException("Couldn't close resource", ex);
		        }
		}
	}

	void createGraph(File osmFile) throws IOException{

		long wayStart = -1;
		long relationStart = -1;
		long counter = 1;
		OSMInputFile in = null;
		try{

			in = new OSMInputFile(osmFile).setWorkerThreads(workers).open();

			GenericOsmElement item;
			while ((item = in.getNext()) != null)
			{
				switch (item.getType()){
				case GenericOsmElement.NODE:
					//addToNodeList((NodeOSM) item);
					break;
				case GenericOsmElement.WAY:
					if (wayStart < 0){
						wayStart = counter;
					}
					processWay((WayOSM) item);
					break;
				case GenericOsmElement.RELATION:
					if (relationStart < 0){
						relationStart = counter;
					}
					//processRelation((RelationOSM) item);
					break;
				case GenericOsmElement.FILEHEADER:
					break;
				default:
					throw new IllegalStateException("Unknown type " + item.getType());
				}

			}

		} catch (Exception ex){
			throw new RuntimeException("Couldn't process file " + osmFile + ", error: " + ex.getMessage(), ex);
		} finally {
			in.close();
		}

	}
	void prepareHighwayNode( long osmId ){
		try{
			Integer tmpIndex = getNodeMap().get(osmId);
	        if (tmpIndex == null){
	            getNodeMap().put(osmId, PILLAR_NODE);
	        } else{
	            getNodeMap().put(osmId, TOWER_NODE);
	        }
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("osmid " + osmId);
		}
		 	
		
	}
	
	void addToNodeList( NodeOSM node){
		 osmNodes.put(node.getId(), node);		
	}


	/**
	 * Maps OSM IDs (long) to internal node IDs (int)
	 * @return 
	 */
	protected Map<Long, Integer> getNodeMap(){
		return osmNodeIdToInternalNodeMap;
	}
	static int cc = 0;
	void processWay( WayOSM way ){
		
		boolean accept= true;
		if (way.getNodes().size() < 2){
			accept = false;
		}
		// ignore multipolygon geometry
		if (!way.hasTags()){
			accept = false;
		}
			
		if(!acceptWay(way)){
			accept = false;
		}
		if(!accept){
			return;
		}
		
		long wayOsmId = way.getId();
		
		List<EdgeImpl> createdEdges = new ArrayList<EdgeImpl>();
	 
		createdEdges.addAll(addOSMWay(way,way.getDirection(),way.getNodes(),wayOsmId));
		
		
		for (EdgeImpl edgeImpl : createdEdges) {
			graph.addEdge(edgeImpl);
		}
		
		//graph.save();
		
	}
	
	private boolean acceptWay(WayOSM way){
		String key = "highway";
		
		if(way.getTags().containsKey(key)){
			String tag = way.getTagValue(key);
			if(tag.equals("footway") 
					|| tag.equals("pedestrian")
					|| tag.equals("steps")
					|| tag.equals("cycleway")
					|| tag.equals("path")
					|| tag.equals("bus_guideway")
					|| tag.equals("bridleway")
					&& (!way.containsTagEqual("motor_vehicle", "permissive") || !way.containsTagEqual("motor_vehicle", "yes") || !way.containsTagEqual("motorcar", "permissive"))){
				
				return false;
			}
			if(way.containsTagEqual(key, "track")){				
				String trackType = way.getTagValue("tracktype");				
	            if (trackType != null &&  !trackType.equals("grade1") && !trackType.equals("grade2") && !trackType.equals("grade3")){
	            	return false;
	            }     
	          
			}
			if (way.containsTagEqual("impassable", "yes") || way.containsTagEqual("status", "impassable") || ( way.containsTagEqual("motor_vehicle", "no") || way.containsTagEqual("surface", "ground"))){
				return false;
			}
			if((way.containsValue("private") || way.containsTagEqual("access", "private") ||  way.containsTagEqual("access", "no") ) && !way.containsTagEqual("motor_vehicle", "permissive") ){
				return false;
			}
						
			if(way.containsTagEqual(key, "service") && way.containsTagEqual("tunnel", "passage")){
				return false;
			}
						
			return true;
			
		}
		
		if(way.containsTagEqual("route", "shuttle_train") || way.containsTagEqual("route", "ferry")){
			
			String motorcarTag = way.getTagValue("motorcar");
            if (motorcarTag == null)
                motorcarTag = way.getTagValue("motor_vehicle");

            if (motorcarTag == null && !way.hasTag("foot") && !way.hasTag("bicycle") || "yes".equals(motorcarTag))
                return true;
		}
		return false;
		
		
	}
	/**
     * This method creates from an OSM way (via the osm ids) one or more edges in the graph.
     *	 ONE_WAY = 0;
	 	 BIDIRECIONAL = 1;
	 	 REVERSE = 2;
     */
    Collection<EdgeImpl> addOSMWay( WayOSM way, final int direcao,TLongList osmNodeIds, final long wayOsmId ){
        
    	way = schrinkWayIfGate(way);
    	osmNodeIds = way.getNodes();
    	
        List<EdgeImpl> newEdges = new ArrayList<EdgeImpl>(5);
    
        try{
        	int tempIndex = 0;
        	int tmpNode = 0;
            for (int i = 1; i < osmNodeIds.size(); i++){
            	
                long osmId = osmNodeIds.get(i);
                tmpNode = getNodeMap().get(osmId);
                
                if (tmpNode == EMPTY)                	
                		continue;
                
                if (tmpNode == TOWER_NODE){
                	    long tmpFromNode = osmNodeIds.get(tempIndex);
                		NodeOSM fromNode = osmNodes.get(tmpFromNode);
                		
                		long tempToNode = osmNodeIds.get(i);
                		NodeOSM toNode = osmNodes.get(tempToNode);
                		
                		Node n = new NodeImpl(fromNode.getId(), fromNode.getLat(), fromNode.getLon());
                		Node n2 = new NodeImpl(toNode.getId(), toNode.getLat(), toNode.getLon());
                		
                		long fromNodeId; 
                		long toNodeId;
                              		
                		if(!getNosnoGrafo().containsKey(tmpFromNode)){
                       		graph.addNode(n);
                       		getNosnoGrafo().put(tmpFromNode, n.getId());
                       		fromNodeId = n.getId();
                    		
                		}else{
                			fromNodeId = getNosnoGrafo().get(tmpFromNode);               			
                		}
                		
                		if(!getNosnoGrafo().containsKey(tempToNode)){
                			graph.addNode(n2);
                			getNosnoGrafo().put(tempToNode, n2.getId());
                			toNodeId = n2.getId();
                		}else{
                			toNodeId = getNosnoGrafo().get(tempToNode);       
                		}
                		List<Point> geometry = new ArrayList<Point>();
                		
                		for(int j = tempIndex; j < i;j++){
                			long tmpId = osmNodeIds.get(j);
                			NodeOSM nodeOsm = osmNodes.get(tmpId);
                			Point p = new Point(nodeOsm.getLat(), nodeOsm.getLon());
                			geometry.add(p);
                		}
                		
                		if(WayOSM.ONE_WAY == direcao){
                		   EdgeImpl e = new EdgeImpl(wayOsmId, fromNodeId, toNodeId, 1, "teste", geometry);
                 		   newEdges.add(e);
                		}else if(WayOSM.BIDIRECIONAL == direcao){
                			 EdgeImpl e = new EdgeImpl(wayOsmId, fromNodeId, toNodeId, 1, "teste", geometry);
                			 EdgeImpl f = new EdgeImpl(wayOsmId, toNodeId, fromNodeId, 1, "teste", geometry);
                   		   	newEdges.add(e);
                   		   	newEdges.add(f);
                		}else if(WayOSM.REVERSE == direcao){
                			 EdgeImpl e = new EdgeImpl(wayOsmId, toNodeId,fromNodeId, 1, "teste", geometry);
                			 newEdges.add(e);
                		}
                		   
                		  tempIndex = i;
                	
                    continue;
                	
                }if (tmpNode == PILLAR_NODE){
                       continue;
                }

            }
            
            // caso o nó inicial seja tower e os outros nós sejam ponta de ramo ou o nó tenha varios towernodes porém os nós finais sejam ponta de ramo
            if(tempIndex == 0 || tempIndex < osmNodeIds.size()-1){
            	long osmId = osmNodeIds.get(tempIndex);
            	tmpNode = getNodeMap().get(osmId);
            
                if(tmpNode == TOWER_NODE){
                	
                	long tmpFromNode = osmNodeIds.get(tempIndex);
            		NodeOSM fromNode = osmNodes.get(tmpFromNode);
            		
            		long tempToNode = osmNodeIds.get(osmNodeIds.size()-1);
            		NodeOSM toNode = osmNodes.get(tempToNode);
            		
            		Node n = new NodeImpl(fromNode.getId(), fromNode.getLat(), fromNode.getLon());
            		Node n2 = new NodeImpl(toNode.getId(), toNode.getLat(), toNode.getLon());
            		
            		long fromNodeId; 
            		long toNodeId;
            		if(!getNosnoGrafo().containsKey(tmpFromNode)){
                   		graph.addNode(n);
                   		//logger.info(n.toString());
                   		getNosnoGrafo().put(tmpFromNode, n.getId());
                   		fromNodeId = n.getId();
                		
            		}else{
            			fromNodeId = getNosnoGrafo().get(tmpFromNode);               			
            		}
            		
            		if(!getNosnoGrafo().containsKey(tempToNode)){
            			graph.addNode(n2);
            			//logger.info(n2.toString());
            			getNosnoGrafo().put(tempToNode, n2.getId());
            			toNodeId = n2.getId();
            		}else{
            			toNodeId = getNosnoGrafo().get(tempToNode);       
            		}
            		List<Point> geometry = new ArrayList<Point>();
            		
            		for(int j = 0; j <osmNodeIds.size();j++){
            			long tmpId = osmNodeIds.get(j);
            			NodeOSM nodeOsm = osmNodes.get(tmpId);
            			Point p = new Point(nodeOsm.getLat(), nodeOsm.getLon());
            			geometry.add(p);
            		}
            		
            		if(WayOSM.ONE_WAY == direcao){
             		   EdgeImpl e = new EdgeImpl(wayOsmId, fromNodeId, toNodeId, 1, "teste", geometry);
              		   newEdges.add(e);
             		}else if(WayOSM.BIDIRECIONAL == direcao){
             			 EdgeImpl e = new EdgeImpl(wayOsmId, fromNodeId, toNodeId, 1, "teste", geometry);
             			 EdgeImpl f = new EdgeImpl(wayOsmId, toNodeId, fromNodeId, 1, "teste", geometry);
                		   	newEdges.add(e);
                		   	newEdges.add(f);
             		}else if(WayOSM.REVERSE == direcao){
             			 EdgeImpl e = new EdgeImpl(wayOsmId, toNodeId,fromNodeId, 1, "teste", geometry);
             			 newEdges.add(e);
             		}
            		
                }
            }
            
        } catch (Exception ex){
            logger.info(ex.toString());
        }
       
        return newEdges;
    }
    
    WayOSM schrinkWayIfGate(WayOSM way){
    	TLongList osmNodeIds = way.getNodes();
    	TLongList tmpsIds = new TLongArrayList(5);
    	for (int i = 0; i < osmNodeIds.size(); i++){
    		long tmpFromNode = osmNodeIds.get(i);
    		NodeOSM fromNode = osmNodes.get(tmpFromNode);
    		tmpsIds.add(osmNodeIds.get(i));
    		if(fromNode.hasTag("barrier") && (!fromNode.containsTagEqual("motorcar", "yes") )){
    			way.setNodes(tmpsIds);
    			return way;
    		}
    	}
    	
    	return way; 
    }
    
    EdgeImpl addEdge( int fromIndex, int toIndex, List<Point> pointList, long wayOsmId )
    {
        // sanity checks
        if (fromIndex < 0 || toIndex < 0)
            throw new AssertionError("to or from index is invalid for this edge " + fromIndex + "->" + toIndex + ", points:" + pointList);
        
        
        double lat, lon = Double.NaN;
        
        ArrayList<Point> pillarNodes = new ArrayList<Point>(pointList.size()-2);
        int nodes = pointList.size();
        
        for (int i = 1; i < nodes; i++){
            lat = pointList.get(i).getLatitude();
            lon = pointList.get(i).getLongitude();
            if (nodes > 2 && i < nodes - 1){
                pillarNodes.add(new Point(lat, lon));
            }
        }

        EdgeImpl iter = new EdgeImpl(wayOsmId, fromIndex, toIndex, 1, "teste", pointList);

      
        return iter;
    }
    
    
    private TLongLongHashMap getNosnoGrafo(){
    	return nosNoGrafo;
    }
    



}
