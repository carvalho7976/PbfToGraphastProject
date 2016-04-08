package br.ufc.quixada.tcc.poiReader;

import java.util.ArrayList;
import java.util.List;

public class Filtro {
	private String key;
	private String value;
	private String category;
	
	public List<Filtro> childs = new ArrayList<Filtro>();
	
	public Filtro(String key, String value, String category) {
		this.key = key.isEmpty() ? null : key;
		this.value = value.isEmpty() ? null : value;
		this.category = category.isEmpty() ? null : category;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getCategory() {
		return category;
	}
	
	public boolean hasKey() {
		return key != null;
	}
	
	public boolean hasValue() {
		return value != null;
	}
	
	public boolean hasCategory() {
		return category != null;
	}
}
