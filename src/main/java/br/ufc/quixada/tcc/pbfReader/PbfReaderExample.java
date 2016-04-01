package br.ufc.quixada.tcc.pbfReader;

import java.io.*;
import java.util.List;

import javax.swing.plaf.synth.SynthSeparatorUI;

import org.openstreetmap.osmosis.osmbinary.*;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseInfo;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;
import org.openstreetmap.osmosis.osmbinary.Osmformat.StringTable;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;
import org.openstreetmap.osmosis.osmbinary.file.*;

import com.graphhopper.reader.pbf.PbfDecoder;
import com.graphhopper.reader.pbf.PbfFieldDecoder;
/**
 * Demonstrates how to read a file. Reads sample.pbf from the resources folder
 * and prints details about it to the standard output.
 *
 * @author Michael Tandy
 */
public class PbfReaderExample {
	public static void main(String[] args) throws Exception {
		
		
		InputStream input = PbfReaderExample.class.getResourceAsStream("/luxembourg-latest.osm.pbf");
        BlockReaderAdapter brad = new TestBinaryParser();
        new BlockInputStream(input, brad).process();
    }

    private static class TestBinaryParser extends BinaryParser {

        @Override
        protected void parseRelations(List<Relation> rels) {
            if (!rels.isEmpty())
                System.out.println("Number of Relations: "+ rels.size());
           
//            for (Relation relation : rels) {
//				 System.out.println("relation id " + relation.getId());
//			}
            Relation r = null;
        }

        @Override
        protected void parseDense(DenseNodes nodes) {
            long lastId=0;
            long lastLat=0;
            long lastLon=0;
            
            //criar um primitiveBlock com dados de input stream
            //Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(data);
            //pegar o primitiveBlock para criar a strintable para decodificar as chaves e valores; 
            // PbfDecoder pbfDecoder = new PbfFieldDecoder(primitiveBlock);
                        
            //System.out.println("Numero de dense nodes: " + nodes.getIdCount());
     /*       for (int i=0 ; i<nodes.getIdCount() ; i++) {
                lastId += nodes.getId(i);
                lastLat += nodes.getLat(i);
                lastLon += nodes.getLon(i);
                
                int key = nodes.getKeysVals(i);
                if(key > 0)
                	System.out.println("key_val " + key);
                
                //System.out.printf("Dense node, ID %d @ %.6f,%.6f\n",lastId,parseLat(lastLat),parseLon(lastLon));
            }*/
            List<Integer> keys = nodes.getKeysValsList();
            for (Integer key : keys) {
            	if(key > 0)
            	   System.out.println("keys " + key);
				
			}
        }

        @Override
        protected void parseNodes(List<Node> nodes) {
        	if(!nodes.isEmpty())
        		System.out.println("Numero de regular nodes: " + nodes.size());
            for (Node n : nodes) {
            	
            	
              /*  System.out.printf("Regular node, ID %d @ %.6f,%.6f\n",
                        n.getId(),parseLat(n.getLat()),parseLon(n.getLon()));*/
            }
        }

        @Override
        protected void parseWays(List<Way> ways) {
        	
            //System.out.println("Numero de ways: " + ways.size());
        	/*for (Way w : ways) {
                System.out.println("Way ID " + w.getId());
                StringBuilder sb = new StringBuilder();
                sb.append("  Nodes: ");
                long lastRef = 0;
                for (Long ref : w.getRefsList()) {
                    lastRef+= ref;
                    sb.append(lastRef).append(" ");
                }
                sb.append("\n  Key=value pairs: ");
                for (int i=0 ; i<w.getKeysCount() ; i++) {
                    sb.append(getStringById(w.getKeys(i))).append("=")
                            .append(getStringById(w.getVals(i))).append(" ");
                }
                System.out.println(sb.toString());
            }*/
        }

        @Override
        protected void parse(HeaderBlock header) {
            System.out.println("Got header block.");
            System.out.println("header " + header.getSource());
            
        }

        public void complete() {
            System.out.println("Complete!");
        }

		

    }

}
