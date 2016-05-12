package br.ufc.quixada.tcc.pbfReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.RelationOSM;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.PbfReader;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.Sink;
/**
 * Demonstrates how to read a file. Reads sample.pbf from the resources folder
 * and prints details about it to the standard output.
 *
 * @author Michael Tandy
 */
public class PbfReaderExample implements Sink, Closeable{
	
	private static ArrayList<NodeOSM> nodeList = new ArrayList<NodeOSM>();
	private static ArrayList<WayOSM> wayList = new  ArrayList<WayOSM>();
	
	

	   
	public static void main(String[] args) throws Exception {
		
		Thread pbfReaderThread;

		InputStream input = PbfReaderExample.class.getResourceAsStream("/luxembourg-latest.osm.pbf");

		PbfReader reader = new PbfReader(input, new PbfReaderExample(), 1);
		pbfReaderThread = new Thread(reader, "PBF Reader");
		pbfReaderThread.start();
		
		//BlockReaderAdapter brad = new TestBinaryParser();
		//new BlockInputStream(input, brad).process();
    }

    private static class TestBinaryParser extends BinaryParser {

        @Override
        protected void parseRelations(List<Relation> rels) {
        	System.out.println(rels.size());
          
        }

        @Override
        protected void parseDense(DenseNodes nodes) {
            
       }

        @Override
        protected void parseNodes(List<Node> nodes) {
        }

        @Override
        protected void parseWays(List<Way> ways) {
        	for (Way way : ways) {
				WayOSM wayOsm = new WayOSM(way.getId());
        	}
        }

        @Override
        protected void parse(HeaderBlock header) {
                       
        }

        public void complete() {
            System.out.println("Numero de nós: " + nodeList.size());
        }

		

    }

    
	public void close() throws IOException {
		System.out.println("fechou garoto");
		
	}
	
	public void complete() {
		System.out.println("terminei garoto");
		
	}

	public void process(GenericOsmElement item) {
		
		if(item != null){
			
			switch (item.getType()) {
				case  GenericOsmElement.NODE:
					NodeOSM node = (NodeOSM) item;
					if(node.toString().length() > 2)
						System.out.println("Nó: " + node.toString());					
					break;
				case GenericOsmElement.WAY:
					WayOSM way = (WayOSM) item;
					System.out.println("way: " + way.toString());
					//TODO
					break;
				case GenericOsmElement.RELATION:
					RelationOSM rel = (RelationOSM) item;
					System.out.println("relation: " + rel.toString());
					break;
			default:
				break;
			}
		}
		
	}

}
