package br.ufc.quixada.tcc.osm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GenericOsmElement {
	public static final int NODE = 0;
	public static final int WAY = 1;
	public static final int RELATION = 2;
	public static final int FILEHEADER = 3;
	private final int type;
	private final long id;
	private final Map<String, Object> properties = new HashMap<String, Object>(5);

	protected GenericOsmElement( long id, int type ){
		this.id = id;
		this.type = type;
	}

	public long getId(){
		return id;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getTag( String key, T defaultValue ){
		T val = (T) properties.get(key);
		if (val == null)
			return defaultValue;
		return val;
	}

	public void setTag( String name, Object value )	{
		properties.put(name, value);
	}
	
	public void setTags( Map<String, String> newTags )
    {
        properties.clear();
        if (newTags != null)
            for (Entry<String, String> e : newTags.entrySet())
            {
                setTag(e.getKey(), e.getValue());
            }
    }
	public Map<String, Object> getTags() {
	        return properties;
	}

	public void removeTag( String name ){
		properties.remove(name);
	}

	public void clearTags(){
		properties.clear();
	}

	public int getType(){
		return type;
	}

	public boolean isType( int type ){
		return this.type == type;
	}
	public boolean hasTags() {
        return !properties.isEmpty();
    }
	
	public boolean hasTag(String key){
		return properties.containsKey(key);
	}
	//sem filtro 
	public boolean containsTagEqual(String key, String value){
		if(properties.containsKey(key)){
			if(properties.get(key).toString().equals(value)){
				return true;
			}
		}
		return false;
	}
	public boolean containsValue(String value){
		return properties.containsValue(value);
	}
	public String getTagValue(String key){
		try{
			return properties.get(key).toString();
		}catch(Exception e){
			return null;
		}
		
	}
	
	@Override
	public String toString(){
		return properties.toString();
	}

}
