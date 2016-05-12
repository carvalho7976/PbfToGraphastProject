package br.ufc.quixada.tcc.osm.model;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

public class WayOSM extends GenericOsmElement {
	 private  TLongList nodes = new TLongArrayList(5);
	 
	 public void setNodes(TLongList nodes) {
		this.nodes = nodes;
	}

	public WayOSM(long id){
		 super(id, WAY);
	 }
	 
	 public TLongList getNodes(){
	        return nodes;
	 }
	 
}
