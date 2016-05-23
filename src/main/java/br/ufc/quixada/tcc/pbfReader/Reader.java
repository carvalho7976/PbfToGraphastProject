package br.ufc.quixada.tcc.pbfReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.graphast.geometry.Point;
import org.graphast.model.EdgeImpl;
import org.graphast.model.GraphImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.OSMInputFile;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.repository.NodeRepository;
import br.ufc.quixada.tcc.repository.Repository;
import gnu.trove.list.TLongList;
import it.unimi.dsi.fastutil.longs.Long2IntArrayMap;

public class Reader {
	private static int workers = 4;

	protected static final int EMPTY = -1;
	// pillar node is >= 3
	protected static final int PILLAR_NODE = 1;
	// tower node is <= -3
	protected static final int TOWER_NODE = -2;
	
	public static Repository nodesList = new NodeRepository();
	public  Logger logger = LoggerFactory.getLogger(this.getClass());

	private Long2IntArrayMap osmNodeIdToInternalNodeMap;
	private int nextTowerId = 0;
	private int nextPillarId = 0;
	private File osmFile;
	private GraphImpl graph;
	private String graphastTmpDir;

	public Reader(File osmFile, String graphHastTmpDir) {
		this.graphastTmpDir = graphHastTmpDir;
		this.osmFile = osmFile;
		osmNodeIdToInternalNodeMap = new Long2IntArrayMap();
		 this.graph = new GraphImpl(graphastTmpDir);
		  
	}
	
