package br.ufc.quixada.tcc.pbfReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.RelationOSM;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.PbfReader;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.Sink;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
/**
 * Demonstrates how to read a file. Reads sample.pbf from the resources folder
 * and prints details about it to the standard output.
 *
 * @author Michael Tandy
 */
public class PbfReaderExample implements Sink, Closeable{
		
		   
	public static void main(String[] args) throws Exception {
		
		Thread pbfReaderThread;

		InputStream input = PbfReaderExample.class.getResourceAsStream("/luxembourg-latest.osm.pbf");

		PbfReader reader = new PbfReader(input, new PbfReaderExample(), 1);
		pbfReaderThread = new Thread(reader, "PBF Reader");
		pbfReaderThread.start();
		
		//BlockReaderAdapter brad = new TestBinaryParser();
		//new BlockInputStream(input, brad).process();
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
					//if(node.toString().length() > 2)
						//System.out.println("Nó: " + node.toString());					
					break;
				case GenericOsmElement.WAY:
					WayOSM way = (WayOSM) item;					
					TLongList nos = way.getNodes();
					System.out.print("way:");
					for (int i = 0; i < nos.size(); i++) {
						System.out.print(" " + nos.get(i) + " ");
					}
					System.out.println("====================");
					
					//System.out.println("way: " + way.toString());
					//TODO
					break;
				case GenericOsmElement.RELATION:
					RelationOSM rel = (RelationOSM) item;
					//System.out.println("relation: " + rel.toString());
					break;
			default:
				break;
			}
		}
		
	}

}
