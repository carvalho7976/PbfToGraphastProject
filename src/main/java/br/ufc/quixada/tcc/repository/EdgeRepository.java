package br.ufc.quixada.tcc.repository;

import org.graphast.model.Edge;

import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

public class EdgeRepository {
	
	private ObjectBigArrayBigList<Edge> listEdges;
	
	public EdgeRepository() {
		listEdges = new ObjectBigArrayBigList<Edge>();
	}
	
	public void addEdge(Edge e){
		listEdges.add(e);
	}
	public ObjectBigArrayBigList<Edge> getAllEdges(){
		return listEdges;
	}

}
