package br.ufc.quixada.tcc.poiReader;

public class Coordenadas {
	private int latitude;
	private int longitude;
	
	public Coordenadas(int latitude, int longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public String toString(){
		return latitude + " , " + longitude;
	}
	
	public int getLatitude() {
		return latitude;
	}

	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}

	public int getLongitude() {
		return longitude;
	}

	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}
	
}
