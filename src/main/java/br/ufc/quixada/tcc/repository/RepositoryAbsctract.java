package br.ufc.quixada.tcc.repository;

import br.ufc.quixada.tcc.osm.model.GenericOsmElement;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

public abstract class RepositoryAbsctract implements Repository{
	private  ObjectBigArrayBigList<GenericOsmElement> listaDeElementos;

	public RepositoryAbsctract() {
		listaDeElementos = new ObjectBigArrayBigList<GenericOsmElement>();
	}	

	public void add(GenericOsmElement element) {
		listaDeElementos.add(element);		
	}

	public GenericOsmElement find(long id) {
		for (GenericOsmElement genericOsmElement : listaDeElementos) {
			if(genericOsmElement.getId() == id)
				return genericOsmElement;
		}

		return null;
	}
	public GenericOsmElement findByIndex(long index){
		return listaDeElementos.get(index);
	}

	public ObjectBigArrayBigList<GenericOsmElement> getAll() {
		return listaDeElementos;
	}
}
