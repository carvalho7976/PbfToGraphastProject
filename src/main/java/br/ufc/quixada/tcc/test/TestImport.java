package br.ufc.quixada.tcc.test;

import org.graphast.config.Configuration;
import org.graphast.importer.OSMImporterImpl;
import org.graphast.model.GraphBounds;
import org.graphast.model.GraphImpl;

public class TestImport {
	
	
	public static void main(String[] args) {
		
		 String graphastTmpDir = Configuration.USER_HOME + "/graphast/tmp/osmimporter";
		 
		 OSMImporterImpl test = new OSMImporterImpl("monaco-latest.osm.pbf", graphastTmpDir);
		 test.execute();
	}

}
