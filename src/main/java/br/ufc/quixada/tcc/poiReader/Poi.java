package br.ufc.quixada.tcc.poiReader;

public class Poi {
	private String[] values;
	private Coordenadas latLon;
	private String cat;
	private String OsmId;
	
	public Poi(String[] Values, String cat, String OsmId, Coordenadas latLon){
		this.values = values;
		this.cat = cat;
		this.OsmId = OsmId;
		this.latLon = latLon;
	}
	
	public String toString(){
		String val = "";
		for(int i =0; values.length > i; i++){
			if(values[i] != null){
				val += values[i] + ",";
			}
		}
		
		return "Tags: " + val + "#OsmId: " + OsmId + "#cat: " + cat + "#LatLon: " + latLon.toString();
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public Coordenadas getLatLon() {
		return latLon;
	}

	public void setLatLon(Coordenadas latLon) {
		this.latLon = latLon;
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
