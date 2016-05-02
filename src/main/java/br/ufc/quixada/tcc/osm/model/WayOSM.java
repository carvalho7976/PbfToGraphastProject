package br.ufc.quixada.tcc.osm.model;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.morbz.osmonaut.osm.Way;

public class WayOSM extends GenericOsmElement {
	 protected final TLongList nodes = new TLongArrayList(5);
	 
	 public WayOSM(long id){
		 super(id, WAY);
	 }
	 
	 public TLongList getNodes(){
	        return nodes;
	 }
}
