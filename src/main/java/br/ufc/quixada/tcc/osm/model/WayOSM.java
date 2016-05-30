package br.ufc.quixada.tcc.osm.model;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

public class WayOSM extends GenericOsmElement {
	 public static final int ONE_WAY = 0;
	 public static final int BIDIRECIONAL = 1;
	 public static final int REVERSE = 2;

	
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
	 public int getDirection(){
		 if(containsTagEqual("oneway", "yes")
				 ||containsTagEqual("oneway", "true")
				 ||containsTagEqual("oneway", "1")
				 ||containsTagEqual("oneway", "-1")
				 || hasTag("vehicle:backward")
				 || hasTag("vehicle:forward")
                 || hasTag("motor_vehicle:backward")
                 || hasTag("motor_vehicle:forward") 
                 || containsTagEqual("junction", "roundabout")){
			 
			 if( containsTagEqual("oneway", "-1")  || containsTagEqual("vehicle:forward", "no")   || containsTagEqual("motor_vehicle:forward", "no")){
				 return REVERSE;
			 }
			return ONE_WAY;
		 }
		 return BIDIRECIONAL;
		 
	 }
	 public boolean hasTagFilter(String key){
			if(getTags().containsKey(key)){
				String tag = getTags().get(key).toString();
				if(tag.equals("footway") || tag.equals("pedestrian") || tag.equals("steps") || tag.equals("cycleway")){
					return false;
				}
				return true;
			}
			return false;
	}
	 
	 
}
