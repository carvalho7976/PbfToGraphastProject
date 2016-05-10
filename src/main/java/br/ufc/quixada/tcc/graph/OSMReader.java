package br.ufc.quixada.tcc.graph;

import static com.graphhopper.util.Helper.nf;

import java.io.File;

import org.graphast.model.GraphBounds;
import org.graphast.model.GraphImpl;

import com.graphhopper.reader.OSMNode;

import br.ufc.quixada.tcc.estruturasGraphhoper.GHLongIntBTree;
import br.ufc.quixada.tcc.estruturasGraphhoper.LongIntMap;
import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.OSMInputFile;
import br.ufc.quixada.tcc.osm.model.RelationOSM;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;

public class OSMReader {
	
	private int workerThreads = -1;
	private LongIntMap osmNodeIdToInternalNodeMap;
	private TLongLongHashMap osmNodeIdToNodeFlagsMap;
    private TLongLongHashMap osmWayIdToRouteWeightMap;	
    protected static final int EMPTY = -1;
    // pillar node is >= 3
    protected static final int PILLAR_NODE = 1;
    // tower node is <= -3
    protected static final int TOWER_NODE = -2;
    private int nextTowerId = 0;
			
    public OSMReader(){
    	 osmNodeIdToInternalNodeMap = new GHLongIntBTree(200);
         osmNodeIdToNodeFlagsMap = new TLongLongHashMap(200, .5f, 0, 0);
         osmWayIdToRouteWeightMap = new TLongLongHashMap(200, .5f, 0, 0);
    }
			
			
	/**
     * Creates the edges and nodes files from the specified osm file.
     */
    private void writeOsm2Graph( File osmFile ){
        int tmp = (int) Math.max(getNodeMap().getSize() / 50, 100);
        
        GraphBounds graph = (GraphBounds) new GraphImpl("foo");
       
        
        long wayStart = -1;
        long relationStart = -1;
        long counter = 1;
        OSMInputFile in = null;
        try
        {
            in = new OSMInputFile(osmFile).setWorkerThreads(workerThreads).open();
            LongIntMap nodeFilter = getNodeMap();

            GenericOsmElement item;
            while ((item = in.getNext()) != null)
            {
                switch (item.getType())
                {
                    case GenericOsmElement.NODE:
                        if (nodeFilter.get(item.getId()) != -1)
                        {
                            processNode((NodeOSM) item);
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
                        processRelation((RelationOSM) item);
                        break;
                    case GenericOsmElement.FILEHEADER:
                        break;
                    default:
                        throw new IllegalStateException("Unknown type " + item.getType());
                }
                if (++counter % 100000000 == 0)
                {
                    LOGGER.info(nf(counter) + ", locs:" + nf(locations) + " (" + skippedLocations + ") " + Helper.getMemInfo());
                }
            }

            // logger.info("storage nodes:" + storage.nodes() + " vs. graph nodes:" + storage.getGraph().nodes());
        } catch (Exception ex)
        {
            throw new RuntimeException("Couldn't process file " + osmFile + ", error: " + ex.getMessage(), ex);
        } finally
        {
            Helper.close(in);
        }

        finishedReading();
        if (graph.getNodes() == 0)
            throw new IllegalStateException("osm must not be empty. read " + counter + " lines and " + locations + " locations");
    }
    
    private void processNode( NodeOSM node ){
        
    	if (isInBounds(node))
            addNode(node);
    }
    boolean addNode( NodeOSM node ){
        int nodeType = getNodeMap().get(node.getId());
        if (nodeType == EMPTY)
            return false;

        double lat = node.getLat();
        double lon = node.getLon();
        if (nodeType == TOWER_NODE){
            addTowerNode(node.getId(), lat, lon);
        } else if (nodeType == PILLAR_NODE){
        	
        	//TODO
            //pillarInfo.setNode(nextPillarId, lat, lon);
            //getNodeMap().put(node.getId(), nextPillarId + 3);
            //nextPillarId++;
        }
        return true;
    }
    
    int addTowerNode( long osmId, double lat, double lon){
         nodeAccess.setNode(nextTowerId, lat, lon);

        int id = -(nextTowerId + 3);
        getNodeMap().put(osmId, id);
        nextTowerId++;
        return id;
    }
    protected TLongLongMap getNodeFlagsMap() {
        return osmNodeIdToNodeFlagsMap;
    }

    boolean isInBounds( NodeOSM node ) {
        return true;
    }
    protected LongIntMap getNodeMap(){
        return osmNodeIdToInternalNodeMap;
    }
    
}
