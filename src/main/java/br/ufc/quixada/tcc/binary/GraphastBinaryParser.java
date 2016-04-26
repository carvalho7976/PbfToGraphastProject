package br.ufc.quixada.tcc.binary;

import java.util.List;

import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;


/**TODO 
 * fazer mapeamento entre elementos OSM e Graphast 
 * implementar metodos para ler os elementos OSM, e converter esses elementos OSM em elementos do Graphast
 * 
 * **/
public class GraphastBinaryParser extends BinaryParser {

	public void complete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void parseRelations(List<Relation> rels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void parseDense(DenseNodes nodes) {
				
	}

	@Override
	protected void parseNodes(List<Node> nodes) {
		for (Node node : nodes) {
			System.out.println(node.getLat() + " " + node.getLon());
		}
		
	}

	@Override
	protected void parseWays(List<Way> ways) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void parse(HeaderBlock header) {
		// TODO Auto-generated method stub
		
	}

}