	public GraphImpl execute() throws IOException{
		
		logger.info("processing... highways");
		processHighWays(osmFile);
		
		logger.info("creating graph");
		createGraph(osmFile);
		
		return this.graph;
	}
	void processHighWays(File osmFile) throws IOException{

		OSMInputFile in = null;

		try	{
			in = new OSMInputFile(osmFile).setWorkerThreads(workers).open();

			
			GenericOsmElement item;
			while ((item = in.getNext()) != null){
				//logger.info(item.toString());

				if (item.isType(GenericOsmElement.WAY)){

					final WayOSM way = (WayOSM) item;

					//ignore broken or complex geometry
					if (way.getNodes().size() > 2 && way.hasTags()){
						TLongList wayNodes = way.getNodes();
						int s = wayNodes.size();
						for (int index = 0; index < s; index++){
							prepareHighwayNode(wayNodes.get(index));
						}

					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			in.close();
		}
	}

	void createGraph(File osmFile) throws IOException{

		int tmp = (int) Math.max(getNodeMap().size() / 50, 100);

		long wayStart = -1;
		long relationStart = -1;
		long counter = 1;
		OSMInputFile in = null;
		try{

			in = new OSMInputFile(osmFile).setWorkerThreads(workers).open();
			Long2IntArrayMap nodeFilter = getNodeMap();

			GenericOsmElement item;
			while ((item = in.getNext()) != null)
			{
				switch (item.getType())
				{
				case GenericOsmElement.NODE:
					if (nodeFilter.get(item.getId()) != -1){
						addNode((NodeOSM) item);
					}
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
		int tmpIndex = getNodeMap().get(osmId);
		if (tmpIndex == EMPTY){
			// osmId is used exactly once
			getNodeMap().put(osmId, PILLAR_NODE);
		} else if (tmpIndex > EMPTY){
			// mark node as tower node as it occured at least twice times
			getNodeMap().put(osmId, TOWER_NODE);
		}
	}

	boolean addNode( NodeOSM node ) {

		int nodeType = getNodeMap().get(node.getId());
		if (nodeType == EMPTY)
			return false;
		if (nodeType == TOWER_NODE){
			addTowerNode(node.getId());
		} else if (nodeType == PILLAR_NODE){
			getNodeMap().put(node.getId(), nextPillarId + 3);
			nextPillarId++;
		}
		nodesList.add(node);
		return true;
	}

	int addTowerNode( long osmId) {
		int id = -(nextTowerId + 3);
		getNodeMap().put(osmId, id);
		nextTowerId++;
		return id;
	}

	/**
	 * Maps OSM IDs (long) to internal node IDs (int)
	 */
	protected Long2IntArrayMap getNodeMap(){
		return osmNodeIdToInternalNodeMap;
	}

	void processWay( WayOSM way ){

		if (way.getNodes().size() < 2)
			return;

		// ignore multipolygon geometry
		if (!way.hasTags())
			return;

		long wayOsmId = way.getId();

		
		List<EdgeImpl> createdEdges = new ArrayList<EdgeImpl>();
	 
		// no barriers - simply add the whole way
		createdEdges.addAll(addOSMWay(way.getNodes(),wayOsmId));
		
		for (EdgeImpl edgeImpl : createdEdges) {
			graph.addEdge(edgeImpl);
		}
		graph.save();
		
	}
	/**
     * This method creates from an OSM way (via the osm ids) one or more edges in the graph.
     */
    Collection<EdgeImpl> addOSMWay( final TLongList osmNodeIds, final long wayOsmId ){
        
    	List<Point> pointList = getGeometryFromWay(osmNodeIds);
       
        List<EdgeImpl> newEdges = new ArrayList<EdgeImpl>(5);
        int firstNode = -1;
      
        try{
            for (int i = 0; i < osmNodeIds.size(); i++)
            {
                long osmId = osmNodeIds.get(i);
                int tmpNode = getNodeMap().get(osmId);
                if (tmpNode == EMPTY)
                    continue;

                // skip osmIds with no associated pillar or tower id (e.g. !OSMReader.isBounds)
                if (tmpNode == TOWER_NODE)
                    continue;

                if (tmpNode == PILLAR_NODE){
                	 
                       continue;
                }

                if (tmpNode <= -TOWER_NODE && tmpNode >= TOWER_NODE)
                    throw new AssertionError("Mapped index not in correct bounds " + tmpNode + ", " + osmId);

                if (tmpNode < TOWER_NODE) {
                    // TOWER node
                    tmpNode = -tmpNode - 3;
                    NodeOSM node = (NodeOSM) nodesList.find(tmpNode);
                    
                    Point tmp = new Point(node.getLat(), node.getLon());
                    pointList.add(tmp);
                    if (firstNode >= 0)
                    {
                        newEdges.add(addEdge(firstNode, tmpNode, pointList,  wayOsmId));
                        
                    }
                    firstNode = tmpNode;
                }
            }
        } catch (RuntimeException ex){
           
        }
        return newEdges;
    }
    EdgeImpl addEdge( int fromIndex, int toIndex, List<Point> pointList, long wayOsmId )
    {
        // sanity checks
        if (fromIndex < 0 || toIndex < 0)
            throw new AssertionError("to or from index is invalid for this edge " + fromIndex + "->" + toIndex + ", points:" + pointList);
        
        
        double towerNodeDistance = 0;
        double lat, lon, ele = Double.NaN;
        
        ArrayList<Point> pillarNodes = new ArrayList<Point>(pointList.size()-2);
        int nodes = pointList.size();
        
        for (int i = 1; i < nodes; i++){
            // we could save some lines if we would use pointList.calcDistance(distCalc);
            lat = pointList.get(i).getLatitude();
            lon = pointList.get(i).getLongitude();
            if (nodes > 2 && i < nodes - 1){
            	
                pillarNodes.add(new Point(lat, lon));
            }
        }

        EdgeImpl iter = new EdgeImpl(wayOsmId, fromIndex, toIndex, 1, "teste", pointList);

      
        return iter;
    }
    
    private List<Point> getGeometryFromWay(TLongList nodeList){
    	
    	List<Point> geometry = new ArrayList<Point>();
    	int nulo = 0;
    	int naoNulo = 0;
    	for(int i =0; i < nodeList.size(); i++) {
			NodeOSM tempNode = (NodeOSM) nodesList.find(nodeList.get(i));
			if(tempNode != null){
				Point p = new Point(tempNode.getLat(),tempNode.getLon());
				geometry.add(p);
				logger.info("não nulos " + naoNulo++);
			}else{
				logger.info("nulos " + nulo++);
			}
			
		}
    	return geometry;
    }



}