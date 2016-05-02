package br.ufc.quixada.tcc.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;
import org.openstreetmap.osmosis.osmbinary.file.BlockReaderAdapter;

import com.graphhopper.reader.OSMWay;
import com.graphhopper.reader.pbf.PbfFieldDecoder;

import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.pbfReader.PbfReaderExample;
import gnu.trove.list.TLongList;


/**TODO 
 * fazer mapeamento entre elementos OSM e Graphast 
 * implementar metodos para ler os elementos OSM, e converter esses elementos OSM em elementos do Graphast
 * 
 * **/
public class GraphastBinaryParser extends BinaryParser {

	public void complete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void parseRelations(List<Relation> rels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void parseDense(DenseNodes nodes) {
				
	}

	@Override
	protected void parseNodes(List<Node> nodes) {
		for (Node node : nodes) {
			System.out.println(node.getLat() + " " + node.getLon());
		}
		
	}
	
	@Override
	protected void parse(HeaderBlock header) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void parseWays(List<Way> ways) {
		
		PbfFieldDecoder fieldDecoder = new PbfFieldDecoder();
		for (Way way : ways){
            Map<String, String> tags = buildTags(way.getKeysList(), way.getValsList(), fieldDecoder);
           
            WayOSM wayOsm = new WayOSM(way.getId());
          
            long nodeId = 0; 
            TLongList wayNodes = wayOsm.getNodes();
            System.out.println("way ");
            for (long nodeIdOffset : way.getRefsList()){
                nodeId += nodeIdOffset;
                wayNodes.add(nodeId);
            }
           
        }
		
	}

	
 private Map<String, String> buildTags( List<Integer> keys, List<Integer> values, PbfFieldDecoder fieldDecoder ){

	    if (keys.size() != values.size()){
	            throw new RuntimeException("Number of tag keys (" + keys.size() + ") and tag values ("
	             + values.size() + ") don't match");
	    }
	        

	        Iterator<Integer> keyIterator = keys.iterator();
	        Iterator<Integer> valueIterator = values.iterator();
	        if (keyIterator.hasNext())
	        {
	            Map<String, String> tags = new HashMap<String, String>(keys.size());
	            while (keyIterator.hasNext())
	            {
	                String key = fieldDecoder.decodeString(keyIterator.next());
	                String value = fieldDecoder.decodeString(valueIterator.next());
	                tags.put(key, value);
	            }
	            return tags;
	        }
	        return null;
	    }

	
	public static void main(String[] args) {
		InputStream input = GraphastBinaryParser.class.getResourceAsStream("/luxembourg-latest.osm.pbf");
        BlockReaderAdapter brad = new GraphastBinaryParser();
        try {
			new BlockInputStream(input, brad).process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
