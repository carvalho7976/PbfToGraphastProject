// This software is released into the Public Domain.  See copying.txt for details.
package br.ufc.quixada.tcc.readerbasedOnOsmosis;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;

import java.util.List;

/**
 * Instances of this interface are used to receive results from PBFBlobDecoder.
 * <p>
 * @author Brett Henderson
 */
public interface PbfBlobDecoderListener
{
    /**
     * Provides the listener with the list of decoded entities.
     * <p>
     * @param decodedEntities The decoded entities.
     */
    void complete( List<GenericOsmElement> decodedEntities );

    /**
     * Notifies the listener that an error occurred during processing.
     */
    void error( Exception ex );
}
