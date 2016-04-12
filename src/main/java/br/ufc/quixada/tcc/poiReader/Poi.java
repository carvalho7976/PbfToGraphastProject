package br.ufc.quixada.tcc.poiReader;

import net.morbz.osmonaut.osm.LatLon;

public class Poi {
	private String[] values;
	private String cat;
	private String OsmId;
	private LatLon latLon;
	
	public Poi(String[] values2, String cat2, LatLon center, String id) {
		this.values = values2;
		this.cat = cat2;
		this.latLon = center;
		this.OsmId = id;
	}

	public String toString(){
		String val = "";
		for(int i =0; values.length > i; i++){
			if(values[i] != null){
				val += values[i] + "";
			}
		}
		
		return "Tags: " + val + " #OsmId: " + OsmId + " #cat: " + cat + " #LatLon: " + latLon.toString();
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}
	
	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public String getOsmId() {
		return OsmId;
	}

	public void setOsmId(String osmId) {
		OsmId = osmId;
	}
	
	

}
