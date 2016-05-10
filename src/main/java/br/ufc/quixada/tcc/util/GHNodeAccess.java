/*
 *  Licensed to Peter Karich under one or more contributor license
 *  agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  Peter Karich licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package br.ufc.quixada.tcc.util;

import org.graphast.model.GraphBounds;
import org.graphast.model.GraphBoundsImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;

/**
 * A helper class for GraphHopperStorage for its node access.
 * <p>
 * @author Peter Karich
 */
class GHNodeAccess 
{
    private final GraphBoundsImpl that;

    public GHNodeAccess( GraphBoundsImpl that){
        this.that = that;
    }
      
    public final void setNode( int nodeId, double lat, double lon ){
        setNode(nodeId, lat, lon, Double.NaN);
    }

    
    public final void setNode( int nodeId, double lat, double lon, double ele ) {
    	
    	Node node = new NodeImpl();
    	node.setLatitude(lat);
    	node.setLongitude(lon);
    	node.setId((long) nodeId);
    	that.accessNeighborhood(node);
    }
   
    public final double getLatitude( int nodeId ) {
        return that.getNode(nodeId).getLatitude();
    }

    public final double getLongitude( int nodeId ){
        return that.getNode(nodeId).getLongitude();
    }


   
    public final double getLat( int nodeId ){
        return getLatitude(nodeId);
    }

    public final double getLon( int nodeId ){
        return getLongitude(nodeId);
    }




 
}
