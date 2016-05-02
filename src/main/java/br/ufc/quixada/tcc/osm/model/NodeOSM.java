package br.ufc.quixada.tcc.osm.model;

public class NodeOSM extends GenericOsmElement {
	private final double lat;
    private final double lon;
    
    public NodeOSM( long id, double lat, double lon )
    {
        super(id, NODE);

        this.lat = lat;
        this.lon = lon;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLon()
    {
        return lon;
    }

    public double getEle()
    {
        Object ele = getTags().get("ele");
        if (ele == null)
            return Double.NaN;
        return (Double) ele;
    }

    @Override
    public void setTag( String name, Object value )
    {
        if ("ele".equals(name))
        {
            if (value == null)
                value = null;
            else if (value instanceof String)
            {
                String str = (String) value;
                str = str.trim().replaceAll("\\,", ".");
                if (str.isEmpty())
                    value = null;
                else
                    try
                    {
                        value = Double.parseDouble(str);
                    } catch (NumberFormatException ex)
                    {
                        return;
                    }
            } else
                // force cast
                value = ((Number) value).doubleValue();
        }
        super.setTag(name, value);
    }


}	
