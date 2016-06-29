package br.ufc.quixada.tcc.readerbasedOnOsmosis;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;

public interface Sink
{
    void process( GenericOsmElement item );

    void complete();
}
