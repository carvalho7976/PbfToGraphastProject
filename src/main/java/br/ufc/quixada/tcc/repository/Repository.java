package br.ufc.quixada.tcc.repository;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

public interface Repository {
	public void add(GenericOsmElement element);
	public GenericOsmElement find(long id);
	public ObjectBigArrayBigList<GenericOsmElement> getAll();
	public GenericOsmElement findByIndex(long index);
}
