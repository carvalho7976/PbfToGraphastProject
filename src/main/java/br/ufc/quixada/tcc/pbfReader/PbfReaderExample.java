package br.ufc.quixada.tcc.pbfReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.graphast.config.Configuration;
import org.graphast.geometry.Point;
import org.graphast.importer.OSMImporterImpl;
import org.graphast.model.Edge;
import org.graphast.model.EdgeImpl;
import org.graphast.model.GraphBounds;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import br.ufc.quixada.tcc.osm.model.NodeOSM;
import br.ufc.quixada.tcc.osm.model.RelationOSM;
import br.ufc.quixada.tcc.osm.model.WayOSM;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.PbfReader;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.Sink;
import br.ufc.quixada.tcc.repository.EdgeGenerator;
import br.ufc.quixada.tcc.repository.NodeRepository;
import br.ufc.quixada.tcc.repository.Repository;
import br.ufc.quixada.tcc.repository.RepositoryAbsctract;
import br.ufc.quixada.tcc.repository.WayRepository;
import gnu.trove.list.TLongList;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

public class PbfReaderExample implements Sink, Closeable{
	
	public static String graphastTmpDir = Configuration.USER_HOME + "/graphast/tmp/osmimporter";
	public static GraphBounds graph;
	public static long idEdgesTest = 1;

	public  Logger logger = LoggerFactory.getLogger(this.getClass());
	public static Repository nodesList = new NodeRepository();
	public static Repository wayList = new WayRepository();
		   
	public static void main(String[] args) throws Exception {
		
		graphastTmpDir = Configuration.USER_HOME + "/graphast/tmp/osmimporter";
		 graph = new GraphImpl(graphastTmpDir);
		 
		 //OSMImporterImpl test = new OSMImporterImpl("luxembourg-latest.osm.pbf", graphastTmpDir);
		 //test.execute();

		Thread pbfReaderThread;

		InputStream input = PbfReaderExample.class.getResourceAsStream("/luxembourg-latest.osm.pbf");

		PbfReader reader = new PbfReader(input, new PbfReaderExample(), 4);
		pbfReaderThread = new Thread(reader, "PBF Reader");
		pbfReaderThread.start();
    }  

    
	public void close() throws IOException {
		System.out.println("fechou garoto");
		
	}
	
	public void complete() {
		logger.info("nodes added " + nodesList.getAll().size64());
		logger.info("ways added " + wayList.getAll().size64());

		EdgeGenerator test = new EdgeGenerator();
		
		long size = wayList.getAll().size64();
		for(long i = 0; i < size; i++){
			test.createBaseAndAdjNodes((WayOSM)wayList.findByIndex(i));
		}
		System.out.println("base nodes " + test.getBaseNodesIds().size());
		System.out.println("adj nodes " + test.getAdjNodesIds().size());
	
	}

	public void process(GenericOsmElement item) {
		
		if(item != null){
			
			switch (item.getType()) {
				case  GenericOsmElement.NODE:
					NodeOSM node = (NodeOSM) item;
					nodesList.add(node);		
					break;
				case GenericOsmElement.WAY:
					WayOSM way = (WayOSM) item;
					wayList.add(way);
					break;
				case GenericOsmElement.RELATION:
					//TODO
					break;
			default:
				break;
			}
		}
		
	}

}
