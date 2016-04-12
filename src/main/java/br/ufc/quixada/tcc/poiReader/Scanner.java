package br.ufc.quixada.tcc.poiReader;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import net.morbz.osmonaut.EntityFilter;
import net.morbz.osmonaut.IOsmonautReceiver;
import net.morbz.osmonaut.Osmonaut;
import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.EntityType;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;



public class Scanner {
		
	private static List<Filtro> filters;
	private static boolean onlyClosedWays = true;
	private static boolean printPois = true;
	private static int poisFound = 0;
	private static String[] requiredTags = { "name" };
	private static String[] outputTags = { "name" };
	private static int lastPrintLen = 0;
	public static String inputFile = "luxembourg-latest.osm.pbf";
	
	public File getFile(){
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(inputFile).getFile());
		return file;
	}
	public URL getInputFile(){
		ClassLoader classLoader = getClass().getClassLoader();
		return classLoader.getResource(inputFile);
	}
	
	public static void main(String[] args) {
				
		Scanner scanner = new Scanner();
		File file = scanner.getFile();
		
		if(!file.exists()) {
			System.out.println("Error: Input file doesn't exist");
			System.exit(-1);
		}
		
		// Check OSM entity types
		boolean parseNodes = true;
		boolean parseWays = true;
		boolean parseRelations = false;
			
		// Get filter rules
		FilterFileParser parser = new FilterFileParser(null);
		filters = parser.parse();
		if(filters == null) {
			System.exit(-1);
		}
		// Setup OSMonaut
		EntityFilter filter = new EntityFilter(parseNodes, parseWays, parseRelations);
		Osmonaut naut = new Osmonaut(scanner.getInputFile().getPath(), filter, false);
		
				
		// Start OSMonaut
		naut.scan(new IOsmonautReceiver() {
			
		    public boolean needsEntity(EntityType type, Tags tags) {
		    	// Are there any tags?
				if(tags.size() == 0) {
					return false;
				}
				
				// Check required tags
		    	for(String tag : requiredTags) {
		    		if(!tags.hasKey(tag)) {
		    			return false;
		    		}
		    	}		    	
		    	// Check category
		        return getCategory(tags, filters) != null;
		    }
	    
		    public void foundEntity(Entity entity) {
		    	// Check if way is closed
		    	if(onlyClosedWays && entity.getEntityType() == EntityType.WAY) {
		    		if(!((Way)entity).isClosed()) {
		    			return;
		    		}
		    	}				
				// Get category
				Tags tags = entity.getTags();
				String cat = getCategory(tags, filters);
				if(cat == null) {
					return;
				}
				
				// Make OSM-ID
				String id = "";
				switch(entity.getEntityType()) {
					case NODE:
						id = "N";
						break;
					case WAY:
						id = "W";
						break;
					case RELATION:
						id = "R";
						break;
				}
				id += entity.getId();
				
				// Make output tags
				String[] values = new String[outputTags.length];
				for(int i = 0; i < outputTags.length; i++) {
					String key = outputTags[i];
					if(tags.hasKey(key)) {
						values[i] = tags.get(key);
					}
				}		    	
		        // Make POI
				poisFound++;
				Poi poi = new Poi(values, cat, entity.getCenter(), id);
				
				// Output
				if(printPois) {
					System.out.println(poi);
				}
				
			}
		});
						
		printPoisFound();
		
		
		
	}
	
	private static void printPoisFound() {
		// Clear output
		while(lastPrintLen > 0) {
			System.out.print('\b');
			lastPrintLen--;
		}
		
		// Output count
		String newStr = poisFound + " POIs found";
		//System.out.println(newStr);
		lastPrintLen = newStr.length();
	}
	
		
	/* Categories */
	private static String getCategory(Tags tags, List<Filtro> filters) {
		// Iterate filters
		String cat = null;
		for(Filtro filter : filters) {
			cat = getCategoryRecursive(filter, tags, null);
			if(cat != null) {
				return cat;
			}
		}
		return null;
	}
	
	private static String getCategoryRecursive(Filtro filter, Tags tags, String key) {
		// Use key of parent rule or current
		if(filter.hasKey()) {
			key = filter.getKey();
		}
		
		// Check for key/value
		if(tags.hasKey(key)) {
			if(filter.hasValue() && !filter.getValue().equals(tags.get(key))) {
				return null;
			}
		} else {
			return null;
		}
		
		// If childs have categories, those will be used
		for(Filtro child : filter.childs) {
			String cat = getCategoryRecursive(child, tags, key);
			if(cat != null) {
				return cat;
			}
		}
		return filter.getCategory();
	}
}