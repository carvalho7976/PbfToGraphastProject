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
	protected Map<String, Object> getTags() {
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

	@Override
	public String toString(){
		return properties.toString();
	}

}
